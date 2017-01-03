package org.seamcat.model.generic;

import org.seamcat.model.RadioSystem;

public interface GenericSystem extends RadioSystem {

    String TX_POWER_CONTROL_GAIN = "tx power control gain";
    String COVERAGE_RADIUS = "coverage radius";
    String PATH_AZIMUTH = "path azimuth";
    String PATH_DISTANCE_FACTOR = "path distance factor";

    GenericReceiver getReceiver();

    GenericTransmitter getTransmitter();

    GenericLink getLink();

    InterfererDensity getInterfererDensity();
}
