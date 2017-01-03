package org.seamcat.presentation;

import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.IdElement;
import org.seamcat.model.InterferenceLinkElement;
import org.seamcat.model.Library;
import org.seamcat.model.Workspace;
import org.seamcat.model.engines.InterferenceSimulationEngine;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.types.result.LongResultType;
import org.seamcat.model.types.result.SingleValueTypes;
import org.seamcat.model.workspace.InterferenceLinkUI;
import org.seamcat.model.workspace.SimulationControl;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.eventprocessing.ReadOnlyPanel;
import org.seamcat.presentation.genericgui.GenericPanel;
import org.seamcat.presentation.genericgui.item.IntegerItem;
import org.seamcat.presentation.menu.ToolBar;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;

public class WorkspaceReadOnlyView extends JScrollPane {

    public WorkspaceReadOnlyView(final Workspace workspace, ActionListener exportListener, final ReplayAction replayListener) {
        JPanel panel = new JPanel(new BorderLayout());

        final IntegerItem eventNumber = new IntegerItem();

        JButton export = new JButton("Export workspace");
        export.addActionListener(exportListener);
        JButton replay = ToolBar.button("SCENARIO_REPLAY", "SCENARIO_REPLAY_TOOLTIP", null);

        JPanel controlPanel = new JPanel(new LabeledPairLayout());
        SimulationControl control = workspace.getSimulationControl();
        ReadOnlyPanel.readOnly(controlPanel, SimulationControl.class, control);
        JPanel top = new JPanel(new BorderLayout());
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        exportPanel.add(export);

        GenericPanel eventPanel = new GenericPanel();
        eventPanel.addItem(eventNumber);
        eventPanel.initializeWidgets();
        eventNumber.setValue(0);

        replay.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                SimulationResult results = workspace.getSimulationResults();
                if (results != null) {
                    long seed = getSeed(results);
                    replayListener.replay(seed, eventNumber.getValue());
                }
            }
        });
        exportPanel.add(replay);
        exportPanel.add(eventPanel);
        JLabel helpButton = new JLabel(SeamcatIcons.getImageIcon("SEAMCAT_ICON_HELP", SeamcatIcons.IMAGE_SIZE_TOOLBAR));
        exportPanel.add(helpButton);
        helpButton.setToolTipText("See SEAMCAT manual");
        helpButton.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                SeamcatHelpResolver.showHelp(WorkspaceReadOnlyView.class.getName());
            }
        });

        top.add( exportPanel, BorderLayout.NORTH );
        top.add(new BorderPanel(controlPanel, "Simulation Control"), BorderLayout.CENTER);
        panel.add( top, BorderLayout.NORTH );

        JPanel ilsPanel = new JPanel();
        ilsPanel.setLayout(new BoxLayout(ilsPanel, BoxLayout.Y_AXIS));
        List<InterferenceLinkElement> interferenceLinkUIs = workspace.getInterferenceLinkUIs();
        for (int i = 0; i < interferenceLinkUIs.size(); i++) {
            InterferenceLinkElement element = interferenceLinkUIs.get(i);
            InterferenceLinkUI il = element.getSettings();
            JPanel ilPanel = new JPanel(new LabeledPairLayout());
            ilPanel.add(new JLabel("<html><b>Interfering system</b></html>"), LabeledPairLayout.LABEL);
            ilPanel.add(new JLabel(element.getName()), LabeledPairLayout.FIELD);
            ReadOnlyPanel.compositeReadOnly(ilPanel, InterferenceLinkUI.class, il);
            ilsPanel.add(new BorderPanel(ilPanel, "Victim to Interfering System Link " + (i+1)));
        }
        panel.add( ilsPanel, BorderLayout.CENTER );


        JPanel sysPanels = new JPanel();
        sysPanels.setLayout( new BoxLayout(sysPanels, BoxLayout.Y_AXIS));

        SystemModel victimSystem = workspace.getVictimSystem();
        JPanel sysPanel = new JPanel(new LabeledPairLayout());
        ReadOnlyPanel.compositeReadOnly(sysPanel, Library.getSystemModelClass(victimSystem), victimSystem);
        sysPanels.add( new BorderPanel(sysPanel, "Victim System"));

        int count = 1;
        for (IdElement<SystemModel> element : workspace.getSystemModels()) {
            if ( workspace.getVictimSystemId().equals(element.getId()) ) continue;
            sysPanel = new JPanel(new LabeledPairLayout());
            ReadOnlyPanel.compositeReadOnly(sysPanel, Library.getSystemModelClass(element.getElement()), element.getElement());
            sysPanels.add( new BorderPanel(sysPanel, "Interfering System " + count));
            count++;
        }

        List<EventProcessingConfiguration> epps = workspace.getEventProcessingList();
        if (epps != null && !epps.isEmpty()) {
            JPanel eppsPanel = new JPanel();
            eppsPanel.setLayout(new BoxLayout(eppsPanel, BoxLayout.Y_AXIS));
            for (EventProcessingConfiguration epp : epps) {
                JPanel eppPanel = new JPanel(new LabeledPairLayout());
                ReadOnlyPanel.addReadOnly(eppPanel, epp);
                eppsPanel.add(new BorderPanel(eppPanel, epp.toString()));
            }
            JPanel withEpp = new JPanel(new BorderLayout());
            withEpp.add( sysPanels, BorderLayout.CENTER);
            withEpp.add( eppsPanel, BorderLayout.SOUTH);
            panel.add(withEpp, BorderLayout.SOUTH);
        } else {
            panel.add( sysPanels, BorderLayout.SOUTH);
        }


        getViewport().add( panel );
    }

    private long getSeed( SimulationResult result ) {
        SimulationResultGroup statistics = result.getSeamcatResult(InterferenceSimulationEngine.STATISTICS);
        for (SingleValueTypes<?> type : statistics.getResultTypes().getSingleValueTypes()) {
            if ( type instanceof LongResultType && type.getName().equals(InterferenceSimulationEngine.SIMULATION_SEED)) {
                return ((LongResultType) type).getValue();
            }
        }

        return -1;
    }


}
