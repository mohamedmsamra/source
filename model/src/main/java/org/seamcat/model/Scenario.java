package org.seamcat.model;

import org.seamcat.model.simulation.result.PreSimulationResults;
import org.seamcat.model.types.EventProcessing;
import org.seamcat.model.types.InterferenceLink;

import java.util.List;

/**
 * Scenario represents the interference scenario as set up in SEAMCAT.
 */
public interface Scenario {

    /**
     * how many events or snapshots should be simulated when running
     * this scenario as a Monte Carlo simulation
     * @return number of events to be simulated
     */
    int numberOfEvents();

    /**
     * Returns the list of interference links defined in this scenario
     * @return list of interference links
     */
    List<? extends InterferenceLink> getInterferenceLinks();

    /**
     * Returns the list of Event Processing Plugins configured
     * for this scenario
     * @return the list of configured EPP for this scenario
     */
    List<EventProcessing> getEventProcessingList();

    /**
     * Returns the victim system of this scenario. Notice that only
     * a single victim system is possible
     * @return the victim system
     */
    RadioSystem getVictimSystem();

    /**
     * Get the pre-simulation results for a specific system. Pre-simulation
     * values are calculated before the snapshot simulation begins.
     * @param system the system for which to get the pre-simulation results for
     * @return pre-simulation results
     */
    PreSimulationResults getPreSimulationResults(RadioSystem system);
}
