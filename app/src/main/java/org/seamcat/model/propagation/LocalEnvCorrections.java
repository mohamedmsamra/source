package org.seamcat.model.propagation;

public class LocalEnvCorrections {
    public double rMedianLoss;
    public double rStdDev;

    public LocalEnvCorrections(double rMedianLoss, double rStdDev) {
        this.rMedianLoss = rMedianLoss;
        this.rStdDev = rStdDev;
    }
}
