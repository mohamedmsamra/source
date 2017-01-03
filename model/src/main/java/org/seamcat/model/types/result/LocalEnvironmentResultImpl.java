package org.seamcat.model.types.result;

import org.seamcat.model.simulation.result.LocalEnvironmentResult;
import org.seamcat.model.types.LocalEnvironment;

/**
 * A mutable version of a LocalEnvironmentResult. This is
 * only to be needed if plugins should want to use it
 * (typically in relation with the LinkResultImpl).
 *
 * NOTICE: This is not the class used internally in SEAMCAT.
 */
public class LocalEnvironmentResultImpl implements LocalEnvironmentResult {

    private LocalEnvironment.Environment environment;
    private double wallLoss, wallLossStdDev;

    @Override
    public LocalEnvironment.Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment( LocalEnvironment.Environment environment ) {
        this.environment = environment;
    }

    @Override
    public double getWallLoss() {
        return wallLoss;
    }

    public void setWallLoss( double wallLoss ) {
        this.wallLoss = wallLoss;
    }

    @Override
    public double getWallLossStdDev() {
        return wallLossStdDev;
    }

    public void setWallLossStdDev( double wallLossStdDev ) {
        this.wallLossStdDev = wallLossStdDev;
    }
}
