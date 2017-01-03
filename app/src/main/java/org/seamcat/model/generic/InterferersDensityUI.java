package org.seamcat.model.generic;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Function;
import org.seamcat.model.plugin.Config;

public interface InterferersDensityUI {

    @Config(order = 1, name = "Density of Tx", unit = "1/km\u00B2")
    double densityTx();
    double densityTx = 1.0;

    @Config(order = 2, name = "Prob. of transmission")
    double probabilityOfTransmission();
    double probabilityOfTransmission = 1.0;

    @Config(order = 3, name = "Activity", unit = "1/h")
    Function activity();
    Function activity = Factory.functionFactory().constantFunction(1.0);

    @Config(order = 4, name = "Time", unit = "hour")
    double hourOfDay();
    double hourOfDay = 1.0;
}
