package org.seamcat.model.scenariocheck;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.Workspace;
import org.seamcat.model.core.InterferenceLink;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.distributions.*;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.generic.GenericReceiver;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.presentation.systems.generic.InterferenceCriteriaCalculator;
import org.seamcat.simulation.generic.GenericVictimSystemSimulation;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.String.format;
import static org.seamcat.model.scenariocheck.InterferingLinkCheck.fAndU;

public class VictimLinkCheck extends AbstractCheck {

    private static final String LINK = "Victim Link";

    public VictimLinkCheck(String prefix) {
        result.setCheckName(prefix+LINK);

    }

    public ScenarioCheckResult check(Workspace workspace) {
        SystemSimulationModel vsl = workspace.getVictimSystemLink();
        org.seamcat.model.types.Transmitter vt = vsl.getSystem().getTransmitter();
        Bounds bounds = workspace.getVictimFrequency().getBounds();
        double rFreqVrMin = bounds.getMin();
        double rFreqVrMax = bounds.getMax();
        boolean bFreqVrBounded = bounds.isBounded();
        double victimFreqRange = rFreqVrMax - rFreqVrMin;
        org.seamcat.model.types.Receiver vr = vsl.getSystem().getReceiver();
        double bandwidthRange = victimFreqRange + (vr.getBandwidth() / 2);

        List<org.seamcat.model.types.InterferenceLink> links = new ArrayList<>();
        links.addAll( workspace.getInterferenceLinks() );
        boolean isUsingCognitiveRadio = GenericVictimSystemSimulation.crCount(links) > 0;

        //TODO added consistency VLR BW against blocking mask - start
        String bandwidthWarning = "<HtMl><p style = 'color:red; font-weight:bold;'>This will cause erroneously averaged mask values.</p>";
        if (vsl.getSystem() instanceof GenericSystem){
            if (((GenericReceiver) vsl.getSystem().getReceiver()).getBlockingAttenuationMode().equals(GenericReceiver.BlockingAttenuationMode.MODE_SENSITIVITY)){
                if (vsl.getSystem().getReceiver().getBlockingMask().evaluateMin() >= 0){
                    addErrorMsg("Using the Blocking mode SENSITIVITY requires on the Blocking mask absolute values in dBm" + bandwidthWarning);
                }
            } else {
                if (vsl.getSystem().getReceiver().getBlockingMask().evaluateMax() <= 0){
                    if (!vsl.getSystem().getReceiver().getBlockingMask().isConstant()
                            || !Mathematics.equals(0,vsl.getSystem().getReceiver().getBlockingMask().getConstant(),1e-5)) { // this assumes that the default constant = 0 is always NOT MODE_SENSITIVITY
                        addErrorMsg("Using the Blocking mode 'User defined' or 'Protection ratio' requires positive values on the Blocking mask" + bandwidthWarning);
                    }
                }
            }
            if (!vsl.getSystem().getReceiver().getBlockingMask().isConstant() && checkPseudoConstant(vsl.getSystem().getReceiver().getBlockingMask())){
                double att = vsl.getSystem().getReceiver().getBlockingMask().evaluate(0), bw = 0;
                for (double f=0;f<vsl.getSystem().getReceiver().getBlockingMask().getBounds().getMax();f+=0.01){
                    if (Math.abs(att - vsl.getSystem().getReceiver().getBlockingMask().evaluate(f)) > 3){
                        bw = f;
                        break;
                    }
                }
                if (2*bw/vsl.getSystem().getReceiver().getBandwidth() > 1.4 || 2*bw/vsl.getSystem().getReceiver().getBandwidth() < 0.7)
                    addErrorMsg("The 3-dB-bandwidth of the Blocking mask (" + Math.rint(2*bw*1000)/1000 + " MHz) " +
                            "conflicts with the VLR bandwidth set to " + vsl.getSystem().getReceiver().getBandwidth() + " MHz" + bandwidthWarning);
            }
        } else  if (vsl.isDMASystem()){
            if (!vsl.getSystem().getReceiver().getBlockingMask().isConstant() && checkPseudoConstant(vsl.getSystem().getReceiver().getBlockingMask())){
                double att = vsl.getSystem().getReceiver().getBlockingMask().evaluate(0), bw = 0;
                for (double f=0;f<vsl.getSystem().getReceiver().getBlockingMask().getBounds().getMax();f+=0.01){
                    if (Math.abs(att - vsl.getSystem().getReceiver().getBlockingMask().evaluate(f)) > 3){
                        bw = f;
                        break;
                    }
                }
                if (2 * bw/vsl.getDMASystem().getSystemSettings().getBandwidth() > 1.4 ||
                        2 * bw/vsl.getDMASystem().getSystemSettings().getBandwidth()< 0.7)
                    addErrorMsg("The 3-dB-bandwidth of the Blocking mask/ACS (" + Math.rint(2*bw*1000)/1000 + " MHz) " +
                            "conflicts with the system bandwidth set to " + vsl.getDMASystem().getSystemSettings().getBandwidth() + " MHz" + bandwidthWarning);
            }
        }
        //TODO added consistency VLR BW against blocking mask - end

        if (!vsl.isDMASystem()) {
            GenericReceiver gvr = (GenericReceiver) vr;
            GenericTransmitter gvt = (GenericTransmitter) vt;
            if (bFreqVrBounded) {
                if (!isUsingCognitiveRadio) {
                    // Victim Receiver Blocking check
                    checkBlocking(workspace, vsl, rFreqVrMin, rFreqVrMax);

                    // Vitim Receiver Intermodulation Check
                    if (gvr.isIntermodulationRejectionOption()) {
                        checkIntermodulation(workspace, gvr, rFreqVrMin, rFreqVrMax);
                    }

                }
            } else {
                addErrorMsg("Unbounded frequency distribution in victim link");
            }

            // Check for consistency of interference criteria values
            if (!(InterferenceCriteriaCalculator.isConsistent(gvr.getProtectionRatio(), gvr.getExtendedProtectionRatio(),
                    gvr.getNoiseAugmentation(), gvr.getInterferenceToNoiseRatio()))) {
                addErrorMsg("The interference criteria is not consistent." +
                        "<br>Use the interference criteria calculator to find a consistent setup." +
                        "<br>Note that these values are used in the blocking calculation for Protection " +
                        "<br> ratio and Sensitivity mode. You may disregard this warning if you are not" +
                        "<br> intending to use one of these two modes. However, it is highly recommended" +
                        "<br> to keep the values consistent, in particular if you intend to use a criterion" +
                        "<br> different from C/I for the interference calculation. Criteria not in line " +
                        "<br> with the noise floor and the sensitivity might cause physically incorrect results.");
            }



            // Check random parameters of victim link
            checkDistribution(workspace.getVictimFrequency(), LINK,
                    "Victim Receiver: Frequency");
            checkDistribution(gvr.getNoiseFloor(), LINK,
                    "Victim Receiver: Noise Floor");
            checkDistribution(gvr.getHeight(), LINK,
                    "Victim Receiver: Antenna Height");
            checkDistribution(gvr.getAntennaPointing().getAzimuth(), LINK,
                    "Victim Receiver: Antenna Azimuth");
            checkDistribution(gvr.getAntennaPointing().getElevation(), LINK,
                    "Victim Receiver: Antenna Elevation");

            checkDistribution(gvt.getPower(), LINK,
                    "Victim Link Transmitter Power");
            checkDistribution(gvt.getHeight(), LINK,
                    "Victim Link Transmiter: Antenna Height");
            checkDistribution(gvt.getAntennaPointing().getAzimuth(), LINK,
                    "Victim Link Transmiter: Antenna Azimuth");
            checkDistribution(gvt.getAntennaPointing().getElevation(), LINK,
                    "Victim Link Transmiter: Antenna Elevation");

            // Userdefined blocking attenuation mode in combi with overloading
            if (gvr.isUsingOverloading()) {
                if (gvr.getBlockingAttenuationMode() == GenericReceiver.BlockingAttenuationMode.USER_DEFINED) {
                    addErrorMsg("The Blocking attenuation mode in user defined and overloading feature have been selected," +
                            "<br> therefore the Blocking response and the receiver filter of the overloading are the same element." +
                            "<br>Please make sure that they are not accounted twice.");
                }

                if (isUsingCognitiveRadio) {
                    {
                        Function function = gvr.getOverloadingMask();
                        if (!function.isConstant()) {
                            DiscreteFunction func = (DiscreteFunction) function;
                            double min = func.getBounds().getMin();
                            double max = func.getBounds().getMax();
                            if (!func.isConstant() && (max < victimFreqRange || abs(min) < victimFreqRange)) {
                                addErrorMsg(format("Victim Receiver: Overloading mask does not cover the full frequency offset range: -%s to +%s", fAndU(victimFreqRange), fAndU(victimFreqRange)));
                            }
                        }
                    }
                    {
                        Function function = gvr.getReceiverFilter();
                        if (!function.isConstant()) {
                            DiscreteFunction func = (DiscreteFunction) function;
                            double min = func.getBounds().getMin();
                            double max = func.getBounds().getMax();
                            if (!func.isConstant() && (max < victimFreqRange || abs(min) < victimFreqRange)) {
                                addErrorMsg(format("Victim Receiver: Receiver filter function does not cover the full frequency offset range: -%s to +%s", fAndU(victimFreqRange), fAndU(victimFreqRange)));
                            }
                        }
                    }
                }
            }

            if (isUsingCognitiveRadio) {

                // Check blocking response
                Function function = vr.getBlockingMask();
                if (!function.isConstant()) {
                    DiscreteFunction func = (DiscreteFunction) function;
                    double min = func.getBounds().getMin();
                    double max = func.getBounds().getMax();
                    if (!func.isConstant() && (max < victimFreqRange || abs(min) < victimFreqRange)) {
                        addErrorMsg(format("Victim Receiver: Blocking response function does not cover the full frequency offset range: -%s to +%s", fAndU(victimFreqRange), fAndU(victimFreqRange)));
                    }
                }

            }
        }


        if (isUsingCognitiveRadio) {

            Distribution freqDist = workspace.getVictimFrequency();
            if (freqDist instanceof GaussianDistributionImpl ||
                    freqDist instanceof RayleighDistributionImpl ||
                    freqDist instanceof UniformPolarDistanceDistributionImpl ||
                    freqDist instanceof UniformPolarAngleDistributionImpl) {
                addErrorMsg("<html>Frequency distribution of the victim system link cannot be of type Gaussian,<br>Rayleigh or UniformPolar distance/angle in combination with cognitive radio features</html>");
            }

            EmissionMask emissionsMask = vt.getEmissionsMask();
            Bounds emissionsMaskBounds = emissionsMask.getBounds();
            double emissionsMaskMin = emissionsMaskBounds.getMin();
            double emissionsMaskMax = emissionsMaskBounds.getMax();

            if (!emissionsMask.isConstant() && (emissionsMaskMax < bandwidthRange || abs(emissionsMaskMin) < bandwidthRange)) {
                addErrorMsg(format("Victim Link Transmitter: Emissions mask function does not cover the full frequency offset range: -%s to +%s", fAndU(bandwidthRange), fAndU(bandwidthRange)));
            }

            if (vt.isUsingEmissionsFloor()) {
                EmissionMask emissionsFloor = vt.getEmissionsFloor();
                Bounds emissionsFloorBounds = emissionsFloor.getBounds();
                double emissionsFloorMin = emissionsFloorBounds.getMin();
                double emissionsFloorMax = emissionsFloorBounds.getMax();

                if (!emissionsFloor.isConstant() && (emissionsFloorMax < bandwidthRange || abs(emissionsFloorMin) < bandwidthRange)) {
                    addErrorMsg(format("Victim Link Transmitter: Unwanted emissions floor function does not cover the full frequency offset range: -%s to +%s", fAndU(bandwidthRange), fAndU(bandwidthRange)));
                }
            }

        }

        return result;
    }

