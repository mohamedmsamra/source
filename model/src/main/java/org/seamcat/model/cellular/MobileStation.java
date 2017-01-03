package org.seamcat.model.cellular;

import org.seamcat.model.distributions.Distribution;

public interface MobileStation {

    Distribution getAntennaHeight();

    Distribution getAntennaGain();

    Distribution getMobility();
}
