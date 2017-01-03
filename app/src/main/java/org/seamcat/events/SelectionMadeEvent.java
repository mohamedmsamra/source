package org.seamcat.events;

public class SelectionMadeEvent extends ContextEvent {

    private String name;

    public SelectionMadeEvent(Object context, String name ) {
        super(context);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
