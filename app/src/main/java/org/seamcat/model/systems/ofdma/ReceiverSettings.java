package org.seamcat.model.systems.ofdma;

import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.plugin.Config;

public interface ReceiverSettings {

    @Config(order = 1, name = "Blocking mask/ACS", unit = "MHz", rangeUnit = "dB", information = "CELLULAR_BLOCKING_MASK_INFO")
    BlockingMask blockingMask();

    @Config(order = 2, name = "Standard desensitisation", unit = "dB")
    double standardDesensitisation();
    double standardDesensitisation = 8.0;

    @Config(order = 3, name = "Target I/N", unit = "dB")
    double targetINR();
    double targetINR = 1.0;

}
