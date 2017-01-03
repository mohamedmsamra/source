package org.seamcat.model.plugin.propagation;

import org.seamcat.model.plugin.Config;

public interface FreespaceInput {
    @Config(order = 1, name = "Variations std. dev.", unit = "dB", toolTip = "Variations standard deviation")
    double stdDev();

    boolean variations = false;
}
