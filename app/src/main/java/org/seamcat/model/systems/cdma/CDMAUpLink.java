package org.seamcat.model.systems.cdma;

import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.OptionalDoubleValue;

public interface CDMAUpLink {

    @Config(order = 1, name = "Target network noise rise", unit = "dB")
    double targetNetworkNoiseRise();
    double targetNetworkNoiseRise = 5.5;

    @Config(order = 2, name = "Target cell noise rise", unit = "dB")
    OptionalDoubleValue targetCellNoiseRise();
    OptionalDoubleValue targetCellNoiseRise = new OptionalDoubleValue(false, 0.1);

    @Config(order = 3, name = "MS max. transmit power", unit = "dBm", toolTip = "Mobile station maximum transmit power")
    double msMaximumTransmitPower();
    double msMaximumTransmitPower = 25.0;

    @Config(order = 4, name = "MS power control range", unit = "dB", toolTip = "Mobile station power control range")
    double msPowerControlRange();
    double msPowerControlRange = 75.0;

    @Config(order = 5, name = "PC convergence precision", unit = "dB", toolTip = "Power control convergence precision")
    double pcConvergencePrecision();
    double pcConvergencePrecision = 0.001;
}
