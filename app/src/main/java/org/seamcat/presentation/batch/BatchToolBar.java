package org.seamcat.presentation.batch;

import org.seamcat.presentation.WorkspaceView;
import org.seamcat.presentation.menu.ToolBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BatchToolBar extends JPanel {
    private final JCheckBox jCheckBox;
    private JButton export,newWs,open,duplicate;

    public BatchToolBar(final BatchView view) {
        setLayout(new BorderLayout());
        add(createWorkspaceToolbar(), BorderLayout.NORTH);
        defaultToolBar();
        newWs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                view.handleNewWorkspace();
            }
        });

        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                view.handleOpenWorkspace();
            }
        });

        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                view.handleExportWorkspace();
            }
        });

        duplicate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                view.handleDuplicateWorkspace();
            }
        });
        jCheckBox = new JCheckBox("Automatically save data when a workspace completes simulation");
        jCheckBox.setSelected( view.isIncrementalSave() );
        jCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                view.setIncrementalSave( jCheckBox.isSelected());
            }
        });
        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jPanel.add(jCheckBox);
        add(jPanel, BorderLayout.SOUTH);
    }

    private JPanel createWorkspaceToolbar() {
        JPanel bar = new JPanel( new FlowLayout(FlowLayout.LEFT));
        newWs = ToolBar.button("SEAMCAT_ICON_WORKSPACE_NEW", "MENU_ITEM_TEXT_NEW_WORKSPACE", null);
        bar.add(newWs);
        open = ToolBar.button("SEAMCAT_ICON_WORKSPACE_OPEN", "MENU_ITEM_TEXT_OPEN_WORKSPACE", null);
        bar.add(open);
        export = ToolBar.button("SEAMCAT_ICON_EXPORT_LIBRARY", "MENU_ITEM_TEXT_EXPORT_WORKSPACE", null);
        bar.add(export);
        duplicate = ToolBar.button("SEAMCAT_ICON_DUPLICATE", "MENU_ITEM_TEXT_DUPLICATE_WORKSPACE", null);
        bar.add( duplicate );
        return bar;
    }

    private void defaultToolBar() {
        export.setEnabled(false);
        duplicate.setEnabled( false );
        open.setEnabled( true );
        newWs.setEnabled( true );
    }

    public void updateEnablement(WorkspaceView view) {
        boolean enable = view != null;
        export.setEnabled( enable );
        duplicate.setEnabled( enable );

        open.setEnabled(true);
        newWs.setEnabled( true );
    }
}