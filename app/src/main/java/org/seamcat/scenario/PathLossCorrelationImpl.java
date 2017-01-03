package org.seamcat.scenario;

import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.GaussianDistributionImpl;
import org.seamcat.model.types.PathLossCorrelation;

public class PathLossCorrelationImpl implements PathLossCorrelation {

    private boolean usingPathLossCorrelation = false;
    private double pathLossVariance = 10;
    private double correlationFactor = 0.5;
    private AbstractDistribution distribution;

    @Override
    public boolean isUsingPathLossCorrelation() {
        return usingPathLossCorrelation;
    }

    @Override
    public double getPathLossVariance() {
        return pathLossVariance;
    }

    @Override
    public double getCorrelationFactor() {
        return correlationFactor;
    }

    public void setUsingPathLossCorrelation(boolean usingPathLossCorrelation) {
        this.usingPathLossCorrelation = usingPathLossCorrelation;
    }

    public void setPathLossVariance(double pathLossVariance) {
        this.pathLossVariance = pathLossVariance;
    }

    public void setCorrelationFactor(double correlationFactor) {
        this.correlationFactor = correlationFactor;
    }

    public void initDistribution() {
        distribution = new GaussianDistributionImpl(0, pathLossVariance );
    }

    public double trial() {
        return distribution.trial();
    }
}
