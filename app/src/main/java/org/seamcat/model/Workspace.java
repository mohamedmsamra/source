package org.seamcat.model;

import org.apache.log4j.Logger;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.model.core.InterferenceLink;
import org.seamcat.model.core.ScenarioOutlineModel;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.model.engines.InterferenceSimulationEngine;
import org.seamcat.model.engines.SimulationListener;
import org.seamcat.model.engines.SimulationPool;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.factory.Model;
import org.seamcat.model.generic.GenericReceiver;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.eventprocessing.CustomUI;
import org.seamcat.model.plugin.eventprocessing.CustomUITab;
import org.seamcat.model.plugin.eventprocessing.PanelDefinition;
import org.seamcat.model.simulation.InterferenceLinkSimulation;
import org.seamcat.model.simulation.VictimSystemSimulation;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.SystemModelGeneric;

import org.seamcat.model.types.Receiver;
import org.seamcat.model.workspace.SimulationControl;
import org.seamcat.objectutils.WorkspaceCloneHelper;
import org.seamcat.plugin.EventProcessingConfiguration;
import org.seamcat.presentation.replay.SingleEventSimulationResult;
import org.seamcat.scenario.WorkspaceScenario;
import org.seamcat.simulation.Simulation;
import org.seamcat.simulation.cellular.CellularInterfererInterferenceLinkSimulation;
import org.seamcat.simulation.cellular.CellularVictimSystemSimulation;
import org.seamcat.simulation.result.PreSimulationResultsImpl;

import java.io.File;
import java.util.*;

/**
 * This class represents a SEAMCAT workspace. A workspace contains the
 * simulation scenario setup attributes for the system or systems to be
 * simulated, some simulation control attributes and the results.
 *
 * @author Christian Petersen
 */
public class Workspace {

    private static final Logger LOG = Logger.getLogger(Workspace.class);

    private String name;
    private boolean useUserDefinedDRSS;
    private Distribution userDefinedDRSS = Factory.distributionFactory().getConstantDistribution(0);

    private boolean hasBeenCalculated = false;
    private List<ICEConfiguration> iceConfigurations = new ArrayList<ICEConfiguration>();
    private List<InterferenceLink> interferenceLinks = new ArrayList<InterferenceLink>();

    private SimulationResult simulationResult;
    private ScenarioOutlineModel scenarioOutlineModel;

    private String victimSystemId;
    private List<IdElement<SystemModel>> systemModels;
    private List<InterferenceLinkElement> interferenceLinkUIs;
    private List<EventProcessingConfiguration> eventProcessingList;
    private SimulationControl simulationControl;
    private Distribution victimFrequency;
    private List<Distribution> interferingLinkFrequency = new ArrayList<>();

    private WorkspaceScenario scenario;

    private List<MigrationIssue> migrationIssues;
    private File path;
    private Simulation simulation;

    public String getFileExtension() {
        if ( isHasBeenCalculated() ) {
            return ".swr";
        } else {
            return ".sws";
        }
    }

    public Workspace() {
        eventProcessingList = new ArrayList<>();
        migrationIssues = new ArrayList<>();
    }

    public void addIceConfiguration(ICEConfiguration iceconf) {
        iceConfigurations.add(iceconf);
    }

    public List<ICEConfiguration> getIceConfigurations() {
        return iceConfigurations;
    }

    public List<InterferenceLink> getInterferenceLinks() {
        return interferenceLinks;
    }

    public SimulationResult getSimulationResults() {
        return simulationResult;
    }

    public void setSimulationResult( SimulationResult simulationResult ) {
        this.simulationResult = simulationResult;
    }

    public SystemSimulationModel getVictimSystemLink() {
        return scenario.getSystem(getVictimSystemId());
    }

    public boolean hasDMASubSystem() {
        if ( !( getVictimSystem() instanceof SystemModelGeneric)) return true;
        

        for (InterferenceLinkElement ui : interferenceLinkUIs) {
            if ( ! (getSystem(ui.getInterferingSystemId()) instanceof SystemModelGeneric)) return true;
           
        }
        return false;
    }

    public boolean isHasBeenCalculated() {
        return hasBeenCalculated;
    }

    public void setHasBeenCalculated(boolean hasBeenCalculated) {
        this.hasBeenCalculated = hasBeenCalculated;
    }

    public void removeIceConfiguration(ICEConfiguration iceconf) {
        iceConfigurations.remove(iceconf);
    }

