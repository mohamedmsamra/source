package org.seamcat.model.plugin.propagation;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;

public interface SphericalDiffractionInput {
    @Config(order = 1, name = "Wall loss (indoor indoor)", unit = "dB")
    double wallLossInIn();

    @Config(order = 2, name = "Wall loss std. dev. (indoor indoor)", unit = "dB", toolTip = "Wall loss standard deviation (indoor indoor)")
    double wallLossStdDev();

    @Config(order = 3, name = "Loss between adjacent Floor", unit = "dB")
    double adjacentFloorLoss();

    @Config(order = 4, name = "Empirical parameters")
    double empiricalParameters();

    @Config(order = 5, name = "Size of the room", unit = "m", toolTip = "droom")
    double sizeOfRoom();

    @Config(order = 6, name = "Height of each floor", unit = "m", toolTip = "hfloor")
    double floorHeight();

    @Config(order = 7, name = "Water concentration", unit = "g/m")
    double waterConcentration();

    @Config(order = 8, name = "Earth surface admittance")
    double earthSurfaceAdm();

    @Config(order = 9, name = "Refraction index gradient", unit = "1/km")
    double refractionGradient();

    @Config(order = 10, name = "Refraction layer probability", unit = "%")
    double refractionProb();

    @Config(order = 11, name = "Time percentage", unit = "%")
    Distribution timePercentage();

    boolean variations = false;
    double wallLossInIn = 5;
    double wallLossStdDev = 10;
    double adjacentFloorLoss = 18.3;
    double empiricalParameters = 0.46;
    double sizeOfRoom = 4;
    double floorHeight = 3;
    double waterConcentration = 3;
    double earthSurfaceAdm = 0.00001;
    double refractionGradient = 40;
    double refractionProb = 1;
    Distribution timePercentage = Factory.distributionFactory().getUniformDistribution(50,50);
}
