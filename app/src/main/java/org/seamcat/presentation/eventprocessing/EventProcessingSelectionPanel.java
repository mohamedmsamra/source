package org.seamcat.presentation.eventprocessing;

import org.seamcat.commands.DisplayHelpNameCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.model.factory.Model;
import org.seamcat.model.Workspace;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.LabeledPairLayout;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.builder.AsActionListener;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.GenericListDetailDialog;
import org.seamcat.presentation.menu.ToolBar;
import org.seamcat.util.StringHelper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class EventProcessingSelectionPanel extends JPanel {

    private EventProcessingPanel container;
    private Workspace model;
    private JList list;
    private DefaultListModel listModel;
    private JToolBar toolBar;
    private JButton duplicate,delete,export;

    public EventProcessingSelectionPanel(EventProcessingPanel container, Workspace model) {
        this.container = container;
        this.model = model;
        list = new JList();
        listModel = new DefaultListModel();
        for (EventProcessingConfiguration configuration : model.getEventProcessingList()) {
            listModel.addElement(configuration);
        }
        initialize();
    }

    private void initialize() {
        toolBar = new JToolBar();
        toolBar.setFocusable(false);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        setupToolbar();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(list);
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(toolBar, BorderLayout.NORTH);
        leftPanel.add(jScrollPane, BorderLayout.CENTER);

        list.setModel(listModel);
        list.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                container.updateCurrentEditor();
                if (!listModel.isEmpty() && list.getSelectedIndex() != -1) {
                    if ( !(list.getSelectedValue() instanceof EventProcessingConfiguration)) {
                        return;
                    }
                    container.selected(list.getSelectedIndex());
                } else {
                    container.deSelect();
                }
                update();
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyChar() == KeyEvent.VK_DELETE) {
                    delete();
                }
            }
        });
        if ( !listModel.isEmpty() ) {
            list.setSelectedIndex(0);
        }

        setLayout(new BorderLayout());
        add(leftPanel, BorderLayout.CENTER);
        update();
    }

    private void setupToolbar() {
        toolBar.add(ToolBar.button("SEAMCAT_ICON_ADD", "TOOLBAR_EPP_ADD", this, "add"));
        duplicate = ToolBar.button("SEAMCAT_ICON_DUPLICATE", "TOOLBAR_EPP_DUPLICATE", this, "duplicate");
        toolBar.add(duplicate);
        delete = ToolBar.button("SEAMCAT_ICON_DELETE_TRASH", "TOOLBAR_EPP_REMOVE", this, "delete");
        toolBar.add(delete);
        export = ToolBar.button("SEAMCAT_ICON_EXPORT_LIBRARY", "TOOLBAR_EPP_EXPORT", this, "export");
        toolBar.add(export);

        toolBar.add(Box.createHorizontalGlue());
        toolBar.addSeparator();
        toolBar.add(ToolBar.button("SEAMCAT_ICON_HELP", "HELP_CONTENTS_MENU_ITEM_TEXT", new DisplayHelpNameCommand("EventProcessingPlugins")));
    }

    @AsActionListener("add")
    public void addDialog() {
        container.updateCurrentEditor();
        List<PluginConfiguration> plugins = new ArrayList<PluginConfiguration>( Model.getInstance().getLibrary().getPluginConfigurations(EventProcessingConfiguration.class));
        GenericListDetailDialog<PluginConfiguration> dialog = new GenericListDetailDialog<PluginConfiguration>(MainWindow.getInstance(), "Event processing plugin", plugins) {
            public void selectedElement(PluginConfiguration model) {
                JPanel jPanel = new JPanel(new LabeledPairLayout());
                jPanel.add( new JLabel("Name"), LabeledPairLayout.LABEL);
                jPanel.add( new JLabel(model.description().name()), LabeledPairLayout.FIELD);
                jPanel.add( new JLabel("Description"), LabeledPairLayout.LABEL);
                jPanel.add( new JLabel(model.description().description()), LabeledPairLayout.FIELD);
                ReadOnlyPanel.addReadOnly(jPanel, model);
                setDetail(new BorderPanel(jPanel, "Event Processing Plugin"));
            }
        };
        if ( dialog.display() ) {
            add((EventProcessingConfiguration) dialog.getSelectedValue());
            update();
        }
    }

    private void add( EventProcessingConfiguration add) {
        model.getEventProcessingList().add(add);
        listModel.addElement( add );
        list.setSelectedIndex( listModel.getSize()-1);
    }

    @AsActionListener("duplicate")
    public void duplicate(){
        container.updateCurrentEditor();
        EventProcessingConfiguration original = (EventProcessingConfiguration) list.getSelectedValue();
        EventProcessingConfiguration clone = original.deepClone();
        String duplicatedName = StringHelper.getDuplicatedName(clone.description().name(), listModel);
        clone.setName( duplicatedName );
        add( clone );
        update();
    }

    @AsActionListener("delete")
    public void delete(){
        int index = list.getSelectedIndex();
        listModel.removeElement( list.getSelectedValue() );
        model.getEventProcessingList().remove( index );
        if ( index > 0 ) {
            list.setSelectedIndex( index -1 );
        }
    }

    @AsActionListener("export")
    public void export() {
        container.updateCurrentEditor();
        EventProcessingConfiguration configuration = (EventProcessingConfiguration)list.getSelectedValue();
        if ( configuration != null ) {
            EventProcessingConfiguration clone = configuration.deepClone();
            if ( Model.getInstance().getLibrary().addPluginConfiguration(clone) ) {
                Model.getInstance().getLibrary().addJarFile( clone );
                Model.getInstance().persist();
                EventBusFactory.getEventBus().publish(new InfoMessageEvent("Configuration exported to library"));
            } else {
                EventBusFactory.getEventBus().publish(new InfoMessageEvent("Name already exists in library. Could not export"));
            }
        }
    }

    public EventProcessingConfiguration get( int index ) {
        return (EventProcessingConfiguration) listModel.get(index);
    }

    public void update() {
        boolean enable = !listModel.isEmpty() && list.getSelectedIndex() != -1;
        delete.setEnabled(enable);
        duplicate.setEnabled(enable);
        export.setEnabled( enable );
        revalidate();
        repaint();
    }
}
