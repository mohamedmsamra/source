package org.seamcat.presentation;

import org.seamcat.cdma.CDMADownlinkSystem;
import org.seamcat.cdma.CDMASystem;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.Subscriber;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.*;
import org.seamcat.model.*;
import org.seamcat.model.core.InterferenceLink;
import org.seamcat.model.eventprocessing.CustomUIPanels;
import org.seamcat.model.factory.DataExporterImpl;
import org.seamcat.model.functions.DataExporter;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Service;
import org.seamcat.model.plugin.eventprocessing.CustomUI;
import org.seamcat.model.plugin.eventprocessing.CustomUITab;
import org.seamcat.model.plugin.eventprocessing.PostProcessing;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.model.systems.cdma.SystemModelCDMAUpLink;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.objectutils.WorkspaceCloneHelper;
import org.seamcat.ofdma.OfdmaSystem;
import org.seamcat.plugin.CustomUIState;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.presentation.compareVector.WorkspaceVectors;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.InterferenceCalculationsPanel;
import org.seamcat.presentation.eventprocessing.ControlButton;
import org.seamcat.presentation.genericgui.panelbuilder.Cache;
import org.seamcat.presentation.replay.SingleEventPanel;
import org.seamcat.presentation.replay.SingleEventSimulationResult;
import org.seamcat.presentation.systems.cdma.CDMAPlotModel;
import org.seamcat.presentation.systems.cdma.CDMASystemPlotPanel;
import org.seamcat.presentation.systems.cdma.CapacityFindingStatusPanel;
import org.seamcat.simulation.SimulationState;
import org.seamcat.simulation.generic.GenericVictimSystemSimulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

import static org.seamcat.eventbus.EventBusFactory.getEventBus;

public class SimulationView extends JTabbedPane {

    private Workspace workspace;
    private SimulationState original;
    private boolean isRunning;

    public boolean isEgeRunning() {
        return isRunning;
    }

    public void setIsRunning( boolean isRunning ) {
        this.isRunning = isRunning;
    }

    public WorkspaceVectors getResultVectors() {
        return new WorkspaceVectors(workspace.getName(), workspace.getSimulationResults() );
    }

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private SeamcatDistributionPlot sdp;
    private JPanel resultsPanel = new JPanel();
    private InterferenceCalculationsPanel itc;
    private Map<EventProcessingConfiguration, List<CustomUIPanels>> customUIPanels = new HashMap<>();
    private CapacityFindingStatusPanel capacityPanel;

    public SimulationView(Workspace workspace) {
        super(SwingConstants.TOP);

        this.workspace = workspace;
        original = WorkspaceCloneHelper.clone(new SimulationState(workspace.getSimulationResults(), workspace.getEventProcessingList()));

        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        Subscriber.subscribe(this);

        if ( workspace.isHasBeenCalculated() ) {
            showResultPanels( true );
            RSSEvent rssEvent = new RSSEvent(workspace);
            rssEvent.setCurrentEvent(-1);
            rssEvent.setRss( get(workspace.getSimulationResults(), SimulationResult.DRSS ) );
            rssEvent.setIrssU( get(workspace.getSimulationResults(), SimulationResult.IRSS_UNWANTED));
            if ( workspace.getSimulationResults().hasSeamcatGroup(SimulationResult.IRSS_BLOCKING)) {
                rssEvent.setIrssB( get(workspace.getSimulationResults(), SimulationResult.IRSS_BLOCKING));
            }
            if ( workspace.getSimulationResults().hasSeamcatGroup(SimulationResult.IRSS_SELECTIVITY)) {
                rssEvent.setIrssB( get(workspace.getSimulationResults(), SimulationResult.IRSS_SELECTIVITY));
            }

            EventBusFactory.getEventBus().publish(rssEvent);
            EventBusFactory.getEventBus().publish(new InfoMessageEvent(STRINGLIST.getString("OPEN_WORKSPACE_HAS_RESULTS")));
        } else {
            showResultPanels( false );
        }
        // update model to make sure all external classes are cached
        updateModel();
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void startSimulation() {
        File logFile = workspace.simulate(sdp);
        isRunning = false;
        workspace.setScenarioOutlineModel( sdp.getModel() );
        showResults();
        simulationComplete( workspace.getSimulationControl().numberOfEvents()-1, workspace);
        updateControlButtons();
        getEventBus().publish(new SimulationCompleteEvent());
        updateModel();
        if (workspace.getSimulationControl().debugMode()) {
            new SimulationLogDialog( logFile ).display();
            EventBusFactory.getEventBus().publish( new InfoMessageEvent( String.format( STRINGLIST.getString("EGE_LOGGING"), logFile.getAbsolutePath())));
        }
    }

    private VectorValues get(SimulationResult simulationResult, String groupName ) {
        if ( simulationResult.hasSeamcatGroup( groupName) ) {
            return GenericVictimSystemSimulation.calculate( simulationResult.getSeamcatResult(groupName).getResultTypes().getVectorResultTypes().get(0).getValue().asArray());
        } else {
            return new VectorValues("N/A", "N/A", "N/A");
        }
    }

    private void destroy() {
        Subscriber.unSubscribeDeep( this );
        sdp.destroy();
        sdp = null;
        removeAll();
        workspace = null;
    }

    private void reloadItc() {
        if ( itc != null ) {
            itc.init();
        }
    }

    private void appendTable(SimulationResultGroupTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseClickTypeHandler(table));
        int width = table.getPreferredSize().width;
        int height = Math.min(5, (1 + model.getRowCount()) * table.getRowHeight());
        table.setPreferredScrollableViewportSize(new Dimension(width, height));
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add( table.getTableHeader(), BorderLayout.NORTH );
        jPanel.add( table, BorderLayout.CENTER);

        if ( model.getHelpContents() != null ) {
            resultsPanel.add(new BorderPanel(jPanel, model.toString(), "See SEAMCAT manual", model.getHelpContents()));
        } else {
            resultsPanel.add(new BorderPanel(jPanel, model.toString()));
        }
    }

