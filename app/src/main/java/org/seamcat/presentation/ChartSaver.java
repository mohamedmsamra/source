package org.seamcat.presentation;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;

import java.io.File;
import java.io.IOException;

public class ChartSaver {

    private static final Logger LOG = Logger.getLogger(ChartSaver.class);

    public static void saveChart(ChartPanel panel) {
        SeamcatJFileChooser chooser = new SeamcatJFileChooser();
        File file = chooser.getCurrentDirectory();
        panel.setDefaultDirectoryForSaveAs( file );
        try {
            panel.doSaveAs();
        } catch (IOException e) {
            LOG.error("Error saving image", e);
        }
    }
}
