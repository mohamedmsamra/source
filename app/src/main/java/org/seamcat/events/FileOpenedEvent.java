package org.seamcat.events;

public class FileOpenedEvent {

    private String absoluteLocation;

    public FileOpenedEvent(String absoluteLocation) {
        this.absoluteLocation = absoluteLocation;
    }
    
    public String getAbsoluteLocation() {
        return absoluteLocation;
    }
}
