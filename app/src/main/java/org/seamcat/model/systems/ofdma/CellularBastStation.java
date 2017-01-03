package org.seamcat.model.systems.ofdma;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.types.AntennaGain;

public interface CellularBastStation {

    @Config(order = 1, name = "Antenna height", unit = "m")
    Distribution antennaHeight();
    Distribution antennaHeight = Factory.distributionFactory().getConstantDistribution(30.0);

    @Config(order = 2, name = "Antenna tilt", unit = "deg")
    Distribution antennaTilt();

    @Config(order = 3, name = "antenna", embed = true)
    AntennaGain antennaGain();
}
