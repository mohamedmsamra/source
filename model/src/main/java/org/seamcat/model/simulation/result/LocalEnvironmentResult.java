package org.seamcat.model.simulation.result;

import org.seamcat.model.types.LocalEnvironment;

public interface LocalEnvironmentResult {

    LocalEnvironment.Environment getEnvironment();

    double getWallLoss();

    double getWallLossStdDev();
}
