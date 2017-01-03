package org.seamcat.cdma;

import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.model.mathematics.Mathematics;

public class CDMAUplink extends AbstractDmaLink {

	protected CDMASystem cdmasystem;

    private CdmaUserTerminal ue;

	public CDMAUplink(CdmaBaseStation _cell, CdmaUserTerminal _user, CDMASystem _cdmasystem) {
		super(true, _cell, _user);
		cdmasystem = _cdmasystem;
        ue = _user;
        setFrequency( _cdmasystem.getFrequency() );
	}

    /**
     * calculate the achieved C/I
     *<p></p>
     * In the uplink, each mobile station perfectly achieves the target C/I, Eb/N0_target, during the power control loop<br>
     *     convergence, assuming that the maximum transmit (TX) power, max_MS_Tx_Pw, is not exceeded. Those mobile stations <br>
     *         not able to achieve Eb/N0_target after convergence of the power control loop are considered in outage.
     *<p></p>
     * The Local-mean Signal-to-interference power ratio in the uplink, (C/I)UL, is calculated by multiplying the <br>
     *     received signal power S (=receivedPower) by the processing gain G (processingGain), and dividing the result<br>
     *         by the total interference power (IntereferenceTotal)
     * <p></p>
     * <code>AchievedCI (dB)= processingGain (dB) + receivedPower (dB)- InterferenceTotal (dB)</code>
     *
     * @return achieved C/I in uplink
     */
	public double calculateAchivedCI() {
		double procGain = cdmasystem.getProcessingGain();
        double Itotal = getBaseStation().calculateTotalInterference_dBm(this);
        double recPower = getTotalReceivedPower();
        return procGain + recPower - Itotal;
	}

    @Override
    public CdmaUserTerminal getUserTerminal() {
        return ue;
    }

    @Override
	public double calculateCurrentReceivePower_dBm() {
        totalReceivedPower = ue.getCurrentTransmitPowerIndBm() - effectivePathLoss;
        return totalReceivedPower;
	}

	public double calculateInitialReceivePower() {
		double thermalNoise_dB = Mathematics.fromWatt2dBm( cdmasystem.getResults().getThermalNoise() );
		double noiseRise_target_dB = cdmasystem.getSystemSettings().getCDMASettings().getUpLinkSettings()
		      .getTargetNetworkNoiseRise();
		double sir_target_db = getUserTerminal().getLinkLevelData().getEbNo();
		double processing_gain_db = cdmasystem.getProcessingGain();

		totalReceivedPower = thermalNoise_dB + noiseRise_target_dB
		      + sir_target_db - processing_gain_db;

		return totalReceivedPower;
	}
	
	public void activateLink() {
		//Do nothing
	}

    /**
     * Based on a received power level and the calculated pathloss a transmit power level is calculated.
     *
     * @param receivePower
     * @return total transmit power in dBm
     */
	public double calculateTotalTransmitPower(double receivePower) {
		double eff = getEffectivePathloss();
		double totalTransmitPower = receivePower + eff;
		
		if (totalTransmitPower > getUserTerminal().getMaxTxPower()) {
			totalTransmitPower = getUserTerminal().getMaxTxPower();
		} else if (totalTransmitPower < getUserTerminal().getMinTxPower()) {
			totalTransmitPower = getUserTerminal().getMinTxPower();
		}
		getUserTerminal().setCurrentTransmitPower_dBm(totalTransmitPower);
		return totalTransmitPower;

	}

	/**
	 * Method that returns the achieved Ec/Ior ratio for the initial power levels
     *
     * Initializes an active uplink connection
     * <p></p>
	 * Calls calculateInitialReceivePower to set the initial received power level.
     * <p></p>
	 * Based on this initial received power level and the calculated pathloss an initial transmit power level is calculated.
	 * 
	 * @return achieved Ec/Ior ratio for the initial power levels
	 */
	public double initializePowerLevels() {
		calculateInitialReceivePower();
		calculateTotalTransmitPower(totalReceivedPower);

        return calculateAchivedCI();
	}
}