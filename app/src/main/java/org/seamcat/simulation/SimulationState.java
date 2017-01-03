package org.seamcat.simulation;

import org.seamcat.model.plugin.eventprocessing.PanelDefinition;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.plugin.EventProcessingConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SimulationState {

    private SimulationResult simulationResult;
    private List<Map<PanelDefinition<?>, Object>> customUIState;

    public SimulationState( SimulationResult simulationResult,  List<EventProcessingConfiguration> configurations) {
        this.simulationResult = simulationResult;
        customUIState = new ArrayList<>();
        for (EventProcessingConfiguration configuration : configurations) {
            customUIState.add( configuration.getCustomUIState().get());
        }
    }

    public SimulationState( List<Map<PanelDefinition<?>, Object>> customUIState, SimulationResult simulationResult ) {
        this.simulationResult = simulationResult;
        this.customUIState = customUIState;
    }

    public SimulationState( SimulationResult simulationResult ) {
        this.simulationResult = simulationResult;
    }

    public SimulationResult getSimulationResult() {
        return simulationResult;
    }

    public List<Map<PanelDefinition<?>, Object>> getConfigurations() {
        return customUIState;
    }
}
