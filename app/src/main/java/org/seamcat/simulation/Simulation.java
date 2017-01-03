package org.seamcat.simulation;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMASystem;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.Workspace;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.engines.SimulationListener;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.factory.Model;
import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.InterferenceLinkSimulation;
import org.seamcat.model.simulation.VictimSystemSimulation;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.systems.SimulationInterferingLink;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.scenario.WorkspaceScenario;
import org.seamcat.simulation.cellular.*;
import org.seamcat.simulation.generic.GenericInterfererInterferenceLinkSimulation;
import org.seamcat.simulation.generic.GenericVictimSystemSimulation;
import org.seamcat.simulation.result.MutableEventResult;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Simulation {

    private static Logger LOG = Logger.getLogger(Simulation.class);

    private Workspace workspace;
    private WorkspaceScenario scenario;

    private long simulationSeed;
    private SimulationListener listener;
    private File logfile;
    private FileAppender logfileAppender;
    private AbstractDmaSystem lastEventVictimState;
    private List<InterferenceLinkSimulation> iSims;
    private EmissionMask pseudoEmission;
    private double bwVLR;


    public Simulation(Workspace workspace, WorkspaceScenario scenario) {
        this(workspace, scenario, new SimulationListener() {
            public void preSimulate(int totalEvents) {}

            public void eventComplete(EventResult eventResult, VictimSystemSimulation victimSimulation, List<InterferenceLinkSimulation> interferenceSimulations) {}

            public void postSimulate() {}
        });
    }

    public Simulation(Workspace workspace, WorkspaceScenario scenario, SimulationListener listener) {
        this.workspace = workspace;
        this.workspace.setSimulationResult(new SimulationResult());
        this.scenario = scenario;
        this.listener = listener;
        simulationSeed = RandomAccessor.getRandom().nextLong();
    }

    public SimulationResult getSimulationResult() {
        return workspace.getSimulationResults();
    }

    public long getSimulationSeed() {
        return simulationSeed;
    }

    public void appendDebugLog() {
        try {
            logfile = new File(Model.seamcatHome + File.separator + "logfiles" + File.separator);
            if (!logfile.exists()) {
                logfile.mkdirs();
            }
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(System.currentTimeMillis());
            logfile = new File(logfile, workspace.getName() + " - " + cal.get(Calendar.YEAR) + (cal.get(Calendar.MONTH) + 1 < 10 ? "0" : "") + (cal.get(Calendar.MONTH) + 1) + cal.get(Calendar.DAY_OF_MONTH) + "_" + cal.get(Calendar.HOUR_OF_DAY) + "." + cal.get(Calendar.MINUTE) + ".log");
            logfile.createNewFile();
            logfileAppender = new FileAppender(Model.getInstance().getLogFilePattern(), logfile.getAbsolutePath(), true);
            Logger.getLogger("org.seamcat").addAppender(logfileAppender);
            Logger.getLogger("org.seamcat").setLevel(Level.DEBUG);
        } catch (IOException ex) {
            LOG.warn("Unable to create EGE log file", ex);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("\nStarting new event generation at: " + new java.sql.Timestamp(System.currentTimeMillis()) + "\n");
        }
    }

    public void removeDebugLog() {
        Logger.getLogger("org.seamcat").removeAppender(logfileAppender);
        Logger.getLogger("org.seamcat").setLevel(Logger.getRootLogger().getLevel());
        logfileAppender.close();
    }

    public void preSimulationSingle() {
        if (!workspace.getSimulationControl().debugMode()) {
            appendDebugLog();
        }
        try {
            preSimulation();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void preSimulation() throws InterruptedException {
        if (workspace.getSimulationControl().debugMode()) {
            appendDebugLog();
        }

        workspace.setScenario(scenario);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting Event Generation");
        }
        listener.preSimulate(scenario.numberOfEvents());

        //TODO trial for blocking  - start modification part 1
        calculateBlockAverage(workspace.getVictimSystemLink().getSystem().getReceiver().getPseudoBlockingMask());
        //TODO end modification part 1

        if (workspace.getVictimSystemLink().isDMASystem()) {
            workspace.getVictimSystemLink().getDMASystem().initialize(new MutableEventResult(-1));
            workspace.getVictimSystemLink().getDMASystem().performPreSimulationTasks(workspace.getVictimFrequency().trial());
        }

        if (workspace.getVictimSystemLink().isCDMASystem()) {
            CDMASystem cdma = (CDMASystem) workspace.getVictimSystemLink().getDMASystem();
            cdma.findNonInterferedCapacity(scenario.getPreSimulationResults(scenario.getVictimSystem()), workspace);
        }
        for (int i = 0; i < workspace.getInterferenceLinks().size(); i++) {
            org.seamcat.model.core.InterferenceLink il = workspace.getInterferenceLinks().get(i);
            if (il.getInterferingLink().isDMASystem()) {
                il.getDMASystem().initialize(new MutableEventResult(-1));
                il.getDMASystem().performPreSimulationTasks(workspace.getInterferingLinkFrequency().get(i).trial());
            }
            if (il.getInterferingLink().isCDMASystem()) {
                CDMASystem cdma = (CDMASystem) il.getDMASystem();
                cdma.findNonInterferedCapacity(scenario.getPreSimulationResults(il.getInterferingSystem()), workspace);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Generating %d events", scenario.numberOfEvents()));
            if (workspace.getVictimSystemLink().isDMASystem()) {
                LOG.debug("Position of Victim Receiver is dynamic");
            } else {
                GenericSystem sys = (GenericSystem) workspace.getVictimSystemLink().getSystem();
                LOG.debug("Position of Victim Receiver is " + (sys.getLink().getRelativeLocation().useCorrelatedDistance() ? "fixed" : "dynamic"));
            }
        }
    }


    //TODO added by KK - start part 2

    /**
     * calculates an averaged value of the origin blocking mask <br/>
     * and puts the resulting masks as scatter diagram to the pre-simulation results
     *
     * @param maskOrigin origin as SEAMCAT Function
     */
    private void calculateBlockAverage(Function maskOrigin) {

        // TODO Move to InterferenceCalculator
        bwVLR = workspace.getVictimSystemLink().getSystem().getReceiver().getBandwidth();
        if (workspace.getVictimSystemLink().isOFDMASystem())
            bwVLR /= 1000.; //REVISIT  when bandwidth OFDMA is corrected
        Distribution frequencyVLR = workspace.getVictimFrequency();
        double bwOffsetVLR = 0;
        if (!maskOrigin.isConstant()) {
            setPseudoEmission(maskOrigin);
            Bounds boundsVLR = getBoundsVLR(maskOrigin);
            if (!boundsVLR.isBounded()) {
                bwOffsetVLR = 0; // TODO consider a flat shape of the mask; added by KK
            } else {
                bwOffsetVLR = Math.rint(((boundsVLR.getMax() - boundsVLR.getMin()) / 2 - boundsVLR.getMax()) * 1000) / 1000;
                setPseudoEmission(maskOrigin);
            }

            for (int i = 0; i < workspace.getInterferenceLinks().size(); i++) {
                List<Point2D> maskPoints = new ArrayList<>();
                Distribution frequencyILT = workspace.getInterferenceLinks().get(i).getInterferingSystem().getFrequency();
                double bwILT = 0, bwOffsetILT = 0;
                if (workspace.getInterferenceLinks().get(i).getInterferingSystem() instanceof GenericSystem) {
                    Bounds boundsILT = workspace.getInterferenceLinks().get(i).getInterferingSystem().getTransmitter().getBandwidthBounds();
                    bwILT = Math.rint((boundsILT.getMax() - boundsILT.getMin()) * 1000) / 1000;
                    bwOffsetILT = bwILT / 2 - boundsILT.getMax();
                    bwOffsetILT = Math.rint(bwOffsetILT * 1000) / 1000; // kHz
                } else if (workspace.getInterferenceLinks().get(i).getInterferingSystem() instanceof CellularSystem) {
                    bwILT = workspace.getInterferenceLinks().get(i).getInterferingSystem().getTransmitter().getBandwidth();
                    if (((CellularSystem) workspace.getInterferenceLinks().get(i).getInterferingSystem()).getOFDMASettings() != null)
                        bwILT /= 1000.;//REVISIT  when bandwidth OFDMA is corrected
                }
                double step = Math.min(0.1, bwILT / 20);
                if (bwVLR < 1) step = Math.min(0.1, bwVLR / 20);
                step = Math.rint(step * 1000) / 1000;
                if (workspace.getInterferenceLinks().get(i).getInterferingSystem().getName().contains("OFDMA UpLink")) {
                    frequencyILT = Factory.distributionFactory().getUniformDistribution(frequencyILT.getBounds().getMin() - 2 * bwILT,
                            frequencyILT.getBounds().getMax() + 2 * bwILT); // considering the frequency distribution of UE inside the system bandwidth
                }
                Distribution fOffsets;
                fOffsets = Factory.distributionFactory().getUniformDistribution(
                        frequencyILT.getBounds().getMin() - frequencyVLR.getBounds().getMax(),
                        frequencyILT.getBounds().getMax() - frequencyVLR.getBounds().getMin());

                // TODO Added by Mads to ensure step is not zero
                if (Mathematics.equals(step, 0, 0.00001)) {
                    // minimum step size
                    step = 0.001;
                }

                for (double tOffset = fOffsets.getBounds().getMin(); tOffset <= fOffsets.getBounds().getMax(); tOffset += step) {
                    tOffset = Math.rint(tOffset * 1000) / 1000;
                    double lower, upper, fOffset, correction;
                    double blockingResponse;
                    if (Math.abs(tOffset) < (bwILT + bwVLR) / 2) {
                        // overlap upper and lower
                        if (tOffset - bwOffsetILT - bwOffsetVLR - bwILT / 2 >= -bwVLR / 2 && tOffset - bwOffsetILT - bwOffsetVLR + bwILT / 2 <= bwVLR / 2) {
                            blockingResponse = 1000.0; // completely  inside
                        } else if (Math.rint((tOffset - bwOffsetILT - bwOffsetVLR - bwILT / 2) * 1000) / 1000 < -bwVLR / 2 && Math.rint((tOffset - bwOffsetILT - bwOffsetVLR + bwILT / 2) * 1000) / 1000 > bwVLR / 2) {
                            //overlaps both lower part
                            lower = tOffset - bwOffsetILT - bwOffsetVLR - bwILT / 2;
                            upper = -bwVLR / 2 - bwOffsetVLR;
                            fOffset = upper - (upper - lower) / 2;
                            double partBelow = getIntegral(Math.rint((upper - lower) * 1000) / 1000, maskOrigin, fOffset);
                            partBelow = -10 * Math.log10(Math.pow(10, -partBelow / 10) * Math.abs(upper - lower) / bwILT);
                            lower = bwVLR / 2 - bwOffsetVLR;
                            //overlaps both upper part
                            upper = tOffset - bwOffsetILT - bwOffsetVLR + bwILT / 2;
                            fOffset = lower + (upper - lower) / 2;
                            double partAbove = getIntegral(Math.rint((upper - lower) * 1000) / 1000, maskOrigin, fOffset);
                            partAbove = -10 * Math.log10(Math.pow(10, -partAbove / 10) * Math.abs(upper - lower) / bwILT);
                            blockingResponse = -10 * Math.log10((Math.pow(10, -partAbove / 10) + Math.pow(10, -partBelow / 10)));

                        } else if (Math.rint((tOffset - bwOffsetILT - bwOffsetVLR + bwILT / 2) * 1000) / 1000 > bwVLR / 2) {
                            // overlaps above
                            lower = bwVLR / 2 - bwOffsetVLR;
                            upper = tOffset - bwOffsetILT - bwOffsetVLR + bwILT / 2;
                            correction = Math.abs(upper - lower) / bwILT;
                            fOffset = lower + Math.abs(upper - lower) / 2;
                            blockingResponse = getIntegral(Math.rint((upper - lower) * 1000) / 1000, maskOrigin, fOffset);
                            blockingResponse = -10 * Math.log10(Math.pow(10, -blockingResponse / 10) * correction);
                        } else {
                            lower = tOffset - bwOffsetILT - bwOffsetVLR - bwILT / 2;
                            upper = -bwVLR / 2 - bwOffsetVLR;
                            correction = Math.abs(upper - lower) / bwILT;
                            fOffset = upper - (upper - lower) / 2;
                            blockingResponse = getIntegral(Math.rint((upper - lower) * 1000) / 1000, maskOrigin, fOffset);
                            blockingResponse = -10 * Math.log10(Math.pow(10, -blockingResponse / 10) * correction);
                        }

                    } else {// no overlap
                        //bandwidth correction = 1 due to total ILT bandwidth
                        blockingResponse = getIntegral(bwILT, maskOrigin, tOffset);
                    }
                    if (Mathematics.equals(fOffsets.getBounds().getMin(), fOffsets.getBounds().getMax(), 0.0001)) {// both frequencies constant
                        Function rBlockingMask = Factory.functionFactory().constantFunction(Math.rint(blockingResponse * 10) / 10);
                        scenario.getPreSimulationResults(workspace.getInterferenceLinks().get(i).getInterferingSystem()).setBlockingMaskIntegral(rBlockingMask);
                    } else {
                        maskPoints.add(new Point2D(tOffset, blockingResponse));
                    }
                }
                if (maskPoints.size() > 0) {// to be skipped in case blocking response is constant
                    Collections.sort(maskPoints);
                    Function rBlockingMask = Factory.functionFactory().discreteFunction(maskPoints);
                    scenario.getPreSimulationResults(workspace.getInterferenceLinks().get(i).getInterferingSystem()).setBlockingMaskIntegral(rBlockingMask);
                }
            }

            /*
+            //TODO just to put the generated masks onto the pre-simulated results - start
+            ScatterDiagramResultType scatterDiagramResultType;
+            PreSimulationResults typesMap = scenario.getPreSimulationResults(scenario.getVictimSystem());
+            ResultTypes types = typesMap.getPreSimulationResults();
+            for (String key : keys) {
+                if (blockingMasksIntegral.get(key).isConstant()) {
+                    types.getSingleValueTypes().add(new DoubleResultType("Blocking response: " + key, "dB", blockingMasksIntegral.get(key).getConstant()));
+                } else {
+                    scatterDiagramResultType = new ScatterDiagramResultType(key, "offset", "dB");
+                    scatterDiagramResultType.getScatterPoints().addAll(blockingMasksIntegral.get(key).getPoints());
+                    types.getScatterDiagramResultTypes().add(scatterDiagramResultType);
+                }
+            }
+            scenario.getPreSimulationResults(scenario.getVictimSystem()).setPreSimulationResults(types);
+            //TODO just to put the generated masks onto the pre-simulated results - end
+*/
            }
        }

    private Bounds getBoundsVLR(Function maskOrigin) {
        if (maskOrigin.getBounds().isBounded()) { //TODO modified by KK to cover masks defined as pseudo constant
            double ref = maskOrigin.evaluate(0);
            double min = 0, max = 0;
            if (!Mathematics.equals(ref, maskOrigin.evaluateMax(), 0.1)) {
                while (maskOrigin.evaluate(max) - ref <= 3.0 && max < maskOrigin.getBounds().getMax()) {
                    max += 0.01;
                }
                while (maskOrigin.evaluate(min) - ref <= 3.0 && min > maskOrigin.getBounds().getMin()) {
                    min -= 0.01;
                }
                return new Bounds(min, max, true);
            } else {
                return new Bounds(min, max, false);
            }
        } else {
            return null;
        }
    }

    private double getIntegral(double rangeToIntegrate, Function maskOrigin, double tOffset) {
        if (maskOrigin.isConstant()) {
            return maskOrigin.getConstant();
        } else if (getPseudoEmission() == null || getPseudoEmission().getPoints().size() < 2) {
            setPseudoEmission(maskOrigin);
            return getIntegral(rangeToIntegrate, maskOrigin, tOffset);
        } else {
            double att = -getPseudoEmission().integrate(tOffset, rangeToIntegrate);
            att += 10 * Math.log10(rangeToIntegrate); // normalise to 1 MHz; the bandwidth correction to ILT is done by the calling method
            return att;
        }
        //getIntegral(rangeToIntegrate, maskOrigin, tOffset);
        //return 10e-9;
    }

    private EmissionMask getPseudoEmission() {return pseudoEmission;}

    private void setPseudoEmission(Function maskOrigin) {

        List<Double> ref = new ArrayList<>();
        List<Point2D> pseudoMask = new ArrayList<>();
        for (int i = 0; i < maskOrigin.getPoints().size(); i++) {
            ref.add(bwVLR * 1000.); // even the reference bandwidth is not really required due to the integration method supposes 'normalised' mask values
            pseudoMask.add(new Point2D(maskOrigin.getPoints().get(i).getX(), -1 * (maskOrigin.getPoints().get(i).getY())));
        }
        this.pseudoEmission = Factory.functionFactory().emissionMask(pseudoMask, ref);
    }
    //TODO end modification part 2


    public void eventComplete(EventResult eventResult, VictimSystemSimulation victimSimulation, List<InterferenceLinkSimulation> interferenceSimulations) {
        listener.eventComplete(eventResult, victimSimulation, interferenceSimulations);
        if (lastEvent(eventResult)) {
            // last event, save state of simulated models
            // get all state
            if (victimSimulation instanceof CellularVictimSystemSimulation) {
                lastEventVictimState = ((CellularVictimSystemSimulation) victimSimulation).getVictim();
            }
            iSims = new ArrayList<>();
            for (InterferenceLinkSimulation iSim : interferenceSimulations) {
                iSims.add(iSim);
            }
        }
    }

    protected boolean lastEvent(EventResult eventResult) {
        return eventResult.getEventNumber() == scenario.numberOfEvents() - 1;
    }

    public AbstractDmaSystem getLastVictimState() {
        return lastEventVictimState;
    }

    public List<InterferenceLinkSimulation> getLastEventInterferenceLinkSimulations() {
        return iSims;
    }

    public void postSimulationSingle() {
        if (!workspace.getSimulationControl().debugMode()) {
            removeDebugLog();
        }
    }

    public void postSimulation() {
        listener.postSimulate();
        if (workspace.getSimulationControl().debugMode()) {
            removeDebugLog();
        }
    }

    public Scenario getScenario() {
        return scenario;
    }

    public List<SimulationInterferingLink> getInterferenceLinkSimulations() {
        return null;
    }

    public InterferenceLinkSimulation getInterferenceLinkSimulation(InterferenceLink interferenceLink) {
        if (interferenceLink.getInterferingSystem() instanceof GenericSystem) {
            return new GenericInterfererInterferenceLinkSimulation();
        } else {
            return new CellularInterfererInterferenceLinkSimulation(interferenceLink);
        }
    }


    public VictimSystemSimulation getVictimSystemSimulation() {
        RadioSystem victimSystem = getScenario().getInterferenceLinks().get(0).getVictimSystem();
        if (victimSystem instanceof GenericSystem) {
            return new GenericVictimSystemSimulation(workspace.getScenario(), workspace.isUseUserDefinedDRSS(), workspace.getUserDefinedDRSS());
        } else {
            CellularSystem cellularSystem = (CellularSystem) victimSystem;
            if (cellularSystem.isUpLink()) {
                if (cellularSystem.getCDMASettings() == null) {
                    return new OFDMAUpLinkVictimSystemSimulation(workspace);
                } else {
                    return new CDMAUpLinkVictimSystemSimulation(workspace);
                }
            } else {
                if (cellularSystem.getCDMASettings() == null) {
                    return new OFDMADownLinkVictimSystemSimulation(workspace);
                } else {
                    return new CDMADownLinkVictimSystemSimulation(workspace);
                }
            }
        }
    }

    public File getLogfile() {
        return logfile;
    }

    public void setSimulationSeed(long simulationSeed) {
        this.simulationSeed = simulationSeed;
    }

}
