package org.seamcat.cdma;

import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.model.mathematics.Mathematics;

public class CDMADownlink extends AbstractDmaLink {

	protected CDMASystem cdmasystem;
	protected double receivedTrafficChannelPowerdBm;

    public CDMADownlink(CdmaBaseStation _cell, CdmaUserTerminal _user, CDMASystem _cdmasystem) {
		super(false, _cell, _user );
		cdmasystem = _cdmasystem;
        setFrequency(_cdmasystem.getFrequency());
	}

    /**
     * This method calculates the received power.
     * <p></p>
     * For downlink case the calculated power represent the amount of power received at mobile.
     * <p></p>
     * Downlink equation:
     * <p></p>
     * <code>Rx = Tx - EffectivePathloss</code>
     */
    @Override
	public double calculateCurrentReceivePower_dBm() {
        totalReceivedPower = getBaseStation().getCurrentTransmitPower_dBm() - getEffectivePathloss();
        return totalReceivedPower;
	}

    @Override
    public CdmaBaseStation getBaseStation() {
        return (CdmaBaseStation) super.getBaseStation();
    }

    /**
     * performs a sort of power control if the calculated transmitted traffic channel power is higher than the max <br>
     *     traffic channel power
     *
     */
	public double calculateTransmittedTrafficChannelPowerIndBm() {

		transmittedTrafficChannelPowerdBm = getEffectivePathloss() + receivedTrafficChannelPowerdBm;

		double pMax = ((CDMADownlinkSystem)cdmasystem).getMaxTrafficChannelPowerIndBm();
		if (transmittedTrafficChannelPowerdBm > pMax) {

			double difference = transmittedTrafficChannelPowerdBm - pMax;

			transmittedTrafficChannelPowerdBm = pMax;

			receivedTrafficChannelPowerdBm -= difference;

			powerScaledDownToMax = true;
		} else {
			powerScaledDownToMax = false;
		}

		return transmittedTrafficChannelPowerdBm;
	}

    /**
     * method that do strictly nothing, i.e. empty method.
     */
	public void activateLink() {
		//Do nothing
	}

	public double getReceivedTrafficChannelPowerdBm() {
		return receivedTrafficChannelPowerdBm;
	}

	/**
     * Downlink power balance scaling
     * <p></p>
     * it calculates:
     * <p></p>
     * <code>transmittedTrafficChannelPowerdB = transmittedTrafficChannelPowerdBm * scaleValue</code>
     * <p></p>
     * and update received power values to correspond to scaled transmit power
     * <p></p>
     * <code>receivedTrafficChannelPowerdBm = transmittedTrafficChannelPowerdBm - getEffectivePathloss</code>
     */
	public void scaleTransmitPower(double scaleValue) {
		transmittedTrafficChannelPowerdBm = Mathematics.fromWatt2dBm(Mathematics.fromdBm2Watt(transmittedTrafficChannelPowerdBm) * scaleValue);
		receivedTrafficChannelPowerdBm = transmittedTrafficChannelPowerdBm - getEffectivePathloss();
	}

	public void setReceivedTrafficChannelPowerdBm(
			double receivedTrafficChannelPowerdBm) {
		this.receivedTrafficChannelPowerdBm = receivedTrafficChannelPowerdBm;
	}
}