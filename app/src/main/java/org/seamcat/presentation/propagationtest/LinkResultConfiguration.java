package org.seamcat.presentation.propagationtest;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;

public interface LinkResultConfiguration {

    @Config(order = 1, name = "Distance", unit = "km")
    Distribution distance();
    Distribution distance = Factory.distributionFactory().getConstantDistribution(1);

    @Config(order = 2, name = "Frequency", unit = "MHz")
    Distribution frequency();
    Distribution frequency = Factory.distributionFactory().getConstantDistribution(900);

    @Config(order = 3, name = "TX Height", unit = "m")
    Distribution txHeight();
    Distribution txHeight = Factory.distributionFactory().getConstantDistribution(10);

    @Config(order = 4, name = "RX Height", unit = "m")
    Distribution rxHeight();
    Distribution rxHeight = Factory.distributionFactory().getConstantDistribution(10);
}
