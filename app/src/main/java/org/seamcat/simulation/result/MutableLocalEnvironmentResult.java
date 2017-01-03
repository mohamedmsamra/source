package org.seamcat.simulation.result;

import org.seamcat.model.simulation.result.LocalEnvironmentResult;
import org.seamcat.model.types.LocalEnvironment;

public class MutableLocalEnvironmentResult implements LocalEnvironmentResult {

    private LocalEnvironment.Environment environment = LocalEnvironment.Environment.Outdoor;
    private double wallLoss = 0;
    private double stdDev = 0;

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
        return stdDev;
    }

    public void setWallLossStdDev( double stdDev ) {
        this.stdDev = stdDev;
    }
}
