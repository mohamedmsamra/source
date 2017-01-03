package org.seamcat.scenario;

import org.seamcat.model.cellular.cdma.CDMADownLink;

public class CDMADownLinkImpl implements CDMADownLink {

    private double successThreshold;
    private double pilotChannelFraction;
    private double overheadChannelFraction;
    private double maximumBroadcastChannel;
    private double maximumTrafficChannelFraction;

    public CDMADownLinkImpl() {
        successThreshold = 0.5;
        pilotChannelFraction = 0.15;
        overheadChannelFraction = 0.05;
        maximumBroadcastChannel = 40;
        maximumTrafficChannelFraction = 0.15;
    }

    @Override
    public double getSuccessThreshold() {
        return successThreshold;
    }

    public void setSuccessThreshold(double successThreshold ) {
        this.successThreshold = successThreshold;
    }

    @Override
    public double getPilotChannelFraction() {
        return pilotChannelFraction;
    }

    public void setPilotChannelFraction( double pilotChannelFraction ) {
        this.pilotChannelFraction = pilotChannelFraction;
    }

    @Override
    public double getOverheadChannelFraction() {
        return overheadChannelFraction;
    }

    public void setOverheadChannelFraction( double overheadChannelFraction ) {
        this.overheadChannelFraction = overheadChannelFraction;
    }

    @Override
    public double getMaximumBroadcastChannel() {
        return maximumBroadcastChannel;
    }

    public void setMaximumBroadcastChannel(double maximumBroadcastChannel ) {
        this.maximumBroadcastChannel = maximumBroadcastChannel;
    }

    @Override
    public double getMaximumTrafficChannelFraction() {
        return maximumTrafficChannelFraction;
    }

    public void setMaximumTrafficChannelFraction( double maximumTrafficChannelFraction ) {
        this.maximumTrafficChannelFraction = maximumTrafficChannelFraction;
    }
}
