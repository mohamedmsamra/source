package org.seamcat.model.systems;

import org.seamcat.model.RadioSystem;

import java.util.List;

public interface SystemPlugin<UI extends SystemModel, T extends RadioSystem> {

    enum CorrelationMode {
        NONE,
        CLOSEST,
        UNIFORM,
        CORRELATED
    }

    T convert( UI ui );

    ParallelSimulation simulationInstance();

    List<CorrelationMode> getCorrelationModes();

    List<String> getCorrelationPointNames();
}
