package org.seamcat.presentation.batch;

import org.seamcat.batch.BatchJobList;
import org.seamcat.model.Workspace;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.WorkspaceView;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BatchWorkspacesView extends JTabbedPane {

    private final BatchJobList list;

    public BatchWorkspacesView(final BatchView view, BatchJobList list) {
        this.list = list;
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                WorkspaceView ws = (WorkspaceView) getSelectedComponent();
                view.selectedTab(ws);
            }
        });

        for (Workspace workspace : list.getBatchJobs()) {
            add(new WorkspaceView(workspace), workspace.getName());
        }
        if ( getTabCount() > 0 ) {
            setSelectedIndex( 0 );
        }
    }


    protected void addView( final WorkspaceView view ) {
        list.addBatchJob(view.getWorkspace());
        addTab(view.getWorkspaceName(), view);
        JPanel tab = MainWindow.closableTab(view.getWorkspaceName(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( view.close() ) {
                    list.remove( view.getWorkspace() );
                    remove(view);
                }
            }
        });
        setTabComponentAt( getTabCount()-1, tab );
        setSelectedIndex( getTabCount()-1 );
    }

    public void exportSelected() {
        WorkspaceView view = (WorkspaceView) getSelectedComponent();
        if ( view != null ) {
            view.forceSave();
        }
    }

    public void duplicateSelected() {
        WorkspaceView view = (WorkspaceView) getSelectedComponent();
        if (view != null) {
            addView( view.duplicate() );
        }
    }

    public BatchJobList getBachJob() {
        return list;
    }

    public void updateModel() {
        for (int i=0; i<getTabCount(); i++) {
            ((WorkspaceView)getComponentAt(i)).updateModel();
        }
    }
}