    public void resetEventGeneration() {
        hasBeenCalculated = false;

        iceConfigurations.clear();
        addIceConfiguration(new ICEConfiguration());
    }

    public File simulate(SimulationListener listener) {
        hasBeenCalculated = true;

        simulation = new Simulation(this, scenario, listener);
        InterferenceSimulationEngine engine = new InterferenceSimulationEngine();
        if ( getSimulationControl().debugMode() ) {
            // for debug, run in a single thread pool to
            // avoid random interleaved output
            SimulationPool pool = new SimulationPool(1);
            simulationResult = engine.simulateInterference(simulation, pool);
            pool.destroy();
        } else {
            simulationResult = engine.simulateInterference(simulation, Model.getSimulationPool());
        }

        if ( simulationResult != null ) {
            // we are not cancelled
            setWorkspacePostSimulationState( this, simulation );
        }
        return simulation.getLogfile();
    }

    public SingleEventSimulationResult simulateSingle( long simulationSeed, int eventNumber ) {
        Workspace clone = WorkspaceCloneHelper.clone(this);
        WorkspaceScenario scenario = new WorkspaceScenario( clone );
        // copy pre-Sim-Results to new scenario
        Map<RadioSystem, PreSimulationResultsImpl> newPre = scenario.getPreSimulationResultsMap();
        Iterator<Map.Entry<RadioSystem, PreSimulationResultsImpl>> iterator = newPre.entrySet().iterator();
        Map<RadioSystem, PreSimulationResultsImpl> pre = this.scenario.getPreSimulationResultsMap();
        for (PreSimulationResultsImpl result : pre.values()) {
            iterator.next().setValue( result );
        }

        // set PreSimulated for DMA systems also!!!
        List<InterferenceLink> interferenceLinks = scenario.getInterferenceLinks();
        for (InterferenceLink link : interferenceLinks) {
            if ( link.getInterferingLink().isDMASystem() ) {
                PreSimulationResultsImpl results = scenario.getPreSimulationResults(link.getInterferingSystem());
                link.getInterferingLink().getDMASystem().setResults( results );
            }
            if ( link.getVictimLink().isDMASystem()) {
                PreSimulationResultsImpl results = scenario.getPreSimulationResults(link.getVictimSystem());
                link.getVictimLink().getDMASystem().setResults( results );
            }
        }

        Simulation simulation = new Simulation(clone, scenario) {
            protected boolean lastEvent(EventResult eventResult) {
                return true;
            }
        };
        simulation.setSimulationSeed( simulationSeed );
        EventResult single = null;
        try {
            simulation.preSimulationSingle();
            single = new InterferenceSimulationEngine().single(simulation.getVictimSystemSimulation(), eventNumber, simulation);
            setWorkspacePostSimulationState( clone, simulation);
            if ( single != null ) {
                // not cancelled
                simulation.postSimulationSingle();
            }
        } catch (RuntimeException e ) {
            LOG.error("Error during simulation", e);
        }
        File logfile = simulation.getLogfile();

        return new SingleEventSimulationResult(logfile, single, scenario, clone);
    }

    private void setWorkspacePostSimulationState(Workspace workspace, Simulation simulation) {
        VictimSystemSimulation victimSimulation = simulation.getVictimSystemSimulation();
        if ( victimSimulation instanceof CellularVictimSystemSimulation) {
            cellularVictimSimulation = simulation.getLastVictimState();
        }
        List<InterferenceLinkSimulation> iSims = simulation.getLastEventInterferenceLinkSimulations();
        List<InterferenceLinkElement> interferenceLinkUIs1 = workspace.getInterferenceLinkUIs();
        for (int i = 0; i < interferenceLinkUIs1.size(); i++) {
            InterferenceLinkSimulation iLinkSim = iSims.get(i);
            if ( iLinkSim instanceof CellularInterfererInterferenceLinkSimulation) {
                cellularInterferingSystemSimulation.put(i, ((CellularInterfererInterferenceLinkSimulation) iLinkSim).getSystem());
            }
        }
    }

    // Due to legacy, the cellular systems must be accessible for some of the UI Panels
    public AbstractDmaSystem cellularVictimSimulation;
    public Map<Integer, AbstractDmaSystem> cellularInterferingSystemSimulation = new HashMap<>();

    public void setScenarioOutlineModel(ScenarioOutlineModel scenarioOutlineModel) {
        this.scenarioOutlineModel = scenarioOutlineModel;
    }

    public ScenarioOutlineModel getScenarioOutlineModel() {
        return scenarioOutlineModel;
    }

