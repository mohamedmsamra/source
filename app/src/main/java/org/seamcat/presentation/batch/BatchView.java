package org.seamcat.presentation.batch;

import org.seamcat.batch.BatchJobList;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.factory.Model;
import org.seamcat.model.Workspace;
import org.seamcat.model.scenariocheck.ScenarioCheckResult;
import org.seamcat.model.scenariocheck.ScenarioCheckUtils;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.objectutils.WorkspaceCloneHelper;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.LoadedWorkspace;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.WorkspaceView;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BatchView extends JPanel {

    private final BatchJobList list;
    private BatchToolBar toolBar;
    private GenericPanelEditor<Description> idPanel;
    private BatchWorkspacesView workspaces;

    private BatchViewState state;

    public BatchView( String name, String description ) {
        this(new BatchJobList(name, description));
        list.setAbsoluteLocation(Model.getWorkspacePath() + name + ".sbj");
    }

    public String getTitle() {
        return list.getDescription().name();
    }

    public void setTitle(String name) {
        list.setDescription(new DescriptionImpl(name, list.getDescription().description()));
    }

    public BatchView( BatchJobList list) {
        super(new BorderLayout());
        this.list = list;
        state = list.getViewState();

        toolBar = new BatchToolBar(this);
        workspaces = new BatchWorkspacesView(this, list);
        Description prototype = Factory.prototype(Description.class);
        Factory.when(prototype.name()).thenReturn(list.getDescription().name());
        Factory.when(prototype.description()).thenReturn(list.getDescription().description());
        idPanel = new GenericPanelEditor<>( MainWindow.getInstance(), Description.class, Factory.build( prototype ));
        JPanel top = new JPanel(new GridLayout(1, 2));

        top.add(new BorderPanel(idPanel, "Identification"));
        top.add(new BorderPanel(toolBar, "Control"));

        add(top, BorderLayout.NORTH);
        add( new BorderPanel(workspaces, "Workspaces"), BorderLayout.CENTER );

    }

    public boolean isIncrementalSave() {
        return list.getIncrementalSave();
    }

    public void setIncrementalSave( boolean incrementalSave ) {
        list.setIncrementalSave( incrementalSave );
    }

    protected void handleNewWorkspace() {
        workspaces.addView( new WorkspaceView( MainWindow.getInstance().openDefaultWorkspace()) );
    }

    protected void handleOpenWorkspace() {
        MainWindow.getInstance().openWorkspace(new LoadedWorkspace() {
            @Override
            public void loadedWorkspace(Workspace workspace) {
                workspaces.addView( new WorkspaceView( workspace));
            }
        });
    }

    protected void handleDuplicateWorkspace() {
        workspaces.duplicateSelected();
    }

    protected void handleExportWorkspace() {
        workspaces.exportSelected();
    }

    protected void selectedTab( WorkspaceView view) {
        toolBar.updateEnablement( view );
    }


    public boolean okToSimulate() {
        workspaces.updateModel();

        for (Workspace workspace : list.getBatchJobs()) {
            workspace.createScenario();
            java.util.List<ScenarioCheckResult> results = ScenarioCheckUtils.checkWorkspace(workspace, true);
            if (!MainWindow.displayScenarioCheckResults(results, false, true, MainWindow.getInstance())) {
                return false;
            }
        }
        return true;
    }


    public boolean dirty() {
        BatchViewState current = list.getViewState();
        if ( state.getName().equals(current.getName())
                && state.getDescription().equals(current.getDescription())
                && state.isIncrementalSave() == current.isIncrementalSave()) {
            if ( state.getWorkspaces().size() == current.getWorkspaces().size() ) {
                List<Workspace> wss = state.getWorkspaces();
                for (int i = 0; i < wss.size(); i++) {
                    if ( !(WorkspaceCloneHelper.equals(wss.get(i), current.getWorkspaces().get(i)))) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    public boolean close() {
        if ( dirty() ) {
            int result = DialogHelper.closeDirtyBatch(list.getDescription().name());
            if ( result == JOptionPane.CANCEL_OPTION ) {
                return false;
            } else if ( result == JOptionPane.NO_OPTION ) {
                return true;
            } else {
                // save and then close
                save();
                return true;
            }
        }
        return true;
    }

    public void save() {
        workspaces.updateModel();

        BatchJobList list = workspaces.getBachJob();
        BatchIOHandler.save( list  );
        state = list.getViewState();
    }

    public BatchJobList getList() {
        return list;
    }

    public void consistencyCheck() {
        workspaces.updateModel();
        List<ScenarioCheckResult> results = new ArrayList<>();
        for (Workspace workspace : list.getBatchJobs()) {
            workspace.createScenario();
            results.addAll( ScenarioCheckUtils.checkWorkspace(workspace,true) );
        }
        MainWindow.displayScenarioCheckResults(results, true, false, MainWindow.getInstance());
    }
}
