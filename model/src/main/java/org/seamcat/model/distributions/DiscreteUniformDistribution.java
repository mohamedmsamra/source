package org.seamcat.model.distributions;

/**
 * The discrete uniform is the discrete alternative of the Uniform distribution.
 * <p></p>
 * The Discrete uniform distribution is defined by the following parameters:
 * <ol>
 *   <ul>Lower bound Xmin of the distribution</ul>
 *   <ul>Upper bound Xmax of the distribution</ul>
 *   <ul>Step S (e.g. channel spacing in the case of frequency distributions)</ul>
 *   <ul>Step shift Ss (e.g. to set the step values to the centre frequency of the channel)</ul>
 * </ol>
 */
public interface DiscreteUniformDistribution extends Distribution {

    double getMin();

    double getMax();

    double getStep();

    double getStepShift();
}
