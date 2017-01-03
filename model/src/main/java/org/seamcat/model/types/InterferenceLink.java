package org.seamcat.model.types;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.generic.InterferingLinkRelativePosition;

/**
 * This is a interference link of the scenario. It is a link between the
 * victim system and an interfering system.
 */
public interface InterferenceLink<T extends RadioSystem> extends Link {

    RadioSystem getVictimSystem();

    T getInterferingSystem();

    InterferingLinkRelativePosition getInterferingLinkRelativePosition();

    // to be moved to a AsVictimSystemLinkUI
    PathLossCorrelation getPathLossCorrelation();

    /**
     * Returns the calculated simulation radius, which might be different
     * from the getInterferingLinkRelativePosition().getSimulationRadius() as
     * specified by the scenario. The correlation mode will determine is this
     * value is different
     * @return the calculated simulation
     */
    double getCalculatedSimulationRadius();

}
