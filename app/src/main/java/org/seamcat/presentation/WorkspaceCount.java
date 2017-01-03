package org.seamcat.presentation;

import org.seamcat.Seamcat;

import java.util.prefs.Preferences;

public class WorkspaceCount {

    private final static String WORKSPACE_COUNT = "WORKSPACE_COUNT";
    private final static String BATCH_COUNT = "BATCH_COUNT";

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(Seamcat.class);
    }

    public static int getAndUpdateCount() {
        return getAndUpdate(WORKSPACE_COUNT);
    }

    public static int getAndUpdateBatchCount() {
        return getAndUpdate(BATCH_COUNT);
    }


    private static int getAndUpdate( String name) {
        String current = preferences().get(name, "");
        if (current.length() <= 0) {
            current = "0";
        }

        int currentInt = Integer.parseInt(current);
        currentInt++;

        preferences().put( name, ""+currentInt);

        return currentInt;
    }
}
