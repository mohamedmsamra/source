package org.seamcat.model.scenariocheck;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.Workspace;
import org.seamcat.model.cellular.CellularReceiver;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.core.InterferenceLink;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.MaskFunction;
import org.seamcat.model.generic.GenericReceiver;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.types.Receiver;
import org.seamcat.model.types.SensingLink;
import org.seamcat.model.types.Transmitter;

import static java.lang.Math.abs;
import static java.lang.String.format;

public class InterferingLinkCheck extends AbstractCheck {

	private static final String LINK = "Interfering Link";

	public InterferingLinkCheck(String prefix) {
		result.setCheckName(prefix+LINK);
	}

	public static String fAndU(double freq) {
		if ((freq > 1.0) || (freq < -1.0)) {
			return freq + " MHz";
		} else {
			return (1000.0 * freq) + " kHz";
		}
	}

	public ScenarioCheckResult check(Workspace workspace) {
        SystemSimulationModel vsl = workspace.getVictimSystemLink();
		Bounds vslBounds = workspace.getVictimFrequency().getBounds();
		boolean bFreqVrBounded = vslBounds.isBounded();
		double rFreqVrMin = vslBounds.getMin();
		double rFreqVrMax = vslBounds.getMax();
		double victimFreqRange = rFreqVrMax - rFreqVrMin;
		Receiver vlr = vsl.getSystem().getReceiver();
        boolean isUsingOverloading = false;
        double overloadingMaskMin = 0, overloadingMaskMax = 0, receiverFilterMin = 0, receiverFilterMax = 0;
        boolean bIntermodBounded = false;

		Function overloadingMask = null;
		Function receiverFilter = null;
		
		if (vlr instanceof GenericReceiver) {
            GenericReceiver gvlr = (GenericReceiver) vlr;
            isUsingOverloading = gvlr.isUsingOverloading();

            if ( isUsingOverloading ) {
                overloadingMask = gvlr.getOverloadingMask();
                overloadingMaskMin = overloadingMask.getBounds().getMin();
                overloadingMaskMax = overloadingMask.getBounds().getMax();

                receiverFilter = gvlr.getReceiverFilter();
                receiverFilterMin = receiverFilter.getBounds().getMin();
                receiverFilterMax = receiverFilter.getBounds().getMax();
            }

            if ( gvlr.isIntermodulationRejectionOption() ) {
                bIntermodBounded = gvlr.getIntermodulationRejection().getBounds().isBounded();
            }
		}

		for (InterferenceLink il : workspace.getInterferenceLinks()) {
            RadioSystem is = il.getInterferingSystem();
			//TODO action point from STG#43: add consistency check protection distance
            if (!(is instanceof CellularSystem)) {
                InterferingLinkRelativePosition.CorrelationMode mode = il.getInterferingLinkRelativePosition().getCorrelationMode();
                String msg = "";
                switch (mode) {
                    case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR:
                        msg = doProtectionDistanceCheck(il, false);
                        break;
                    case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT:
                        msg = doProtectionDistanceCheck(il, false);
                        break;
                    case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR:
                        msg = doProtectionDistanceCheck(il, true);
                        break;
                    case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT:
                        msg = doProtectionDistanceCheck(il, true);
                        break;
                    case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR:
                        msg = doProtectionDistanceCheck(il, true);
                        break;
                    case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT:
                        msg = doProtectionDistanceCheck(il, true);
                        break;
                }
                if (!msg.isEmpty()) addErrorMsg(msg);
            }

            boolean deviceIsCR = false;
            Transmitter it = is.getTransmitter();
            if ( it instanceof GenericTransmitter ) {
                GenericTransmitter git = (GenericTransmitter) it;

                checkDistribution(git.getPower(), is.getName(), "Interfering Transmitter: Power supplied");
                checkDistribution(git.getHeight(), is.getName(), "Interfering Transmitter: Antenna Height");

                deviceIsCR = git.isInterfererCognitiveRadio();
            }

            checkDistribution(is.getFrequency(), is.getName(), "Interfering Transmitter: Frequency");
            checkDistribution(il.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor(), is.getName(), "It->Vr: Path Distance");
            checkDistribution(il.getInterferingLinkRelativePosition().getRelativeLocation().getPathAzimuth(), is.getName(), "It->Vr: Path Azimuth");


			// Check 3 - Check consistency of frequency distributions of
			// victim receiver and current interfering transmitter masks
			double rFreqItMin, rFreqItMax, rUnwantedMin, rUnwantedMax, rUnwantedFloorMin = 0, rUnwantedFloorMax = 0;
			boolean bFreqItBounded = false, bUnwantedBounded = false, bUnwantedFloorBounded = false;

			{
				Bounds bounds = is.getFrequency().getBounds();
				rFreqItMin = bounds.getMin();
				rFreqItMax = bounds.getMax();
				bFreqItBounded = bounds.isBounded();
			}

			{
				Bounds bounds = it.getEmissionsMask().getBounds();
				rUnwantedMin = bounds.getMin();
				rUnwantedMax = bounds.getMax();
				bUnwantedBounded = bounds.isBounded();
			}

			// UPGRADE : tester si unwanted floor active
			if (it.isUsingEmissionsFloor()) {
				Bounds bounds = it.getEmissionsFloor().getBounds();

				rUnwantedFloorMin = bounds.getMin();
				rUnwantedFloorMax = bounds.getMax();
				bUnwantedFloorBounded = bounds.isBounded();
			}

			// Test model application (if not CR)
            if (!deviceIsCR && !bFreqItBounded) {
                // Warns if frequency distribution is not bounded
                addErrorMsg("Unbounded frequency distribution in " + is.getName());
            }

            double rBlockingMin = 0, rBlockingMax = 0;
			boolean bBlockingBounded = false;

			double rIntermodMin = 0;
			double rIntermodMax = 0;

            Receiver ilr = is.getReceiver();
            checkDistribution(ilr.getHeight(), is.getName(), "Interfering Transmitter: Antenna Height");

			double rBVr = vlr.getBandwidth();
			if (vlr instanceof CellularReceiver) {
				if (workspace.getVictimSystemLink().isOFDMASystem()){
					rBVr /= 1000.; // TODO added by KK to convert kHz [RB * bwRB] to MHz
				}
			}

			if (bFreqItBounded && bFreqVrBounded && !deviceIsCR) {
				if (bBlockingBounded) {
					if (rFreqItMax - rFreqVrMin > rBlockingMax || rFreqItMin - rFreqVrMax < rBlockingMin) {
						addErrorMsg("Blocking response range (" + fAndU(rBlockingMin) + ", " + fAndU(rBlockingMax)
						      + ")does not match interfering transmitter frequency range (" + fAndU(rFreqItMin) + ", "
						      + fAndU(rFreqItMax) + " )<br>and victim receiver frequency range (" + fAndU(rFreqVrMin) + " , "
						      + fAndU(rFreqVrMax) + " ) in interferer: " + is.getName());
					}
				}
				if (bIntermodBounded) {
					if (rFreqItMax - rFreqVrMin > rIntermodMax || rFreqItMin - rFreqVrMax < rIntermodMin) {
						addErrorMsg("Intermodulation rejection range (" + fAndU(rIntermodMin) + ", " + fAndU(rIntermodMax)
						      + ") does not match interfering transmitter frequency range (" + fAndU(rFreqItMin) + " , "
						      + fAndU(rFreqItMax) + " ) <br>and victim receiver frequency range (" + fAndU(rFreqVrMin)
						      + " , " + fAndU(rFreqVrMax) + " ) in interferer: " + is.getName());
					}
				}
				if (bUnwantedBounded) {
					if (rUnwantedMax < rUnwantedMin) {
						addErrorMsg("Unwanted emissions range is not valid: [" + rUnwantedMin + " to " + rUnwantedMax + "]");
					} else if (rFreqVrMax - rFreqItMin + rBVr / 2 > rUnwantedMax
					      || rFreqVrMin - rFreqItMax - rBVr / 2 < rUnwantedMin) {
						addErrorMsg("Unwanted emissions range (" + fAndU(rUnwantedMin) + ", " + fAndU(rUnwantedMax)
						      + ") does not match interfering transmitter frequency range (" + fAndU(rFreqItMin) + " , "
						      + fAndU(rFreqItMax) + " )<br>and victim receiver frequency range (" + fAndU(rFreqVrMin) + " , "
						      + fAndU(rFreqVrMax) + " ) +/- receiver bandwidth (" + fAndU(rBVr) + ") in interferer: " + is.getName());
					}
				}
				if (it.isUsingEmissionsFloor() && bUnwantedFloorBounded) {
					if (rFreqVrMax - rFreqItMin + rBVr / 1000 / 2 > rUnwantedFloorMax
					      || rFreqVrMin - rFreqItMax - rBVr / 1000 / 2 < rUnwantedFloorMin) {
						addErrorMsg("Unwanted emissions floor range (" + rUnwantedMin + ", " + rUnwantedMax
						      + ") does not match interfering transmitter frequency range (" + fAndU(rFreqItMin) + " , "
						      + fAndU(rFreqItMax) + " )<br>and victim receiver frequency range (" + fAndU(rFreqVrMin) + " , "
						      + fAndU(rFreqVrMax) + " ) +/- receiver bandwidth (" + fAndU(rBVr / 1000) + ") in interferer: " + is.getName());
					}
				}
			}

			if (isUsingOverloading && !deviceIsCR) {
				if (!overloadingMask.isConstant()) {
					// Constant case:
					if (rFreqVrMin == rFreqVrMax
					      && rFreqItMax == rFreqItMin) {
						if (rFreqVrMax - rFreqItMax == 0){ // Fi and Fv are the same
							if (overloadingMaskMax < rFreqItMax - rFreqVrMax){
								addErrorMsg("Overloading Mask upper limit (" + fAndU(overloadingMaskMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
							if (overloadingMaskMin > rFreqVrMax - rFreqItMin){
								addErrorMsg("Overloading Mask lower limit (" + fAndU(overloadingMaskMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
						}
						
						if (rFreqVrMax - rFreqItMax < 0){ // Fi is higher than Fv (upper bound)
							if (overloadingMaskMax < rFreqItMax - rFreqVrMax){
								addErrorMsg("Overloading Mask upper limit (" + fAndU(overloadingMaskMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
						}
						if (rFreqVrMax - rFreqItMax > 0){ // Fi is smaller than Fv (lower bound)
							if (overloadingMaskMin > rFreqItMin - rFreqVrMax){
								addErrorMsg("Overloading Mask lower limit (" + fAndU(overloadingMaskMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
						}
					}
					// Non constant case
					else {
					
						if ((rFreqVrMin - rFreqItMin < 0)&& (rFreqVrMax - rFreqItMin < 0)){ // Fv_min and Fv_max are below (or equal) Fi_min and Fi_max
							if (overloadingMaskMax < rFreqItMax - rFreqVrMin){
								addErrorMsg("Overloading Mask upper limit (" + fAndU(overloadingMaskMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
						}
						
						if ((rFreqVrMin - rFreqItMin < 0)&& (rFreqVrMax - rFreqItMin >= 0)&& (rFreqVrMax - rFreqItMax < 0)){ // Fv_min and Fv_max are on both side of Fi_min but below Fi_max
							if (overloadingMaskMax < rFreqItMax - rFreqVrMin){
								addErrorMsg("Overloading Mask upper limit (" + fAndU(overloadingMaskMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
							if (overloadingMaskMin > rFreqItMin - rFreqVrMax){
								addErrorMsg("Overloading Mask lower limit (" + fAndU(overloadingMaskMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMin) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
							}
						}
						
						if ((rFreqVrMin - rFreqItMin >= 0)&& (rFreqVrMax - rFreqItMax <= 0)){ // Fv_min and Fv_max are between Fi_min and Fi_max
							if (overloadingMaskMax < rFreqItMax - rFreqVrMin){
								addErrorMsg("Overloading Mask upper limit (" + fAndU(overloadingMaskMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
							if (overloadingMaskMin > rFreqItMin - rFreqVrMax){
								addErrorMsg("Overloading Mask lower limit (" + fAndU(overloadingMaskMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMin) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
							}
						}
						
						if ((rFreqVrMin - rFreqItMin > 0) &&(rFreqVrMin - rFreqItMax <= 0)&& (rFreqVrMax - rFreqItMax > 0)){ // Fv_min is between Fi_min and Fi_max and Fv_max is above Fi_max
							if (overloadingMaskMax < rFreqItMax - rFreqVrMin){
								addErrorMsg("Overloading Mask upper limit (" + fAndU(overloadingMaskMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
							if (overloadingMaskMin > rFreqItMin- rFreqVrMax ){
								addErrorMsg("Overloading Mask lower limit (" + fAndU(overloadingMaskMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMin) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
							}
						}
						
						if ((rFreqVrMin - rFreqItMax > 0)&& (rFreqVrMax - rFreqItMax > 0)){ // Fv_min and Fv_max are above Fi_max
							if (overloadingMaskMin > rFreqItMin- rFreqVrMax){
								addErrorMsg("Overloading Mask lower limit (" + fAndU(overloadingMaskMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMin) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
							}
						}
					}
				}

				if (!receiverFilter.isConstant()) {
					// Constant case:
					if (rFreqVrMin == rFreqVrMax
					      && rFreqItMax == rFreqItMin) {
						if (rFreqVrMax - rFreqItMax == 0){ // Fi and Fv are the same
							if (receiverFilterMax < rFreqItMax - rFreqVrMax){
								addErrorMsg("Overloading receiver filter mask upper limit (" + fAndU(receiverFilterMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
							if (receiverFilterMin > rFreqVrMax - rFreqItMin){
								addErrorMsg("Overloading receiver filter mask lower limit (" + fAndU(receiverFilterMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
						}
						
						if (rFreqVrMax - rFreqItMax < 0){ // Fi is higher than Fv (upper bound)
							if (receiverFilterMax < rFreqItMax - rFreqVrMax){
								addErrorMsg("Overloading receiver filter mask upper limit (" + fAndU(receiverFilterMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
						}
						if (rFreqVrMax - rFreqItMax > 0){ // Fi is smaller than Fv (lower bound)
							if (receiverFilterMin > rFreqItMin - rFreqVrMax){
								addErrorMsg("Overloading receiver filter mask lower limit (" + fAndU(receiverFilterMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
						}
					}
					// Non constant case
					else {
					
						if ((rFreqVrMin - rFreqItMin < 0)&& (rFreqVrMax - rFreqItMin < 0)){ // Fv_min and Fv_max are below (or equal) Fi_min and Fi_max
							if (receiverFilterMax < rFreqItMax - rFreqVrMin){
								addErrorMsg("Overloading receiver filter mask upper limit (" + fAndU(receiverFilterMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
						}
						
						if ((rFreqVrMin - rFreqItMin < 0)&& (rFreqVrMax - rFreqItMin >= 0)&& (rFreqVrMax - rFreqItMax < 0)){ // Fv_min and Fv_max are on both side of Fi_min but below Fi_max
							if (receiverFilterMax < rFreqItMax - rFreqVrMin){
								addErrorMsg("Overloading receiver filter mask upper limit (" + fAndU(receiverFilterMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
							if (receiverFilterMin > rFreqItMin - rFreqVrMax){
								addErrorMsg("Overloading receiver filter mask lower limit (" + fAndU(receiverFilterMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMin) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
							}
						}
						
						if ((rFreqVrMin - rFreqItMin >= 0)&& (rFreqVrMax - rFreqItMax <= 0)){ // Fv_min and Fv_max are between Fi_min and Fi_max
							if (receiverFilterMax < rFreqItMax - rFreqVrMin){
								addErrorMsg("Overloading receiver filter mask upper limit (" + fAndU(receiverFilterMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
							if (receiverFilterMin > rFreqItMin - rFreqVrMax){
								addErrorMsg("Overloading receiver filter mask lower limit (" + fAndU(receiverFilterMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMin) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
							}
						}
						
						if ((rFreqVrMin - rFreqItMin > 0) &&(rFreqVrMin - rFreqItMax <= 0)&& (rFreqVrMax - rFreqItMax > 0)){ // Fv_min is between Fi_min and Fi_max and Fv_max is above Fi_max
							if (receiverFilterMax < rFreqItMax - rFreqVrMin){
								addErrorMsg("Overloading receiver filter mask upper limit (" + fAndU(receiverFilterMax)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMax) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMin) + ")");
							}
							if (receiverFilterMin > rFreqItMin- rFreqVrMax ){
								addErrorMsg("Overloading receiver filter mask lower limit (" + fAndU(receiverFilterMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMin) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
							}
						}
						
						if ((rFreqVrMin - rFreqItMax > 0)&& (rFreqVrMax - rFreqItMax > 0)){ // Fv_min and Fv_max are above Fi_max
							if (receiverFilterMin > rFreqItMin- rFreqVrMax){
								addErrorMsg("Overloading receiver filter mask lower limit (" + fAndU(receiverFilterMin)
								      + ") does not match the interfering transmitter frequency (" 
								      + fAndU(rFreqItMin) + ")<br> of Interfering Link ["
								      + is.getName() + "]"
								      + " and the victim receiver frequency (" + fAndU(rFreqVrMax) + ")");
							}
						}
					}
				}
			}

			if (deviceIsCR) {
				SensingLink sensingLink = ((GenericTransmitter)it).getSensingLink();

                MaskFunction eirp = sensingLink.getEIRPInBlockMask();
                Bounds eirpBounds = eirp.getBounds();

				if (!eirp.isConstant() && (eirpBounds.getMax() < victimFreqRange || abs(eirpBounds.getMin()) < victimFreqRange)) {
					addErrorMsg(format("Interferer <%s>: EIRP max inblock limit function does not cover the full frequency range of the victimlink: -%s to +%s", is.getName(), fAndU(victimFreqRange), fAndU(victimFreqRange)));
				}

				Function detectionThreshold = sensingLink.getDetectionThreshold();
				double detectionThresholdMin = detectionThreshold.getBounds().getMin();
				double detectionThresholdMax = detectionThreshold.getBounds().getMax();

				if (!detectionThreshold.isConstant() && (detectionThresholdMax < victimFreqRange || abs(detectionThresholdMin) < victimFreqRange)) {
					addErrorMsg(format("Interferer <%s>: CR detection threshold function does not cover the full frequency offset range: -%s to +%s", is.getName(), fAndU(victimFreqRange), fAndU(victimFreqRange)));
				}

				double bandwidthRange = victimFreqRange + (vlr.getBandwidth() / 2);
				EmissionMask emissionsMask = it.getEmissionsMask();
				Bounds emissionsMaskBounds = emissionsMask.getBounds();
				double emissionsMaskMin = emissionsMaskBounds.getMin();
				double emissionsMaskMax = emissionsMaskBounds.getMax();

				if (!emissionsMask.isConstant() && (emissionsMaskMax < bandwidthRange || abs(emissionsMaskMin) < bandwidthRange)) {
					addErrorMsg(format("Interferer <%s>: Unwanted emissions mask function does not cover the full frequency offset range: -%s to +%s", is.getName(), fAndU(bandwidthRange), fAndU(bandwidthRange)));
				}

				if (it.isUsingEmissionsFloor()) {
					EmissionMask emissionsFloor = it.getEmissionsFloor();
					Bounds emissionsFloorBounds = emissionsFloor.getBounds();
					double emissionsFloorMin = emissionsFloorBounds.getMin();
					double emissionsFloorMax = emissionsFloorBounds.getMax();

					if (!emissionsFloor.isConstant() && (emissionsFloorMax < bandwidthRange || abs(emissionsFloorMin) < bandwidthRange)) {
						addErrorMsg(format("Interferer <%s>: Unwanted emissions floor function does not cover the full frequency offset range: -%s to +%s", is.getName(), fAndU(bandwidthRange), fAndU(bandwidthRange)));
					}
				}
			}
		}
		
		return result;
	}

	//TODO added by KK: action point from STG#43
	private String doProtectionDistanceCheck(InterferenceLink il, boolean usePathFactor) {
		double simulationRadius = il.getInterferingLinkRelativePosition().getSimulationRadius();
		double pathDistanceFactor = 1.0, protectionDistance = 0;
		String msg = "";
		Distribution pd = il.getInterferingLinkRelativePosition().getProtectionDistance();
		Distribution pf = il.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor();
		if (!pd.getBounds().isBounded()) {
			msg += "<br/><br/>It is recommended to <strong>not</strong> use Gaussian or Rayleigh distributions for the Protection distance<br/>" +
					"due to the theoretically unlimited range of the simulated protection distances might conflict with the simulation radius.";
		} else {
			protectionDistance = pd.getBounds().getMax();
		}
		if (usePathFactor) {
			if (!pf.getBounds().isBounded()) {
				msg += "<br/><br/>It is recommended to <strong>not</strong> use Gaussian or Rayleigh distributions for the Path distance factor<br/>" +
						"due to the theoretically unlimited range of the simulated distances might conflict with propagation models.";
			} else {
				pathDistanceFactor = pf.getBounds().getMax();
			}
		}
		if (pd.getBounds().isBounded() && pf.getBounds().isBounded()) {// checks only 'valid' distributions
			if (pathDistanceFactor * simulationRadius < protectionDistance) {
				msg += "SEAMCAT has detected that you have set your Protection distance (" + protectionDistance + " km) " +
						"larger than the Simulation radius (" + (simulationRadius * pathDistanceFactor) + " km). " +
						"<br/><strong>NOTE: This check takes account of the product of the 'Simulation radius' and the 'Path distance factor'</strong>. ";
			}
		}
		return msg;
	}
}