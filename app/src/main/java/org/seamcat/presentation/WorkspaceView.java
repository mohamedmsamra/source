package org.seamcat.presentation;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.Subscriber;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.events.WorkspaceViewClosedEvent;
import org.seamcat.model.Workspace;
import org.seamcat.model.scenariocheck.ScenarioCheckResult;
import org.seamcat.model.scenariocheck.ScenarioCheckUtils;
import org.seamcat.model.workspace.SimulationControl;
import org.seamcat.objectutils.WorkspaceCloneHelper;
import org.seamcat.presentation.eventprocessing.EventProcessingPanel;
import org.seamcat.presentation.systems.ScenarioPanel;
import org.seamcat.presentation.systems.SystemsPanel;

import javax.swing.*;
import java.io.File;
import java.util.ResourceBundle;

import static org.seamcat.model.factory.Factory.*;

public class WorkspaceView extends JTabbedPane {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private SystemsPanel systems;
    private ScenarioPanel scenario;
    private EventProcessingPanel eventProcessingPanel;

    private Workspace originalWorkspace;
    private Workspace workspace;

    public String getWorkspaceName() {
        return workspace.getName();
    }

    public WorkspaceView duplicate() {
        updateModel();
        Workspace duplicate = WorkspaceCloneHelper.clone(workspace);
        duplicate.setName(duplicate.getName() + " - duplicate");
        return new WorkspaceView( duplicate );
    }

    public WorkspaceView(Workspace workspace) {
        super(SwingConstants.TOP);

        this.workspace = workspace;

        systems = new SystemsPanel( this, workspace );
        scenario = new ScenarioPanel(systems, workspace);
        scenario.register();

        add(STRINGLIST.getString("TAB_SYSTEMS"), systems);
        add(STRINGLIST.getString("TAB_SCENARIO"), scenario);
        eventProcessingPanel = new EventProcessingPanel(this);
        add("Event Processing", eventProcessingPanel);
        setSelectedIndex(0);

        Subscriber.subscribe(this);
        originalWorkspace = WorkspaceCloneHelper.clone(workspace);
    }

    public void updatePanels() {
        scenario.refreshFromModel();

    }

    public Workspace getWorkspace() {
        return workspace;
    }

    private void destroy() {
        Subscriber.unSubscribeDeep( this );

        systems = null;
        removeAll();
        EventBusFactory.getEventBus().publish(new WorkspaceViewClosedEvent(this));
        workspace = null;
    }

    public void updateModel() {
        // if called after destroy fields might be null
        scenario.updateModel();
        systems.updateModel();
        eventProcessingPanel.updateCurrentEditor();
    }

    private boolean check() {
        java.util.List<ScenarioCheckResult> results = ScenarioCheckUtils.checkWorkspace(workspace, false);
        return MainWindow.displayScenarioCheckResults(results, false, true, MainWindow.getInstance());
    }

    public void consistencyCheck() {
        updateModel();
        workspace.createScenario();
        java.util.List<ScenarioCheckResult> results = ScenarioCheckUtils.checkWorkspace(workspace, false);
        MainWindow.displayScenarioCheckResults(results, true, false, MainWindow.getInstance());
    }

    public boolean okToSimulate() {
        updateModel();
        workspace.createScenario();

        if (workspace.hasDMASubSystem() && workspace.getSimulationControl().numberOfEvents() > 1000) {
            switch (DialogHelper.simulationSettings()) {
                case (JOptionPane.OK_OPTION):
                    return prepareUIForSimulationNoCheck(false);

                case JOptionPane.NO_OPTION:
                    SimulationControl prototype = prototype(SimulationControl.class, workspace.getSimulationControl());
                    when( prototype.numberOfEvents() ).thenReturn( 100 );
                    workspace.setSimulationControl( build( prototype ));
                    workspace.createScenario();
                    return prepareUIForSimulationNoCheck(false);

                default:
                    return false;
            }
        }else{
            return prepareUIForSimulationNoCheck(false);
        }
    }

    public boolean prepareUIForSimulationNoCheck( boolean batch ) {
        if ( batch ) {
            updateModel();
        }
        return check();
    }

    public boolean match( Workspace workspace ) {
        return this.workspace == workspace;
    }

    public boolean dirty() {
        updateModel();
        return !WorkspaceCloneHelper.equals( workspace, originalWorkspace );
    }

    private boolean dirty;

    public void forceSave() {
        updateModel();
        saveWorkspace();
    }

    public boolean save() {
        dirty = dirty();
        if ( dirty ) {
            saveWorkspace();
            return true;
        }
        return true;
    }

    public boolean close() {
        dirty = dirty();
        boolean result;
        if ( !dirty ) {
            result = true;
        } else {
            result = closeNoResults();
        }
        if ( result ) {
            destroy();
        }
        return result;
    }

    private boolean closeNoResults() {
        int shouldBeSaved = DialogHelper.closeNoResults(workspace.getName());

        switch (shouldBeSaved) {
            case 0: // save with results
                saveWorkspace();
                return true;
            case 1: // no
                EventBusFactory.getEventBus().publish(new InfoMessageEvent(STRINGLIST.getString("CLOSE_WORKSPACE_NOT_SAVED")));

                return true;
            default: // cancel the close
                EventBusFactory.getEventBus().publish(new InfoMessageEvent(STRINGLIST.getString("CANCEL_CLOSE_OPERATION")));
                return false;
        }
    }

    public void saveWorkspaceAs(File file) {
        updateModel();
        workspace.setPath( file );
        String name = file.getName().substring(0, file.getName().lastIndexOf("."));
        workspace.setName(name);
        saveWorkspace();
    }

    private void saveWorkspace() {
        originalWorkspace = WorkspaceCloneHelper.clone( workspace );
        MainWindow.getInstance().saveWorkspace( workspace );
    }

    @Override
    public String toString() {
        return workspace.getName();
    }

}
