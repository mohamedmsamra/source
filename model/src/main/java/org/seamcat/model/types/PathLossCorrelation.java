package org.seamcat.model.types;

public interface PathLossCorrelation {

    boolean isUsingPathLossCorrelation();

    double getPathLossVariance();

    double getCorrelationFactor();

}
