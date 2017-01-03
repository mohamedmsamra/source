package org.seamcat.simulation.calculator;

import org.apache.log4j.Logger;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.generic.GenericReceiver;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.types.Receiver;
import org.seamcat.model.types.Transmitter;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;

public class InterferenceCalculator {

    private static final Logger LOG = Logger.getLogger(InterferenceCalculator.class);

    public static void unwantedInterference(Scenario scenario, MutableInterferenceLinkResult result) {
        double rItPower = result.getTxPower();
        double rItVrEffectivePathLoss = result.getEffectiveTxRxPathLoss();

        // Relative unwanted emission in dBc
        double rUnwantedEmissionRel = itUnwantedEmissions(scenario, result);

        // Power control gain
        double rItPowerControlGain = 0;

        Transmitter transmitter = result.getInterferenceLink().getInterferingSystem().getTransmitter();
        if (transmitter instanceof GenericTransmitter && ((GenericTransmitter) transmitter).isUsingPowerControl()) {
            rItPowerControlGain = result.getInterferingSystemLink().getValue(GenericSystem.TX_POWER_CONTROL_GAIN);
        }

        // Calculation of unwanted emission
        double rUnwantedEmissionAbs = rItPower + rUnwantedEmissionRel + rItPowerControlGain;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering Transmitter Tx Power trial = " + rItPower);
            LOG.debug("Absolute Unwanted Emission = ItPower + RelUnwanted + ItPower Gain = " + rUnwantedEmissionAbs);
        }

