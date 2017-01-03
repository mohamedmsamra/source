package org.seamcat.simulation.generic;

import org.apache.log4j.Logger;
import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Function;
import org.seamcat.model.generic.GenericReceiver;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.simulation.CollectedResults;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorResultType;
import org.seamcat.simulation.OptionalSimulation;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResults;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.seamcat.model.mathematics.Mathematics.powerSummation;
import static org.seamcat.model.simulation.result.SimulationResult.dBm;

/**
 * Implementation of the overloading algorithm
 */
public class Overloading implements OptionalSimulation {

    private static Logger LOG = Logger.getLogger(Overloading.class);
    public static String OVERL      = "Delta Overloading";

    private final GenericSystem victimSystem;
    private final Scenario scenario;

    public Overloading( GenericSystem victimSystem, Scenario scenario ) {
        this.victimSystem = victimSystem;
        this.scenario = scenario;
    }

    @Override
    public void collect( MutableEventResult result ) {
        LOG.debug("Overloading summation");
        // Overloading summation
        // Delta-overloading (set to extreme low to ensure any calculated values
        // will always be closer to the threshold)
        double vrFreq = result.getVictimSystemLinks().get(0).getFrequency();
        Function oF = victimSystem.getReceiver().getOverloadingMask();

        List<Double> sumFreq = new ArrayList<>();
        List<Double> sumIRSSo= new ArrayList<>();
        int sumN = 0;

        for (InterferenceLink iLink : scenario.getInterferenceLinks()) {
            MutableInterferenceLinkResults link = result.getInterferenceLinkResult( iLink );
            for (MutableInterferenceLinkResult linkResult : link.getInterferenceLinkResults()) {
                IRSSOverloadingValue iRSSoValue = overloadingInterference(linkResult);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Summarizing iTx (freq=%f,iRSSo=%f)", iRSSoValue.getFrequency(), iRSSoValue.getiRSSo()));
                }
                // Find existing sum for frequency
                boolean existingSum = false;
                for (int y = 0; y < sumN && !existingSum; y++) {
                    if (sumFreq.get(y) == iRSSoValue.getFrequency()) {
                        sumIRSSo.set(y, powerSummation(sumIRSSo.get(y), iRSSoValue.getiRSSo()) );

                        if ( LOG.isDebugEnabled()) {
                            LOG.debug(String.format("Adding to existing sum (freq=%f,sum(iRSSo)=%f)", iRSSoValue.getFrequency(), sumIRSSo.get(y)));
                        }
                        existingSum = true;
                    }
                }

                // If no sum exists
                if (!existingSum) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format("New sum (sumN=%d, freq=%f,sum(iRSSo)=%f)", sumN, iRSSoValue.getFrequency(), iRSSoValue.getiRSSo()));
                    }

                    sumIRSSo.add(iRSSoValue.getiRSSo());
                    sumFreq.add(iRSSoValue.getFrequency());
                    sumN++;
                }
            }
        }
        LOG.debug("Subtracting overload thresholds");
        LOG.debug("Selecting strongest overload");
        // Calculate delta overload (iRSSo - Oth(freq))
        double rSumO = -999999999;
        for (int x = 0; x < sumN; x++) {
            double dFreq = sumFreq.get(x) - vrFreq;
            double oth = oF.evaluate(dFreq);
            sumIRSSo.set( x, sumIRSSo.get(x) - oth );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug(String.format("New sum (sumN=%d,freq=%f,dFreq=%f,oth=%f,delta_overload=%f)", x, sumFreq.get(x), dFreq, oth, sumIRSSo.get(x)));
            }
            rSumO = Math.max(rSumO, sumIRSSo.get(x));
        }
        if ( LOG.isDebugEnabled()) {
            LOG.debug(String.format("rSumO = %f", rSumO));
        }
        result.addValue(OVERL, rSumO);
    }

    private IRSSOverloadingValue overloadingInterference(MutableInterferenceLinkResult result ) {
        double rItPower = result.getInterferingSystemLink().getTxPower();

        GenericReceiver victimReceiver = (GenericReceiver) result.getInterferenceLink().getVictimSystem().getReceiver();
        GenericTransmitter interfererTransmitter = (GenericTransmitter) result.getInterferenceLink().getInterferingSystem().getTransmitter();
        GenericSystemPlugin.pathAntGains(result, victimReceiver, interfererTransmitter);
        double rItVrEffectivePathLoss = result.getEffectiveTxRxPathLoss();
        double rFreqIt = result.getInterferingSystemLink().getFrequency();
        double rFreqVr = result.getVictimSystemLink().getFrequency();

        double dFreq = rFreqIt - rFreqVr;
        double rFilter = victimReceiver.getReceiverFilter().evaluate(dFreq);

        double riRSSValue = rItPower - rItVrEffectivePathLoss - rFilter ;

        // Power Control
        double rItPowerControlGain = 0;
        if (interfererTransmitter.isUsingPowerControl()) {
            rItPowerControlGain = result.getInterferingSystemLink().getValue(GenericSystem.TX_POWER_CONTROL_GAIN);
        }
        riRSSValue += rItPowerControlGain;
        IRSSOverloadingValue overloading = new IRSSOverloadingValue(result.getFrequency(), riRSSValue);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Victim receiver overloading calculations");
            LOG.debug(format("Interfering Transmitter power = %f", rItPower));
            LOG.debug(format("Interfering Transmitter -> Victim Receiver Effective Path Loss (with MCL) = %f", rItVrEffectivePathLoss));
            LOG.debug(format("Interfering Transmitter frequency %f", result.getInterferingSystemLink().getFrequency()));
            LOG.debug(format("Delta frequency = %f", dFreq));
            LOG.debug(format("Victim Receiver Filtering = %f", rFilter));
            LOG.debug("IT power control gain = " + rItPowerControlGain);
        }

        return overloading;
    }

    @Override
    public SimulationResultGroup buildResult(CollectedResults collected) {
        ResultTypes over = new ResultTypes();
        over.getVectorResultTypes().add( new VectorResultType(OVERL, dBm, collected.vectorResults().remove(OVERL)));

        return new SimulationResultGroup(OVERL, over, scenario);
    }
}
