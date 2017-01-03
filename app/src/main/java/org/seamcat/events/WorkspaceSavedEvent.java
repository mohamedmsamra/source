package org.seamcat.events;

public class WorkspaceSavedEvent {

    private String absoluteLocation;

    public WorkspaceSavedEvent(String absoluteLocation) {
        this.absoluteLocation = absoluteLocation;
    }
    
    public String getAbsoluteLocation() {
        return absoluteLocation;
    }
}
