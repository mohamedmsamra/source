package org.seamcat.events;

public class EgeCanBeStoppedEvent {

    private boolean canBeStopped;

    public EgeCanBeStoppedEvent(boolean canBeStopped ) {
        this.canBeStopped = canBeStopped;
    }

    public boolean canBeStopped() {
        return canBeStopped;
    }
}
