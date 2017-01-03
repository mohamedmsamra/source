package org.seamcat.model.simulation;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.types.EventProcessing;
import org.seamcat.model.types.InterferenceLink;

import java.util.List;

public interface VictimSystemSimulation<T extends RadioSystem> {

    void simulate(EventResult eventResult);

    Point2D getSystemPosition( EventResult eventResult, InterferenceLink<GenericSystem> link );

    void collect(EventResult eventResult);

    void postSimulation(SimulationResult simulationResult);

    List<SimulationResultGroup> buildResults( CollectedResults collected );

    List<EventProcessing> getEmbeddedEPPs();
}
