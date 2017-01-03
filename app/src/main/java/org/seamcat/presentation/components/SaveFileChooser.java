package org.seamcat.presentation.components;

import org.apache.commons.io.FilenameUtils;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.SeamcatJFileChooser;
import org.seamcat.tabulardataio.FileDataIO;
import org.seamcat.tabulardataio.FileFormat;
import org.seamcat.tabulardataio.TabularDataFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class SaveFileChooser {


    public static FileDataIO chooseFile(Component parent)  {
        JFileChooser chooser = new SeamcatJFileChooser();
        FileDataIO result = null;

        chooser.resetChoosableFileFilters();
        chooser.setAcceptAllFileFilterUsed(false);
        for (FileFormat fileFormat: TabularDataFactory.allFormats()) {
            chooser.addChoosableFileFilter(new FileNameExtensionFilter(fileFormat.getName(), fileFormat.getExtension()));
        }

        if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            FileFilter filter = chooser.getFileFilter();
            if ( filter != null && filter instanceof FileNameExtensionFilter ) {
                String wantedExtension = ((FileNameExtensionFilter) filter).getExtensions()[0];
                if ( !selectedFile.getName().endsWith( "." + wantedExtension ) ) {
                    selectedFile = new File( selectedFile.getAbsolutePath() + "." + wantedExtension );
                }
            }
            if (TabularDataFactory.findByFile(selectedFile) != null) {
                result = new FileDataIO();
                result.setFile(selectedFile);
            } else {
                DialogHelper.generalSeamcatError("Unsupported file format: " + FilenameUtils.getExtension(selectedFile.getName()));
            }
        }
        return result;
    }

}
