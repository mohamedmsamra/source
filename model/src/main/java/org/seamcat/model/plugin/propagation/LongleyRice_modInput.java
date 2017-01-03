package org.seamcat.model.plugin.propagation;

/* LongleyRice interface modified by THALES/Béatrice MARTIN : restore one climate value which was missing
 */

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;

public interface LongleyRice_modInput {

    @Config(order = 1, name = "Mean surface refractivity")
    int meanSurface();

    @Config(order = 2, name = "Terrain irregular parameter", unit = "m")
    double terrainIrregular();

    @Config(order = 3, name = "Conductivity", unit = "S/m")
    double conductivity();

    @Config(order = 4, name = "Relative permittivity")
    double relPermit();

    @Config(order = 5, name = "Polarization", values = "Horizontal,Vertical")
    String polarisation();

    @Config(order = 6, name = "Siting criteria", values = "Random,Careful,Very Careful")
    String siteCriteria();

    //Patch THALES : restore "Maritime Temperate over sea" which was missing
    //@Config(order = 7, name = "Radio Climate", values = "Equatorial,Continental Subtropical,Maritime Subtropical,Desert,Continental Temperate,Maritime Temperate over land")
    @Config(order = 7, name = "Radio Climate", values = "Equatorial,Continental Subtropical,Maritime Subtropical,Desert,Continental Temperate,Maritime Temperate over land,Maritime Temperate over sea")
    String radioClimate();
    //End of patch

    @Config(order = 8, name = "Time percentage [1 ... 99%]")
    Distribution timePercentage();

    @Config(order = 9, name = "Location percentage [1 ... 99%]")
    Distribution locationPercentage();

    @Config(order = 10, name = "Confidential percentage [1 ... 99%]")
    Distribution confidentPercent();

    @Config(order = 11, name = "Mode of variability", values = "Single,Individual,Broadcast")
    String variability();

    @Config(order = 12, name = "Standard deviation", unit = "dB")
    double stdDev();

    int meanSurface = 301;
    double terrainIrregular = 90.;
    double conductivity = 0.005;
    double relPermit = 15.;
    String polarisation = "Horizontal";
    String siteCriteria = "Random";
    String radioClimate = "Continental Temperate";
    Distribution timePercentage = Factory.distributionFactory().getUniformDistribution(50., 50.);
    Distribution locationPercentage = Factory.distributionFactory().getUniformDistribution(50., 50.);
    Distribution confidentPercent = Factory.distributionFactory().getUniformDistribution(50., 50.);
    String variability = "Broadcast";
    double stdDev = 0.;
    boolean variations = false;

}