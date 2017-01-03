package org.seamcat.presentation.genericgui.item;

import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.eventprocessing.PluginConfigurationPanel;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class EmbeddedPluginItem extends AbstractItem<PluginConfiguration, Object> {

    private List<WidgetAndKind> widgets;
    private PluginConfigurationPanel editor;

    public EmbeddedPluginItem(JFrame parent, PluginConfiguration configuration, Class<? extends PluginConfiguration> clazz) {
        editor = new PluginConfigurationPanel(parent, configuration, false, clazz);
        widgets = new ArrayList<WidgetAndKind>();
        widgets.add(new WidgetAndKind(editor, WidgetKind.NONE));
    }

    @Override
    public PluginConfiguration getValue() {
        return editor.getModel();
    }

    @Override
    public void setValue(PluginConfiguration value) {
        editor.setModel( value );
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        return widgets;
    }
}