    public List<EventProcessingConfiguration> getEventProcessingList() {
        return eventProcessingList;
    }

    public void setEventProcessingList(List<EventProcessingConfiguration> eventProcessingList) {
        this.eventProcessingList = eventProcessingList;
    }

    public void setScenario(WorkspaceScenario scenario) {
        this.scenario = scenario;
        Receiver receiver = scenario.getVictimSystem().getReceiver();
        if ( receiver instanceof GenericReceiver ) {
            for (ICEConfiguration iceconf : iceConfigurations) {
                iceconf.setAllowIntermodulation(((GenericReceiver) receiver).isIntermodulationRejectionOption());
            }
        }
    }

    public WorkspaceScenario getScenario() {
        return scenario;
    }

    public List<MigrationIssue> getMigrationIssues() {
        return migrationIssues;
    }

    public void setMigrationIssues(List<MigrationIssue> migrationIssues) {
        this.migrationIssues = migrationIssues;
    }

    public void addInterferenceLink( InterferenceLinkElement element, Distribution frequency ) {
        interferenceLinkUIs.add( element );
        interferingLinkFrequency.add( frequency );
    }

    public List<InterferenceLinkElement> getInterferenceLinkUIs() {
        return interferenceLinkUIs;
    }

    public void setInterferenceLinkUIs(List<InterferenceLinkElement> interferenceLinkUIs) {
        this.interferenceLinkUIs = interferenceLinkUIs;
    }

    public String getVictimSystemId() {
        return victimSystemId;
    }

    public void setVictimSystemId(String victimSystemId) {
        this.victimSystemId = victimSystemId;
    }

    public List<IdElement<SystemModel>> getSystemModels() {
        return systemModels;
    }

    public void setSystemModels(List<IdElement<SystemModel>> systemModels) {
        this.systemModels = systemModels;
    }

    public SystemModel getSystem(String id) {
        for (IdElement<SystemModel> systemModel : systemModels) {
            if ( systemModel.getId().equals(id)) return systemModel.getElement();
        }

        return null;
    }

    public SystemModel getVictimSystem() {
        return getSystem( victimSystemId );
    }

    public SimulationControl getSimulationControl() {
        return simulationControl;
    }

    public void setSimulationControl(SimulationControl simulationControl) {
        this.simulationControl = simulationControl;
    }

    public void prepareSimulate() {
        setSimulationResult(new SimulationResult());

        for (EventProcessingConfiguration configuration : eventProcessingList) {
            configuration.setId(UUID.randomUUID().toString());

            // create initial values for custom ui models
            CustomUITab tabs = (CustomUITab) configuration.getPluginClass().getAnnotation(CustomUITab.class);
            if ( tabs != null ) {
                for (Class<? extends CustomUI> aClass : tabs.value()) {
                    try {
                        CustomUI customUI = aClass.newInstance();
                        for (PanelDefinition definition : customUI.panelDefinitions()) {
                            configuration.getCustomUIState().get().put( definition, ProxyHelper.newInstance( definition.getModelClass()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public void createScenario() {
        scenario = new WorkspaceScenario(this);
    }

    public void prune() {
        Set<String> match = new HashSet<>();
        for (IdElement<SystemModel> model : systemModels) {
            match.add(model.getId());
        }

        match.remove(getVictimSystemId());
        for (InterferenceLinkElement ui : interferenceLinkUIs) {
            match.remove(ui.getInterferingSystemId());
        }

        for (String id : match) {
            systemModels.remove( new IdElement<SystemModel>(id, getSystem(id)));
        }
    }

    public void setPath(File path) {
        this.path = path;
    }

    public File getPath() {
        return path;
    }

    public Distribution getVictimFrequency() {
        return victimFrequency;
    }

    public void setVictimFrequency(Distribution victimFrequency) {
        this.victimFrequency = victimFrequency;
    }

    public List<Distribution> getInterferingLinkFrequency() {
        return interferingLinkFrequency;
    }

    public boolean isUseUserDefinedDRSS() {
        return useUserDefinedDRSS;
    }

    public void setUseUserDefinedDRSS(boolean useUserDefinedDRSS) {
        this.useUserDefinedDRSS = useUserDefinedDRSS;
    }

    public Distribution getUserDefinedDRSS() {
        return userDefinedDRSS;
    }

    public void setUserDefinedDRSS(Distribution userDefinedDRSS) {
        this.userDefinedDRSS = userDefinedDRSS;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}