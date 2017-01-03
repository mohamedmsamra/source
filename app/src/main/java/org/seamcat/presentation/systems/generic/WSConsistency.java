package org.seamcat.presentation.systems.generic;

import org.seamcat.model.plugin.Config;

public interface WSConsistency {

    @Config(order = 1, name = "Noise floor", unit = "dBm")
    double noiseFloor();

    @Config(order = 2, name = "Sensitivity", unit = "dBm")
    double sensitivity();

    @Config(order = 3, name = "Workspace consistency", information = "WORKSPACE_CONSISTENCY")
    boolean wsConsistency();
}
