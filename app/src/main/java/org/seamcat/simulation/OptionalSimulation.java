package org.seamcat.simulation;

import org.seamcat.model.simulation.CollectedResults;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.simulation.result.MutableEventResult;

public interface OptionalSimulation {

    void collect( MutableEventResult result );

    SimulationResultGroup buildResult(CollectedResults collected );
}
