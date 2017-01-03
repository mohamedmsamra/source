package org.seamcat.model.systems.cdma;

import org.seamcat.model.plugin.Config;

public interface CDMADownLink {

    @Config(order = 1, name = "Success threshold", unit = "dB")
    double successThreshold();
    double successThreshold = 0.5;

    @Config(order = 2, name = "Pilot channel fraction")
    double pilotChannelFraction();
    double pilotChannelFraction = 0.15;

    @Config(order = 3, name = "Overhead channel fraction")
    double overheadChannelFraction();
    double overheadChannelFraction = 0.05;

    @Config(order = 4, name = "Max. broadcast power", unit = "dBm", toolTip = "Maximum broadcast power")
    double maxBroadcastPower();
    double maxBroadcastPower = 40.0;

    @Config(order = 5, name = "Max. traffic channel fraction", toolTip = "Maximum traffic channel fraction")
    double maxTrafficChannelFraction();
    double maxTrafficChannelFraction = 0.15;
}
