package org.seamcat.model.scenariocheck;

import org.seamcat.cdma.CDMALinkLevelData.LinkType;
import org.seamcat.cdma.CDMASystem;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.Workspace;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.core.InterferenceLink;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.Function;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.ofdma.OfdmaSystem;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.scenario.CDMASettingsImpl;

import static org.seamcat.model.scenariocheck.InterferingLinkCheck.fAndU;

public class CDMACheck extends AbstractCheck {

	private static final String LINK = "dma system";

	public CDMACheck(String prefix) {
		result.setCheckName(prefix+LINK + " check");
	}

	public ScenarioCheckResult check(Workspace workspace) {
		checkVictimCDMA(workspace);
		checkInterferingCDMA(workspace);
		return result;
	}

	private void checkInterferingCDMA(Workspace workspace) {
		for (InterferenceLink il : workspace.getInterferenceLinks()) {

            RadioSystem is = il.getInterferingSystem();
            if ( is instanceof CellularSystem ) {
                CellularSystem cs = (CellularSystem) is;

                if (cs.getOFDMASettings() != null ) {
                    if (cs.getOFDMASettings().getPathLossCorrelation().isUsingPathLossCorrelation()) {
                        PropagationModel pm = cs.getLink().getPropagationModel();
                        if ( pm instanceof PropagationModelConfiguration) {
                            boolean variationSelected = ((PropagationModelConfiguration) pm).isVariationSelected();
                            if ( variationSelected ) {
                                result.setCheckName("Ofdma System Check");
                                addErrorMsg("Interfering System has both pathloss correlation and internal variation selected. This is not supported.");
                            }
                        }
                    }
                    if (cs.getOFDMASettings().getUpLinkSettings() != null ) {
                        int usRB = cs.getOFDMASettings().getNumberOfSubCarriersPerMobileStation();
                        int usersNeeded = cs.getOFDMASettings().getMaxSubCarriersPerBaseStation() / usRB;

                        if (usersNeeded > cs.getUsersPerCell()) {
                            addErrorMsg("You have selected " + usRB + " subcarriers per user and maximum " + cs.getOFDMASettings().getMaxSubCarriersPerBaseStation()
                                    + " subcarriers per basestation - but only " + cs.getUsersPerCell() + " users per cell. This may lead to a not fully loaded system.");
                        }

                        int bsRB = cs.getOFDMASettings().getMaxSubCarriersPerBaseStation();

                        if (bsRB % usRB != 0 ) {
                            addErrorMsg("You have selected a maximum of " + bsRB + " resourceblocks per basestation and " + usRB + " resourceblocks per user. This will result in a not fully loaded system.<br />" +
                                    "With your current settings the system can not be loaded more than " + Mathematics.round(100.0-(((bsRB/usRB) / (bsRB*1.0))*100)) + "%");
                        }
                    }



                }

                if (cs.getCDMASettings() != null ) {
                    CDMASettingsImpl cdma = (CDMASettingsImpl) cs.getCDMASettings();
                    Distribution freq = il.getInterferingSystem().getFrequency();

                    if (!freq.getBounds().isBounded()) {
                        addErrorMsg("Frequency of CDMA System is unbounded for: "
                                + il.getInterferingSystem().getName());
                    }
                    double deltaFreq = Math.abs(freq.trial() - cdma.getLld().getFrequency());
                    if (deltaFreq > 300) {
                        addErrorMsg("Frequency difference between system and link level data for CDMA System:"
                                + il.getInterferingSystem().getName()
                                + " is "
                                + deltaFreq
                                + " Mhz. Link level data may not be applicable at this frequency.");
                    }
                    if (cdma.getLld().getLinkType() == LinkType.UPLINK != (cs.getCDMASettings().getUpLinkSettings()!=null)) {
                        addErrorMsg("Link direction mismatch between link level data and CDMA System: "
                                + il.getInterferingSystem().getName());
                    }
                }
            }
		}
	}

