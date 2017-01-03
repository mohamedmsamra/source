package org.seamcat.model.plugin.propagation;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.OptionalDoubleValue;

public interface P1546ver4Input {
    @Config(order = 1, name = "General environment", values = "Urban,Suburban,Rural,Dense Urban")
    String generalEnvironment();
    String generalEnvironment = "Urban";

    @Config(order = 2, name = "System", values = "Mobile,Broadcasting digital,Broadcasting analogue")
    String system();

    @Config(order = 3, name = "Time percentage", unit = "%")
    Distribution timePercentage();
    Distribution timePercentage = Factory.distributionFactory().getUniformDistribution(50,50);

    @Config(order = 4, name = "User specified local clutter height", unit = "m")
    OptionalDoubleValue localClutter();
    OptionalDoubleValue localClutter = new OptionalDoubleValue(false, 0);

    @Config(order = 5, name = "User specified std. dev.", unit = "dB", toolTip = "User specified standard deviation")
    OptionalDoubleValue stdDev();
    OptionalDoubleValue stdDev = new OptionalDoubleValue(false, 0);

    @Config(order = 6, name = "Area of location variability", values = "500 x 500 m,< 2 km radius,< 50 km radius")
    String area();

    @Config(order = 7, name = "Terminal designations (Annex 5.1,1): use option a), b) and c (recommended)")
    boolean terminalDesignations();
    boolean terminalDesignations = true;

    @Config(order = 8, name = "Buildings of uniform height are assumed (recommended)")
    boolean uniformBuildingHeight();
    boolean uniformBuildingHeight = true;

    boolean variations = false;
}
