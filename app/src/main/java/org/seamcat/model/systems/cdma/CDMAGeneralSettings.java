package org.seamcat.model.systems.cdma;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.model.plugin.Config;

public interface CDMAGeneralSettings {

    @Config(order = 1, name = "Receiver noise figure", unit = "dB")
    double receiverNoiseFigure();
    double receiverNoiseFigure = 4.0;

    @Config(order = 2, name = "Handover margin", unit = "dB")
    double handoverMargin();
    double handoverMargin = 4.0;

    @Config(order = 3, name = "Call drop threshold", unit = "dB")
    double callDropThreshold();
    double callDropThreshold = 3.0;

    @Config(order = 4, name = "Voice bit rate", unit = "kbps")
    double voiceBitRate();
    double voiceBitRate = 9.6;

    @Config(order = 5, name = "Reference bandwidth", unit = "MHz")
    double bandwidth();
    double bandwidth = 1.25;

    @Config(order = 6, name = "Minimum coupling loss", unit = "dB")
    double minimumCouplingLoss();
    double minimumCouplingLoss = 70.0;

    @Config(order = 7, name = "Voice activity factor")
    double voiceActivityFactor();
    double voiceActivityFactor = 1.0;

    @Config(order = 8, name = " ", downLink = true)
    CDMALinkLevelData lld();

}
