package org.seamcat.model.cellular.cdma;

public interface CDMASettings {

    /**
     * Network call drop threshold, in dB
     */
    double getCallDropThreshold();

    /**
     * Network voice traffic bit rate, in kbps
     */
    double getVoiceBitRate();

    /**
     * Factor of voice active users in network.
     */
    double getVoiceActivityFactor();

    // Capacity
    boolean isSimulateNonInterferedCapacity();

    int getDeltaUsersPerCell();

    int getNumberOfTrials();

    double getToleranceOfInitialOutage();

    double getTargetNoiseRisePrecision();

    CDMAUpLink getUpLinkSettings();

    CDMADownLink getDownLinkSettings();

}
