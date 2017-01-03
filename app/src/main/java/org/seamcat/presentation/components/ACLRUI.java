package org.seamcat.presentation.components;

import org.seamcat.model.plugin.Config;
import org.seamcat.model.systems.CalculatedValue;

public interface ACLRUI {

    @Config(order = 1, name = "Show ACLR", defineGroup = "aclr")
    boolean showACLR();
    boolean showACLR = true;

    @Config(order = 2, name = "Adjacent channel", unit = "MHz", group = "aclr")
    double adjacentChannel();
    double adjacentChannel = 1.0;

    @Config( order = 3, name = "Display ACLR with Interfering Emission Bandwidth [display only]", unit = "MHz", group = "aclr")
    double displayACLR();
    double displayACLR = 1.25;

    @Config( order = 4, name = "Update ACLR display")
    CalculatedValue update();
}
