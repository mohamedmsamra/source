package org.seamcat.model.simulation.result;

import org.seamcat.model.types.InterferenceLink;

import java.util.LinkedHashMap;
import java.util.List;

public interface EventResult {

    /**
     * The number of this event. It will be in the range of 0 to
     * the max number of events -1, as configured in the scenario
     *
     * @return number of this event
     */
    int getEventNumber();

    /**
     * For a given value return the corresponding value. Notice this might
     * return null if the name is not one of the collected results
     *
     * @param name
     * @return value for that name
     */
    Double getValue( String name );

    /**
     * Get all values collected during this simulated event
     *
     * @return map of all collected events
     */
    LinkedHashMap<String, Double> getValues();

    /**
     * Get all vector values collected during this simulation. Vector values are
     * when per snapshot you want to collect multiple values for a given name.

     * @return map of all collected vector values
     */
    LinkedHashMap<String, List<Double>> getVectorValues();

    /**
     * Get all the interference link results of this event for the given
     * interference link.
     *
     * @param link the interference link for which you want to see the
     *             interference link results for
     * @return InterferenceLinkResults containing all interference link results for this link
     */
    InterferenceLinkResults getInterferenceLinkResult(InterferenceLink link);


    /**
     * Get a list of all link results available inside the
     * victim system.
     *
     * @return list of link results
     */
    List<? extends LinkResult> getVictimSystemLinks();


    /**
     * Get the list of all interfering elements for this simulation
     * @return list of interfering elements
     */
    List<? extends Interferer> getInterferingElements();
}
