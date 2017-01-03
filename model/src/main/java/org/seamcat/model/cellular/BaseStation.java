package org.seamcat.model.cellular;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.types.AntennaGain;

public interface BaseStation {

    Distribution getHeight();

    Distribution getTilt();

    AntennaGain getAntennaGain();
}
