package org.seamcat.model.distributions;

/**
 * It represents a continuous uniform distribution, with given min and max values, and all intermediate values having <br>
 *     equal probability
 */
public interface UniformDistribution extends Distribution {

    double getMin();

    double getMax();
}
