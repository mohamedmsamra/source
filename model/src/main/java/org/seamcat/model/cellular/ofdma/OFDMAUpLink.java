package org.seamcat.model.cellular.ofdma;

public interface OFDMAUpLink {

    double getMaximumAllowedTransmitPowerOfMS();

    double getMinimumTransmitPowerOfMS();

    double getPowerScalingThreshold();

    double getBalancingFactor();

}
