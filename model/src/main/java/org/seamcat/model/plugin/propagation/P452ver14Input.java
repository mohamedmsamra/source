package org.seamcat.model.plugin.propagation;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;

public interface P452ver14Input {
    @Config(order = 1, name = "Diffraction")
    boolean diffraction();

    @Config(order = 2, name = "Tropospheric scatter")
    boolean troposphericScatter();

    @Config(order = 3, name = "Ducting/layer reflection")
    boolean layerReflection();

    @Config(order = 4, name = "Water concentration", unit = "g/m³")
    double waterConcentration();

    @Config(order = 5, name = "Surface pressure", unit = "hPa")
    double surfacePressure();

    @Config(order = 6, name = "Refraction index gradient", unit = "1/km")
    double refractionIndex();

    @Config(order = 7, name = "Surface temperature", unit = "deg C")
    double surfaceTemperature();

    @Config(order = 8, name = "Latitude", unit = "deg")
    double latitude();

    @Config(order = 9, name = "Additional clutter loss at the Tx", unit = "dB", toolTip = "Additional clutter loss at the transmitter")
    double clutterLossTx();

    @Config(order = 10, name = "Additional clutter loss at the Rx", unit = "dB", toolTip = "Additional clutter loss at the receiver")
    double clutterLossRx();

    @Config(order = 11, name = "Antenna gain at the Tx", unit = "dB", toolTip = "Antenna gain at the transmitter")
    double antennaGainTx();

    @Config(order = 12, name = "Antenna gain at the Rx", unit = "dB", toolTip = "Antenna gain at the receiver")
    double antennaGainRx();

    @Config(order = 13, name = "Sea level surface refractivity")
    double seaLevelSurfaceRefractivity();

    @Config(order = 14, name = "Time percentage", unit = "%")
    Distribution timePercentage();
    boolean diffraction = true;
    boolean troposphericScatter = true;
    boolean layerReflection = true;
    double waterConcentration = 3;
    double surfacePressure = 1013.25;
    double refractionIndex = 40;
    double surfaceTemperature = 15;
    double latitude = 45;
    double seaLevelSurfaceRefractivity = 325;
    Distribution timePercentage = Factory.distributionFactory().getUniformDistribution(50,50);
}
