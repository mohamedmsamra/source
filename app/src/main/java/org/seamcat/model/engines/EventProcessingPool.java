package org.seamcat.model.engines;

import org.apache.log4j.Logger;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.EventProcessing;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.simulation.Simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Method to capture all the results from the EPPs simulation
 */
public class EventProcessingPool {

    private static final Logger LOG = Logger.getLogger(EventProcessingPool.class);
    private ExecutorService pool;
    private Map<EventProcessing, EventResultIterable> resultIterableMap;
    private final List<SimulationResultGroup> eppResults;
    private List<Future<?>> runningEpps = new ArrayList<>();

    public EventProcessingPool( final Simulation simulation, final List<SimulationResultGroup> eppResults, List<EventProcessing> confs ) {
        this.eppResults = eppResults;
        if (confs.isEmpty() ) {
            return;
        } else {
            pool = Executors.newFixedThreadPool(confs.size());

        }

        resultIterableMap = new HashMap<>();

        for (final EventProcessing processing : confs) {
            final EventResultIterable iterable = new EventResultIterable(simulation);
            resultIterableMap.put(processing, iterable);
            runningEpps.add(pool.submit(new Runnable() {
                public void run() {
                    try {
                        ResultTypes result = processing.evaluate(simulation.getScenario(), iterable);
                        synchronized (eppResults) {
                            String id = ((EventProcessingConfiguration) processing).getId();
                            eppResults.add(new SimulationResultGroup(id, processing.toString(), result, simulation.getScenario()));
                            eppResults.notifyAll();
                        }
                    } catch (Exception e) {
                        LOG.error("Failed evaluation", e);
                        synchronized (eppResults) {
                            eppResults.add(new SimulationResultGroup(processing.toString(), e));
                            eppResults.notifyAll();
                        }
                    }
                }
            }));
        }
    }

    public void eventResult( EventResult eventResult ) {
        if ( pool == null ) return;
        for (EventResultIterable eventResults : resultIterableMap.values()) {
            eventResults.addResult(eventResult);
        }
    }

    public void eventsComplete() {
        if ( pool == null ) return;
        for (EventResultIterable eventResults : resultIterableMap.values()) {
            eventResults.lastAdded();
        }
    }

    public void waitForTermination() throws InterruptedException {
        if ( pool == null ) return;

        // wait for it to complete
        synchronized (eppResults) {
            while (eppResults.size() != resultIterableMap.size()) {
                eppResults.wait();
            }
        }

        pool.shutdown();
    }

    public void cancelAll() {
        for (Future<?> epp : runningEpps) {
            if ( !epp.isDone() ) {
                epp.cancel(true);
            }
        }
    }
}
