package org.seamcat.model.distributions;

/**
 * It is an ordinary Rayleigh distribution defined via its min and standard deviation sigma values
 */
public interface RayleighDistribution extends Distribution {

    double getMin();

    double getStdDev();
}
