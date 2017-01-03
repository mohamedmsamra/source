package org.seamcat.events;

public class FileNotFoundEvent {

    private String absolutePath;

    public FileNotFoundEvent( String absolutePath ) {
        this.absolutePath = absolutePath;
    }
    
    public String getAbsolutePath() {
        return absolutePath;
    }
}
