package org.seamcat.presentation.propagationtest;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.plugin.Config;

public interface CommonConfiguration {

    @Config(order = 1, name = "Number of samples", information = "SAMPLES_COUNT_INFORMATION")
    int samples();
    int samples = 1000;

    @Config(order = 2, name = "Plot parameter" )
    Common common();
    enum Common { Distance, Frequency, TX_Height, RX_Height }

    @Config(order = 3, name = "Common")
    Distribution distribution();
}
