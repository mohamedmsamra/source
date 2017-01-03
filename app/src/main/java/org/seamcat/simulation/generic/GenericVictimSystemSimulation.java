package org.seamcat.simulation.generic;

import org.apache.log4j.Logger;
import org.seamcat.dmasystems.ActiveInterferer;
import org.seamcat.dmasystems.LinkCalculator;
import org.seamcat.events.VectorValues;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.CollectedResults;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.simulation.VictimSystemSimulation;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.types.EventProcessing;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.Transmitter;
import org.seamcat.model.types.result.*;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.scenario.WorkspaceScenario;
import org.seamcat.simulation.OptionalSimulation;
import org.seamcat.simulation.calculator.InterferenceCalculator;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static java.lang.Math.log10;
import static org.seamcat.model.generic.InterferingLinkRelativePosition.CorrelationMode.*;
import static org.seamcat.model.simulation.result.SimulationResult.*;

public class GenericVictimSystemSimulation implements VictimSystemSimulation<GenericSystem> {

    private static Logger LOG = Logger.getLogger(GenericVictimSystemSimulation.class);

    private final WorkspaceScenario scenario;
    private final GenericSystem victimSystem;
    private List<OptionalSimulation> optionals = new ArrayList<>();
    private final boolean useUserDefinedDRSS;
    private final Distribution userDefinedDRSS;

    public static String IRSSU      = "Unwanted summation Vector";
    public static String IRSSB      = "Blocking summation Vector";

    private static Set<InterferingLinkRelativePosition.CorrelationMode> vrModes = new HashSet<>();
    private static Set<InterferingLinkRelativePosition.CorrelationMode> wtModes = new HashSet<>();

