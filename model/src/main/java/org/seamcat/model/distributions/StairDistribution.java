package org.seamcat.model.distributions;

import org.seamcat.model.functions.Function;

/**
 * The user-defined (stair) is the discrete alternative of continuous User-defined function.<br>
 *     <p></p>
 * The Stair function is defined through a discrete set of values Xi, i = 1…N and associated probabilities S(Xi).<br>
 * This is the pair of set (Xi, S(Xi)) where Xi represents all possible values that might be assigned to<br>
 * the variable, whereas S(Xi) represents their cumulative probabilities.<br>
 *     <p></p>
 * Such a distribution will be entered in form of a list of couples (Xi, S(Xi)). Entering such a distribution<br>
 * in cumulated form allow to control that the sum of the probabilities is equal to 1.<br>
 *     <p></p>
 * The definition range of the input Cum. Prob. (S(Xi)) is (0, 1)
 */
public interface StairDistribution extends Distribution {

    Function getCdf();
}
