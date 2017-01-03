package org.seamcat.model.plugin.propagation;

import org.seamcat.model.plugin.Config;

public interface Model_C_IEEE_802_11_rev3_Input {

    @Config(order = 1, name = "Distance to BP", unit = "m", toolTip = "Distance to break point")
    double distanceToBP();

    @Config(order = 2, name = "Log-normal distribution before BP", unit = "dB")
    double logNormBefore();

    @Config(order = 3, name = "Log-normal distribution after BP", unit = "dB")
    double logNormAfter();
}
