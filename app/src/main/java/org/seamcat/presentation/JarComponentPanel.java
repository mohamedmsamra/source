package org.seamcat.presentation;

import org.seamcat.model.plugin.antenna.AntennaGainPlugin;
import org.seamcat.model.plugin.coverageradius.CoverageRadiusPlugin;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.plugin.JarConfigurationModel;
import org.seamcat.plugin.PluginClass;
import org.seamcat.presentation.genericgui.GenericPanel;
import org.seamcat.presentation.genericgui.item.ComponentWrapperItem;
import org.seamcat.presentation.genericgui.item.TextItem;

import javax.swing.*;
import java.awt.*;

public class JarComponentPanel extends JPanel {

    public JarComponentPanel(JarConfigurationModel model) {
        super( new BorderLayout());
        JPanel panel = new JPanel(new LabeledPairLayout());
        panel.add(new JLabel(""), LabeledPairLayout.FIELD);
        add( panel, BorderLayout.NORTH );
        JPanel plugins = new JPanel( new BorderLayout());
        add( plugins, BorderLayout.CENTER );
        if ( model.getPluginClasses() != null && !model.getPluginClasses().isEmpty() ) {
            GenericPanel loaded = new GenericPanel();

            ComponentWrapperItem headline = new ComponentWrapperItem(new JLabel("The following classes have been loaded:"));
            loaded.addItem(headline);
            TextItem item = new TextItem().label("<html><b>Type</b></html>").readOnly();
            item.initialize();
            item.setValue("<html><b>Class</b></html>");
            loaded.addItem(item);
            for (PluginClass aClass : model.getPluginClasses()) {
                Class clazz = aClass.getPluginClass();
                String type = "";
                if ( EventProcessingPlugin.class.isAssignableFrom(clazz) ) {
                    type = "Event Processing";
                } else if ( PropagationModelPlugin.class.isAssignableFrom(clazz) ) {
                    type = "Propagation Model";
                } else if (AntennaGainPlugin.class.isAssignableFrom(clazz)) {
                    type = "Antenna Gain";
                } else if (CoverageRadiusPlugin.class.isAssignableFrom(clazz)) {
                    type = "Coverage Radius";
                }
                TextItem textItem = new TextItem();
                textItem.label("<html><i>" + type + "</i></html>").readOnly();
                textItem.initialize();
                textItem.setValue(aClass.getClassName());
                loaded.addItem( textItem );
            }
            loaded.initializeWidgets();
            plugins.add( loaded );
        } else {
            plugins.add(new JLabel("<html><b color=\"red\"><i>Warning</i><br>No plugin classes found in jar file. This configuration will be deleted when closing the dialog</b></html>"), BorderLayout.NORTH);
        }
        plugins.revalidate();
        plugins.repaint();
    }


}
