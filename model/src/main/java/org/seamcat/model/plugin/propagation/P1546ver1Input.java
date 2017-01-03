package org.seamcat.model.plugin.propagation;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.OptionalDoubleValue;

public interface P1546ver1Input {
    @Config(order = 1, name = "General environment", values = "Urban,Suburban,Rural")
    String generalEnvironment();

    @Config(order = 2, name = "System", values = "Digital (Bw < 1MHz),Digital (Bw > 1MHz),Analogue")
    String system();

    @Config(order = 3, name = "Time percentage", unit = "%")
    Distribution timePercentage();

    @Config(order = 4, name = "User specified local clutter height", unit = "m")
    OptionalDoubleValue clutterHeight();

    String generalEnvironment = "Urban";
    Distribution timePercentage = Factory.distributionFactory().getUniformDistribution(50,50);
    OptionalDoubleValue clutterHeight = new OptionalDoubleValue(false, 0);
    boolean variations = false;
}