    private boolean checkPseudoConstant(BlockingMask blockingMask) {
        return !Mathematics.equals(blockingMask.evaluate(0), blockingMask.evaluateMax(), 0.1);
    }



    private void checkBlocking(Workspace workspace, SystemSimulationModel vsl,
                               double rFreqVrMin, double rFreqVrMax) {
        Function function = vsl.getSystem().getReceiver().getBlockingMask();
        if (!function.isConstant()) {
            DiscreteFunction func = (DiscreteFunction) function;
            double min = func.getBounds().getMin();
            double max = func.getBounds().getMax();



            for (InterferenceLink link : workspace.getInterferenceLinks()) {
                Distribution freq = link.getInterferingSystem().getFrequency();
                Bounds itbounds = freq.getBounds();
                if (itbounds.isBounded()) {
                    // Constant case:
                    if (rFreqVrMin == rFreqVrMax
                            && itbounds.getMax() == itbounds.getMin()) {

                        if (rFreqVrMax - itbounds.getMax() == 0){ // Fi and Fv are the same
                            if (max < itbounds.getMax() - rFreqVrMax){
                                addErrorMsg("Blocking response upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                            if (min > rFreqVrMax - itbounds.getMin()){
                                addErrorMsg("Blocking response lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                        }

                        if (rFreqVrMax - itbounds.getMax() < 0){ // Fi is higher than Fv (upper bound)
                            if (max < itbounds.getMax() - rFreqVrMax){
                                addErrorMsg("Blocking response upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                        }
                        if (rFreqVrMax - itbounds.getMax() > 0){ // Fi is smaller than Fv (lower bound)
                            if (min > itbounds.getMin() - rFreqVrMax){
                                addErrorMsg("Blocking response lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                        }
                    }
                    // Non constant case
                    else {

                        if ((rFreqVrMin - itbounds.getMin() < 0)&& (rFreqVrMax - itbounds.getMin() < 0)){ // Fv_min and Fv_max are below (or equal) Fi_min and Fi_max
                            if (max < itbounds.getMax() - rFreqVrMin){
                                addErrorMsg("Blocking response upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                        }

                        if ((rFreqVrMin - itbounds.getMin() < 0)&& (rFreqVrMax - itbounds.getMin() >= 0)&& (rFreqVrMax - itbounds.getMax() < 0)){ // Fv_min and Fv_max are on both side of Fi_min but below Fi_max
                            if (max < itbounds.getMax() - rFreqVrMin){
                                addErrorMsg("Blocking response upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                            if (min > itbounds.getMin() - rFreqVrMax){
                                addErrorMsg("Blocking response lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMin()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
                            }
                        }

                        if ((rFreqVrMin - itbounds.getMin() >= 0)&& (rFreqVrMax - itbounds.getMax() <= 0)){ // Fv_min and Fv_max are between Fi_min and Fi_max
                            if (max < itbounds.getMax() - rFreqVrMin){
                                addErrorMsg("Blocking response upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                            if (min > itbounds.getMin() - rFreqVrMax){
                                addErrorMsg("Blocking response lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMin()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
                            }
                        }

                        if ((rFreqVrMin - itbounds.getMin() > 0) &&(rFreqVrMin - itbounds.getMax() <= 0)&& (rFreqVrMax - itbounds.getMax() > 0)){ // Fv_min is between Fi_min and Fi_max and Fv_max is above Fi_max
                            if (max < itbounds.getMax() - rFreqVrMin){
                                addErrorMsg("Blocking response upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                            if (min > itbounds.getMin()- rFreqVrMax ){
                                addErrorMsg("Blocking response lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMin()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
                            }
                        }

                        if ((rFreqVrMin - itbounds.getMax() > 0)&& (rFreqVrMax - itbounds.getMax() > 0)){ // Fv_min and Fv_max are above Fi_max
                            if (min > itbounds.getMin()- rFreqVrMax){
                                addErrorMsg("Blocking response lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMin()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
                            }
                        }
                    }
                }
            }
        }
    }


    private void checkIntermodulation(Workspace workspace, GenericReceiver gvr,
                                      double rFreqVrMin, double rFreqVrMax) {
        Function function = gvr.getIntermodulationRejection();
        if (!function.isConstant()) {
            DiscreteFunction func = (DiscreteFunction) function;
            double min = func.getBounds().getMin();
            double max = func.getBounds().getMax();



            for (InterferenceLink link : workspace.getInterferenceLinks()) {
                Distribution freq = link.getInterferingSystem().getFrequency();
                Bounds itbounds = freq.getBounds();
                if (itbounds.isBounded()) {
                    // Constant case:
                    if (rFreqVrMin == rFreqVrMax
                            && itbounds.getMax() == itbounds.getMin()) {

                        if (rFreqVrMax - itbounds.getMax() == 0){ // Fi and Fv are the same
                            if (max < itbounds.getMax() - rFreqVrMax){
                                addErrorMsg("Intermodulation rejection upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                            if (min > rFreqVrMax - itbounds.getMin()){
                                addErrorMsg("Intermodulation rejection lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                        }

                        if (rFreqVrMax - itbounds.getMax() < 0){ // Fi is higher than Fv (upper bound)
                            if (max < itbounds.getMax() - rFreqVrMax){
                                addErrorMsg("Intermodulation rejection upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                        }
                        if (rFreqVrMax - itbounds.getMax() > 0){ // Fi is smaller than Fv (lower bound)
                            if (min > itbounds.getMin() - rFreqVrMax){
                                addErrorMsg("Intermodulation rejection lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                        }
                    }
                    // Non constant case
                    else {

                        if ((rFreqVrMin - itbounds.getMin() < 0)&& (rFreqVrMax - itbounds.getMin() < 0)){ // Fv_min and Fv_max are below (or equal) Fi_min and Fi_max
                            if (max < itbounds.getMax() - rFreqVrMin){
                                addErrorMsg("Intermodulation rejection upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                        }

                        if ((rFreqVrMin - itbounds.getMin() < 0)&& (rFreqVrMax - itbounds.getMin() >= 0)&& (rFreqVrMax - itbounds.getMax() < 0)){ // Fv_min and Fv_max are on both side of Fi_min but below Fi_max
                            if (max < itbounds.getMax() - rFreqVrMin){
                                addErrorMsg("Intermodulation rejection upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                            if (min > itbounds.getMin() - rFreqVrMax){
                                addErrorMsg("Intermodulation rejection lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMin()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
                            }
                        }

                        if ((rFreqVrMin - itbounds.getMin() >= 0)&& (rFreqVrMax - itbounds.getMax() <= 0)){ // Fv_min and Fv_max are between Fi_min and Fi_max
                            if (max < itbounds.getMax() - rFreqVrMin){
                                addErrorMsg("Intermodulation rejection upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                            if (min > itbounds.getMin() - rFreqVrMax){
                                addErrorMsg("Intermodulation rejection lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMin()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
                            }
                        }

                        if ((rFreqVrMin - itbounds.getMin() > 0) &&(rFreqVrMin - itbounds.getMax() <= 0)&& (rFreqVrMax - itbounds.getMax() > 0)){ // Fv_min is between Fi_min and Fi_max and Fv_max is above Fi_max
                            if (max < itbounds.getMax() - rFreqVrMin){
                                addErrorMsg("Intermodulation rejection upper limit (" + fAndU(max)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMax()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
                            }
                            if (min > itbounds.getMin()- rFreqVrMax ){
                                addErrorMsg("Intermodulation rejection lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMin()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
                            }
                        }

                        if ((rFreqVrMin - itbounds.getMax() > 0)&& (rFreqVrMax - itbounds.getMax() > 0)){ // Fv_min and Fv_max are above Fi_max
                            if (min > itbounds.getMin()- rFreqVrMax){
                                addErrorMsg("Intermodulation rejection lower limit (" + fAndU(min)
                                        + ") does not match the interfering transmitter frequency ("
                                        + fAndU(itbounds.getMin()) + ")<br> of Interfering Link ["
                                        + link.getInterferingSystem().getName()+ "]"
                                        + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
                            }
                        }
                    }
                }
            }
        }
    }

}
