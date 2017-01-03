package org.seamcat.events;

public class InfoMessageEvent {
    private String message;
    
    public InfoMessageEvent( String message ) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}
