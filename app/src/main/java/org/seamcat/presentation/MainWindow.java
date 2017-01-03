package org.seamcat.presentation;

import org.apache.log4j.Logger;
import org.seamcat.batch.BatchJobList;
import org.seamcat.calculator.Calculator;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.commands.*;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.*;
import org.seamcat.exception.SimulationInvalidException;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.interfaces.Dispatcher;
import org.seamcat.interfaces.ImportLibraryVisitor;
import org.seamcat.marshalling.LibraryMarshaller;
import org.seamcat.migration.BackwardMigrationNotSupportedException;
import org.seamcat.migration.settings.SettingsMigrator;
import org.seamcat.model.Library;
import org.seamcat.model.MigrationIssue;
import org.seamcat.model.Workspace;
import org.seamcat.model.factory.DataSaver;
import org.seamcat.model.factory.Model;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.scenariocheck.ScenarioCheckResult;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.types.*;
import org.seamcat.objectutils.WorkspaceCloneHelper;
import org.seamcat.plugin.JarConfigurationModel;
import org.seamcat.presentation.batch.BatchIOHandler;
import org.seamcat.presentation.batch.BatchSimulationView;
import org.seamcat.presentation.batch.BatchView;
import org.seamcat.presentation.compareVector.CompareVectorDialog;
import org.seamcat.presentation.compareVector.WorkspaceVectors;
import org.seamcat.presentation.emissiontest.EmissionMaskTestDialog;
import org.seamcat.presentation.library.LibraryExportDialog;
import org.seamcat.presentation.library.LibraryImportDialog;
import org.seamcat.presentation.library.LibraryItemListPanel;
import org.seamcat.presentation.menu.Menus;
import org.seamcat.presentation.menu.ToolBar;
import org.seamcat.presentation.propagationtest.PropagationTestPanel;
import org.seamcat.presentation.report.ReportDialog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.seamcat.eventbus.EventBusFactory.getEventBus;

/**
 * <p>
 * This is the main window in the SEAMCAT graphical user interface. The "look
 * and feel" of the user interface depends on the platform the application is
 * running under. By default the main window tries to adopt the "look and feel"
 * of the platform, but if this is not possible, the java "look and feel" theme,
 * called "Metal", is used.
 * </p>
 *
 * @author Christian Petersen (iPeople Aps)
 */
public final class MainWindow extends JFrame {

    private static final Logger LOG = Logger.getLogger(MainWindow.class);
    public FileDialogHelper fileDialogHelper = new FileDialogHelper();
    private static final String ROOTID = "CLIENT_MODE";
    private static final String SPLASH = "SPLASH";
    private static boolean busySaving = false;
   // public static volatile MainWindow singleton = null;

    private static final MainWindow singleton = new MainWindow();
    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private RecentlyUsed recentlyUsed;
    private Welcome welcome;
    public static final ExecutorService singleThreadPool = Executors.newFixedThreadPool(1);
    private static final ExecutorService fileSaver = Executors.newFixedThreadPool(1);


    private final Calculator calculator = new Calculator(this);
    private DistributionTestDialog distributionTest;

    private EmissionMaskTestDialog functionTest;
    private PropagationTestPanel propagationTestPanel;
    private final CardLayout rootLayout = new CardLayout();
    private final JPanel root = new JPanel(rootLayout);
    private final JTabbedPane workspacesPane = new JTabbedPane(SwingConstants.TOP);
    private final StatusBar statusBar = new StatusBar();
    private ToolBar toolBar = new ToolBar();
    private Menus menus = new Menus();
    private GlobalSimulationState simulationState = GlobalSimulationState.not_running;
    private Future<?> simulation;

    /**
     * Iterates a list of ScenarioCheckResults and displays any contained
     * error-messages provided that one or more "outcomes" were not OK.
     *
     * @param results
     *           List of ScenarioCheckResults
     * @param displayIfOk
     *           d
     * @param askEge
     *           true if user should be asked if EGE should run regardless of
     *           errors
     * @param component
     *           Component to serve as parent for the errormessage dialog
     * @return True if user accepts to run EGE despite the errors or if askEge is
     *         false
     */
    public static boolean displayScenarioCheckResults(List<ScenarioCheckResult> results, boolean displayIfOk, boolean askEge, Component component) {
        boolean runEge;
        boolean outcomeOk = true;

        // Create message string
        StringBuffer messages = new StringBuffer();
        for (ScenarioCheckResult result : results) {
            if (result.getOutcome() != ScenarioCheckResult.Outcome.OK) {
                outcomeOk &= false;
                for (String message : result.getMessages()) {
                    messages.append("<li><u>").append(result.getCheckName());
                    messages.append(":</u> ").append(message).append("</li>");
                }
            }
        }

        // Display error dialogs
        if (!outcomeOk) {
            if (askEge) {
                int ans = DialogHelper.consistencyError(component, messages);
                runEge = ans == JOptionPane.YES_OPTION;
            } else {
                DialogHelper.consistencyErrorPre(component, messages );
                runEge = false;
            }
        } else {
            if (displayIfOk) {
                DialogHelper.consistencyOk(component);
            }
            runEge = true;
        }
        return runEge;
    }

    public static MainWindow getInstance() {
    	   
    	
        return singleton;
    }
    
    
    public static void reset() {
    	
        
    	singleton.root.setEnabled(false);
    	 LOG.debug("Model initiated successfully");
    }

