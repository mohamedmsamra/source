package org.seamcat.model.plugin.propagation;

import org.seamcat.model.plugin.Plugin;
import org.seamcat.model.simulation.result.LinkResult;


/**
 * The user-defined model is intended to allow the user to create his/her own
 * propagation models in Java describing the pass loss calculation.
 *
 */
public interface PropagationModelPlugin<T> extends Plugin<T> {

	/**
	 * Evaluates the path loss<br>
	 * The user-defined model has a standard interface. The interface consists of
	 * two kinds of parameters:<br> * Variable parameters : assumed variable
	 * during a simulation, e.g. distance between transceivers, frequency, etc.<br> *
	 * Static parameters : considered constant in all events generated during a
	 * simulation, e.g. general and local environment parameters.
	 *
	 * @param linkResult
	 *           containing the result for the link for which to calculate the path loss
     * @param variation
     *           selects variation
	 * @param input
	 * 			User defined parameters
	 * @return Path loss
	 */
	double evaluate(LinkResult linkResult, boolean variation, T input);
}
