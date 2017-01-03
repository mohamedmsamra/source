package org.seamcat.presentation.library;

import org.seamcat.interfaces.Dispatcher;
import org.seamcat.interfaces.DuplicateVisitor;
import org.seamcat.model.Library;
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
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.presentation.builder.PanelBuilder.panel;

public class LibraryImportDialog extends LibraryImportExportDialog {

    private final JPanel content;

    public LibraryImportDialog(JFrame owner, Library imported ) {
        super(owner, "Import Libraries", 800, 600);
        setLayout(new BorderLayout());

        content = new JPanel(new WrapLayout());
        content.setSize( new Dimension(750, 600));
        content.add( panel().button("Select All", this, "select").button("Deselect All", this, "deselect").get());

        addGroup( imported.getSystems(), library.getSystems());
        addGroup( imported.getSpectrumEmissionMasks(), library.getSpectrumEmissionMasks() );
        addGroup( imported.getReceiverBlockingMasks(), library.getReceiverBlockingMasks() );
        addGroup( imported.getReceivers(), library.getReceivers() );
        addGroup( imported.getTransmitters(), library.getTransmitters() );
        addGroup( imported.getCDMALinkLevelData(), library.getCDMALinkLevelData() );
        addGroup( imported.getPluginConfigurations(AntennaGainConfiguration.class), library.getPluginConfigurations(AntennaGainConfiguration.class) );
        addGroup( imported.getPluginConfigurations(PropagationModelConfiguration.class), library.getPluginConfigurations(PropagationModelConfiguration.class));
        addGroup( imported.getPluginConfigurations(EventProcessingConfiguration.class), library.getPluginConfigurations(EventProcessingConfiguration.class));
        addGroup( imported.getPluginConfigurations(CoverageRadiusConfiguration.class), library.getPluginConfigurations(CoverageRadiusConfiguration.class));
        addGroup( imported.getInstalledJars(), library.getInstalledJars());

        setLocationRelativeTo(owner);
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(content);
        getContentPane().add(pane, BorderLayout.CENTER);
        getContentPane().add(new NavigateButtonPanel(this, false), BorderLayout.SOUTH);
    }

    private void addGroup( List<? extends LibraryItem> imported, List<? extends LibraryItem> existing ) {
        if ( imported != null && imported.size() > 0 ) {
            LibraryImportGroup group = new LibraryImportGroup(imported, existing);
            String name = Model.getInstance().getLibrary().typeName(imported.get(0));
            groups.add(group);
            content.add(new BorderPanel(group, name));
        }
    }

    public List<LibraryItem> findDuplicateNames() {
        List<LibraryItem> duplicates = new ArrayList<LibraryItem>();
        List<LibraryItem> selected = selectedItems();
        DuplicateVisitor visitor = new DuplicateVisitor(library);
        for (LibraryItem identifiable : selected) {
            if (Dispatcher.dispatch(visitor, identifiable) ) {
                duplicates.add( identifiable );
            }
        }
        return duplicates;
    }
}
