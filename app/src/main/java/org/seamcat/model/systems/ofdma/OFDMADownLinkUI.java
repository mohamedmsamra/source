package org.seamcat.model.systems.ofdma;

import org.seamcat.model.plugin.Config;

public interface OFDMADownLinkUI {

    @Config(order = 1, name = "BS max. transmit power", unit = "dBm", toolTip = "BS maximum transmit power")
    double bsMaximumTransmitPower();
    double bsMaximumTransmitPower = 46.0;
}
