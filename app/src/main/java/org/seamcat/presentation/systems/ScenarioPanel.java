package org.seamcat.presentation.systems;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.CognitiveRadioSettingChangedEvent;
import org.seamcat.events.CorrelatedDistanceSettingChangedEvent;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.IdElement;
import org.seamcat.model.Workspace;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.model.systems.cdma.SystemModelCDMAUpLink;
import org.seamcat.model.systems.generic.SystemModelGeneric;

import org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink;
import org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink;
import org.seamcat.model.workspace.SimulationControl;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.genericgui.GenericPanel;
import org.seamcat.presentation.genericgui.item.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScenarioPanel extends JPanel {

    private final JComboBox<SystemListItem> victimSelector;
    private final DefaultComboBoxModel<SystemListItem> model;
    private DistributionItem frequency;
    private OptionalDistributionItem drss;
    private IntegerItem events;
    private BooleanItem debug;

    private InterferenceLinksPanel interferenceLinksPanel;
    private SystemsPanel systems;
    private Workspace workspace;
    private JPanel victimPanel;
    private GenericPanel drssPanel;

    public ScenarioPanel( final SystemsPanel systems, final Workspace workspace ) {
        super(new BorderLayout());
        this.systems = systems;
        this.workspace = workspace;
        drss = new OptionalDistributionItem(MainWindow.getInstance()).label("User defined dRSS").unit("dBm");
        drssPanel = new GenericPanel();
        drssPanel.addItem(drss);
        drssPanel.initializeWidgets();

        boolean enable = workspace.isUseUserDefinedDRSS();
        drss.setValue(new ValueWithUsageFlag<AbstractDistribution>(enable, (AbstractDistribution) workspace.getUserDefinedDRSS()));

        JSplitPane topContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        topContainer.setDividerLocation(900);
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        victimPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        victimSelector = new JComboBox<SystemListItem>();
        model = new DefaultComboBoxModel<SystemListItem>();
        setSelectionModel();
        victimSelector.setModel(model);
        victimPanel.add(victimSelector);

        frequency = new DistributionItem(MainWindow.getInstance()).label("Frequency");
        GenericPanel fPanel = new GenericPanel();
        fPanel.addItem(frequency);
        fPanel.initializeWidgets();
        victimPanel.add(fPanel);
        if ( workspace.getVictimSystem() instanceof SystemModelGeneric ) {
            victimPanel.add(drssPanel);
        } 

        events = new IntegerItem().label("Number of events");

        GenericPanel ePanel = new GenericPanel();
        ePanel.addItem(events);
        ePanel.initializeWidgets();
        controlPanel.add(ePanel);

        debug = new BooleanItem().label("Run in debug mode");
        GenericPanel dPanel = new GenericPanel();
        dPanel.addItem(debug);
        dPanel.initializeWidgets();
        controlPanel.add(dPanel);

        topContainer.add( new BorderPanel(new JScrollPane(victimPanel), "Victim System"));
        topContainer.add(new BorderPanel(new JScrollPane(controlPanel), "Simulation Control"));
        add(topContainer, BorderLayout.NORTH);


        interferenceLinksPanel = new InterferenceLinksPanel(MainWindow.getInstance(), workspace);
        add(new BorderPanel(interferenceLinksPanel, "Interfering System Links"), BorderLayout.CENTER);

        setSimulationControl(workspace.getSimulationControl());
        refreshFromModel();
        systems.handleEnablement();
    }

    public void register() {
        interferenceLinksPanel.register();

        victimSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                systems.updateModel();

                SystemListItem item = (SystemListItem) model.getSelectedItem();
                if (item != null) {
                    if (item.getElement().getElement() instanceof SystemModelGeneric  ) {
                        if (!(workspace.getVictimSystem() instanceof SystemModelGeneric)  ) {
                            victimPanel.add(drssPanel);
                        }
                    } else if (workspace.getVictimSystem() instanceof SystemModelGeneric  ) {
                        victimPanel.remove(drssPanel);
                    }

                    workspace.setVictimSystemId(item.getElement().getId());
                    selectVictimSystem(workspace.getVictimSystem());
                }

                interferenceLinksPanel.refreshFromModel();
                interferenceLinksPanel.handleEnablement();
            }
        });


        frequency.addItemChangedHandler(new ItemChanged<AbstractDistribution>() {
            public void itemChanged(AbstractDistribution value) {
                workspace.setVictimFrequency(value);
            }
        });

        debug.getValueWidget().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (debug.getValue()) {
                    events.setValue(5);
                } else {
                    events.setValue(20000);
                }
            }
        });

        EventBusFactory.getEventBus().subscribe(this);
    }

    public void unregister() {

    }

    @UIEventHandler
    public void handle(CorrelatedDistanceSettingChangedEvent changed ) {
        systems.updateModel();
        systems.handleEnablement();
    }

    @UIEventHandler
    public void handle(CognitiveRadioSettingChangedEvent changed ) {
        systems.updateModel();
        interferenceLinksPanel.handleEnablement();
    }

    private void setSelectionModel() {
        model.removeAllElements();
        String id = workspace.getVictimSystemId();
        SystemListItem victim = null;
        for (IdElement<SystemModel> system : workspace.getSystemModels()) {
            SystemListItem item = new SystemListItem(system);
            if ( system.getId().equals(id)) {
                victim = item;
            }
            model.addElement( item);
        }

        if ( victim != null ) {
            model.setSelectedItem( victim );
        }
    }


    private void selectVictimSystem( SystemModel victim ) {
        Distribution freq;
        if ( victim instanceof SystemModelGeneric ) {
            freq = ((SystemModelGeneric) victim).general().frequency();
            
        } 
        
        else if ( victim instanceof SystemModelCDMAUpLink ) {
            freq = ((SystemModelCDMAUpLink) victim).general().frequency();
        } else if ( victim instanceof SystemModelCDMADownLink) {
            freq = ((SystemModelCDMADownLink) victim).general().frequency();
        } else if ( victim instanceof SystemModelOFDMAUpLink) {
            freq = ((SystemModelOFDMAUpLink) victim).general().frequency();
        } else {
            freq = ((SystemModelOFDMADownLink) victim).general().frequency();
        }

        frequency.setValue((AbstractDistribution) freq);
    }

    private void setSimulationControl( SimulationControl control ) {
        events.setValue(control.numberOfEvents());
        debug.setValue(control.debugMode());
    }

    public SimulationControl getSimulationControl() {
        SimulationControl prototype = Factory.prototype(SimulationControl.class);
        Factory.when(prototype.numberOfEvents()).thenReturn(events.getValue());
        Factory.when( prototype.debugMode()).thenReturn(debug.getValue());
        return Factory.build( prototype );
    }

    public void updateModel() {
        workspace.setSimulationControl(getSimulationControl());
        SystemListItem selectedItem = (SystemListItem) victimSelector.getSelectedItem();
        interferenceLinksPanel.updateModel();
        workspace.setVictimSystemId(selectedItem.getElement().getId());
        ValueWithUsageFlag<AbstractDistribution> value = drss.getValue();
        workspace.setUseUserDefinedDRSS(value.useValue);
        workspace.setUserDefinedDRSS(value.value);
    }

    public void refreshFromModel() {
        setSelectionModel();
        frequency.setValue((AbstractDistribution) workspace.getVictimFrequency());
        interferenceLinksPanel.refreshFromModel();
    }
}