        // Optional threshold with unwanted emission floor
        if (transmitter.isUsingEmissionsFloor()) {
            rUnwantedEmissionAbs = Math.max(rUnwantedEmissionAbs, itUnwantedReference(scenario, result));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Using unwanted emission floor");
                LOG.debug("Absolute Unwanted Emission = MAX(Abs Unwanted, Ref Unwanted) = " + rUnwantedEmissionAbs);
            }
        }

        result.setRiRSSUnwantedValue(rUnwantedEmissionAbs - rItVrEffectivePathLoss);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering Transmitter -> Victim Receiver Effective Path Loss (with MCL)= " + rItVrEffectivePathLoss);
            LOG.debug("IT power control gain = " + rItPowerControlGain);
        }
    }

    public static void blockingInterference(Scenario scenario, MutableInterferenceLinkResult result) {//TODO scenario reference added by KK
        double rItPower = result.getInterferingSystemLink().getTxPower();
        double rItVrEffectivePathLoss = result.getEffectiveTxRxPathLoss();

        //TODO start modification KK - note that scenario is added to get access to the pre-simulated results
        double rAttenuation = 0;
        {
            double rFreqIt, rFreqVr;
            rFreqIt = result.getInterferingSystemLink().getFrequency();
            rFreqVr = result.getVictimSystemLink().getFrequency();
            Function blockingMaskIntegral = scenario.getPreSimulationResults(result.getInterferenceLink().getInterferingSystem()).getBlockingMaskIntegral();
            if (blockingMaskIntegral != null) {
                if ( blockingMaskIntegral.getBounds().contains( rFreqIt - rFreqVr ))
                {
                    rAttenuation = blockingMaskIntegral.evaluate(rFreqIt - rFreqVr);
                } else {
                    LOG.error("OUT OF BOUNDS");
                    rAttenuation = vrAttenuation(result);
                }
            } else { // in case no pre-simulated results available
                rAttenuation = vrAttenuation(result);
            }
        }
        //TODO end modification KK


        result.setBlockingAttenuation(rAttenuation);
        // Power Control
        double rItPowerControlGain = 0;
        if ( result.getInterferenceLink().getInterferingSystem() instanceof GenericSystem ) {
            GenericSystem system = (GenericSystem) result.getInterferenceLink().getInterferingSystem();
            if (system.getTransmitter().isUsingPowerControl()) {
                rItPowerControlGain = result.getInterferingSystemLink().getValue(GenericSystem.TX_POWER_CONTROL_GAIN);
            }
        }

        result.setRiRSSBlockingValue(rItPower + rItPowerControlGain - rItVrEffectivePathLoss - rAttenuation);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering Transmitter power = " + rItPower);
            LOG.debug("Interfering Transmitter -> Victim Receiver Effective Path Loss (with MCL)= " + rItVrEffectivePathLoss);
            LOG.debug("Victim Receiver Attenuation = " + rAttenuation);
            LOG.debug("IT power control gain = " + rItPowerControlGain);
        }
    }

    private static double vrAttenuation(MutableInterferenceLinkResult result) {
        Function blockingAttenuation;
        double rResult;
        double rFreqIt, rFreqVr;

        rFreqIt = result.getInterferingSystemLink().getFrequency();
        rFreqVr = result.getVictimSystemLink().getFrequency();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Cached Interfering Transmitter Frequency = " + rFreqIt);
            LOG.debug("Cached Victim Receiver Frequency = " + rFreqVr);
        }


        double offset = 0;
        Receiver receiver = result.getInterferenceLink().getVictimSystem().getReceiver();
        if ( result.getInterferenceLink().getVictimSystem() instanceof GenericSystem) {
            GenericReceiver genericReceiver = (GenericReceiver) receiver;
            if (genericReceiver.getBlockingAttenuationMode() == GenericReceiver.BlockingAttenuationMode.PROTECTION_RATIO) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Attenuation Mode = Protection ratio");
                }
                offset = vrAttenuationProcRatio(genericReceiver);
            } else if (genericReceiver.getBlockingAttenuationMode() == GenericReceiver.BlockingAttenuationMode.MODE_SENSITIVITY) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Attenuation Mode = Sensitivity");
                }
                offset = vrAttenuationSens(genericReceiver);
            }
        }
        blockingAttenuation = receiver.getBlockingMask();

        Function offsetFunction = blockingAttenuation.offset( offset );
        rResult = offsetFunction.evaluate(rFreqIt - rFreqVr);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Victim Receiver Blocking Response.evaluate(FrequencyIT (" + rFreqIt + ") - Frequency VR (" + rFreqVr + ") ) = " + rResult);
        }

        return rResult;
    }

    private static double vrAttenuationProcRatio(GenericReceiver victimReceiver) {
        double rOffset = victimReceiver.getNoiseAugmentation() + victimReceiver.getExtendedProtectionRatio() - victimReceiver.getInterferenceToNoiseRatio();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Victim Receiver C/(N+I) Level = " + victimReceiver.getExtendedProtectionRatio());
            LOG.debug("Victim Receiver (N+I)/N Level = " + victimReceiver.getNoiseAugmentation());
            LOG.debug("Victim Receiver I/N Level = " + victimReceiver.getInterferenceToNoiseRatio());
            LOG.debug("Victim Receiver Blocking Response Offset = " + rOffset + " = (N+I)/N + C/(N+I) + I/N");
        }

        return rOffset;
    }

    private static double vrAttenuationSens(GenericReceiver victimReceiver) {
        double rOffset = victimReceiver.getExtendedProtectionRatio() - victimReceiver.getSensitivity() - victimReceiver.getInterferenceToNoiseRatio();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Victim Receiver CNI Level = " + victimReceiver.getExtendedProtectionRatio());
            LOG.debug("Victim Receiver Sensitivity = " + victimReceiver.getSensitivity());
            LOG.debug("Victim Receiver IN level = " + victimReceiver.getInterferenceToNoiseRatio());
            LOG.debug("Victim Receiver Blocking Response Offset = " + rOffset + " = CNI - Sensitivity - IN");
        }
        return rOffset;
    }


    private static double itUnwantedEmissions(Scenario scenario, MutableInterferenceLinkResult result) {
        EmissionMask unWantEmission;
        double rResult;
        double rFreqIt, rFreqVr;
        double rVrBandwidth;

        rFreqIt = result.getInterferingSystemLink().getFrequency();
        rFreqVr = result.getVictimSystemLink().getFrequency();

        unWantEmission = scenario.getPreSimulationResults( result.getInterferenceLink().getInterferingSystem()).getNormalizedEmissionsMask();
        rVrBandwidth = result.getRxBandwidth();

        rResult = unWantEmission.integrate(rFreqVr - rFreqIt, rVrBandwidth);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering Transmitter Frequency = " + rFreqIt);
            LOG.debug("Victim Receiver Frequency = " + rFreqVr);
            LOG.debug("Victim Receiver Bandwith = " + rVrBandwidth);
            LOG.debug("Relative Unwanted Emission = .integrate((VrFreq - ItFreq), VrBandwith) = " + rResult + " dBc");
        }
        result.setValue(RadioSystem.UNWANTED_EMISSION_INTEGRATION, rResult);
        return rResult;
    }

    private static double itUnwantedReference(Scenario scenario, MutableInterferenceLinkResult result) {
        EmissionMask unWantReference;
        double rResult;
        double rFreqIt, rFreqVr;
        double rVrBandwidth;

        rFreqIt = result.getInterferingSystemLink().getFrequency();
        rFreqVr = result.getVictimSystemLink().getFrequency();
        rVrBandwidth = result.getRxBandwidth(); //getInterferenceLink().getVictimSystem().getReceiver().getBandwidth();

        unWantReference = scenario.getPreSimulationResults(result.getInterferenceLink().getInterferingSystem()).getNormalizedEmissionsFloor();

        rResult = unWantReference.integrate(rFreqVr - rFreqIt, rVrBandwidth);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering Transmitter Frequency = " + rFreqIt);
            LOG.debug("Victim Receiver Frequency = " + rFreqVr);
            LOG.debug("Victim Receiver Bandwith = " + rVrBandwidth);
            LOG.debug("Reference Unwanted Emission = .integrate((VrFreq - ItFreq), VrBandwith) = " + rResult + " dBc");
        }

        return rResult;
    }


    public static Bounds calculateBounds(EmissionMask emissionMask ) {
        double lower = 0, upper = 0, powerStep, offset;
        double limit = emissionMask.evaluate(0.) - 23.0;
        double start = emissionMask.getBounds().getMin();
        double step = 0.01;  // 10 kHz //TODO changed to 10 kHz to cover narrow band transmitters like PMSE
        /* lower part */
        offset = start;
        powerStep = emissionMask.evaluate(start);
        if (powerStep >= limit) { // flat mask
            lower = start;
        } else {
            while (powerStep < limit) {
                offset += step;
                powerStep = emissionMask.evaluate(offset);
            }
            lower = offset;
        }

        /* upper part*/
        start = emissionMask.getBounds().getMax();
        offset = start;
        powerStep = emissionMask.evaluate(start);
        if (powerStep >= limit) {
            upper = start;
        } else {
            while (powerStep < limit) {
                offset -= step;
                powerStep = emissionMask.evaluate(offset);
            }
            upper = offset;
        }
        return new Bounds(lower, upper, true);
    }

}
