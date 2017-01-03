package org.seamcat.model.systems;

import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.InterferenceLink;

public class SimulationInterferingLink {

    public InterferenceLink<?> getInterferenceLink() {
        return null;
    }

    public SystemPlugin getSystem() {
        return null;
    }

    public void simulate(Scenario scenario, EventResult eventResult, Point2D victimCorrelationPoint) {
        getSystem().simulationInstance().simulateAsInterferingSystem( scenario, eventResult, getInterferenceLink(), victimCorrelationPoint);
    }
}
