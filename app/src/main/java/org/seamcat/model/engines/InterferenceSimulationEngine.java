package org.seamcat.model.engines;

import org.seamcat.exception.SimulationInvalidException;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.CollectedResults;
import org.seamcat.model.simulation.InterferenceLinkSimulation;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.simulation.VictimSystemSimulation;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.types.EventProcessing;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.*;
import org.seamcat.simulation.Simulation;
import org.seamcat.simulation.result.MutableEventResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class InterferenceSimulationEngine {

    public static final String STATISTICS = "Statistics";
    public static final String SIMULATION_SEED = "Simulation seed";

    private static DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * overall run of the simulation
     * <ol>
     *     <li>get the simulation seed</li>
     *     <li>initialise the victim simulation and EPPs</li>
     *     <li>pre-simulation: one victim and loop over all the interferers</li>
     *     <li>schedule all events</li>
     * </ol>
     *
     * @param simulation
     * @param pool
     */
    public SimulationResult simulateInterference( final Simulation simulation, SimulationPool pool ) {
        EventProcessingPool eppPool = null;
        LinkedList<Future<EventResult>> events = new LinkedList<>();
        try {
            final long simulationBegin = System.currentTimeMillis();
            RandomAccessor.fixSeed(simulation.getSimulationSeed());

            // initialize
            final VictimSystemSimulation victimSimulation = simulation.getVictimSystemSimulation();
            List<EventProcessing> epps = new ArrayList<>(victimSimulation.getEmbeddedEPPs());
            epps.addAll(simulation.getScenario().getEventProcessingList());
            SimulationResult simulationResult = simulation.getSimulationResult();
            simulation.preSimulation();
            // add pre simulation results
            Scenario scenario = simulation.getScenario();

            List<SimulationResultGroup> preResults = simulationResult.getSystemPreSimulationResults();
            ResultTypes preVictim = scenario.getPreSimulationResults(scenario.getVictimSystem()).getPreSimulationResults();
            Set<RadioSystem> uniqueSystems = new HashSet<>();
            uniqueSystems.add(scenario.getVictimSystem());
            int count = 1;
            preResults.add(new SimulationResultGroup("" + count, "Victim Pre Simulation", preVictim, scenario));
            for (InterferenceLink link : scenario.getInterferenceLinks()) {
                RadioSystem system = link.getInterferingSystem();
                if (uniqueSystems.contains(system)) continue;
                uniqueSystems.add(system);
                ResultTypes resultTypes = scenario.getPreSimulationResults(system).getPreSimulationResults();
                count++;
                preResults.add(new SimulationResultGroup("" + count, system.getName() + " Pre Simulation", resultTypes, scenario));
            }

            eppPool = new EventProcessingPool(simulation, simulationResult.getEventProcessingResults(), epps);

            long eventsBegin = System.currentTimeMillis();

            // schedule all events
            events = new LinkedList<>();
            for (int i = 0; i < simulation.getScenario().numberOfEvents(); i++) {
                events.add(pool.getPool().submit(new SingleEvent(i) {
                    public EventResult call() throws Exception {
                        try {
                            MutableEventResult single = single(victimSimulation, eventNumber, simulation);
                            single.getValues();
                            return single;
                        } catch (SimulationInvalidException e ) {
                            throw e;
                        } catch (RuntimeException e ) {
                            e.printStackTrace();
                            throw new SimulationInvalidException("Simulation terminated due to error", e);
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException("Error");
                        }
                    }
                }));
            }

            try {
                CollectedResults results = new CollectedResults(simulation.getScenario().numberOfEvents());
                while (!events.isEmpty()) {
                    MutableEventResult eventResult = (MutableEventResult) events.removeFirst().get();
                    if (eventResult == null) break;
                    eppPool.eventResult(eventResult);
                    collectResults(eventResult, results);
                }
                eppPool.eventsComplete();

                addCollectedResults(victimSimulation, simulation, results);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if ( cause instanceof SimulationInvalidException) {
                    eppPool.cancelAll();
                    throw (SimulationInvalidException) cause;
                }
            }

            eppPool.waitForTermination();

            victimSimulation.postSimulation(simulationResult);
            simulation.postSimulation();

            long simulationEnd = System.currentTimeMillis();
            double eventDuration = (simulationEnd - eventsBegin) / 1000.0;
            double totalDuration = (simulationEnd - simulationBegin) / 1000.0;
            ResultTypes stats = new ResultTypes();
            stats.getSingleValueTypes().add(new IntegerResultType("Simulated performed on", "processor", pool.getPoolSize()));
            stats.getSingleValueTypes().add(new DoubleResultType("Total simulation duration", "second", totalDuration));
            stats.getSingleValueTypes().add(new DoubleResultType("Event generation duration", "second", eventDuration));
            stats.getSingleValueTypes().add(new IntegerResultType("Calculation rate", "events/second", new Long(Math.round(simulation.getScenario().numberOfEvents() / eventDuration)).intValue()));
            stats.getSingleValueTypes().add(new StringResultType("Simulation date", timeFormat.format(new Date())));
            stats.getSingleValueTypes().add(new LongResultType(SIMULATION_SEED, "", simulation.getSimulationSeed()));
            simulationResult.getSeamcatResults().add(new SimulationResultGroup(STATISTICS, stats, simulation.getScenario()));

            return simulationResult;
        } catch (InterruptedException e ) {
            // thread is interrupted
            if ( eppPool != null) {
                eppPool.cancelAll();
            }
            while (!events.isEmpty()) {
                events.removeFirst().cancel(true);
            }
            return null;
        }
    }

    public MutableEventResult single( VictimSystemSimulation victimSimulation, int eventNumber, Simulation simulation ) {
        fixSeed( simulation, eventNumber);

        MutableEventResult eventResult = new MutableEventResult(eventNumber);
        victimSimulation.simulate(eventResult);

        List<InterferenceLinkSimulation> iSims = new ArrayList<>();
        /*for (SimulationInterferingLink link : simulation.getInterferenceLinkSimulations()) {
            Point2D victimPos = victimSimulation.getSystemPosition( eventResult, link.getInterferenceLink());
            link.simulate(simulation.getScenario(), eventResult, victimPos);
        }*/
        for (InterferenceLink interferenceLink : simulation.getScenario().getInterferenceLinks()) {
            InterferenceLinkSimulation iSim = simulation.getInterferenceLinkSimulation(interferenceLink);
            iSims.add(iSim);
            Point2D victimPos = victimSimulation.getSystemPosition( eventResult, interferenceLink);
            iSim.simulate(simulation.getScenario(), eventResult, interferenceLink, victimPos);
        }

        victimSimulation.collect(eventResult);

        simulation.eventComplete(eventResult, victimSimulation, iSims);

        return eventResult;
    }

    protected void fixSeed( Simulation simulation, int eventNumber) {
        long seed = simulation.getSimulationSeed() + (eventNumber+1)*31;
        RandomAccessor.fixSeed( seed );
    }

    private void addCollectedResults(VictimSystemSimulation victimSimulation, Simulation simulation, CollectedResults results) {
        List<SimulationResultGroup> groups = victimSimulation.buildResults(results);
        simulation.getSimulationResult().getSeamcatResults().addAll( groups );
    }

    private void collectResults(EventResult eventResult, CollectedResults results ) {
        for (Map.Entry<String, Double> value : eventResult.getValues().entrySet()) {
            results.vector( value.getKey() )[eventResult.getEventNumber()] = value.getValue();
        }
    }

    abstract class SingleEvent implements Callable<EventResult> {
        protected final int eventNumber;
        public SingleEvent( int eventNumber ) { this.eventNumber = eventNumber; }
    }
}
