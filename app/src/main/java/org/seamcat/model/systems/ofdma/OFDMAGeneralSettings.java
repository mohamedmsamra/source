package org.seamcat.model.systems.ofdma;

import org.seamcat.model.functions.Function;
import org.seamcat.model.generic.Defaults;
import org.seamcat.model.plugin.Config;

public interface OFDMAGeneralSettings {

    @Config(order = 1, name = "Max. RBs per BS", toolTip = "Maximum resource blocks per base station")
    int maxSubcarriersBs();
    int maxSubcarriersBs = 51;

    @Config(order = 2, name = "Number of RBs per MS", toolTip = "Number of resource blocks per mobile station")
    int maxSubcarriersMs();
    int maxSubcarriersMs = 17;

    @Config(order = 3, name = "Handover margin", unit = "dB")
    double handoverMargin();
    double handoverMargin = 3.1;

    @Config(order = 4, name = "Minimum coupling loss", unit = "dB")
    double minimumCouplingLoss();
    double minimumCouplingLoss = 70.0;

    @Config(order = 5, name = "System bandwidth", unit = "MHz")
    double bandwidth();
    double bandwidth = 10.0;

    @Config(order = 6, name = "Receiver noise figure", unit = "dB")
    double receiverNoiseFigure();
    double receiverNoiseFigure = 4.0;

    @Config(order = 7, name = "Bandwidth of a RB", unit = "kHz", toolTip = "Bandwidth of a resource block")
    double bandwidthResourceBlock();
    double bandwidthResourceBlock = 180.0;

    @Config(order = 8, name = "Bitrate mapping", unit = "Bit rate (bps/Hz)", rangeUnit = "SINR (dB)")
    Function bitRateMapping();
    Function bitRateMapping = Defaults.defaultOFDMABitRateMapping();


}
