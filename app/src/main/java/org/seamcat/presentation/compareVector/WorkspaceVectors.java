package org.seamcat.presentation.compareVector;

import org.seamcat.model.simulation.result.SimulationResult;

public class WorkspaceVectors {
    private String workspaceTitle;
    private SimulationResult simulationResult;

    public WorkspaceVectors( String workspaceTitle, SimulationResult simulationResult ) {
        this.workspaceTitle = workspaceTitle;
        this.simulationResult = simulationResult;
    }

    public String getWorkspaceTitle() {
        return workspaceTitle;
    }

    public SimulationResult getSimulationResult() {
        return simulationResult;
    }
}
