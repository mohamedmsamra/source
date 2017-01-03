package org.seamcat.model.types;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.EmissionMask;

import java.util.List;

public interface Transmitter {

    EmissionMask getEmissionsMask();

    boolean useEmissionMaskAsBEM();

    EmissionMask getEmissionsFloor();

    boolean isUsingEmissionsFloor();

    double getBandwidth();

    Bounds getBandwidthBounds();

    List<LocalEnvironment> getLocalEnvironments();

    AntennaGain getAntennaGain();

    Distribution getHeight();

}
