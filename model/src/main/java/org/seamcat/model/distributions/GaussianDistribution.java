package org.seamcat.model.distributions;

/**
 * It is an ordinary Gaussian (Normal) distribution defined by mean m and standard deviation sigma values
 */
public interface GaussianDistribution extends Distribution {

    double getMean();

    double getStdDev();
}
