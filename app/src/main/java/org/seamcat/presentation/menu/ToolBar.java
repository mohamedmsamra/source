package org.seamcat.presentation.menu;

import org.seamcat.commands.*;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.*;
import org.seamcat.presentation.*;
import org.seamcat.presentation.batch.BatchSimulationView;
import org.seamcat.presentation.batch.BatchView;
import org.seamcat.presentation.builder.PanelBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

import static org.seamcat.presentation.menu.Menus.action;

public class ToolBar {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private JToolBar toolBar;
    private JButton newWorkspace;
    private JButton openWorkspace;
    private JButton saveWorkspace;
    private JButton reportWorkspace;
    private JButton importLibrary;
    private JButton exportLibrary;
    private JButton runEGE;
    private JButton stopEGE;
    private JButton checkConsistency;
    private JButton newBatchJob;
    private JButton compareVectors;

    public ToolBar() {
        EventBusFactory.getEventBus().subscribe(this);
    }

    public void createToolBar() {
        toolBar = new JToolBar();
        toolBar.setName(STRINGLIST.getString("TOOLBAR_TITLE_TEXT"));
        toolBar.setFocusable(false);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        createButtonNewWorkspace();
        createButtonBatchOperation();
        createButtonOpenWorkspace();
        createButtonSaveWorkspace();
        toolBar.addSeparator();
        createButtonImportLibrary();
        createButtonExportLibrary();
        toolBar.addSeparator();
        createButtonRunEGE();
        createButtonStopEGE();
        createButtonReport();
        createButtonCheckConsistency();
        toolBar.addSeparator();
        createMenuCompareVectors();
        createButtonCalculator();
        toolBar.addSeparator();
        createButtonHelp();
        createButtonBug();
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    private void enable(boolean enable) {
        newWorkspace.setEnabled(enable);
        newBatchJob.setEnabled(enable);
        openWorkspace.setEnabled(enable);
        saveWorkspace.setEnabled(enable);
        importLibrary.setEnabled(enable);
        exportLibrary.setEnabled(enable);
        compareVectors.setEnabled( enable );
    }

    @UIEventHandler
    public void handleShowToolBar(ShowToolBarCommand command) {
        toolBar.setVisible(command.isVisible());
    }

    public void handleSelectedComponent(Component component) {
        GlobalSimulationState state = MainWindow.getInstance().getSimulationState();
        if ( state == GlobalSimulationState.running ) {
            runEGE.setEnabled(false);
            stopEGE.setEnabled(true);
            reportWorkspace.setEnabled(false);
            checkConsistency.setEnabled(false);
        } else {
            reportWorkspace.setEnabled(true);
            if ( component instanceof SimulationView) {
                runEGE.setEnabled(false);
                checkConsistency.setEnabled(false);
                stopEGE.setEnabled(false);
            } else if ( component instanceof WorkspaceView ) {
                runEGE.setEnabled(true);
                checkConsistency.setEnabled(true);
                stopEGE.setEnabled(false);
            } else if ( component instanceof BatchView ) {
                runEGE.setEnabled(true);
                checkConsistency.setEnabled(true);
                stopEGE.setEnabled(false);
            } else if ( component instanceof BatchSimulationView) {
                runEGE.setEnabled(false);
                checkConsistency.setEnabled(false);
                stopEGE.setEnabled(false);
            }
        }

    }

    @UIEventHandler
    public void handleSimulationComplete(SimulationCompleteEvent event) {
        if (newWorkspace == null ) return;
        enable(true);
        stopEGE.setEnabled(false);
        reportWorkspace.setEnabled(true);
    }

    @UIEventHandler
    public void handleSimulationStarted(SimulationStartedEvent event) {
        if (newWorkspace == null ) return;
        enable(false);
        stopEGE.setEnabled(true);
    }

    @UIEventHandler
    // Mohamed Samra Edit: disable all the tools in the bar except opening of creating new Workspace
    public void handleEmptyRootPanel(WorkspacesPaneEmptyEvent event) {
        openWorkspace.setEnabled( false);
        newWorkspace.setEnabled( true );
        newBatchJob.setEnabled(true);
        importLibrary.setEnabled(true);
        exportLibrary.setEnabled( true );
        saveWorkspace.setEnabled(true);

        runEGE.setEnabled(true);
        checkConsistency.setEnabled(true);
        stopEGE.setEnabled(true);
        reportWorkspace.setEnabled(true);
    }

    @UIEventHandler
    public void handleNonEmptyRootPanel(WorkspacesPaneNonEmptyEvent event) {
        importLibrary.setEnabled(true);
        exportLibrary.setEnabled(true);
        newWorkspace.setEnabled(true);
       openWorkspace.setEnabled(true);
        saveWorkspace.setEnabled(true);
        newBatchJob.setEnabled(true);
        compareVectors.setEnabled( true );
    }

    @UIEventHandler
    public void handleCanBeStopped(EgeCanBeStoppedEvent event) {
        stopEGE.setEnabled(event.canBeStopped());
    }

    private void createButtonNewWorkspace() {
        newWorkspace = button("SEAMCAT_ICON_WORKSPACE_NEW", "MENU_ITEM_TEXT_NEW_WORKSPACE", NewWorkspaceCommand.class);
        toolBar.add(newWorkspace);
    }

    private void createButtonOpenWorkspace() {
        openWorkspace = button("SEAMCAT_ICON_WORKSPACE_OPEN", "MENU_ITEM_TEXT_OPEN_WORKSPACE", OpenWorkspaceCommand.class);
        toolBar.add(openWorkspace);
    }

    private void createButtonSaveWorkspace() {
        saveWorkspace = button("SEAMCAT_ICON_WORKSPACE_SAVE", "MENU_ITEM_TEXT_SAVE_WORKSPACE", SaveWorkspaceCommand.class);
        toolBar.add(saveWorkspace);
    }

    private void createButtonImportLibrary() {
        importLibrary = button("SEAMCAT_ICON_IMPORT_LIBRARY", "MENU_ITEM_TEXT_IMPORT_LIBRARY", ImportLibraryCommand.class);
        toolBar.add(importLibrary);
    }

    private void createButtonExportLibrary() {
        exportLibrary = button("SEAMCAT_ICON_EXPORT_LIBRARY", "MENU_ITEM_TEXT_EXPORT_LIBRARY", ExportLibraryCommand.class);
        toolBar.add(exportLibrary);
    }

    private void createButtonRunEGE() {
        runEGE = button("SEAMCAT_ICON_SIMULATION_START", "MENU_ITEM_TEXT_RUN_EGE", RunEGECommand.class);
        toolBar.add(runEGE);
    }

    private void createButtonStopEGE() {
        stopEGE = button("SEAMCAT_ICON_SIMULATION_STOP", "MENU_ITEM_TEXT_STOP_EGE", StopEGECommand.class);
        toolBar.add(stopEGE);
    }

    private void createButtonCheckConsistency() {
        checkConsistency = button("SEAMCAT_ICON_CHECK_CONSISTENCY", "MENU_ITEM_TEXT_CHECK_CONSISTENCY", CheckConsistencyCommand.class);
        toolBar.add(checkConsistency);
    }

    private void createButtonBatchOperation() {
        newBatchJob = button("SEAMCAT_ICON_NEW_BATCH", "MENU_ITEM_TEXT_NEW_BATCH_OPERATION", NewBatchCommand.class);
        toolBar.add(newBatchJob);
    }

    private void createButtonReport() {
        reportWorkspace = button("SEAMCAT_ICON_GENERATE_REPORT", "MENU_ITEM_TEXT_REPORT", GenerateReportCommand.class);
        toolBar.add(reportWorkspace);
    }

    private void createButtonHelp() {
        toolBar.add(button("SEAMCAT_ICON_HELP", "HELP_CONTENTS_MENU_ITEM_TEXT", DisplayHelpCommand.class));
    }

    private void createButtonBug() {
        toolBar.add(button("SEAMCAT_ICON_BUG_ERROR", "ERROR_REPORT_MENU_ITEM_TOOLTIP", DisplayReportErrorCommand.class));
    }


    private void createButtonCalculator() {
        toolBar.add(button("SEAMCAT_ICON_CALCULATOR", "TEST_CALCULATOR_MENU_ITEM_TEXT", DisplayTestCalculatorCommand.class));
    }

    private void createMenuCompareVectors() {
        compareVectors = button("SEAMCAT_ICON_INTERFERENCE_CALCULATIONS", "TEST_COMPARE_VECTORS_MENU_ITEM_TOOLTIP", DisplayCompareVectorCommand.class);
        toolBar.add( compareVectors );
    }


    public static JButton button(String icon, String toolTip, Object instance, String method ) {
        return PanelBuilder.addAction( button(icon, toolTip, null), instance, method );
    }
    public static JButton button(String icon, String toolTip, Class<?> command) {
        JButton button = new JButton();
        button.setIcon(SeamcatIcons.getImageIcon(icon, SeamcatIcons.IMAGE_SIZE_TOOLBAR));
        if (toolTip != null) {
            button.setToolTipText(STRINGLIST.getString(toolTip));
        }
        if (command != null) {
            button.addActionListener(action(command));
        }
        button.setFocusable(false);
        return button;
    }

    public static JButton button(String icon, String toolTip, Object command) {
        JButton button = new JButton();
        button.setIcon(SeamcatIcons.getImageIcon(icon, SeamcatIcons.IMAGE_SIZE_TOOLBAR));
        if (toolTip != null) {
            button.setToolTipText(STRINGLIST.getString(toolTip));
        }
        if (command != null) {
            button.addActionListener(action(command));
        }
        button.setFocusable(false);
        return button;
    }
}
