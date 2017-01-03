package org.seamcat.presentation.genericgui.item;

import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.PluginDialog;
import org.seamcat.presentation.eventprocessing.PluginConfigurationPanel;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.valuepreview.ButtonWithValuePreviewTip;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PluginItem extends AbstractItem<PluginConfiguration, Object> {

    private PluginConfiguration configuration;
    private PluginConfigurationPanel editor;
    private JFrame parent;
    private String name;
    private JLabel preview;
    private JButton pluginEditButton;
    private ActionListener actionListener;

    public PluginItem(JFrame parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public PluginConfiguration getValue() {
        return configuration;
    }

    @Override
    public void setValue(PluginConfiguration value) {
        configuration = value;
        updateValuePreview();
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();

        pluginEditButton = new ButtonWithValuePreviewTip("Edit");
        actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDialog();
            }
        };
        pluginEditButton.addActionListener(actionListener);
        widgets.add(new WidgetAndKind(pluginEditButton, WidgetKind.VALUE));

        preview = new JLabel();
        widgets.add(new WidgetAndKind(preview, WidgetKind.VALUE_PREVIEW));

        return widgets;
    }

    private void showDialog() {
        editor = new PluginConfigurationPanel(parent, configuration, false, configuration.getClass());
        PluginDialog dialog = new PluginDialog(parent, name, editor);

        if ( dialog.display() ) {
            configuration = editor.getModel();
            updateValuePreview();
        }
    }

    private void updateValuePreview() {
        preview.setText("["+configuration.description().name()+"]");
    }

    public void dispose() {
        super.dispose();
        pluginEditButton.removeActionListener(actionListener);
    }
}
