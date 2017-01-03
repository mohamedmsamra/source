package org.seamcat.model.simulation;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.InterferenceLink;

public interface InterferenceLinkSimulation<T extends RadioSystem> {

    void simulate( Scenario scenario, EventResult result, InterferenceLink<T> link, Point2D victimSystemPosition );
}
