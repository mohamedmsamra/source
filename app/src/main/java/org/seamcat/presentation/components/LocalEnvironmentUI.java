package org.seamcat.presentation.components;

import org.seamcat.model.plugin.Config;

public interface LocalEnvironmentUI {

    @Config(order = 1, name = "Probability", unit = "%")
    double probability();

    @Config(order = 2, name = "Wall Loss", unit = "dB")
    double wallLoss();

    @Config(order = 3, name = "Std. Dev.", unit = "dB")
    double stdDev();
}
