package org.seamcat.model.cellular.cdma;

public interface CDMADownLink {

    /**
     * Network call success rate, in dB
     */
    double getSuccessThreshold();

    double getPilotChannelFraction();

    double getOverheadChannelFraction();

    double getMaximumBroadcastChannel();

    double getMaximumTrafficChannelFraction();

}
