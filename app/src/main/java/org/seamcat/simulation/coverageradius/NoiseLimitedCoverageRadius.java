package org.seamcat.simulation.coverageradius;

import org.apache.log4j.Logger;
import org.seamcat.model.Scenario;
import org.seamcat.model.generic.GenericLink;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.coverageradius.CoverageRadiusPlugin;
import org.seamcat.model.propagation.Stats;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.simulation.result.MutableLinkResult;

import java.util.List;

import static org.seamcat.mathematics.Constants.KM_TO_METERS;
import static org.seamcat.simulation.LocalEnvironmentSelector.pickLocalEnvironment;

public class NoiseLimitedCoverageRadius implements CoverageRadiusPlugin<NoiseLimitedCoverageRadius.Input> {

    private static final Logger LOG = Logger.getLogger(NoiseLimitedCoverageRadius.class);

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
    }

    @Override
    public double evaluate(GenericSystem genericSystem, Input input) {
        double rRefAntHeightWt, rRefAntHeightVr, rFrequencyVr, rSens, rPeakGainVr, rPeakGainWt,
                rRefPower, rMaxDist, rMinDist;
        double rRmax;
        GenericLink path = genericSystem.getLink();
        PropagationModel propagationModel = path.getPropagationModel();

        double rFadingLoss, rAvailability, rFadingStdDev;

        rFadingStdDev = input.fadingStdDev();
        rAvailability = (input.availability()) / 100;
        rRefAntHeightVr = input.rxAntennaHeight();
        rRefAntHeightWt = input.txAntennaHeight();
        rFrequencyVr = input.frequency();
        rSens = genericSystem.getReceiver().getSensitivity();
        rPeakGainVr = genericSystem.getReceiver().getAntennaGain().peakGain();
        rPeakGainWt = genericSystem.getTransmitter().getAntennaGain().peakGain();
        rMaxDist = input.maxDistance() * KM_TO_METERS;
        rMinDist = input.minDistance() * KM_TO_METERS;
        if (rMinDist == 0){
            rMinDist = 1; // equivalent to 1 m
        }
        if (rMaxDist == 0){
            rMaxDist = 1; // equivalent to 1 m
        }
        rRefPower = input.txPower();

        rFadingLoss = -Stats.qi(rAvailability) * rFadingStdDev;

        double rR1, rR2, rR = 0;
        double rL1, rL2, rL;
        double rLogR1, rLogR2, rLogR;
        double rDR = 0;
        int i = 0;

        rR1 = rMinDist;
        rR2 = rMaxDist;

        rLogR1 = Math.log10(rR1);
        rLogR2 = Math.log10(rR2);
        MutableLinkResult result = new MutableLinkResult();
        result.setFrequency(rFrequencyVr);
        result.rxAntenna().setLocalEnvironment(pickLocalEnvironment(genericSystem.getReceiver().getLocalEnvironments()));
        result.txAntenna().setLocalEnvironment(pickLocalEnvironment(genericSystem.getTransmitter().getLocalEnvironments()));
        result.setTxRxDistance(rR1 / KM_TO_METERS);
        result.txAntenna().setHeight(rRefAntHeightWt);
        result.rxAntenna().setHeight(rRefAntHeightVr);

        rL1 = rRefPower
                + rPeakGainWt
                + rPeakGainVr
                - rSens
                - rFadingLoss
                - propagationModel.evaluate(result, false);


        result.setTxRxDistance(rR2 / KM_TO_METERS);
        rL2 = rRefPower
                + rPeakGainWt
                + rPeakGainVr
                - rSens
                - rFadingLoss
                - propagationModel.evaluate(result, false);


        if (LOG.isDebugEnabled()) {
            LOG.debug("R1 = MinDist = " + rMinDist);
            LOG.debug("R2 = MaxDist = " + rMaxDist);
            LOG.debug("L1 = rRefPower + rPeakGainWt + rPeakGainVr - rSens - rFadingLoss - "
                    + propagationModel.getClass()
                    + ".medianLoss(rFrequencyVr,rR1 / KMTOM, rRefAntHeightWt, "
                    + "rRefAntHeightVr, rPeakGainWt, rPeakGainVr) -> "
                    + rL1
                    + " = "
                    + rRefPower
                    + " + "
                    + rPeakGainWt
                    + " + "
                    + rPeakGainVr
                    + " - "
                    + rSens
                    + " - "
                    + rFadingLoss
                    + " - "
                    + propagationModel.getClass()
                    + ".medianLoss("
                    + rFrequencyVr
                    + ","
                    + rR1 / KM_TO_METERS
                    + ","
                    + rRefAntHeightWt + "," + rRefAntHeightVr + ")");
            LOG.debug("L2 = rRefPower + rPeakGainWt + rPeakGainVr - rSens - rFadingLoss - "
                    + propagationModel.getClass()
                    + ".medianLoss(rFrequencyVr,rR2 / KMTOM, rRefAntHeightWt, "
                    + "rRefAntHeightVr, rPeakGainWt, rPeakGainVr) -> "
                    + rL2
                    + " = "
                    + rRefPower
                    + " + "
                    + rPeakGainWt
                    + " + "
                    + rPeakGainVr
                    + " - "
                    + rSens
                    + " - "
                    + rFadingLoss
                    + " - "
                    + propagationModel.getClass()
                    + ".medianLoss("
                    + rFrequencyVr
                    + ","
                    + rR2 / KM_TO_METERS
                    + ","
                    + rRefAntHeightWt + "," + rRefAntHeightVr + ")");

        }

        if (rL1 * rL2 >= 0) {
            // Zero value is not bracketted
            rRmax = rMaxDist/KM_TO_METERS;
        } else if (rL1 > 0) {
            // swap rL2 and rL1
            rL = rL1;
            rL1 = rL2;
            rL2 = rL;

            // swap rLogR2 and rLogR1
            rLogR = rLogR1;
            rLogR1 = rLogR2;
            rLogR2 = rLogR;

            // swap rLogR2 and rLogR1
            rR = rR1;
            rR1 = rR2;
            rR2 = rR;

        }
        for (i = 0; i < 1000; i++) {
            rLogR = rLogR1 + (rLogR2 - rLogR1) * rL1 / (rL1 - rL2);
            rR = Math.pow(10.0, rLogR);
            result.setTxRxDistance(rR / KM_TO_METERS);
            rL = rRefPower
                    + rPeakGainWt
                    + rPeakGainVr
                    - rSens
                    - rFadingLoss
                    - propagationModel.evaluate(result, false);

            if (rL < 0.0) {
                rDR = rL1 - rL;
                rL1 = rL;
                rR1 = rR;
                rLogR1 = rLogR;
            } else {
                rDR = rL2 - rL;
                rL2 = rL;
                rR2 = rR;
                rLogR2 = rLogR;
            }

            if (Math.abs(rDR) < 0.1 || rL == 0.0) {
                break;
            }
        }
        rRmax = rR / KM_TO_METERS;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Transceiver name: " + genericSystem.toString());
            LOG.debug("FadingStdDev = " + rFadingStdDev);
            LOG.debug("Availability = " + rAvailability);
            LOG.debug("RefAntHeightWt = " + rRefAntHeightWt);
            LOG.debug("RefAntHeightVr = " + rRefAntHeightVr);
            LOG.debug("FrequencyVr = " + rFrequencyVr);
            LOG.debug("Sensitivity = " + rSens);
            LOG.debug("PeakGainVr = " + rPeakGainVr);
            LOG.debug("PeakGainWt = " + rPeakGainWt);
            LOG.debug("MaxDist = " + rMaxDist);
            LOG.debug("MinDist = " + rMinDist);
            LOG.debug("RefPower = " + rRefPower);
            LOG.debug("FadingLoss = -Stats.qi(Availability) * FadingStdDev = "
                    + rFadingLoss + " = " + -Stats.qi(rAvailability) + " * "
                    + rFadingStdDev);
            LOG.debug("Coverage Radius (Noise Limited) = " + rRmax);
        }

        return rRmax;
    }

    public interface Input {
        @Config(order = 1, name = "Ref. antenna height (Rx)", unit = "m")
        double rxAntennaHeight();

        @Config(order = 2, name = "Ref. antenna height (Tx)", unit = "m")
        double txAntennaHeight();

        @Config(order = 3, name = "Ref. frequency (Tx)", unit = "MHz")
        double frequency();

        @Config(order = 4, name = "Ref. power (Tx)", unit = "dBm")
        double txPower();

        @Config(order = 5, name = "Minimum distance", unit = "km")
        double minDistance();

        @Config(order = 6, name = "Maximum distance", unit = "km")
        double maxDistance();

        @Config(order = 7, name = "Availability", unit = "%")
        double availability();

        @Config(order = 8, name = "Fading Std. Dev.", unit = "dB")
        double fadingStdDev();
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Noise Limited",
                "<html>The coverage of the transmitter is limited only by propagation <br>" +
                        "losses and other elements of link budget, with received signal operating <br>" +
                        "at the very sensitivity limit.</html>");
    }
}
