package org.seamcat.presentation;

import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.NavigateButtonPanel;
import org.seamcat.presentation.eventprocessing.PluginConfigurationPanel;

import javax.swing.*;
import java.awt.*;

public class PluginDialog extends EscapeDialog {

    public PluginDialog(Frame parent, String title, PluginConfigurationPanel editor) {
        super(parent, true);
        setTitle("Plugin Editor");
        setSize(new Dimension(700, 450));
        JPanel content = new JPanel(new BorderLayout());
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(editor);
        content.add(new BorderPanel( pane, title ), BorderLayout.CENTER);
        content.add(new NavigateButtonPanel(this), BorderLayout.SOUTH);
        getContentPane().add(content, BorderLayout.CENTER);
        this.setLocationRelativeTo(parent);
    }

}
