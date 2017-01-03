package org.seamcat.presentation;

import org.seamcat.model.Workspace;

public class LoadedWorkspace {

    public void loadedWorkspace( Workspace workspace ) {
        MainWindow.getInstance().addWorkSpaceToView( workspace );
    }
}
