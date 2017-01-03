package org.seamcat.presentation.genericgui.panelbuilder;

import org.seamcat.plugin.PluginConfiguration;

import javax.swing.*;
import java.awt.*;

public class PluginEditorPanel<Model> extends JPanel {

    private GenericPanelEditor<Model> editor;

    public PluginEditorPanel(JFrame owner, PluginConfiguration<?,Model> configuration ) {
        super(new BorderLayout());

        editor = new GenericPanelEditor<Model>(owner, configuration);
        add(editor, BorderLayout.CENTER);
    }

    public Model getModel() {
        return editor.getModel();
    }

    public void setGlobalRelevance( boolean globalRelevance ) {
        editor.setGlobalRelevance( globalRelevance );
    }
}
