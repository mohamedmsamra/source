package org.seamcat.model.generic;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;

public interface RXAntennaPointingUI {
    @Config(order = 1, name = "ANTENNA_POINTING_HEIGHT", unit = "m")
    Distribution antennaHeight();
   // double x= 1.5;
    //Distribution antennaHeight = Factory.distributionFactory().getConstantDistribution(x);

    @Config(order = 2, name = "ANTENNA_POINTING_AZIMUTH_DOMAIN_RX")
    boolean antennaPointingAzimuth();
    boolean antennaPointingAzimuth = true;

    @Config(order = 3, name = "ANTENNA_POINTING_AZIMUTH", unit = "deg")
    Distribution azimuth();

    @Config(order = 4, name = "ANTENNA_POINTING_ELEVATION_DOMAIN_RX")
    boolean antennaPointingElevation();

    @Config(order = 5, name = "ANTENNA_POINTING_ELEVATION", unit = "deg")
    Distribution elevation();
}