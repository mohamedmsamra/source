package org.seamcat.events;

import java.util.List;

public class RecentlyUsedChangedEvent {
    private List<String> history;

    public RecentlyUsedChangedEvent( List<String> history ) {
        this.history = history;
    }

    public List<String> getHistory() {
        return history;
    }
}
