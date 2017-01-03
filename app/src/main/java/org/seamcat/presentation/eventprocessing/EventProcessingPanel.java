package org.seamcat.presentation.eventprocessing;

import org.seamcat.eventbus.Subscriber;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.TextWidgetValueUpdatedEvent;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.WorkspaceView;
import org.seamcat.presentation.components.BorderPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class EventProcessingPanel extends JPanel {

    private final EventProcessingSelectionPanel selectionPanel;
    private JPanel detailPanel;
    private JLabel noSelection = new JLabel("No selected event processing plugins");
    private PluginConfigurationPanel currentEditor;
    private List<EventProcessingConfiguration> model;
    private int index;

    public EventProcessingPanel( WorkspaceView view ) {
        super(new BorderLayout());
        detailPanel = new JPanel(new BorderLayout());
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);

        model = view.getWorkspace().getEventProcessingList();
        selectionPanel = new EventProcessingSelectionPanel(this, view.getWorkspace());
        jSplitPane.add(selectionPanel);
        detailPanel.add( noSelection, BorderLayout.CENTER);
        jSplitPane.add(new BorderPanel(detailPanel, "Configure Selected Plugin"));
        if ( !model.isEmpty() ){
            layoutDetail(0, model.get(0));
        }
        add(jSplitPane);
        Subscriber.subscribe(this);
    }

    private void layoutDetail( int index, EventProcessingConfiguration configuration ) {
        this.index = index;
        detailPanel.removeAll();
        currentEditor = new PluginConfigurationPanel(MainWindow.getInstance(), configuration, true, EventProcessingConfiguration.class);
        JScrollPane comp = new JScrollPane(currentEditor);
        comp.setBorder( new EmptyBorder(0,0,0,0));
        detailPanel.add(comp);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    public void selected(int index) {
        layoutDetail(index, model.get(index));
    }

    public void deSelect() {
        currentEditor = null;
        detailPanel.removeAll();
        detailPanel.add(noSelection, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    @UIEventHandler
    public void handle( TextWidgetValueUpdatedEvent event ) {
        if ( event.getContext() instanceof PluginConfigurationIdentificationPanel) {
            PluginConfiguration model = ((PluginConfigurationIdentificationPanel) event.getContext()).getModel();
            if ( model instanceof EventProcessingConfiguration) {
                selectionPanel.get(index).setName( event.getValue());
                selectionPanel.update();
            }
        }
    }

    public void updateCurrentEditor() {
        if ( currentEditor != null ) {
            PluginConfiguration configuration = currentEditor.getModel();
            model.set( index, (EventProcessingConfiguration) configuration);
        }
    }
}
