package org.seamcat.model.systems.cdma;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.generic.Defaults;
import org.seamcat.model.plugin.Config;

public interface CDMAMobile {

    @Config(order = 1, name = "Antenna height", unit = "m")
    Distribution antennaHeight();
    Distribution antennaHeight = Factory.distributionFactory().getConstantDistribution(1.5);

    @Config(order = 2, name = "Antenna gain", unit = "dB")
    Distribution antennaGain();

    @Config(order = 3, name = "Mobility", unit = "km/h")
    Distribution mobility();
    Distribution mobility = Defaults.defaultMobility();
}
