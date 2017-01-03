package org.seamcat.presentation.library;

import org.seamcat.model.factory.Model;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.CoverageRadiusConfiguration;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.presentation.WrapLayout;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.NavigateButtonPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.seamcat.presentation.builder.PanelBuilder.panel;

public class LibraryExportDialog extends LibraryImportExportDialog {

    private final JPanel content;

    public LibraryExportDialog(JFrame owner) {
        super(owner, "Export Libraries", 800, 600);

        content = new JPanel(new WrapLayout());
        content.setSize(new Dimension(750, 600));
        content.add( panel().button("Select All", this, "select").button("Deselect All", this, "deselect").get());

        addGroup( library.getSystems() );
        addGroup( library.getSpectrumEmissionMasks() );
        addGroup( library.getReceiverBlockingMasks() );
        addGroup( library.getReceivers() );
        addGroup( library.getTransmitters() );
        addGroup( library.getCDMALinkLevelData() );
        addGroup(library.getPluginConfigurations(AntennaGainConfiguration.class));
        addGroup(library.getPluginConfigurations(PropagationModelConfiguration.class));
        addGroup(library.getPluginConfigurations(EventProcessingConfiguration.class));
        addGroup(library.getPluginConfigurations(CoverageRadiusConfiguration.class));
        addGroup(library.getInstalledJars());

        setLocationRelativeTo(owner);
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(content);
        getContentPane().add(pane, BorderLayout.CENTER);
        getContentPane().add(new NavigateButtonPanel(this, false), BorderLayout.SOUTH);
    }

    private void addGroup( List<? extends LibraryItem> group ) {
        if ( group != null && group.size() > 0 ) {
            LibraryExportGroup export = new LibraryExportGroup(group);
            String name = Model.getInstance().getLibrary().typeName(group.get(0));

            groups.add(export);
            content.add(new BorderPanel(export, name));
        }
    }
}
