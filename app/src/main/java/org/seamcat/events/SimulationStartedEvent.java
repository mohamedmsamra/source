package org.seamcat.events;

import org.seamcat.presentation.WorkspaceView;

public class SimulationStartedEvent {

    private WorkspaceView workspaceView;

    public SimulationStartedEvent(WorkspaceView workspaceView) {
        this.workspaceView = workspaceView;
    }

    public WorkspaceView getWorkspaceView() {
        return workspaceView;
    }
}
