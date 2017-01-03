package org.seamcat.scenario;

import org.seamcat.model.cellular.ofdma.OFDMADownLink;

public class OFDMADownLinkImpl implements OFDMADownLink {

    private double BSMaximumTransmitPower = 46;

    @Override
    public double getBSMaximumTransmitPower() {
        return BSMaximumTransmitPower;
    }

    public void setBSMaximumTransmitPower(double bsMaximumTransmitPower) {
        this.BSMaximumTransmitPower = bsMaximumTransmitPower;
    }
}
