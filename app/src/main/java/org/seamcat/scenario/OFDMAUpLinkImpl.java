package org.seamcat.scenario;

import org.seamcat.model.cellular.ofdma.OFDMAUpLink;

public class OFDMAUpLinkImpl implements OFDMAUpLink  {

    private double maximumAllowedTransmitPowerOfMS;
    private double minimumTransmitPowerOfMS;
    private double powerScalingThreshold;
    private double balancingFactor;

    public OFDMAUpLinkImpl() {
        maximumAllowedTransmitPowerOfMS = 24;
        powerScalingThreshold = 0.9;
        balancingFactor = 1d;
        minimumTransmitPowerOfMS = -30;
    }

    @Override
    public double getMaximumAllowedTransmitPowerOfMS() {
        return maximumAllowedTransmitPowerOfMS;
    }

    public void setMaximumAllowedTransmitPowerOfMS( double maximumAllowedTransmitPowerOfMS ) {
        this.maximumAllowedTransmitPowerOfMS = maximumAllowedTransmitPowerOfMS;
    }

    @Override
    public double getMinimumTransmitPowerOfMS() {
        return minimumTransmitPowerOfMS;
    }

    public void setMinimumTransmitPowerOfMS( double minimumTransmitPowerOfMS ) {
        this.minimumTransmitPowerOfMS = minimumTransmitPowerOfMS;
    }

    @Override
    public double getPowerScalingThreshold() {
        return powerScalingThreshold;
    }

    public void setPowerScalingThreshold( double powerScalingThreshold ) {
        this.powerScalingThreshold = powerScalingThreshold;
    }

    @Override
    public double getBalancingFactor() {
        return balancingFactor;
    }

    public void setBalancingFactor( double balancingFactor ) {
        this.balancingFactor = balancingFactor;
    }
}
