package org.seamcat.presentation.eventprocessing;

import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.components.GainPlotDialog;
import org.seamcat.presentation.genericgui.GenericPanel;
import org.seamcat.presentation.genericgui.item.CalculatedValueItem;
import org.seamcat.presentation.genericgui.panelbuilder.PluginEditorPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PluginConfigurationPanel extends JPanel {

    private final PluginConfigurationIdentificationPanel id;
    private PluginEditorPanel editor;
    private JPanel container = new JPanel(new BorderLayout());
    private JFrame parent;

    public PluginConfigurationPanel(JFrame parent, final PluginConfiguration configuration, final boolean libraryMode, Class<? extends PluginConfiguration> clazz ) {
        super(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        this.parent = parent;

        editor = new PluginEditorPanel(parent, configuration);
        id = new PluginConfigurationIdentificationPanel(this, parent, libraryMode, configuration, editor, clazz);
        container.add(editor, BorderLayout.CENTER);

        add(id, BorderLayout.NORTH);
        if ( configuration instanceof AntennaGainConfiguration) {
            panel.add(container, BorderLayout.NORTH);

            GenericPanel gainPanel = new GenericPanel();
            CalculatedValueItem item = new CalculatedValueItem().label("Show gain plot");
            gainPanel.addItem(item);
            gainPanel.initializeWidgets();
            item.getEvaluateButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GainPlotDialog dialog = new GainPlotDialog((AntennaGainConfiguration) id.getModel());
                    dialog.display();
                }
            });
            panel.add(gainPanel, BorderLayout.CENTER);

            add(panel, BorderLayout.CENTER);
        } else {
            add(container, BorderLayout.CENTER);
        }
    }



    public void setModel( PluginConfiguration configuration ) {
        editor = new PluginEditorPanel(parent, configuration);
        id.setContainer( editor );
        id.setModel(configuration);
        container.removeAll();
        container.add( editor, BorderLayout.CENTER );
        container.revalidate();
        container.repaint();
    }

    public PluginConfiguration getModel() {
        return id.getModel();
    }

    public PluginConfigurationIdentificationPanel getIdPanel() {
        return id;
    }

    public void setGlobalRelevance(boolean globalRelevance) {
        id.setGlobalRelevance( globalRelevance );
        editor.setGlobalRelevance(globalRelevance);
    }
}
