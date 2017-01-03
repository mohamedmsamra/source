package org.seamcat.model.plugin.propagation;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.OptionalDoubleValue;

public interface JTG56Input {
    //TODO input
    @Config(order = 1, name = "Allow reciprocity in the antenna height", toolTip = "<html>This JTG56 plugin allows reciprocity in the antenna height. <br>" +
            "Note that the combination of low antenna height (i.e. 1.5 m)<br>" +
            "for both Rx and Tx should be limited to small distances as this <br>" +
            "combination is not valid for the P1546.</html>")
    boolean allowReciprocity();

    boolean allowReciprocity = true;

    @Config(order = 2, name = "use SE42 modification (Hata instead of Free Space for d <= 0.1 km)", toolTip = "for the transition range: cut off distance to 100 m")
    boolean useSE42();

    boolean useSE42 = false;

    @Config(order = 3, name = "General Environment", values = "URBAN,SUBURBAN,RURAL")
    String generalEnv();

    @Config(order = 4, name = "Time probability [1% or 50%]", unit = "%")
    Distribution time();

    Distribution time = Factory.distributionFactory().getConstantDistribution(50.);

    @Config(order = 5, name = "Cutt off discance [< 100 m]", unit = "m")
    double cutOff();

    double cutOff = 40.;

    @Config(order = 6, name = "User defined local clutter height", unit = "m")
    OptionalDoubleValue userClutter();
    OptionalDoubleValue userClutter = new OptionalDoubleValue(false, 10.);


}