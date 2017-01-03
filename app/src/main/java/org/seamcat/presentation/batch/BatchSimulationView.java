package org.seamcat.presentation.batch;

import org.seamcat.batch.BatchJobList;
import org.seamcat.model.factory.Model;
import org.seamcat.model.Workspace;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.SimulationView;
import org.seamcat.presentation.compareVector.WorkspaceVectors;
import org.seamcat.presentation.components.BorderPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BatchSimulationView extends JTabbedPane {

    private final BatchResultsPanel resultsPanel;
    private List<SimulationView> views = new ArrayList<>();
    private BatchStatusPanel statusPanel;
    private BatchJobList jobList;
    private boolean incrementalSave = false;

    public List<WorkspaceVectors> getResultVectors() {
        List<WorkspaceVectors> vectors = new ArrayList<>();
        for (Workspace workspace : jobList.getBatchJobs()) {
            vectors.add( new WorkspaceVectors(workspace.getName(), workspace.getSimulationResults()));
        }
        return vectors;
    }

    public void setTitle( String name ) {
        jobList.setDescription( new DescriptionImpl(name, jobList.getDescription().description()));
    }

    public String getTitle( ){
        return jobList.getDescription().name();
    }

    public BatchJobList getJobList() {
        return jobList;
    }

    public BatchSimulationView( BatchJobList jobList ) {
        this.jobList = jobList;
        for (Workspace ws : jobList.getBatchJobs()) {
            SimulationView view = new SimulationView(ws);
            views.add(view);
        }

        statusPanel = new BatchStatusPanel();
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add( new BorderPanel(statusPanel, "Batch status"), BorderLayout.NORTH );
        resultsPanel = new BatchResultsPanel(jobList);
        jPanel.add(resultsPanel);
        add(jPanel, "Results");

        for (SimulationView view : views) {
            add(view, "Results[" + view.getWorkspace().getName() + "]");
        }
    }

    public void setIncrementalSave( boolean incrementalSave ) {
        this.incrementalSave = incrementalSave;
    }

    public boolean dirty() {
        for (SimulationView view : views) {
            if ( view.dirty() ) {
                return true;
            }
        }
        return false;
    }


    public boolean close() {
        if ( dirty() ) {
            int result = DialogHelper.closeDirtyBatch(getJobList().getDescription().name());
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

    public void startSimulation() {
        int steps = views.size() + 1;
        if ( incrementalSave ) {
            steps += views.size();
        }
        statusPanel.initialize(steps);
        for (SimulationView view : views) {
            statusPanel.updateStatus( "Simulating: " + view.getWorkspace().getName());
            view.startSimulation();
            if ( incrementalSave ) {
                statusPanel.updateStatus("Saving: " + view.getWorkspace().getName());
                // save workspace directly here
                try {
                    // we have to wait for workspace positions to be added
                    // find a fix for that, i.e. do not set workspace data
                    // through a SwingUtil.invokeLater
                    Thread.sleep(500);
                } catch (InterruptedException e ) {
                    e.printStackTrace();
                }
                Model.saveWorkspace( view.getWorkspace() );
            }
        }
        complete();
        resultsPanel.render();
    }

    public void complete() {
        statusPanel.complete();
    }

    public void save() {
        if ( !jobList.hasLocation() ) {
            jobList.setAbsoluteLocation(Model.getWorkspacePath() + jobList.getDescription().name() + ".sbr");
        }
        BatchIOHandler.save(jobList);
    }

    public boolean isSimulating() {
        for (SimulationView view : views) {
            if ( view.isEgeRunning() ) return true;
        }

        return false;
    }
}
