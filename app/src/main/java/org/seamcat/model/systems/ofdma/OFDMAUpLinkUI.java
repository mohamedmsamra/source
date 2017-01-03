package org.seamcat.model.systems.ofdma;

import org.seamcat.model.plugin.Config;
import org.seamcat.model.systems.CalculatedValue;

public interface OFDMAUpLinkUI {

    @Config(order = 1, name = "Max. allowed transmit power of MS", unit = "dBm", toolTip = "Maximum allowed transmit power of MS")
    double getMaximumAllowedTransmitPowerOfMS();
    double getMaximumAllowedTransmitPowerOfMS = 24.0;

    @Config(order = 2, name = "Min. transmit power of MS", unit = "dBm", toolTip = "Minimum transmit power of MS")
    double getMinimumTransmitPowerOfMS();
    double getMinimumTransmitPowerOfMS = -30.0;

    @Config(order = 3, name = "Power scaling threshold")
    double getPowerScalingThreshold();
    double getPowerScalingThreshold = 0.9;

    @Config(order = 4, name = "Balancing factor (0<y<1)")
    double getBalancingFactor();
    double getBalancingFactor = 1.0;

    @Config(order = 5, name = "Estimate coupling loss percentile", unit = "dB")
    CalculatedValue percentile();

}
