package org.seamcat.ofdma;

import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.model.mathematics.Mathematics;

public class OfdmaDownlink extends AbstractDmaLink {

	protected AbstractDmaSystem system;

	public OfdmaDownlink(OfdmaBaseStation base, OfdmaMobile mobile, AbstractDmaSystem system) {
		super(false, base, mobile );
		this.system = system;
        setFrequency( system.getFrequency() );
	}

    @Override
    public OfdmaBaseStation getBaseStation() {
        return (OfdmaBaseStation) super.getBaseStation();
    }

    /**
     * compute the current receive power at the UE (since downlink)
     *<p></p>
     * <code>P_Rx = P_Tx - effective path loss</code>
     *<p></p>
     * where P_Tx is equal to <code>current_transmit_power x (userSubResourceBlocks / basestationSubResourceBlocks)</code>
     *
     */
    @Override
	public double calculateCurrentReceivePower_dBm() {
        double eff = getEffectivePathloss(); //MCL adjusted pathloss including Antenna Gains

		double userSubCarriers = getUserTerminal().getRequestedSubCarriers();
		double basestationSubCarriers = getBaseStation().getSubCarriersInUse();
		
		double fraction = (userSubCarriers / basestationSubCarriers); 
		double power_W  = getBaseStation().calculateCurrentTransmitPower_Watt();
		double adjustedPower_W = power_W * fraction;
		double chPower = Mathematics.fromWatt2dBm(adjustedPower_W) ;

        totalReceivedPower = chPower - eff;
		return totalReceivedPower;
	}

    /**
     * add active mobile station to the activeUsers list
     */
	public void activateLink() {
		getUserTerminal().setServingLink(this);
		getMobileStation().initializeInActiveConnections(this);
		system.getActiveUsers().add(getMobileStation());
	}

	@Override
	public String toString() {
		return "Ofdma Downlink between BS #" + getBaseStation().getCellid() + " and Mobile #" + getUserTerminal().getUserId() + " PL = " + getEffectivePathloss();
	}

	public DownlinkOfdmaMobile getMobileStation() {
		return (DownlinkOfdmaMobile) getUserTerminal();
	}

    @Override
    public OfdmaMobile getUserTerminal() {
        return (OfdmaMobile) super.getUserTerminal();
    }
}
