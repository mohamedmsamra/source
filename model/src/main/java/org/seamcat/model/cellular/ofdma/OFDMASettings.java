package org.seamcat.model.cellular.ofdma;

import org.seamcat.model.functions.Function;
import org.seamcat.model.types.PathLossCorrelation;

public interface OFDMASettings {

    int getMaxSubCarriersPerBaseStation();

    int getNumberOfSubCarriersPerMobileStation();

    double getBandwidthOfResourceBlock();

    Function getBitrateMapping();

    // Capacity
    PathLossCorrelation getPathLossCorrelation();


    OFDMAUpLink getUpLinkSettings();

    OFDMADownLink getDownLinkSettings();

}
