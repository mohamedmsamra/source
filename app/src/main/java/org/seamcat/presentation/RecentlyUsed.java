package org.seamcat.presentation;

import org.seamcat.Seamcat;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.FileNotFoundEvent;
import org.seamcat.events.RecentlyUsedChangedEvent;
import org.seamcat.events.FileOpenedEvent;
import org.seamcat.events.WorkspaceSavedEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

public class RecentlyUsed {

    private static final List<String> historyItems;

    static {
        historyItems = new ArrayList<String>();
        historyItems.add("SEAMCAT_RECENT_FILE_1");
        historyItems.add("SEAMCAT_RECENT_FILE_2");
        historyItems.add("SEAMCAT_RECENT_FILE_3");
        historyItems.add("SEAMCAT_RECENT_FILE_4");
        historyItems.add("SEAMCAT_RECENT_FILE_5");
    }

    public RecentlyUsed() {
        EventBusFactory.getEventBus().subscribe(this);
    }

    private Preferences preferences() {
        return Preferences.userNodeForPackage(Seamcat.class);
    }
    private LinkedList<String> getHistory() {
        LinkedList<String> result = new LinkedList<String>();
        Preferences pref = preferences();

        for (String item : historyItems) {
            getHistoryItem(item, pref, result);
        }
        return result;
    }

    private void getHistoryItem(String name, Preferences pref, List<String> history) {
        String recent = pref.get(name, "");
        if ( recent.length() > 0 ) {
            history.add( recent );
        }
    }

    private void setHistory( List<String> history ) {
        Preferences pref = preferences();

        for ( int i=0; i<history.size(); i++ ) {
            pref.put(historyItems.get(i), history.get(i));
        }
        if ( history.size() < 5 ) {
            for (int i=history.size(); i<5; i++ ) {
                pref.remove( historyItems.get(i));
            }
        }
        EventBusFactory.getEventBus().publish(new RecentlyUsedChangedEvent(history));
    }

    @UIEventHandler
    public void handleFileNotFound( FileNotFoundEvent event ) {
        LinkedList<String> history = getHistory();
        if ( history.contains( event.getAbsolutePath() )) {
            history.remove( event.getAbsolutePath() );
            setHistory( history );
        }
    }

    @UIEventHandler
    public void handleOpenedWorkspace( FileOpenedEvent event ) {
        handleHistory(event.getAbsoluteLocation() );
    }

    @UIEventHandler
    public void handleSavedWorkspace( WorkspaceSavedEvent event ) {
        handleHistory( event.getAbsoluteLocation() );
    }

    private void handleHistory(String location) {
   	 if(location != null) {
   		 
        // get current History
        LinkedList<String> history = getHistory();

        // re-arrange
        history.remove( location );
        if ( history.size() >= 5 ) {
            history.removeLast();
        }

        history.addFirst( location );

        // save history
        setHistory(history);
		}
    }

    public void refresh() {
        setHistory( getHistory() );
    }
}