    static {
        vrModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR );
        vrModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR);
        vrModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR);
        vrModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR);
        vrModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR);
        vrModes.add(VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR);
        vrModes.add(VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR);

        wtModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT);
        wtModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT);
        wtModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT);
        wtModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT);
        wtModes.add(VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT);
        wtModes.add(VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT);
        wtModes.add(VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT);
    }

    public GenericVictimSystemSimulation( WorkspaceScenario scenario,
                                          boolean useUserDefinedDRSS,
                                          Distribution userDefinedDRSS) {
        this.scenario = scenario;
        this.useUserDefinedDRSS = useUserDefinedDRSS;
        this.userDefinedDRSS = userDefinedDRSS;
        victimSystem = (GenericSystem) scenario.getVictimSystem();
        if ( victimSystem.getReceiver().isUsingOverloading() ) {
            optionals.add(new Overloading(victimSystem, scenario));
        }

        if ( victimSystem.getReceiver().isIntermodulationRejectionOption()) {
            optionals.add( new Intermodulation(scenario, victimSystem));
        }
    }

    @Override
    public List<EventProcessing> getEmbeddedEPPs() {
        if ( crCount((List<InterferenceLink>) scenario.getInterferenceLinks()) > 0 ) {
            List<EventProcessing> list = new ArrayList<>();
            list.add( EventProcessingConfiguration.event(CognitiveRadio.class) );
            return list;
        }
        return Collections.emptyList();
    }

    public static int crCount(List<InterferenceLink> links) {
        int crCount = 0;
        for (InterferenceLink link : links) {
            Transmitter transmitter = link.getInterferingSystem().getTransmitter();
            if ( transmitter instanceof GenericTransmitter && ((GenericTransmitter) transmitter).isInterfererCognitiveRadio() ) {
                crCount++;
            }
        }
        return crCount;
    }

    @Override
    public List<SimulationResultGroup> buildResults(CollectedResults collected ) {
        List<SimulationResultGroup> groups = new ArrayList<>();
        Map<String, double[]> collectedResults = collected.vectorResults();

        ResultTypes drss = new ResultTypes();
        groups.add( new SimulationResultGroup(DRSS, drss, scenario));
        drss.getVectorResultTypes().add(new VectorResultType(DRSSVector, dBm, collectedResults.get(DRSSVector)));

        ResultTypes irssU = new ResultTypes();
        groups.add( new SimulationResultGroup(IRSS_UNWANTED, irssU, scenario));
        irssU.getVectorResultTypes().add( new VectorResultType(IRSSU, dBm, collectedResults.remove(IRSSU)));

        ResultTypes irssB = new ResultTypes();
        groups.add( new SimulationResultGroup(IRSS_BLOCKING, irssB, scenario));
        irssB.getVectorResultTypes().add(new VectorResultType(IRSSB, dBm, collectedResults.remove(IRSSB)));

        VectorGroupResultType irssBGroup = new VectorGroupResultType("iRSS Blocking", dBm);
        VectorGroupResultType irssUGroup = new VectorGroupResultType("iRSS Unwanted", dBm);
        for (Map.Entry<String, double[]> entry : collectedResults.entrySet()) {
            if ( entry.getKey().startsWith("iRSS Blocking")) {
                irssBGroup.addVector(new NamedVectorResult(entry.getKey(), entry.getValue()));
            } else if ( entry.getKey().startsWith("iRSS Unwanted")) {
                irssUGroup.addVector(new NamedVectorResult(entry.getKey(), entry.getValue()));
            }
        }
        if ( irssBGroup.size() > 0 ) {
            irssB.getVectorGroupResultTypes().add( irssBGroup );
        }
        if ( irssUGroup.size() > 0 ) {
            irssU.getVectorGroupResultTypes().add( irssUGroup );
        }

        for (OptionalSimulation optional : optionals) {
            groups.add(optional.buildResult(collected));
        }
        return groups;
    }

    @Override
    public void simulate( EventResult eventResult) {
        MutableEventResult result = (MutableEventResult) eventResult;
        LOG.debug("Victim system is generic");

        MutableLinkResult link = result.createVictimSystemLink(scenario);
        double drss =  GenericSystemPlugin.dRSSLinkBudgetDef(link, victimSystem);
        if ( useUserDefinedDRSS ) {
            drss =  userDefinedDRSS.trial();
            if ( LOG.isDebugEnabled() ) {
                LOG.debug("Overriding calculated dRSS with value from user-defined" + " distribution, trialed as = " + drss + " dBm");
            }
        }
        result.addValue(DRSSVector, drss);
        if ( LOG.isDebugEnabled() ) {
            LOG.debug("dRSSValue = " + drss);
        }
    }

    @Override
    public Point2D getSystemPosition( EventResult eventResult, InterferenceLink<GenericSystem> link ) {
        InterferingLinkRelativePosition.CorrelationMode mode = link.getInterferingLinkRelativePosition().getCorrelationMode();
        LinkResult victim = eventResult.getVictimSystemLinks().get(0);
        if ( vrModes.contains(mode) ) return victim.rxAntenna().getPosition();
        else if ( wtModes.contains( mode )) return victim.txAntenna().getPosition();

        throw new RuntimeException("Unknown mode for Generic Victim: " + mode);
    }

    @Override
    public void collect(EventResult eventResult) {
        MutableEventResult result = (MutableEventResult) eventResult;

        double intermediateSumU = 0, intermediateSumB = 0;
        int i=0;
        int total = result.getVictimSystemLinks().size() * result.getInterferingElements().size();
        for (MutableLinkResult vLink : result.getVictimSystemLinks()) {
            for (ActiveInterferer interferer : result.getInterferingElements()) {
                // create InterferenceLinks
                InterferenceLink iLink = interferer.getInterferenceLink();
                MutableInterferenceLinkResult linkResult = new MutableInterferenceLinkResult(iLink, vLink, interferer.getLinkResult());
                interferer.applyInterferenceLinkCalculations( linkResult );
                result.addInterferenceLinkResult( linkResult );
                linkResult.rxAntenna().setGain(iLink.getVictimSystem().getReceiver().getAntennaGain().evaluate(linkResult, linkResult.rxAntenna()));
                linkResult.txAntenna().setGain(linkResult.getTxAntennaGain().evaluate(linkResult, linkResult.txAntenna()));

                LinkCalculator.itVrPropagationLoss( linkResult, iLink.getInterferingLinkRelativePosition().getMinimumCouplingLoss().trial());

                InterferenceCalculator.unwantedInterference(scenario, linkResult);
                InterferenceCalculator.blockingInterference(scenario, linkResult );

                intermediateSumU += Mathematics.dB2Linear(linkResult.getRiRSSUnwantedValue());
                intermediateSumB += Mathematics.dB2Linear(linkResult.getRiRSSBlockingValue());

                String bName = subLinkName("iRSS Blocking - " + iLink.getInterferingSystem().getName(),i, total);
                result.addValue(bName, linkResult.getRiRSSBlockingValue());
                String uName = subLinkName("iRSS Unwanted - " + iLink.getInterferingSystem().getName(),i, total);
                result.addValue(uName, linkResult.getRiRSSUnwantedValue());
                i++;
            }
        }
        result.addValue(IRSSU, intermediateSumU > 0 ? 10 * log10(intermediateSumU) : -1000);
        result.addValue(IRSSB, intermediateSumB > 0 ? 10 * log10(intermediateSumB) : -1000);

        for (OptionalSimulation optional : optionals) {
            optional.collect( result );
        }
    }

    public static String subLinkName(String prefix, int index, int total ) {
        if ( total == 1 ) return prefix;
        return prefix + "_subLink_"+(index+1);
    }

    public static NumberFormat nf = new DecimalFormat("0.00");

    @Override
    public void postSimulation(SimulationResult simulationResult) {
        ResultTypes resultTypes = new ResultTypes();
        simulationResult.getSeamcatResults().add(new SimulationResultGroup("Calculated Radius", resultTypes, scenario));
        if ( !useUserDefinedDRSS ) {
            resultTypes.getSingleValueTypes().add(new DoubleResultType("Victim Link Transmitter - Coverage radius", "km", scenario.getVictimCoverageRadius()));
        }

        for (int ii = 0, stopi = scenario.getInterferenceLinks().size(); ii < stopi; ii++) {
            InterferenceLink link = (InterferenceLink) scenario.getInterferenceLinks().get(ii);
            resultTypes.getSingleValueTypes().add(new DoubleResultType("Interfering Link Transmitter " + (ii + 1) + " - Coverage radius", "km", scenario.getCoverageRadius(link)));
            resultTypes.getSingleValueTypes().add(new DoubleResultType("Interfering Link Transmitter " + (ii + 1) + " - Simulation radius", "km", link.getCalculatedSimulationRadius()));
        }
    }

    public static VectorValues calculate(double[] vector ) {
        if ( vector == null ) return null;
        double median = Mathematics.getMedian(vector, vector.length, true);
        double mean = Mathematics.getAverage(vector, vector.length, true);
        double stdDev = Mathematics.getStdDev(vector, mean, vector.length, true);
        return new VectorValues(nf.format(mean) + " dBm", nf.format(median) + " dBm",nf.format(stdDev) + " dB");
    }
}
