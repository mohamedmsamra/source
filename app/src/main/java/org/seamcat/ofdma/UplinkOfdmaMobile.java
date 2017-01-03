package org.seamcat.ofdma;

import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

public class UplinkOfdmaMobile extends OfdmaMobile {

    protected UplinkOfdmaSystem system;
    private double pl;
    private double plilx;

    public UplinkOfdmaMobile(Point2D point,UplinkOfdmaSystem _system, int _userid, double antGain, double antHeight) {
        super(point, _system, _userid, antGain, antHeight);
        system = _system;
        setUpLinkMode(true);
    }

    /**
     * compute the SINR at the BS for the uplink direction
     *<p></p>
     * <code>SINRAchieved = ReceivedPowerWatt / (SubCarrierRatio * TotalInterference)</code>
     *<p></p>
     * where
     *<p></p>
     * <code>SubCarrierRatio = (SubCarriers_per_UE * ResourceBlockSizeInMHz) / System_Bandwidth</code>
     *
     */
    @Override
    public double calculateSINR()  {
        setReceivedPowerWatt(servingLink.calculateCurrentReceivePower_Watt());
        setTotalInterference(((OfdmaBaseStation) servingLink.getBaseStation()).calculateTotalInterference_Watt(servingLink));

        setSubCarrierRatio( (getRequestedSubCarriers() * system.getResourceBlockSizeInMHz()) / system.getSystemSettings().getBandwidth() );

        setSINRAchieved(Mathematics.linear2dB(getReceivedPowerWatt() / getTotalInterference()));
        return getSINRAchieved();
    }

    @Override
    protected OfdmaUplink[] generateLinksArray() {
        return new OfdmaUplink[getSystem().getNumberOfBaseStations()];
    }

    @Override
    public double getFrequency() {
        if ( servingLink == null ) {
            return getSystem().getFrequency();
        }
        return ((OfdmaUplink)servingLink).calculateFrequency();
    }

    public void setPL(double pl) {
        this.pl = pl;
    }

    public double getPl() {
        return pl;
    }

    public void setPlilx( double plilx ) {
        this.plilx = plilx;
    }

    public double getPlIlx() {
        return plilx;
    }
}
