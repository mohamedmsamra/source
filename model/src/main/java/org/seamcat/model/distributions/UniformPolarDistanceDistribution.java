package org.seamcat.model.distributions;

/**
 * Uniform polar distance is a distribution function designed to define a random positioning of transmitter along the <br>
 *     radius of coverage cell R (i.e. all the points are distributed over a circular area defined by max distance with the <br>
 *         same probability), to achieve a random uniform distribution of transmitters within a circular area centred around a given <br>
 *             zero-point.
 *<p></p>
 * This function has one parameter - max distance - and the uniform polar distance distribution is typically used for <br>
 * deriving distance factor used in calculation of the relative locations of transceivers within a link and between victim <br>
 * and interfering links. The result of the trial on such a distribution, the distance factor, is then multiplied by a <br>
 * coverage radius or simulation radius. Hence the default maximum value of R is set to 1, meaning that after multiplication <br>
 * of this random factor with the radius value, the resulting distance will be distributed uniformly along the entire <br>
 *     coverage/simulation radius.
 *<p></p>
 * Note that this is equivalent to defining a uniform density of transmitters on a circular area together with uniform <br>
 *     polar angle (with 360 deg) or uniform distribution in the angle domain (0,360 deg).
 */
public interface UniformPolarDistanceDistribution extends Distribution {

    double getMaxDistance();
}
