package org.seamcat.model.generic;

import org.seamcat.model.distributions.Distribution;

public interface AntennaPointing {

    Distribution getAzimuth();

    Distribution getElevation();

    boolean getAntennaPointingAzimuth();

    boolean getAntennaPointingElevation();
}
