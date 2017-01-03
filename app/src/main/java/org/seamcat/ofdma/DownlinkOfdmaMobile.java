package org.seamcat.ofdma;

import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import java.util.ArrayList;
import java.util.List;

public class DownlinkOfdmaMobile extends OfdmaMobile implements OfdmaVictim {

    protected DownlinkOfdmaSystem system;

    protected List<OfdmaExternalInterferer> externalInterferers;
    private double interferencePower;

    public DownlinkOfdmaMobile(Point2D point, DownlinkOfdmaSystem _system, int _userid, double antGain, double antHeight) {
        super(point, _system, _userid, antGain, antHeight);
        setUpLinkMode( false );
        system = _system;
        externalInterferers = new ArrayList<OfdmaExternalInterferer>();
    }

    @Override
    protected OfdmaDownlink[] generateLinksArray() {
        return new OfdmaDownlink[getSystem().getNumberOfBaseStations()];
    }

    @Override
    public void addExternalInterferer(OfdmaExternalInterferer ext) {
        externalInterferers.add(ext);
    }

    @Override
    public List<OfdmaExternalInterferer> getExternalInterferers() {
        return externalInterferers;
    }

    @Override
    public double getExternalBlocking_dBm() {
        return getExternalInterferenceBlocking();
    }

    @Override
    public double getExternalUnwanted_dBm() {
        return getExternalInterferenceUnwanted();
    }

    /**
     * calculate the SINR for downlink only (not used in UL)
     * @return SINR in dB
     */
    public double calculateSINR() {
        setReceivedPowerWatt(servingLink.calculateCurrentReceivePower_Watt());
        interferencePower = calculateTotalInterference_Watt();

        setSINRAchieved( Mathematics.linear2dB(getReceivedPowerWatt() / interferencePower) );
        return getSINRAchieved();
    }

    public double getInterferencePower() {
        return Mathematics.linear2dB(interferencePower);
    }


}
