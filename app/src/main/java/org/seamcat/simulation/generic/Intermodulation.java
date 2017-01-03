package org.seamcat.simulation.generic;

import org.apache.log4j.Logger;
import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Function;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.simulation.CollectedResults;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.Unit;
import org.seamcat.model.types.result.NamedVectorResult;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorGroupResultType;
import org.seamcat.model.types.result.VectorResultType;
import org.seamcat.scenario.WorkspaceScenario;
import org.seamcat.simulation.OptionalSimulation;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.log10;

/**
 * Implementation of the intermodulation algorithm
 */
public class Intermodulation implements OptionalSimulation {

    private static Logger LOG = Logger.getLogger(Intermodulation.class);
    public static final String INTERMOD   = "iRSS Intermodulation";
    public static final String IRSS = "iRSS";
    public static final String VR_INTERMOD = "victim intermodulation response";

    private static final String INTER_VEC  = "Intermodulation summation Vector";

    private final WorkspaceScenario scenario;
    private final GenericSystem victimSystem;
    private final double noiseAugmentation;
    private final double interferenceToNoiseRatio;

    public Intermodulation(Scenario scenario, GenericSystem victimSystem) {
        this.victimSystem = victimSystem;
        this.scenario = (WorkspaceScenario) scenario;
        noiseAugmentation = victimSystem.getReceiver().getNoiseAugmentation();
        interferenceToNoiseRatio = victimSystem.getReceiver().getInterferenceToNoiseRatio();
    }


    @Override
    public void collect(MutableEventResult result) {
        List<MutableInterferenceLinkResult> flattenedResult = countInterferenceLinkResults(result);
        int numberOfInterferingSystems = flattenedResult.size();
        if (numberOfInterferingSystems > 1) {
            LOG.debug("Calculation of iRSS - Intermodulation values");
            double rSumI = 0;
            for (int m = 0; m < numberOfInterferingSystems; m++) {
                for (int k = 0; k < numberOfInterferingSystems; k++) {
                    if (m != k) {
                        double value = iRSSLinkBudgetInterMod(flattenedResult.get(m), flattenedResult.get(k), m,k)
                                + flattenedResult.get(m).getVictimSystemLink().getRxNoiseFloor();
                        String name = INTERMOD + prettyPrefix(m,k);
                        rSumI += Math.pow(10, value / 10);
                        result.addValue(name, value);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(name + " = " + value);
                            LOG.debug("rSumI = " + rSumI);
                        }
                    }
                }
            }
            result.addValue(INTER_VEC, 10 * log10(rSumI));
        }
    }

    private String prettyPrefix(int m, int k) {
        return " " + (m + 1) + " / " + (k + 1);
    }

    @Override
    public SimulationResultGroup buildResult(CollectedResults collected) {
        ResultTypes intermod = new ResultTypes();
        intermod.getVectorResultTypes().add( new VectorResultType(INTERMOD + " Summation", Unit.dBm.name(), collected.vectorResults().remove(INTER_VEC)));

        VectorGroupResultType group = new VectorGroupResultType(INTERMOD, Unit.dBm.name());
        for (Map.Entry<String, double[]> entry : collected.vectorResults().entrySet()) {
            if ( entry.getKey().startsWith(INTERMOD)) {
                group.addVector( new NamedVectorResult(entry.getKey(), entry.getValue()));
            }
        }
        intermod.getVectorGroupResultTypes().add( group );

        return new SimulationResultGroup(INTERMOD, intermod, scenario);
    }

    private List<MutableInterferenceLinkResult> countInterferenceLinkResults(MutableEventResult result) {
        List<MutableInterferenceLinkResult> flattenedResult = new ArrayList<>();
        Set<InterferenceLink> set = scenario.getOriginalPositionMap().keySet();
        for ( InterferenceLink link : set) {
            for ( MutableInterferenceLinkResult linkResult : result.getInterferenceLinkResult(link).getInterferenceLinkResults() ) {
                if ( linkResult.getInterferenceLink().getVictimSystem() instanceof GenericSystem &&
                        linkResult.getInterferenceLink().getInterferingSystem() instanceof  GenericSystem) {
                    flattenedResult.add(linkResult);
                }
            }
        }
        return flattenedResult;
    }


    /**
     * Method that calculates the different iRSS due to intermodulation interference for each couple of transmitting <br>
     *     interferer defined in the interfering link (EGE/4030)
     */
    private double iRSSLinkBudgetInterMod(MutableInterferenceLinkResult res1, MutableInterferenceLinkResult res2, int index1, int index2) {
        double riRSSValue1, riRSSValue2;

        riRSSValue1 = getIRSS(res1);
        res1.setValue(IRSS, riRSSValue1);

        riRSSValue2 = getIRSS(res2);
        res2.setValue(IRSS, riRSSValue2);

        double rIntermodResponse = vrInterModResponse(res1, res2);
        res1.getVictimSystemLink().setValue(VR_INTERMOD + prettyPrefix(index1, index2), rIntermodResponse);
        double rSens = victimSystem.getReceiver().getSensitivity();

        if (rIntermodResponse >= Double.MAX_VALUE) {
            return -1000;
        } else {
            return 2 * riRSSValue1 + riRSSValue2 - 3 * rIntermodResponse - 3 * rSens - (3*(noiseAugmentation - interferenceToNoiseRatio));
        }
    }

    private double getIRSS(MutableInterferenceLinkResult link) {
        double iRSS = link.getInterferingSystemLink().getTxPower() - link.getEffectiveTxRxPathLoss();

        if (((GenericSystem)link.getInterferenceLink().getInterferingSystem()).getTransmitter().isUsingPowerControl()) {
            double rItPowerControl1 = link.getInterferingSystemLink().getValue(GenericSystem.TX_POWER_CONTROL_GAIN);
            iRSS += rItPowerControl1;
        }

        return iRSS;
    }

    /**
     * Method that calculates the inter-modulation response of the victim receiver level caused by the interfering <br>
     * transmitter.(EGE/4020)
     */
    private double vrInterModResponse(MutableInterferenceLinkResult res1, MutableInterferenceLinkResult res2) {
        double rFreqVr, rFreq0, rFreq1, rFreq2;
        Function IntermodResponse;

        IntermodResponse = victimSystem.getReceiver().getIntermodulationRejection();
        rFreqVr = res1.getVictimSystemLink().getFrequency();
        rFreq1 = res1.getInterferingSystemLink().getFrequency();
        rFreq2 = res2.getInterferingSystemLink().getFrequency();

        rFreq0 = 2 * rFreq1 - rFreq2;
        return IntermodResponse.evaluate(rFreq0 - rFreqVr);
    }
}
