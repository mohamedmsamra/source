package org.seamcat.model.systems;

import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.InterferenceLink;

public interface ParallelSimulation {

    void preSimulate();

    void simulateAsVictim(EventResult eventResult);

    void simulateAsInterferingSystem(Scenario scenario, EventResult result, InterferenceLink link, Point2D victimSystemPosition );

}
