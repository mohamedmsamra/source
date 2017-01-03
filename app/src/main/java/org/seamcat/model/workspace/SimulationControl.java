package org.seamcat.model.workspace;

import org.seamcat.model.plugin.Config;

public interface SimulationControl {

    @Config(order = 1, name = "Number of events")
    int numberOfEvents();
    int numberOfEvents = 20000;

    @Config(order = 2, name = "Debug mode")
    boolean debugMode();

}
