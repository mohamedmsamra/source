package org.seamcat.presentation.batch;

import org.seamcat.model.Workspace;

import java.util.List;

public class BatchViewState {

    private String name;
    private String description;
    private boolean incrementalSave;

    private List<Workspace> workspaces;

    public BatchViewState( String name, String description, boolean incrementalSave) {
        this.name = name;
        this.description = description;
        this.incrementalSave = incrementalSave;
    }

    public List<Workspace> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<Workspace> workspaces) {
        this.workspaces = workspaces;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isIncrementalSave() {
        return incrementalSave;
    }
}
