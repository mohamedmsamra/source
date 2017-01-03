package org.seamcat.model.cellular.cdma;

public interface CDMAUpLink {

    double getTargetNetworkNoiseRise();

    boolean isCellNoiseRise();

    double getTargetCellNoiseRise();

    double getMSMaximumTransmitPower();

    double getMSPowerControlRange();

    /**
     * Power control loop convergence threshold, in dB
     */
    double getMSConvergencePrecision();

}
