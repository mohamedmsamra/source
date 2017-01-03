package org.seamcat.model.factory;

import org.seamcat.model.functions.DataExporter;
import org.seamcat.model.functions.DataFile;
import org.seamcat.presentation.FileDialogHelper;
import org.seamcat.presentation.MainWindow;

import javax.swing.*;
import java.io.File;

public class DataExporterImpl implements DataExporter  {

    @Override
    public DataFile chooseFile() {
        FileDialogHelper chooser = MainWindow.getInstance().fileDialogHelper;
        if ( JFileChooser.APPROVE_OPTION == chooser.showSaveDialog() ) {
            File file = chooser.getSelectedFile();
            DataFileImpl dataFile = new DataFileImpl(file);
            DataSaver.add( dataFile );
            return dataFile;
        }

        return null;
    }

    @Override
    public DataFile chooseFile(String fileName) {
        FileDialogHelper chooser = MainWindow.getInstance().fileDialogHelper;

        if ( JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(fileName) ) {
            File file = chooser.getSelectedFile();
            DataFileImpl dataFile = new DataFileImpl(file);
            DataSaver.add( dataFile );
            return dataFile;
        }

        return null;
    }
}
