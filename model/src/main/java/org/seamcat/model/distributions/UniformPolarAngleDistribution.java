package org.seamcat.model.distributions;

/**
 * The uniform polar angle is to be used along with "Uniform polar distance", this function is designed to describe a <br>
 *     uniform distribution of transmitters within a circular area centred around a given zero-point. But whereas "Uniform <br>
 *         polar distance" describes random distance to centre point, the "Uniform polar angle" function defines random angle (azimuth)<br>
 * of transmitter with regards to centre point.
 *<p></p>
 * This function has one input parameter - maximum angle Amax - and the generated random values will be placed with equal <br>
 *     probability (uniform distrbution function) within the range -Amax...Amax.
 *<p></p>
 * This distribution is similar to uniform distribution but in the angle domain.
 *
 */
public interface UniformPolarAngleDistribution extends Distribution {

    double getMaxAngle();
}
