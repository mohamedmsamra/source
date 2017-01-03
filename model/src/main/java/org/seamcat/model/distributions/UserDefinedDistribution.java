package org.seamcat.model.distributions;

import org.seamcat.model.functions.Function;

/**
 * It is a continuous distribution defined by its cumulative distribution function (a distribution with values <br>
 *     associated with probability), entered as pairs (x, y=F(x))
 *     <p></p>
 * The definition range of the input Prob. is between 0 and 1.
 */
public interface UserDefinedDistribution extends Distribution {

    Function getCdf();
}
