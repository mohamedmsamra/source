package org.seamcat.presentation;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;

public class FileDialogHelper {

	private final SeamcatJFileChooser FILECHOOSER;
	private boolean selectionMade;

	public FileDialogHelper() {
		FILECHOOSER = new SeamcatJFileChooser();
	}

    private void setFileFilters( FileFilter... filters ) {
        FILECHOOSER.resetChoosableFileFilters();
        for (FileFilter filter : filters) {
            FILECHOOSER.setFileFilter( filter );
        }
    }
	
	public FileDialogHelper openWorkspace( Component owner ) {
		setFileFilters(FileFilters.FILE_FILTER_WORKSPACE, FileFilters.FILE_FILTER_WORKSPACE_RESULT, FileFilters.FILE_FILTER_BATCH, FileFilters.FILE_FILTER_BATCH_RESULT);
        FILECHOOSER.setAcceptAllFileFilterUsed(false);
        FILECHOOSER.setMultiSelectionEnabled(true);
		FILECHOOSER.setDialogTitle("Open Workspace");
        FILECHOOSER.setFileFilter( FileFilters.FILE_FILTER_WORKSPACE );

		selectionMade = FILECHOOSER.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION;
		return this;
	}

    public FileDialogHelper addVector( Component owner) {
        setFileFilters();
        FILECHOOSER.setMultiSelectionEnabled(true);
        FILECHOOSER.setDialogTitle("Select vector(s) to load");

        selectionMade = (FILECHOOSER.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION);
        return this;
    }
	
	public FileDialogHelper importLibrary( Component owner ) {
		setFileFilters( FileFilters.FILE_FILTER_LIBRARY);
		FILECHOOSER.setMultiSelectionEnabled(false);
		FILECHOOSER.setDialogTitle("Import library");

		selectionMade = (FILECHOOSER.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION);
		return this;
	}
	
	public FileDialogHelper exportLibrary( Component owner ) {
		setFileFilters(FileFilters.FILE_FILTER_LIBRARY);
		FILECHOOSER.setMultiSelectionEnabled(false);
		FILECHOOSER.setDialogTitle("Export library");
		
		selectionMade = (FILECHOOSER.showSaveDialog(owner) == JFileChooser.APPROVE_OPTION);
		return this;
	}

    public FileDialogHelper chooseJar( Component owner ) {
        setFileFilters( FileFilters.FILE_FILTER_JAR );
        FILECHOOSER.setMultiSelectionEnabled( false );
        FILECHOOSER.setDialogTitle("Select plugin jar");

        selectionMade = (FILECHOOSER.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION);
        return this;
    }

	public FileDialogHelper saveWorkspaceAs( Component owner, FileFilter filter ) {
		FILECHOOSER.setAcceptAllFileFilterUsed(false);
		setFileFilters(filter);
		FILECHOOSER.setDialogTitle("Save Workspace As");
        selectionMade = (FILECHOOSER.showSaveDialog(owner) == JFileChooser.APPROVE_OPTION);
		return this;
	}

	public File getSelectedFile() {
		return FILECHOOSER.getSelectedFile();
	}

    public File[] getSelectedFiles() {
        return FILECHOOSER.getSelectedFiles();
    }

	public boolean selectionMade() {
		return selectionMade;
	}

	public File getCurrentDirectory() {
		return FILECHOOSER.getCurrentDirectory();
	}

	public FileFilter getFileFilter() {
		return FILECHOOSER.getFileFilter();
	}

    public int showSaveDialog() {
        return FILECHOOSER.showSaveDialog(MainWindow.getInstance());
	}

    public int showSaveDialog(String filename ) {
        FILECHOOSER.setSelectedFile(new File(filename));
        return FILECHOOSER.showSaveDialog(MainWindow.getInstance());
    }
}