	private void checkVictimCDMA(Workspace workspace) {
		SystemSimulationModel vsl = workspace.getVictimSystemLink();

		Bounds bounds = workspace.getInterferenceLinks().get(0).getVictimSystem().getFrequency().getBounds();
		double rFreqVrMin = bounds.getMin();
		double rFreqVrMax = bounds.getMax();

		if (vsl.isCDMASystem() || vsl.isOFDMASystem()) {

			// start of the blocking mask check for CDMA and OFDMA
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

		if (vsl.isOFDMASystem()) {
			OfdmaSystem<?> ofdma = (OfdmaSystem<?>) vsl.getDMASystem();

			if (ofdma.getSystemSettings().getOFDMASettings().getPathLossCorrelation().isUsingPathLossCorrelation()) {
                PropagationModel pm = ofdma.getSystemSettings().getLink().getPropagationModel();
                if ( pm instanceof PropagationModelConfiguration ) {
                    boolean variationSelected = ((PropagationModelConfiguration) pm).isVariationSelected();
                    if ( variationSelected) {
                        result.setCheckName("Ofdma System Check");
                        addErrorMsg("Victim System has both pathloss correlation and internal variation selected. This is not supported.");
                    }
                }
			}
            int bsRB = ofdma.getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation();
            int usRB = ofdma.getSystemSettings().getOFDMASettings().getNumberOfSubCarriersPerMobileStation();
            int usersNeeded = bsRB / usRB;

            if (usersNeeded > ofdma.getSystemSettings().getUsersPerCell()) {
                addErrorMsg("You have selected " + usRB + " subcarriers per user and maximum " + bsRB
						+ " subcarriers per basestation - but only " + ofdma.getSystemSettings().getUsersPerCell() + " users per cell. This may lead to a not fully loaded system.");
            }



			if (bsRB % usRB != 0 ) {
				addErrorMsg("You have selected a maximum of " + bsRB + " resourceblocks per basestation and " + usRB + " resourceblocks per user. This will result in a not fully loaded system.<br />" +
						"With your current settings the system can not be loaded more than " + 
						Mathematics.round(100.0-(((bsRB/usRB) / (bsRB*1.0))*100)) + "%");
			}
		}

		if (vsl.isCDMASystem() && !vsl.isOFDMASystem()) {
			CDMASystem cdma = (CDMASystem) vsl.getDMASystem();
			// double rBVr = cdma.getSystemBandwidth() * 1000;
			Distribution freq = workspace.getInterferenceLinks().get(0).getVictimSystem().getFrequency();

			if (!freq.getBounds().isBounded()) {
				addErrorMsg("Frequency of CDMA System is unbounded for: " + LINK);
			}
			if (cdma.getLinkLevelData() == null) {
				addErrorMsg("No Link Level Data selected for CDMA System: " + LINK);
			} else {
				double deltaFreq = Math.abs(freq.trial()
						- cdma.getLinkLevelData().getFrequency());
				if (deltaFreq > 300) {
					addErrorMsg("Frequency difference between system and link level data for CDMA System:"
							+ LINK
							+ " is "
							+ deltaFreq
							+ " Mhz. Link level data may not be applicable at this frequency.");
				}
				if (cdma.getLinkLevelData().getLinkType() == LinkType.UPLINK != cdma
						.isUplink()) {
					addErrorMsg("Link direction mismatch between link level data and Victim CDMA System");
				}
			}

			// Check consistency of capacity tolerance settings:
			if (cdma.getSystemSettings().getCDMASettings().isSimulateNonInterferedCapacity()) {
				if (cdma.isUplink() && cdma.getSystemSettings().getCDMASettings().getTargetNoiseRisePrecision() < 0) {
					addErrorMsg("Target Noise Rise precision is negative!");
				} else if (cdma.isDownlink() && cdma.getSystemSettings().getCDMASettings().getToleranceOfInitialOutage() < 0) {
					addErrorMsg("Initial allowable outage percentage is negative!");
				}
				if (!cdma.isDownlink()) {
					if (cdma.getSystemSettings().getCDMASettings().getToleranceOfInitialOutage() >= 1) {
						addErrorMsg("Initial Allowed Outage is to high!");
					}
				}
			}
			// Check uplink power control convergence precision
			if (cdma.isUplink()) {
				if (cdma.getSystemSettings().getCDMASettings().getUpLinkSettings().getMSConvergencePrecision() < 0) {
					addErrorMsg("Power Control Convergence Precision is negative!");
				} else if (cdma.getSystemSettings().getCDMASettings().getUpLinkSettings().getMSConvergencePrecision() == 0) {
					addErrorMsg("Power Control Convergence Precision is 0 - this might cause EGE to enter never ending loop");
				} else if (cdma.getSystemSettings().getCDMASettings().getUpLinkSettings().getMSConvergencePrecision() > 0.1) {
					addErrorMsg("Power Control Convergence Precision is more than 0.1 - this is not recommended");
				}
			}
		} 
	}
}