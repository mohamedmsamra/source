package org.seamcat.model.generic;

import org.seamcat.model.plugin.Config;

public interface PathLossCorrelationUI {

    @Config(order = 1, name = "Use pathloss correlation", defineGroup = "pl")
    boolean usePathLossCorrelation();

    @Config(order = 2, name = "Variance", group = "pl", unit = "dB")
    double pathLossVariance();

    @Config(order = 3, name = "Correlation factor", group = "pl")
    double correlationFactor();

    double pathLossVariance = 10.0;
    double correlationFactor = 0.5;
}