    @UIEventHandler
    public void handleSimulationCompleted(SimulationCompleteEvent event) {
        simulationState = GlobalSimulationState.not_running;
        setCursorDefault();
    }

    @UIEventHandler
    public void handleDisplayConfiguration(DisplayConfigurationCommand command) {
        new DialogOptions(this).setVisible(true);
    }

    @UIEventHandler
    public void handleCloseApplication(CloseApplicationCommand command) {
        for (Component component : allViews()) {
            if ( component instanceof WorkspaceView ) {
                if ( !((WorkspaceView) component).close()) {
                    return;
                }
            } else if ( component instanceof SimulationView ) {
                if ( !((SimulationView) component).close()) {
                    return;
                }
            } else if ( component instanceof BatchView ) {
                if ( !((BatchView) component).close()) {
                    return;
                }
            } else if ( component instanceof BatchSimulationView ) {
                if ( !((BatchSimulationView) component).close()) {
                    return;
                }
            }
        }

        Model.getInstance().persist();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saved model");
        }
        while (busySaving) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                // Do nothing
            }
        }

        DataSaver.stop();
        Model.getSimulationPool().destroy();
        System.exit(0);
    }

    @UIEventHandler
    public void handleShowHelp(DisplayHelpCommand command) {
        SeamcatHelpResolver.showHelp(this);
    }

    @UIEventHandler
    public void handleShowInterferingLinkHelp(DisplayHelpNameCommand command) {
        SeamcatHelpResolver.showHelp(command.getName());
    }

    @UIEventHandler
    public void handleNewBatch( NewBatchCommand command ) {
        String name = "Batch " + WorkspaceCount.getAndUpdateBatchCount();
        addWorkspaceViewToPanel(new BatchView(name, ""), name);
    }

    @UIEventHandler
    public void handleDuplicateWorkspace(DuplicateWorkspaceCommand command) {
        WorkspaceView view = currentWorkspaceView();
        if ( view != null ) {
            addWorkspaceViewToPanel(view.duplicate(), view.getWorkspaceName());
        }
    }

    @UIEventHandler
    public void handlePropagationTest(DisplayPropagationTestCommand command) {
        if ( propagationTestPanel == null ) {
            propagationTestPanel = new PropagationTestPanel(this);
        }
        propagationTestPanel.setVisible(true);
    }

    @UIEventHandler
    public void handleTestFunctions(DisplayTestFunctionsCommand command) {
        if  ( functionTest == null ) {
            functionTest = new EmissionMaskTestDialog(this);
        }
        functionTest.setVisible(true);
    }

    @UIEventHandler
    public void handleTestCalculator(DisplayTestCalculatorCommand command) {
        calculator.setVisible(true);
    }

    @UIEventHandler
    public void handleReportError(DisplayReportErrorCommand command) {
        DialogHelper.reportError();
    }

    @UIEventHandler
    public void handleCompareVector(DisplayCompareVectorCommand command) {
        List<WorkspaceVectors> vectorList = new ArrayList<WorkspaceVectors>();
        for (SimulationView view : simulationViews()) {
            vectorList.add( view.getResultVectors() );
        }
        for ( BatchSimulationView view : batchSimulationViews()) {
            vectorList.addAll( view.getResultVectors() );
        }
        new CompareVectorDialog(MainWindow.this, vectorList).showDialog();
    }

    @UIEventHandler
    public void handleAbout(DisplayAboutCommand command) {
        DialogHelper.about();
    }

    @UIEventHandler
    public void handleDistributionTest(DisplayDistributionTestCommand command) {
        if ( distributionTest == null ) {
            distributionTest = new DistributionTestDialog(this);
        }
        distributionTest.setVisible(true);
    }

    
    // this is the part responsible for creating new workspace when a button is clicked in drop-down menu
    @UIEventHandler
    public void handleNewWorkspace(NewWorkspaceCommand command) {
        Workspace workspace = openDefaultWorkspace();
        addWorkspaceViewToPanel(new WorkspaceView(workspace), workspace.getName());
    }

    public Workspace openDefaultWorkspace() {
        long begin = System.currentTimeMillis();
        setCursorBusy();
        try {
            Workspace workspace = Model.openDefaultWorkspace();
            
            workspace.setName( workspace.getName() + " " + WorkspaceCount.getAndUpdateCount());
            return workspace;
        } finally {
            EventBusFactory.getEventBus().publish( new InfoMessageEvent( String.format("Loaded workspace in %d millis",System.currentTimeMillis() - begin)));
            setCursorDefault();
        }
    }

    @UIEventHandler
    public void handleOpenWorkspace(OpenWorkspaceCommand command) {
        openWorkspace(new LoadedWorkspace());
    }

    @UIEventHandler
    public void handleSaveWorkspace(SaveWorkspaceCommand command) {
        currentView(new CurrentView() {
            public void current(WorkspaceView view) {
                view.save();
            }

            public void current(SimulationView view) {
                view.save();
            }

            public void current(BatchView view) {
                view.save();
            }

            public void current(BatchSimulationView view) {
                view.save();
            }
        });
    }

    @UIEventHandler
    public void handleSaveWorkspaceAs(SaveWorkspaceAsCommand command) {
        currentView(new CurrentView() {
            public void current(WorkspaceView view) {
                File file = saveWorksaceAs(FileFilters.FILE_FILTER_WORKSPACE);
                if (file != null) {
                    view.saveWorkspaceAs(file);
                    alignTabTitle(view);
                }
            }

            public void current(SimulationView view) {
                File file = saveWorksaceAs(FileFilters.FILE_FILTER_WORKSPACE_RESULT);
                if (file != null) {
                    view.saveWorkspaceAs(file);
                    alignTabTitle(view);
                }
            }

            @Override
            public void current(BatchView view) {
                File file = saveWorksaceAs(FileFilters.FILE_FILTER_BATCH);
                if (file != null) {
                    view.getList().setAbsoluteLocation(file.getAbsolutePath());
                    String withExtension = file.getName();
                    view.setTitle(withExtension.substring(0, withExtension.length() - 4));
                    view.save();
                    alignTabTitle(view);
                }
            }

            @Override
            public void current(BatchSimulationView view) {
                File file = saveWorksaceAs(FileFilters.FILE_FILTER_BATCH_RESULT);
                if (file != null) {
                    view.getJobList().setAbsoluteLocation(file.getAbsolutePath());
                    String withExtension = file.getName();
                    view.setTitle(withExtension.substring(0, withExtension.length() - 4));
                    view.save();
                    alignTabTitle(view);
                }
            }
        });
    }

    @UIEventHandler
    public void handleCloseAll( CloseAllWorkspaceCommand command ) {
        for (Component component : workspacesPane.getComponents()) {
            closeComponent( component );
        }
    }

    @UIEventHandler
    public void handleCloseOthers( CloseOthersWorkspaceCommand command ) {
        Component selected = workspacesPane.getSelectedComponent();
        for (Component component : workspacesPane.getComponents()) {
            if ( component != selected) {
                closeComponent( component );
            }
        }
    }

    @UIEventHandler
    public void handleCloseUnmodified( CloseUnmodifiedWorkspaceCommand command ) {
        for (Component component : workspacesPane.getComponents()) {
            if ( component instanceof WorkspaceView) {
                if (!((WorkspaceView) component).dirty() ) {
                    closeComponent(component);
                }
            } else if ( component instanceof SimulationView) {
                if ( !((SimulationView) component).dirty()) {
                    closeComponent(component);
                }
            } else if ( component instanceof BatchView) {
                if ( !((BatchView) component).dirty() ) {
                    closeComponent(component);
                }
            } else if ( component instanceof BatchSimulationView) {
                if ( !((BatchSimulationView) component).dirty() ) {
                    closeComponent(component);
                }
            }
        }
    }

    private void closeComponent(Component component ) {
        if ( component instanceof WorkspaceView) {
            if (((WorkspaceView) component).close()) {
                removeComponent( component );
            }
        } else if ( component instanceof SimulationView) {
            if ( ((SimulationView) component).close() ){
                removeComponent( component );
            }
        } else if ( component instanceof BatchView) {
            if ( ((BatchView) component).close() ) {
                removeComponent(component);
            }
        } else if ( component instanceof BatchSimulationView) {
            if (((BatchSimulationView) component).close()) {
                removeComponent(component);
            }
        }
    }

    private CurrentView componentClose = new CurrentView() {
        public void current(WorkspaceView view) {
            if ( view.close() ) {
                removeComponent( view );
            }
        }
        public void current(SimulationView view) {
            if ( view.close() ) {
                removeComponent( view );
            }
        }
        public void current(BatchView view) {
            if ( view.close() ) {
                removeComponent( view );
            }
        }
        public void current(BatchSimulationView view) {
            if ( view.close() ) {
                removeComponent(view);
            }
        }
    };

    @UIEventHandler
    public void handleCloseWorkspace(CloseWorkspaceCommand command) {
        currentView(componentClose);
        System.gc();
    }


    @UIEventHandler
    public void handleImportLibrary(ImportLibraryCommand command) {
        if (fileDialogHelper.importLibrary(this).selectionMade()) {
            try {
                File settingsFile = fileDialogHelper.getSelectedFile();
                new SettingsMigrator().migrateAndShuffleSettingsFiles(settingsFile, Model.getInstance().getPrehistoricSettingsFile());
                Library imported = new LibraryMarshaller().importLibrary(settingsFile);
                LibraryImportDialog dialog = new LibraryImportDialog(this, imported);
                if ( selectImportLibraries(dialog) ) {
                    List<LibraryItem> selected = dialog.selectedItems();
                    ImportLibraryVisitor importer = new ImportLibraryVisitor();
                    for (LibraryItem identifiable : selected) {
                        Dispatcher.dispatch(importer, identifiable);
                    }
                    Model.getInstance().persist();
                    EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format(STRINGLIST.getString("IMPORT_LIBRARY_OK"), settingsFile.getAbsolutePath())));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                LOG.error(exception);
                DialogHelper.invalidFile();
            }
        }
    }

    private boolean selectImportLibraries( LibraryImportDialog dialog ) {
        boolean selecting = true;
        while (selecting ) {
            if ( dialog.display() ) {
                List<LibraryItem> duplicateNames = dialog.findDuplicateNames();
                if ( !duplicateNames.isEmpty() ) {
                    // show error dialog
                    DialogHelper.importLibraryNameConflict( duplicateNames );
                } else {
                    selecting = false;
                }
            } else {
                selecting = false;
            }
        }
        return dialog.isAccept();
    }

    @UIEventHandler
    public void handleExportLibrary(ExportLibraryCommand command) {
        LibraryExportDialog dialog = new LibraryExportDialog(this);
        if ( dialog.display() ) {
            if (fileDialogHelper.exportLibrary(this).selectionMade()) {
                File f = fileDialogHelper.getSelectedFile();
                if (!f.getName().toUpperCase().endsWith(".SLI")) {
                    f = new File(f.getAbsolutePath() + ".sli");
                }
                new LibraryMarshaller().exportLibrary(dialog.selectedItems(), f);
                EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format(STRINGLIST.getString("SAVE_LIBRARY_DONE"), f.getAbsolutePath())));
            }
        }
    }


    @UIEventHandler
    public void handleStopEGE( StopEGECommand command ) {
        if ( simulation != null ) {
            simulation.cancel(true);
        }
        simulationState = GlobalSimulationState.not_running;
    }

    @UIEventHandler
    public void handleRunEGE(RunEGECommand command) {
        currentView(new CurrentView() {
            public void current(BatchView view) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (view.okToSimulate()) {
                    BatchJobList jobList = view.getList();
                    // clone
                    BatchJobList clone = new BatchJobList(jobList.getDescription().name(), jobList.getDescription().description());
                    for (Workspace ws : jobList.getBatchJobs()) {
                        clone.addBatchJob(cloneForSimulation(ws));
                    }
                    if (jobList.hasLocation()) {
                        File file = new File(jobList.getAbsoluteLocation());
                        clone.setAbsoluteLocation(file.getParentFile().getAbsolutePath() + File.separator + jobList.getDescription().name() + ".sbr");
                    }
                    final BatchSimulationView batchView = new BatchSimulationView(clone);
                    batchView.setIncrementalSave(view.isIncrementalSave());
                    simulationState = GlobalSimulationState.running;
                    addWorkspaceViewToPanel(batchView, "Results[" + clone.getDescription().name() + "]");
                    getEventBus().publish(new SimulationStartedEvent(null));
                    simulation = singleThreadPool.submit(new Runnable() {
                        public void run() {
                            try {
                                batchView.startSimulation();
                            } catch (SimulationInvalidException e) {
                                EventBusFactory.getEventBus().publish(new SimulationErrorEvent(batchView, e));
                            }
                        }
                    });
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
/////////////////////////////////////////////
            ///////////////////////
            //////////////////////
            ////////////////////
            public void current(WorkspaceView view) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (view.okToSimulate()) {
                    Workspace clone = cloneForSimulation(view.getWorkspace());
                    final SimulationView simulationView = new SimulationView(clone);
                    simulationView.setIsRunning(true);
                    simulationState = GlobalSimulationState.running;
                    addWorkspaceViewToPanel(simulationView, "Results[" + clone.getName() + "]");
                    getEventBus().publish(new SimulationStartedEvent(null));
                    simulation = singleThreadPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                simulationView.startSimulation();
                            } catch (SimulationInvalidException e) {
                                EventBusFactory.getEventBus().publish(new SimulationErrorEvent(simulationView, e));
                            }
                        }
                    });
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    private Workspace cloneForSimulation( Workspace workspace ) {
        Workspace clone = WorkspaceCloneHelper.clone(workspace);
        clone.prune();
        clone.setScenario(workspace.getScenario());
        clone.getInterferenceLinks().addAll( workspace.getInterferenceLinks() );
        clone.prepareSimulate();
        return clone;
    }

    @UIEventHandler
    public void handleSimulationError( SimulationErrorEvent event ) {
        // Force close view
        workspacesPane.remove(event.getComponent());

        // Display error message
        DialogHelper.simulationError(event.getE());
        LOG.error("Simulation Error", event.getE().getOrigin());
        setCursor(Cursor.getDefaultCursor());
        EventBusFactory.getEventBus().publish(new SimulationCompleteEvent());
        simulationState = GlobalSimulationState.not_running;
        toolBar.handleSelectedComponent(workspacesPane.getSelectedComponent());
    }


    @UIEventHandler
    public void handleCheckConsistency(CheckConsistencyCommand command) {
        currentView(new CurrentView() {
            public void current(WorkspaceView view) {
                view.consistencyCheck();
            }

            public void current(BatchView view) {
                view.consistencyCheck();
            }
        });
    }

    @UIEventHandler
    public void handleGenerateReport(GenerateReportCommand command) {
        final ReportDialog dialog = new ReportDialog();

        currentView(new CurrentView() {
            public void current(SimulationView view) {
                dialog.setReportSource(view.getWorkspace());
                dialog.setVisible(true);
            }

            public void current(BatchSimulationView view) {
                dialog.setReportSource(view.getJobList());
                dialog.setVisible(true);
            }

            public void current(WorkspaceView view) {
                dialog.setReportSource(view.getWorkspace());
                dialog.setVisible(true);
            }

            public void current(BatchView view) {
                dialog.setReportSource(view.getList());
                dialog.setVisible(true);
            }
        });
    }

    @UIEventHandler
    public void handleDisplayTransmitterLibrary(DisplayTransmitterLibraryCommand command) {
        new LibraryItemListPanel<TransmitterModel>(this, TransmitterModel.class, "Transmitter library", "LIBRARY_TRANSMITTER_LIST_TITLE", 1300, 600, 1).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplaySpectrumEmissionMaskLibrary(DisplaySpectrumEmissionMaskLibraryCommand command) {
        new LibraryItemListPanel<EmissionMask>(this, EmissionMask.class, "Spectrum emission mask library", "LIBRARY_SPECTRUM_LIST_TITLE", 800, 500, 1).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplayREM(DisplayReceiverBlockingMaskLibraryCommand command) {
        new LibraryItemListPanel<BlockingMask>(this, BlockingMask.class, "Receiver blocking mask library", "LIBRARY_RECEIVER_BLOCKING_MASKS_TITLE", 800, 500, 1).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplayReceiverLibrary(DisplayReceiverLibraryCommand command) {
        new LibraryItemListPanel<ReceiverModel>(this, ReceiverModel.class, "Receiver library", "LIBRARY_RECEIVER_LIST_TITLE", 1300, 600, 1).setVisible(true);
    }
    
    @UIEventHandler
    public void handleDisplayTReceiverLibrary(DisplayReceiverLibraryCommand command) {
       new LibraryItemListPanel<T_ReceiverModel>(this, T_ReceiverModel.class, "Receiver library", "LIBRARY_RECEIVER_LIST_TITLE", 1300, 600, 1).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplaySystemsLibrary(DisplaySystemsLibraryCommand command) {
        new LibraryItemListPanel<SystemModel>(this, SystemModel.class, "Systems library", "LIBRARY_SYSTEM_LIST_TITLE", 1400, 800, 0).setVisible( true );
    }

    @UIEventHandler
    public void handleDisplayCDMALinkLevelData(DisplayCDMALinkLevelDataCommand command) {
        new LibraryItemListPanel<CDMALinkLevelData>(this, CDMALinkLevelData.class, "CDMA Link level data library", "LIBRARY_CDMA_LLD_LIST_WINDOWTITLE", 800, 500, 1).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplayAntennaLibrary(DisplayAntennaLibraryCommand command) {
        new LibraryItemListPanel<AntennaGain>(this, AntennaGain.class, "Antenna library", "", 1000, 500, 1).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplayCoverageRadiusLibrary(DisplayCoverageRadiusPluginCommand command ) {
        new LibraryItemListPanel<CoverageRadius>(this, CoverageRadius.class, "Coverage radius library", "", 800, 500, 1).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplayPropagationPlugin(DisplayPropagationPluginCommand command) {
        new LibraryItemListPanel<PropagationModel>(this, PropagationModel.class, "Propagation model library", "", 800, 500, 1).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplayEventProcessingLibrary(DisplayEventProcessingLibraryDialogCommand command ) {
        new LibraryItemListPanel<EventProcessing>(this, EventProcessing.class, "Event processing library", "", 800, 500, 0).setVisible(true);    }

    @UIEventHandler
    public void handleDisplayJarInstaller(DisplayInstallJarEditor command ) {
        new LibraryItemListPanel<JarConfigurationModel>(this, JarConfigurationModel.class,"Jar library", "LIBRARY_JAR_INSTALLER", 800, 500, 0).setVisible(true);
    }

    @UIEventHandler
    public void handleDisplayStatusBar(DisplayStatusBarCommand command) {
        statusBar.setVisible(!statusBar.isVisible());
    }

    @UIEventHandler
    public void handleSaveAllWorkspaces(SaveAllWorkspacesCommand command) {
        for (Component component : allViews()) {
            if ( component instanceof WorkspaceView ) {
                ((WorkspaceView) component).save();
            } else if ( component instanceof SimulationView ) {
                ((SimulationView) component).save();
            } else if ( component instanceof BatchView ) {
                ((BatchView) component).save();
            } else if ( component instanceof BatchSimulationView ) {
                ((BatchSimulationView) component).save();
            }
        }
    }

    @UIEventHandler
    public void handleOpenWorkspaceFile(OpenWorkspaceFileCommand command) {
        if (command.getFile().exists()) {
            handleOpen(command.getFile(), new LoadedWorkspace());
        } else {
            EventBusFactory.getEventBus().publish(new FileNotFoundEvent(command.getFile().getAbsolutePath()));
            String message = DialogHelper.saveFaildMessage(command.getFile().getAbsolutePath());
            EventBusFactory.getEventBus().publish(new InfoMessageEvent(message));
        }
    }

    public void openWorkspace( LoadedWorkspace loadedWorkspace ) {
        if (fileDialogHelper.openWorkspace(this).selectionMade()) {
            for (File file : fileDialogHelper.getSelectedFiles()) {
                handleOpen( file, loadedWorkspace );
            }
        }
    }

    private void handleOpen(File file, LoadedWorkspace loadedWorkspace ) {
        String path = file.getAbsolutePath();
        if ( path.endsWith(".sbj") || path.endsWith(".sbr")) {
            // batch
            BatchJobList jobList = BatchIOHandler.load(file);
            if ( jobList == null ) return;
            if ( jobList.hasBeenCalculated() ) {
                // show batch results
                BatchSimulationView view = new BatchSimulationView(jobList);
                view.complete();
                addWorkspaceViewToPanel(view, "Results[" + jobList.getDescription().name() + "]");
            } else {
                addWorkspaceViewToPanel( new BatchView(jobList), jobList.getDescription().name());
            }
        } else {
            openWorkspace( file, loadedWorkspace );
        }
    }

    private File saveWorksaceAs(FileFilters.ExtensionFileFilter filter) {
        if (fileDialogHelper.saveWorkspaceAs(this, filter).selectionMade()) {
            return filter.align(fileDialogHelper.getSelectedFile());
        }

        return null;
    }

    public void saveWorkspace( final Workspace wks ) {
        busySaving = true;
        final ProgressDialog progressDialog = new ProgressDialog( MainWindow.getInstance() );
        progressDialog.setTitle("Saving workspace");
        progressDialog.setExplanation("Saving workspace: " + wks.getName());
        progressDialog.setProgressMessage("Saving workspace...");

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    Model.saveWorkspace( wks );

                    File file = wks.getPath();
                    EventBusFactory.getEventBus().publish(new WorkspaceSavedEvent(file.getAbsolutePath()));

                    if ( wks.isHasBeenCalculated() ) {
                        EventBusFactory.getEventBus().publish(new InfoMessageEvent(DialogHelper.getSaveDoneWithResultsString(file)));
                    } else {
                        EventBusFactory.getEventBus().publish(new InfoMessageEvent(DialogHelper.getSaveDoneString(file)));
                    }
                } finally {
                    busySaving = false;
                }

                return null;
            }

            @Override
            protected void done() {
                progressDialog.close();
                try {
                    get();
                } catch (Exception e) {
                    LOG.error("Exception while saving", e);
                    DialogHelper.saveError();
                }
            }
        };
        worker.execute();
        progressDialog.showModally();

    }

    private void openWorkspace(final File file, final LoadedWorkspace handler) {
        setCursorBusy();
        final ProgressDialog progressDialog = new ProgressDialog(this, new CancelLoadCommand());
        progressDialog.setTitle("Loading");
        progressDialog.setExplanation("Loading workspace " + file.getName());

        new SwingWorker<Workspace, Void>() {
            protected Workspace doInBackground() throws Exception {
                EventBusFactory.getEventBus().publish(new ProgressEvent("Loading workspace"));
                Workspace workspace = Model.openWorkspace(file);
                if ( workspace != null ) {
                    List<MigrationIssue> issues = workspace.getMigrationIssues();
                    if ( !issues.isEmpty() ) {
                        StringBuilder sb = new StringBuilder("<html>Warnings when migrating: <br><br><ul>");
                        for (MigrationIssue issue : issues) {
                            sb.append("<li>").append(issue.getMessage()).append("</li>");
                        }
                        sb.append("</ul></html>");
                        JOptionPane.showMessageDialog(null, sb.toString(), "Migration Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
                return workspace;
            }
            protected void done() {
                setCursorDefault();
                progressDialog.close();
                Workspace loadedWorkspace;
                try {
                    loadedWorkspace = get();
                    if (loadedWorkspace == null) {
                        EventBusFactory.getEventBus().publish(new InfoMessageEvent(String.format("Cancelled loading of '%s'", file.getName())));
                        return;
                    }
                } catch (ExecutionException e ) {
                    Throwable cause = e.getCause();
                    if ( cause instanceof BackwardMigrationNotSupportedException ) {
                        LOG.error("Backward migration not possible", e);
                        DialogHelper.backwardMigrationNotSupported();
                    } else {
                        LOG.error("Error opening workspace", cause);
                        DialogHelper.openWorkspaceError();
                    }
                    return;
                } catch (Exception ex) {
                    LOG.error("Error opening workspace", ex);
                    DialogHelper.openWorkspaceError();
                    return;
                }
                EventBusFactory.getEventBus().publish(new FileOpenedEvent(file.getAbsolutePath()));
                String name = file.getName();
                loadedWorkspace.setName(name.substring(0, name.lastIndexOf(".")));
                handler.loadedWorkspace(loadedWorkspace);
            }
        }.execute();

        progressDialog.showModally();
    }

    protected void addWorkSpaceToView(Workspace loadedWorkspace) {
        if ( loadedWorkspace.isHasBeenCalculated() ) {
            addWorkspaceViewToPanel(new SimulationView(loadedWorkspace), "Results["+loadedWorkspace.getName()+"]");
        } else {
            addWorkspaceViewToPanel(new WorkspaceView(loadedWorkspace), loadedWorkspace.getName());
        }
    }

    private List<SimulationView> simulationViews() {
        List<SimulationView> currentViews = new ArrayList<SimulationView>();
        Component[] views = workspacesPane.getComponents();
        for (Component component : views) {
            if (component instanceof SimulationView) {
                currentViews.add((SimulationView) component);
            }
        }
        return currentViews;
    }

    private List<BatchSimulationView> batchSimulationViews() {
        List<BatchSimulationView> currentViews = new ArrayList<>();
        Component[] views = workspacesPane.getComponents();
        for (Component component : views) {
            if (component instanceof BatchSimulationView) {
                currentViews.add((BatchSimulationView) component);
            }
        }
        return currentViews;
    }

    private List<Component> allViews() {
        List<Component> currentViews = new ArrayList<>();
        Component[] views = workspacesPane.getComponents();
        for (Component component : views) {
            if (component instanceof WorkspaceView) {
                currentViews.add(component);
            } else if ( component instanceof SimulationView) {
                currentViews.add(component);
            } else if ( component instanceof BatchView ) {
                currentViews.add(component);
            } else if ( component instanceof BatchSimulationView ) {
                currentViews.add(component);
            }
        }
        return currentViews;
    }

    public void setCursorBusy() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
    public void setCursorDefault() {
        setCursor(Cursor.getDefaultCursor());
    }

    public void addWorkspaceViewToPanel( final Component view, String name ) {
        rootLayout.show(root, ROOTID);
        workspacesPane.addTab(name, view);

        int index = workspacesPane.indexOfComponent(view);
        workspacesPane.setSelectedIndex(index);
        workspacesPane.setTabComponentAt(workspacesPane.indexOfComponent(view), closableTab(name, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispatchView( view, componentClose );
                System.gc();
            }
        }));
        if ( view instanceof WorkspaceView) {
            EventBusFactory.getEventBus().publish( new WorkspaceViewAddedEvent((WorkspaceView) view));
        }

        updateButtonModels();
        if (workspacesPane.getTabCount() == 1) {
            EventBusFactory.getEventBus().publish(new WorkspacesPaneNonEmptyEvent());
        }
    }

    public void alignTabTitle( Component view ) {
        if ( view instanceof WorkspaceView ) {
            alignTabTitle( (WorkspaceView) view );
        } else if ( view instanceof SimulationView ) {
            workspacesPane.setTabComponentAt(workspacesPane.indexOfComponent(view), new JLabel(((SimulationView) view).getWorkspace().getName()));
        } else if ( view instanceof BatchView ) {
            workspacesPane.setTabComponentAt(workspacesPane.indexOfComponent(view), new JLabel(((BatchView) view).getTitle()));
        } else if ( view instanceof BatchSimulationView ) {
            workspacesPane.setTabComponentAt(workspacesPane.indexOfComponent(view), new JLabel(((BatchSimulationView) view).getTitle()));
        }
    }

    public void alignTabTitle( final WorkspaceView view ) {
        workspacesPane.setTabComponentAt(workspacesPane.indexOfComponent(view),
                closableTab(view.getWorkspaceName(),
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                dispatchView( view, componentClose );
                                System.gc();
                            }
                        }));
    }

    private void removeComponent(Component view) {
        int i = workspacesPane.indexOfComponent(view);
        workspacesPane.removeTabAt(i);
        workspacesPane.remove(view);
        setSplashIfEmpty();
    }

    private void setSplashIfEmpty() {
        if (workspacesPane.getTabCount() == 0) {
            rootLayout.show(root, SPLASH);
            EventBusFactory.getEventBus().publish(new WorkspacesPaneEmptyEvent());
        }
        updateButtonModels();
    }

    public void dispatchView( Component component, CurrentView handler ) {
        if ( component instanceof  WorkspaceView ) {
            handler.current((WorkspaceView) component);
        } else if (component instanceof SimulationView) {
            handler.current((SimulationView)component);
        } else if ( component instanceof BatchView) {
            handler.current((BatchView)component);
        } else if  (component instanceof BatchSimulationView) {
            handler.current((BatchSimulationView)component);
        }
    }

    public void currentView(CurrentView handler) {
        dispatchView( workspacesPane.getSelectedComponent(), handler );
    }

    public WorkspaceView currentWorkspaceView() {
        Component selectedComponent = workspacesPane.getSelectedComponent();
        if ( selectedComponent instanceof  WorkspaceView ) {
            return (WorkspaceView) selectedComponent;
        }

        return null;
    }

    public SimulationView currentSimulationView() {
        Component selectedComponent = workspacesPane.getSelectedComponent();
        if ( selectedComponent instanceof  SimulationView ) {
            return (SimulationView) selectedComponent;
        }

        return null;
    }

    private final WindowListener eventHandlerWindow = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            handleCloseApplication(null);
        }
    };

    /** Creates a new instance of MainWindow */
    private MainWindow() {
        // Fire event when tab changes
        workspacesPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                toolBar.handleSelectedComponent(workspacesPane.getSelectedComponent());
            }
        });
    }

    private void createMenuBar() {
        setJMenuBar(menus.createMenus(new JMenuBar()));
    }

    public void init() {
        calculator.setModal(false);

        // Set MainWIndow attributes
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(eventHandlerWindow);
        setIconImage(SeamcatIcons.getImageIcon("SEAMCAT_ICON_SEAMCAT").getImage());
        setTitle(STRINGLIST.getString("APPLICATION_TITLE") + " University of Birmingham , BCREE " +" - buildtime: " + STRINGLIST.getString("APPLICATION_BUILD_TIME"));
        createMenuBar();
        toolBar.createToolBar();
        getContentPane().add(toolBar.getToolBar(), java.awt.BorderLayout.NORTH);
        getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);
        root.add(new LogoSplash(), SPLASH);
        root.add(workspacesPane, ROOTID);
        
        
        // Added code by Mohamed Samra , so the application will start with new Workspace instead of the empty screen !
       Workspace workspace = openDefaultWorkspace();
       addWorkspaceViewToPanel(new WorkspaceView(workspace), workspace.getName());
       
      /* currentView(new CurrentView() {
           public void current(BatchView view) {
               setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               if (view.okToSimulate()) {
                   BatchJobList jobList = view.getList();
                   // clone
                   BatchJobList clone = new BatchJobList(jobList.getDescription().name(), jobList.getDescription().description());
                   for (Workspace ws : jobList.getBatchJobs()) {
                       clone.addBatchJob(cloneForSimulation(ws));
                   }
                   if (jobList.hasLocation()) {
                       File file = new File(jobList.getAbsoluteLocation());
                       clone.setAbsoluteLocation(file.getParentFile().getAbsolutePath() + File.separator + jobList.getDescription().name() + ".sbr");
                   }
                   final BatchSimulationView batchView = new BatchSimulationView(clone);
                   batchView.setIncrementalSave(view.isIncrementalSave());
                   simulationState = GlobalSimulationState.running;
                   addWorkspaceViewToPanel(batchView, "Results[" + clone.getDescription().name() + "]");
                   getEventBus().publish(new SimulationStartedEvent(null));
                   simulation = singleThreadPool.submit(new Runnable() {
                       public void run() {
                           try {
                               batchView.startSimulation();
                           } catch (SimulationInvalidException e) {
                               EventBusFactory.getEventBus().publish(new SimulationErrorEvent(batchView, e));
                           }
                       }
                   });
               } else {
                   setCursor(Cursor.getDefaultCursor());
               }
           }
/////////////////////////////////////////////
           ///////////////////////
           //////////////////////
           ////////////////////
           public void current(WorkspaceView view) {
               setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
               if (view.okToSimulate()) {
                   Workspace clone = cloneForSimulation(view.getWorkspace());
                   final SimulationView simulationView = new SimulationView(clone);
                   simulationView.setIsRunning(true);
                   simulationState = GlobalSimulationState.running;
                   addWorkspaceViewToPanel(simulationView, "Results[" + clone.getName() + "]");
                   getEventBus().publish(new SimulationStartedEvent(null));
                   simulation = singleThreadPool.submit(new Runnable() {
                       @Override
                       public void run() {
                           try {
                               simulationView.startSimulation();
                           } catch (SimulationInvalidException e) {
                               EventBusFactory.getEventBus().publish(new SimulationErrorEvent(simulationView, e));
                           }
                       }
                   });
               } else {
                   setCursor(Cursor.getDefaultCursor());
               }
           }
       });
       */
 
        
        
        getContentPane().add(root);
        updateButtonModels();
       

        EventBusFactory.getEventBus().subscribe(statusBar);
        statusBar.setLeft("SEAMCAT startup time in milliseconds: " + (System.currentTimeMillis() - org.seamcat.Seamcat.ctm));
        setToolTip();

        recentlyUsed = new RecentlyUsed();
        welcome = new Welcome(this, "Welcome to SEAMCAT", true);
        


        recentlyUsed.refresh();
        // request news from backend
        new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                return BackendRequest.requestNews( STRINGLIST.getString("APPLICATION_TITLE") );
            }

            @Override
            protected void done() {
                try {
                    statusBar.setMiddle(get(), STRINGLIST.getString("NEWS_TOOLTIP"));
                } catch (Exception e) {
                    //LOG.error( "Could not fetch news from backend", e);
                }
            }
        }.execute();
        new SwingWorker<Version, Void>() {
            @Override
            protected Version doInBackground() throws Exception {
                return BackendRequest.requestVersion();
            }

            @Override
            protected void done() {
                try {
                    Version version = get();
                    // match with version of this client
                    int major = Integer.parseInt(STRINGLIST.getString("APPLICATION_MAJOR"));
                    int minor = Integer.parseInt(STRINGLIST.getString("APPLICATION_MINOR"));
                    int patch = Integer.parseInt(STRINGLIST.getString("APPLICATION_PATCH"));

                    if ( version.getMajor() > major ) {
                        // warning!
                        DialogHelper.versionWarning();
                        return;
                    }
                    if ( version.getMinor() > minor ) {
                        // notification only
                        DialogHelper.versionMessage();
                        return;
                    }
                    if ( version.getPatch() > patch ) {
                        EventBusFactory.getEventBus().publish(new InfoMessageEvent("A patched version of SEAMCAT is available for download"));
                    }
                } catch (Exception e) {
                    //LOG.error("Could not read latest version from server", e);
                    //EventBusFactory.getEventBus().publish( new InfoMessageEvent("Could not retrieve version info from server"));
                }
            }
        }.execute();

        fileSaver.submit(new DataSaver());
    }

    private void setToolTip() {
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    }

    private void updateButtonModels() {
        currentView(new CurrentView() {
            public void current(WorkspaceView view) {
                getEventBus().publish(new EgeCanBeStoppedEvent(false));
            }

            public void current(SimulationView view) {
                getEventBus().publish(new EgeCanBeStoppedEvent(view.isEgeRunning()));
            }

            public void current(BatchView view) {
                getEventBus().publish(new EgeCanBeStoppedEvent(false));
            }

            public void current(BatchSimulationView view) {
                getEventBus().publish(new EgeCanBeStoppedEvent(view.isSimulating()));
            }
        });
    }

    public static JPanel closableTab(String name, ActionListener closeAction) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add( new JLabel(name + " "), BorderLayout.CENTER );

        JButton btnClose = new TabButton();
        btnClose.addActionListener(closeAction);

        panel.add( btnClose, BorderLayout.EAST );
        panel.setOpaque(false);

        return panel;
    }

    public GlobalSimulationState getSimulationState() {
        return simulationState;
    }
}
