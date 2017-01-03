package org.seamcat.events;

import org.seamcat.presentation.WorkspaceView;

public class WorkspaceViewAddedEvent {

    private WorkspaceView view;

    public WorkspaceViewAddedEvent(WorkspaceView view) {
        this.view = view;
    }
    
    public WorkspaceView getWorkspaceView() {
        return view;
    }
}
