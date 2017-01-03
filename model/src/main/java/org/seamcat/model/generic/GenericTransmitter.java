package org.seamcat.model.generic;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.types.SensingLink;
import org.seamcat.model.types.Transmitter;

public interface GenericTransmitter extends Transmitter {

    AntennaPointing getAntennaPointing();

    Distribution getPower();

    boolean isUsingPowerControl();

    double getPowerControlStepSize();

    double getPowerControlMinThreshold();

    double getPowerControlDynamicRange();

    boolean isInterfererCognitiveRadio();

    SensingLink getSensingLink();
}
