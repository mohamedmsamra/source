package org.seamcat.model.systems.cdma;

import org.seamcat.model.plugin.Config;

public interface CDMADownLinkCapacity {

    @Config(order = 1, name = "Init users per cell")
    int initUsersPerCell();
    int initUsersPerCell = 20;

    @Config(order = 2, name = "Simulate non-interfered capacity", defineGroup = "capacity")
    boolean simulateNonInterferedCapacity();
    boolean simulateNonInterferedCapacity = true;

    @Config(order = 3, name = "Delta users per cell", group = "capacity")
    int deltaUsersPerCell();
    int deltaUsersPerCell = 20;

    @Config(order = 4, name = "Number of trials", group = "capacity")
    int numberOfTrials();
    int numberOfTrials = 20;

    @Config(order = 5, name = "Tolerance of initial outage", unit = "%", group = "capacity")
    double toleranceOfInitialOutage();
    double toleranceOfInitialOutage = 5.0;

}