    public void updateResults() {
        for (SimulationResultGroupTableModel result : results) {
            result.update();
        }
    }

    private void showResultPanels( final boolean showResults ) {
        JSplitPane resultsTab = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        resultsTab.setDividerLocation(700);
        resultsTab.add(new WorkspaceReadOnlyView(workspace, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Workspace clone = WorkspaceCloneHelper.clone(workspace);
                clone.setName("Exported " + clone.getName());
                MainWindow.getInstance().addWorkspaceViewToPanel(new WorkspaceView(clone), clone.getName());
            }
        }, new ReplayAction() {
            public void replay(final long simulationSeed, final int eventNumber) {
                MainWindow.singleThreadPool.submit(new Runnable() {
                    public void run() {
                        MainWindow.getInstance().setCursorBusy();
                        try {
                            SingleEventSimulationResult result = workspace.simulateSingle(simulationSeed, eventNumber);
                            result.getSimulatedWorkspace().cellularVictimSimulation = workspace.cellularVictimSimulation;
                            simulationComplete(eventNumber, result.getSimulatedWorkspace());
                            EventBusFactory.getEventBus().publish(new InfoMessageEvent("Log file for trace saved in '" + result.getLogFile().getAbsolutePath() + "'"));

                            add(new SingleEventPanel(result, SimulationView.this), "Replay [event=" + result.getEventResult().getEventNumber() + "]");

                        } catch (Exception e ) {
                            e.printStackTrace();
                        } finally {
                            MainWindow.getInstance().setCursorDefault();
                        }
                    }
                });
            }
        }));
        resultsTab.add(new JScrollPane(resultsPanel));
        sdp = new SeamcatDistributionPlot(workspace);

        if ( !workspace.getVictimSystemLink().isDMASystem() ) {
            itc = new InterferenceCalculationsPanel( workspace );
        }

        if ( workspace.hasDMASubSystem() ) {
            if ( !workspace.isHasBeenCalculated() ) {
                for (IdElement<SystemModel> element : workspace.getSystemModels()) {
                    if ( element.getElement() instanceof SystemModelCDMAUpLink ) {
                        SystemModelCDMAUpLink sys = (SystemModelCDMAUpLink) element.getElement();
                        if ( sys.generalSettings().cdmaCapacity().simulateNonInterferedCapacity() ) {
                            capacityPanel = new CapacityFindingStatusPanel(workspace);
                            add("CDMA Capacity Finding", capacityPanel);
                            break;
                        }
                    } else if ( element.getElement() instanceof SystemModelCDMADownLink ) {
                        SystemModelCDMADownLink sys = (SystemModelCDMADownLink) element.getElement();
                        if ( sys.generalSettings().cdmaCapacity().simulateNonInterferedCapacity())  {
                            capacityPanel = new CapacityFindingStatusPanel(workspace);
                            add("CDMA Capacity Finding", capacityPanel);
                            break;
                        }
                    }

                }
            }
        }

        add(STRINGLIST.getString("TAB_TEXT_SIMULATION_OUTLINE"), sdp);
        add(STRINGLIST.getString("TAB_TEXT_SIMULATION_RESULTS"), resultsTab);
        if ( itc != null ) {
            add(STRINGLIST.getString("TAB_TEXT_INTERFERENCE_CALCULATIONS"), itc);
        }

        addEPPCustomUI();
        updateControlButtons();

        workspace.setScenarioOutlineModel( null );

        //sdp.setBatchRun( batch );
        // run simulation in the background
        // notifiers which update the UI will
        // manually run in SwingUtilities.invokeLater
        //sdp.clearAllElements();
        setSelectedComponent(sdp);

        workspace.resetEventGeneration();
        workspace.setScenarioOutlineModel(sdp.getModel());
        if ( showResults ) {
            showResults();
        }
    }

    private void addEPPCustomUI() {
        for (EventProcessingConfiguration conf : workspace.getEventProcessingList()) {
            if ( !customUIPanels.containsKey( conf )) {
                customUIPanels.put( conf, new ArrayList<CustomUIPanels>());
            }
            List<CustomUIPanels> uiPanels = customUIPanels.get(conf);
            // initialize UITabs
            CustomUITab tabs = (CustomUITab) conf.getPluginClass().getAnnotation(CustomUITab.class);
            if ( tabs != null ) {
                for (Class<? extends CustomUI> aClass : tabs.value()) {
                    try {
                        CustomUI customUI = aClass.newInstance();
                        for (Field field : aClass.getDeclaredFields()) {
                            if ( field.getAnnotation(Service.class) != null ) {
                                if ( field.getType().equals(DataExporter.class)) {
                                    field.setAccessible(true);
                                    field.set(customUI, new DataExporterImpl());
                                }
                            }
                        }
                        CustomUIPanels tab = new CustomUIPanels( customUI, conf.getCustomUIState());
                        uiPanels.add(tab);

                        JPanel canvas = new JPanel(new BorderLayout());
                        tab.buildUI(canvas, tab.getPanels());
                        JPanel controlPanel = new JPanel(new FlowLayout());
                        for (Map.Entry<Method,PostProcessing> entry : Cache.ordered(PostProcessing.class, aClass, new Cache.Order<PostProcessing>() {
                            public int getOrder(PostProcessing postProcessing) {
                                return postProcessing.order();
                            }
                        }).entrySet()) {
                            String name = entry.getValue().name();
                            Class<?>[] parameterTypes = entry.getKey().getParameterTypes();
                            if ( parameterTypes.length < 5 ) {
                                Object[] arguments = new Object[parameterTypes.length];
                                int indexOfResultTypes = -1;
                                int indexOfSimulationResults = -1;
                                boolean validControlButton = true;
                                for (int i = 0; i < parameterTypes.length; i++) {
                                    Class<?> type = parameterTypes[i];
                                    if ( Scenario.class.isAssignableFrom( type )) {
                                        arguments[i] = workspace.getScenario();
                                    } else if ( conf.getModelClass().isAssignableFrom( type)) {
                                        arguments[i] = conf.getModel();
                                    } else if ( ResultTypes.class.isAssignableFrom( type )) {
                                        indexOfResultTypes = i;
                                    } else if ( SimulationResult.class.isAssignableFrom(type)) {
                                        indexOfSimulationResults = i;
                                    } else {
                                        validControlButton = false;
                                        break;
                                    }
                                }
                                if ( validControlButton ) {
                                    ControlButton button = new ControlButton(this, name, entry.getKey(), tab.getDecorated(), arguments, indexOfResultTypes, indexOfSimulationResults);
                                    addButton( conf.getId(), button );
                                    controlPanel.add(button);
                                }
                            }
                        }

                        JPanel jPanel = new JPanel(new BorderLayout());
                        jPanel.add(canvas, BorderLayout.CENTER);
                        jPanel.add(controlPanel, BorderLayout.SOUTH);

                        add(tab.getTitle(), jPanel);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private List<ControlButton> addButton( String id, ControlButton button ) {
        List<ControlButton> postButtons = controlButtons.get(id);
        if ( postButtons == null ) {
            postButtons = new ArrayList<>();
            controlButtons.put(id, postButtons );
        }
        postButtons.add(button);
        return postButtons;
    }


    private void updateControlButtons() {
        if (workspace.getSimulationResults() != null && !controlButtons.isEmpty()) {
            for (Map.Entry<String, List<ControlButton>> entry : controlButtons.entrySet()) {
                for (ControlButton button : entry.getValue()) {
                    button.setSimulationResult( workspace.getSimulationResults() );
                }
                for (SimulationResultGroup group : workspace.getSimulationResults().getEventProcessingResults()) {
                    if (group.getId().equals( entry.getKey() )) {

                        for (ControlButton button : entry.getValue()) {
                            button.setResultTypes( group.getResultTypes() );
                        }
                    }
                }
            }
        }
    }

    private Map<String, List<ControlButton>> controlButtons = new HashMap<>();

    private List<SimulationResultGroupTableModel> results = new ArrayList<SimulationResultGroupTableModel>();

    public void showResults() {
        resultsPanel.removeAll();
        results.clear();

        if ( workspace.getSimulationResults() != null ) {
            writeGroup(workspace.getSimulationResults().getSeamcatResults(), false);
            writeGroup(workspace.getSimulationResults().getEventProcessingResults(), true);
            writeGroup(workspace.getSimulationResults().getSystemPreSimulationResults(), false);
        }
    }

    public void simulationComplete( int lastEventIndex, Workspace simulatedWorkspace ) {
        if ( simulatedWorkspace.hasDMASubSystem() ) {
            final CDMASystemPlotPanel plot = new CDMASystemPlotPanel(createUIModel(simulatedWorkspace), lastEventIndex);
            plot.refresh();
            plot.addRemoveBehaviour( new Runnable(){
                public void run() {
                    remove(plot);
                }
            });
            add(STRINGLIST.getString("TAB_TEXT_CELLULAR_STRUCTURE"), plot);

        }
    }

    private Map<String, CDMAPlotModel> createUIModel( Workspace workspace ) {
        Map<String ,CDMAPlotModel> map = new LinkedHashMap<>();

        if ( workspace.getVictimSystemLink().isDMASystem() ) {
            CDMAPlotModel model = create(workspace.cellularVictimSimulation);
            model.name = workspace.getVictimSystemLink().getName();
            model.victimSystem = true;
            map.put("Victim:" + model.name, model );
        }

        List<InterferenceLink> interferenceLinks = workspace.getInterferenceLinks();
        for (int i = 0; i < interferenceLinks.size(); i++) {
            InterferenceLink link = interferenceLinks.get(i);
            if (link.getInterferingLink().isDMASystem()) {
                CDMAPlotModel model = create(workspace.cellularInterferingSystemSimulation.get(i));
                model.name = link.getInterferingLink().getName();
                model.victimSystem = false;
                map.put(i + ":" + model.name, model);
            }
        }

        return map;
    }


    private CDMAPlotModel create( AbstractDmaSystem system ) {
        CDMAPlotModel model = new CDMAPlotModel();
        model.cellularSystem = system.getSystemSettings();
        model.eventResult = system.getEventResult();
        model.preResults = system.getResults().getPreSimulationResults();
        model.activeUsers = new ArrayList<>(system.getActiveUsers());
        if ( system instanceof CDMASystem ) {
            CDMASystem cs = (CDMASystem) system;
            model.droppedUsers = new ArrayList(cs.getDroppedUsers());
            model.inactiveUsers = new ArrayList(cs.getInactiveUsers());
        } else {
            model.droppedUsers = new ArrayList<>();
            model.inactiveUsers = new ArrayList<>();
        }
        model.baseStations = system.getBaseStationArray();
        model.externalInterferers = system.getExternalInterferers();
        model.location = system.getLocation();
        model.referenceCell = system.getReferenceCell();
        model.frequency = system.getFrequency();
        model.intercellDistance = system.getInterCellDistance();
        model.thermalNoise = system.getResults().getThermalNoise();

        if ( system instanceof OfdmaSystem ) {
            model.ofdmaVictims = ((OfdmaSystem) system).getVictims();
            model.processingGain = system.getProcessingGain();
        }

        if (system instanceof CDMASystem ) {
            model.numberOfLLDFound = ((CDMASystem) system).getNumberOfNoLinkLevelDataUsers();
            if ( system instanceof CDMADownlinkSystem) {
                model.maxTrafficChannelPower = Math.round(Mathematics.fromWatt2dBm(((CDMADownlinkSystem) system).getMaxTrafficChannelPowerInWatt()));
            }
        }
        return model;
    }


    private void writeGroup(List<SimulationResultGroup> group, boolean allowEmpty ) {
        if ( group == null || group.isEmpty() ) return;
        for ( SimulationResultGroup gr : group) {
            if ( !allowEmpty && gr.getResultTypes().isEmpty() ) continue;
            SimulationResultGroupTableModel simModel = new SimulationResultGroupTableModel(gr);
            appendTable(simModel);
            results.add( simModel );
        }

    }

    public void updateModel() {
        for (Map.Entry<EventProcessingConfiguration, List<CustomUIPanels>> entry : customUIPanels.entrySet()) {

            CustomUIState state = entry.getKey().getCustomUIState();
            for (CustomUIPanels panels : entry.getValue()) {
                state.get().putAll(panels.getState().get());
            }
        }

        // if called after destroy fields might be null
        reloadItc();
    }

    @UIEventHandler
    public void handleErrorDuringSimulation( ErrorDuringSimulation error ) {
        if ( error.getContext() == workspace ) {
            JOptionPane.showMessageDialog(MainWindow.getInstance(),
                    "<html><b>Exception message:</b> " + error.getSimulationException().getMessage() + "\n" +
                            "Unexpected expected exception occurred during simulation. Simulation will stop",
                    error.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean dirty() {
        updateModel();
        return !WorkspaceCloneHelper.equals(new SimulationState(workspace.getSimulationResults(), workspace.getEventProcessingList()), original);
    }

    private boolean hasResults() {
        return workspace.getSimulationResults() != null;
    }

    private boolean tooBig() {
        return hasResults() && SizeEstimator.resultsTooLargeForSaveOrLoad(workspace);
    }

    private boolean isClose;
    private boolean dirty;

    public boolean save() {
        isClose = false;
        dirty = dirty();
        if ( dirty ) {
            return handleSpecialCasesSave();
        }

        return true;
    }

    public boolean close() {
        isClose = true;
        dirty = dirty();
        boolean result;
        if ( !dirty ) {
            result = true;
        } else {
            result = handleSpecialCasesSave();
        }
        if ( result ) {
            destroy();
        }
        return result;
    }

    // handles saving of workspace, special cases
    private boolean handleSpecialCasesSave() {
        boolean tooBig = tooBig();
        if ( isClose && tooBig )  return closeCleanAndUnmodifiedBig();
        if ( isClose ) return closeCleanAndUnmodified();
        if ( tooBig ) return saveCleanAndUnmodifiedBig();
        return saveCleanAndUnmodified();
    }

    private boolean saveCleanAndUnmodified() {
        // save with results, save without results, cancel
        saveWorkspace(workspace);
        //long eventSizeEstimate = SizeEstimator.eventFileSizeEstimate(workspace);
        //int selection = DialogHelper.saveWorkspaceResults(workspace.getReference(), eventSizeEstimate);
        return true;
    }

    private boolean closeCleanAndUnmodified() {
        long eventSizeEstimate = SizeEstimator.eventFileSizeEstimate(workspace);
        int result = DialogHelper.saveResultsWhenClosing("Results["+workspace.getName()+"]", eventSizeEstimate);
        switch (result) {
            case 0: // save with results
                saveWorkspace(workspace);
                return true;
            case 1: // no
                EventBusFactory.getEventBus().publish(new InfoMessageEvent(STRINGLIST.getString("WORKSPACE_NOT_SAVED")));
                return true;
            default: // cancel the close
                EventBusFactory.getEventBus().publish(new InfoMessageEvent(STRINGLIST.getString("CANCEL_CLOSE_OPERATION")));
                return false;
        }
    }

    private boolean closeCleanAndUnmodifiedBig() {
        int result = DialogHelper.closeCleanAndUnmodifiedBig(workspace.getName());
        switch (result) {
            case 0: // save without results
                saveWorkspace(workspace);
                return true;
            case 1: // no
                EventBusFactory.getEventBus().publish(new InfoMessageEvent(STRINGLIST.getString("CLOSE_WORKSPACE_NOT_SAVED")));
                return true;
            default: // cancel the close
                EventBusFactory.getEventBus().publish(new InfoMessageEvent(STRINGLIST.getString("CANCEL_CLOSE_OPERATION")));
                return false;
        }
    }

    private boolean saveCleanAndUnmodifiedBig() {
        int selection = DialogHelper.saveCleanAndUnmodifiedBig(workspace.getName());
        switch (selection) {
            case 0:
                saveWorkspace(workspace);
                return true;
        }
        return false;
    }

    public void saveWorkspaceAs(File file) {
        updateModel();
        workspace.setPath( file );
        String name = file.getName().substring(0, file.getName().lastIndexOf("."));
        workspace.setName(name);
        boolean tooBig = tooBig();
        // case 1: save w/o results
        if ( tooBig ) saveCleanAndUnmodifiedBig();
        saveCleanAndUnmodified();
    }

    private void saveWorkspace(Workspace workspace) {
        original = WorkspaceCloneHelper.clone( new SimulationState(workspace.getSimulationResults(), workspace.getEventProcessingList()));
        MainWindow.getInstance().saveWorkspace( workspace );
    }

    @Override
    public String toString() {
        if ( workspace != null ) {
            return workspace.getName();
        }
        return super.toString();
    }
}
