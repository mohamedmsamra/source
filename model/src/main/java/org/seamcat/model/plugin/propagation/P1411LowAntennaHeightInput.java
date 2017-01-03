package org.seamcat.model.plugin.propagation;
//---------------------------------------------------------------------------------------------------------------------------------------------------
//
//           ITU-R P.1411 propagation model for both Tx and Rx antennas at low height
//           Definition of the User Interface
//           Tested for SEAMCAT V5.0.0 (Alpha 15, 16 and next ones if no change of plugin interface)
//           Provided by THALES Communications and Security France/Béatrice MARTIN
//

import org.seamcat.model.plugin.Config;

public interface P1411LowAntennaHeightInput {

    @Config(order = 1, name = "General environment", values = "Suburban,Urban, Dense urban/high-rise")
    String generalEnvironment();
    String generalEnvironment = "Suburban";

    @Config(order = 2, name = "Percentage of locations", unit = "%")
    double LocationPercentage();
    double LocationPercentage = 90;

    @Config(order = 3, name = "Width for transition region", unit = "m")
    double WidthTransitionRegion();
    double WidthTransitionRegion = 15;

    @Config(order = 4, name = "Variations std. dev.", unit = "dB", toolTip = "Variations standard deviation")
    double stdDev();

    boolean variations = false;
}