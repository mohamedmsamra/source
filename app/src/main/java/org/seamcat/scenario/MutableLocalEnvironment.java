package org.seamcat.scenario;

import org.seamcat.model.types.LocalEnvironment;

import java.text.DecimalFormat;

public class MutableLocalEnvironment implements LocalEnvironment {

    private static DecimalFormat df = new DecimalFormat("#.##");
    private Environment environment = Environment.Outdoor;
    private double wallLoss = 0;
    private double wallLossStdDev = 0;
    private double probability = 1;

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment( Environment environment ) {
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

    @Override
    public double getProbability() {
        return probability;
    }

    public void setProbability( double probability ) {
        this.probability = probability;
    }

    @Override
    public String toString() {
        String prefix = environment == Environment.Indoor ? String.format(" (wallLoss=%s dB, stdDev=%s)", df.format(wallLoss), df.format(wallLossStdDev)) : "";
        return df.format( probability * 100) + "% " + environment.name() + prefix;
    }

    public void setWallLossStdDev(double wallLossStdDev) {
        this.wallLossStdDev = wallLossStdDev;
    }
}
