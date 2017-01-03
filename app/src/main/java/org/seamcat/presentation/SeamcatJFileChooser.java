package org.seamcat.presentation;

import org.seamcat.Seamcat;
import org.seamcat.model.factory.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.util.prefs.Preferences;

public class SeamcatJFileChooser extends JFileChooser {

    private final static String FILE_DIALOG_PATH = "SEAMCAT_FILE_DIALOG_PATH";
    private final static String FILE_DIALOG_DIMENSION_X = "SEAMCAT_FILE_DIALOG_DIMENSION_X";
    private final static String FILE_DIALOG_DIMENSION_Y = "SEAMCAT_FILE_DIALOG_DIMENSION_Y";

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(Seamcat.class);
    }

    public SeamcatJFileChooser() {
        super(preferences().get(FILE_DIALOG_PATH, Model.seamcatHome.getAbsolutePath() + File.separator + "workspaces"));

        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent he) {
                grabFocusForTextField(getComponents());
            }
        });
    }

    // set the jTextField as focus
    private void grabFocusForTextField(Component[] c) {
        for(Component k:c)
        {
            if(k instanceof JTextField)
            {
                JTextField jt=(JTextField)k;
                jt.grabFocus();
                break;
            }
            else if(k instanceof JPanel)
            {
                JPanel jp=(JPanel)k;
                grabFocusForTextField(jp.getComponents());
            }
        }
    }


    public SeamcatJFileChooser(boolean directoryOnly) {
        this();
        if ( directoryOnly ) {
            setFileSelectionMode( DIRECTORIES_ONLY );
        }

    }

    private void setDialogSize() {
        Dimension openWorkspaceDimensions = new Dimension( Integer.parseInt( preferences().get( FILE_DIALOG_DIMENSION_X, "589") ), Integer.parseInt(preferences().get(FILE_DIALOG_DIMENSION_Y, "306")));
        setPreferredSize(openWorkspaceDimensions);
    }

    private void saveSettings() {
        preferences().put(FILE_DIALOG_PATH, getCurrentDirectory().getAbsolutePath());
        Dimension fileChooserSize = getSize();
        preferences().put(FILE_DIALOG_DIMENSION_X, ""+((Double)fileChooserSize.getWidth()).intValue());
        preferences().put(FILE_DIALOG_DIMENSION_Y, "" + ((Double) fileChooserSize.getHeight()).intValue());
    }

    @Override
    public int showOpenDialog(Component parent) throws HeadlessException {
        setDialogSize();
        int result = super.showOpenDialog(parent);
        saveSettings();
        return result;
    }

    @Override
    public int showSaveDialog(Component parent) throws HeadlessException {
        setDialogSize();
        int result = super.showSaveDialog(parent);
        saveSettings();
        return result;
    }
}
