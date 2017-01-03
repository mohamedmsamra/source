package org.seamcat.model.plugin.propagation;

import org.seamcat.model.plugin.Config;

public interface HataInput {

    boolean variations = true;

    @Config(order = 1, name = "General environment", values = "Urban,Suburban,Rural")
    String generalEnvironment();
    String generalEnvironment = "Urban";

    @Config(order = 2, name = "Propagation environment", values = "Above roof,Below roof")
    String propagationEnvironment();

    @Config(order = 3, name = "Wall loss (indoor indoor)", unit = "dB")
    double wallLossInIn();
    double wallLossInIn = 5;

    @Config(order = 4, name = "Wall loss std. dev. (indoor indoor)", unit = "dB", toolTip = "Wall loss standard deviation (indoor indoor)")
    double wallLossStdDev();
    double wallLossStdDev = 10;

    @Config(order = 5, name = "Loss between adjacent floor", unit = "dB")
    double adjacentFloorLoss();
    double adjacentFloorLoss = 18.3;

    @Config(order = 6, name = "Empirical parameters")
    double empiricalParameters();
    double empiricalParameters = 0.46;

    @Config(order = 7, name = "Size of the room", unit = "m", toolTip = " droom")
    double sizeOfRoom();
    double sizeOfRoom = 4;

    @Config(order = 8, name = "Height of each floor", unit = "m", toolTip = "hfloor")
    double floorHeight();
    double floorHeight = 3;
}
