package org.seamcat.model.systems.ofdma;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;

public interface OFDMAMobile {

    @Config(order = 1, name = "Antenna height", unit = "m")
    Distribution antennaHeight();
    Distribution antennaHeight = Factory.distributionFactory().getConstantDistribution(1.5);

    @Config(order = 2, name = "Antenna gain", unit = "dB")
    Distribution antennaGain();
}
