package org.seamcat.presentation.menu;

import org.seamcat.commands.*;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.SimulationCompleteEvent;
import org.seamcat.events.SimulationStartedEvent;
import org.seamcat.events.WorkspacesPaneEmptyEvent;
import org.seamcat.events.WorkspacesPaneNonEmptyEvent;
import org.seamcat.presentation.SeamcatIcons;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import static org.seamcat.eventbus.EventBusFactory.getEventBus;

/**
 * This class handles all the setup of the menus of SEAMCAT
 */
public class Menus {
	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
	private static final int keyModifier = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
	private JMenuItem welcomeItem;

	private JMenu workspaceMenu;
	@SuppressWarnings("unused")
	private HistoryItemsMenu historyItemsMenu;
	private JMenuItem openWorkspace;
	private JMenuItem saveWorkspace;
	private JMenuItem saveWorkspaceAs;
	private JMenuItem saveAllWorkspaces;
	private JMenuItem closeWorkspace;
	private JMenuItem closeOthers;
	private JMenuItem closeAll;
	private JMenuItem closeUnmodified;
	private JMenuItem importLibrary;
	private JMenuItem exportLibrary;
	private JMenuItem configuration;
	private JMenuItem checkConsistency;
	private JMenuItem reportItem;
	private JMenuItem startSimulation;
    private JMenuItem newBatch;


    public Menus() {
		EventBusFactory.getEventBus().subscribe(this);

	}

	public JMenuBar createMenus(JMenuBar menuBar) {
		createMenuFile(menuBar);
		createMenuView(menuBar);
		createMenuLibrary(menuBar);
		createMenuWorkspace(menuBar);
		createMenuTools(menuBar);
		createHelpMenu(menuBar);
		return menuBar;
	}

	private void createMenuFile(JMenuBar menuBar) {
		final JMenu menu = new JMenu();
		menu.setText(STRINGLIST.getString("FILE_MENU_TEXT"));
		menu.setMnemonic(KeyEvent.VK_F);
		createMenuItemNewWorkspace(menu);
		createMenuItemBatchOperation(menu);
		createMenuItemOpenWorkspace(menu);
		menu.addSeparator();
		createMenuItemSaveWorkspace(menu);
		createMenuItemSaveWorkspaceAs(menu);
		createMenuItemSaveAllWorkspaces(menu);
		menu.addSeparator();
		createMenuItemCloseWorkspace(menu);
		menu.addSeparator();
		createMenuItemImportLibrary(menu);
		createMenuItemExportLibrary(menu);
		menu.addSeparator();
		createMenuItemConfiguration(menu);
		menu.addSeparator();
		historyItemsMenu = new HistoryItemsMenu(menu);
		createMenuItemExit(menu);
		menuBar.add(menu);
	}


	private void createMenuView(final JMenuBar menuBar) {
		final JMenu menu = new JMenu(STRINGLIST.getString("VIEW_MENU_TEXT"));
		menu.setMnemonic(KeyEvent.VK_V);
		createMenuItemViewToolBar(menu);
		createMenuItemViewStatusBar(menu);
		menuBar.add(menu);
	}

	private void createMenuLibrary(final JMenuBar menuBar) {
		JMenu menu = new JMenu(STRINGLIST.getString("LIBRARY_MENU_TEXT"));
		menu.setMnemonic(KeyEvent.VK_L);
		menu.add(item("MENU_ITEM_TEXT_LIBRARY_SYSTEMS", null, "MENU_ITEM_TOOLTIP_LIBRARY_SYSTEMS", DisplaySystemsLibraryCommand.class, (int) 'S', keyModifier, KeyEvent.VK_S));
		menu.add(item("MENU_ITEM_TEXT_LIBRARY_SEM", null, "MENU_ITEM_TOOLTIP_LIBRARY_SPECTRUM", DisplaySpectrumEmissionMaskLibraryCommand.class, (int) 'M', keyModifier, KeyEvent.VK_M));
		menu.add(item("MENU_ITEM_TEXT_LIBRARY_RBM", null, "MENU_ITEM_TOOLTIP_LIBRARY_BLOCKING", DisplayReceiverBlockingMaskLibraryCommand.class, (int) 'B', keyModifier, KeyEvent.VK_B));
		menu.add(item("MENU_ITEM_TEXT_LIBRARY_RECEIVERS", null, "MENU_ITEM_TOOLTIP_LIBRARY_RECEIVERS", DisplayReceiverLibraryCommand.class, (int) 'R', keyModifier, KeyEvent.VK_R));
		menu.add(item("MENU_ITEM_TEXT_LIBRARY_TRANSMITTERS", null, "MENU_ITEM_TOOLTIP_LIBRARY_TRANSMITTERS", DisplayTransmitterLibraryCommand.class, (int) 'T', keyModifier, KeyEvent.VK_T));
		menu.add(item("MENU_ITEM_TEXT_LIBRARY_CDMA_LINK_LEVEL_DATA", null, "MENU_ITEM_TOOLTIP_LIBRARY_CDMA_LINK_LEVEL_DATA", DisplayCDMALinkLevelDataCommand.class, (int) 'L', keyModifier, KeyEvent.VK_L));
        menu.addSeparator();
        menu.add(item("MENU_ITEM_TEXT_LIBRARY_ANTENNAS", null, "MENU_ITEM_TOOLTIP_LIBRARY_ANTENNAS", DisplayAntennaLibraryCommand.class, (int) 'A', keyModifier, KeyEvent.VK_A));
        menu.add(item("MENU_ITEM_TEXT_LIBRARY_COVERAGE_RADIUS", null, "MENU_ITEM_TOOLTIP_COVERAGE_RADIUS", DisplayCoverageRadiusPluginCommand.class, (int) 'C', keyModifier, KeyEvent.VK_C));
        menu.add(item("MENU_ITEM_TEXT_LIBRARY_PROPAGATION_MODEL_PLUGINS", null, "MENU_ITEM_TOOLTIP_LIBRARY_PROPAGATION_MODEL_PLUGINS", DisplayPropagationPluginCommand.class, (int) 'P', keyModifier, KeyEvent.VK_P));
        menu.add(item("MENU_ITEM_TEXT_LIBRARY_EVENT_PROCESSING_PLUGINS", null, "MENU_ITEM_TOOLTIP_LIBRARY_EVENT_PROCESSING_PLUGINS", DisplayEventProcessingLibraryDialogCommand.class, (int) 'E', keyModifier, KeyEvent.VK_E));
        menu.addSeparator();
        menu.add(item("MENU_ITEM_TEXT_LIBRARY_INSTALL_JARS", null, "MENU_ITEM_TOOLTIP_LIBRARY_INSTALL_JAR", DisplayInstallJarEditor.class, (int) 'J', keyModifier, KeyEvent.VK_J));
        menuBar.add(menu);
	}

	private JMenu createMenuWorkspace(JMenuBar menuBar) {
		workspaceMenu = new JMenu();
		workspaceMenu.setMnemonic(KeyEvent.VK_W);
		workspaceMenu.setText(STRINGLIST.getString("MENU_TEXT_WORKSPACE"));
		createMenuItemCheckConsistency(workspaceMenu);
		workspaceMenu.addSeparator();
		createMenuItemReport(workspaceMenu);
		menuBar.add(workspaceMenu);
		workspaceMenu.addSeparator();
		createMenuItemSimulation(workspaceMenu);
		menuBar.add(workspaceMenu);
		return workspaceMenu;
	}

	private void createMenuTools(final JMenuBar menuBar) {
		final JMenu menu = new JMenu();
		menu.setText(STRINGLIST.getString("TOOLS_MENU_TEXT"));
		menu.setMnemonic(KeyEvent.VK_T);
		createMenuTestDistributions(menu);
		createMenuTestPropagationModel(menu);
		createMenuTestFunctions(menu);
		menu.addSeparator();
		createMenuTestCalculator(menu);
		menu.addSeparator();
		createMenuCompareVectors(menu);
		menuBar.add(menu);
	}

	private void createHelpMenu(JMenuBar menuBar) {
		JMenu menu = new JMenu();
		menu.setText(STRINGLIST.getString("HELP_MENU_TEXT"));
		menu.setMnemonic(KeyEvent.VK_H);
		createMenuItemHelp(menu);
		createMenuReportError(menu);
		createMenuAbout(menu);
		createMenuWelcome(menu);
		menuBar.add(menu);
	}

	private void createMenuCompareVectors(JMenu menu) {
		JMenuItem item = item("TEST_COMPARE_VECTORS_MENU_ITEM_TEXT", "SEAMCAT_ICON_INTERFERENCE_CALCULATIONS", "TEST_COMPARE_VECTORS_MENU_ITEM_TOOLTIP", DisplayCompareVectorCommand.class);
		item.setAccelerator(KeyStroke.getKeyStroke('V', keyModifier));
		item.setMnemonic(KeyEvent.VK_C);
		menu.add(item);
	}

	private void createMenuItemBatchOperation(final JMenu menu) {
		newBatch = item("MENU_ITEM_TEXT_NEW_BATCH_OPERATION", "SEAMCAT_ICON_NEW_BATCH", "MENU_ITEM_TOOLTIP_BATCH_OPERATION", NewBatchCommand.class);
        menu.add( newBatch );
	}

	private void createMenuTestDistributions(final JMenu menu) {
		JMenuItem item = item("TEST_DISTRIBUTIONS_MENU_ITEM_TEXT", "SEAMCAT_ICON_TEST_DISTRIBUTION", "TEST_DISTRIBUTIONS_MENU_ITEM_TOOLTIP", DisplayDistributionTestCommand.class);
		item.setAccelerator(KeyStroke.getKeyStroke('D', keyModifier));
		item.setMnemonic(KeyEvent.VK_D);
		menu.add(item);
	}

	private void createMenuTestPropagationModel(final JMenu menu) {
		JMenuItem item = item("TEST_PROPAGATIONS_MENU_ITEM_TEXT", "SEAMCAT_ICON_TEST_PROPAGATION", "TEST_PROPAGATIONS_MENU_ITEM_TOOLTIP", DisplayPropagationTestCommand.class);
		item.setAccelerator(KeyStroke.getKeyStroke('M', keyModifier));
		item.setMnemonic(KeyEvent.VK_M);
		menu.add(item);
	}

	private void createMenuTestFunctions(final JMenu menu) {
		JMenuItem item = item("TEST_FUNCTIONS_MENU_ITEM_TEXT", "SEAMCAT_ICON_TEST_UNWANTED", "TEST_FUNCTIONS_MENU_ITEM_TOOLTIP", DisplayTestFunctionsCommand.class);
		item.setAccelerator(KeyStroke.getKeyStroke('U', keyModifier));
		item.setMnemonic(KeyEvent.VK_U);
		menu.add(item);
	}

	private void createMenuTestCalculator(final JMenu menu) {
		JMenuItem item = item("TEST_CALCULATOR_MENU_ITEM_TEXT", "SEAMCAT_ICON_CALCULATOR", "TEST_CALCULATOR_MENU_ITEM_TOOLTIP", DisplayTestCalculatorCommand.class);
		item.setAccelerator(KeyStroke.getKeyStroke('C', keyModifier));
		item.setMnemonic(KeyEvent.VK_C);
		menu.add(item);
	}

	private void createMenuItemHelp(JMenu menu) {
		menu.add(item("HELP_CONTENTS_MENU_ITEM_TEXT", "SEAMCAT_ICON_HELP", "HELP_CONTENTS_MENU_ITEM_TOOLTIP", DisplayHelpCommand.class, KeyEvent.VK_F1, 0, KeyEvent.VK_H));
	}

	private void createMenuReportError(final JMenu menu) {
		menu.add(item("SEND_ERROR_REPORT", "SEAMCAT_ICON_BUG_ERROR", "ERROR_REPORT_MENU_ITEM_TOOLTIP", DisplayReportErrorCommand.class));
	}

	private void createMenuAbout(JMenu menu) {
		menu.add(item("ABOUT", null, "ABOUT_SEAMCAT_MENU_ITEM_TOOLTIP", DisplayAboutCommand.class));
	}

	private void createMenuWelcome(JMenu menu) {
		welcomeItem = item("WELCOME", null, "WELCOME_MENU_ITEM_TOOLTIP", DisplayWelcomeCommand.class);
		welcomeItem.setEnabled(true);
		menu.add(welcomeItem);
	}

	private void createMenuItemCheckConsistency(final JMenu menu) {
		checkConsistency = item("MENU_ITEM_TEXT_CHECK_CONSISTENCY", "SEAMCAT_ICON_CHECK_CONSISTENCY", "MENU_ITEM_TOOLTIP_CHECK_CONSISTENCY", CheckConsistencyCommand.class, (int) 'Y', InputEvent.CTRL_MASK, KeyEvent.VK_Y);
		menu.add(checkConsistency);
	}

	private void createMenuItemReport(final JMenu menu) {
		reportItem = item("MENU_ITEM_TEXT_REPORT", "SEAMCAT_ICON_GENERATE_REPORT", "MENU_ITEM_TOOLTIP_REPORT", GenerateReportCommand.class, (int) 'R', InputEvent.CTRL_MASK, KeyEvent.VK_R);
		menu.add(reportItem);
	}

	private void createMenuItemSimulation(final JMenu menu) {
		startSimulation = item("MENU_ITEM_TEXT_RUN_EGE", "SEAMCAT_ICON_SIMULATION_START", null, RunEGECommand.class, (int) 'E', InputEvent.CTRL_MASK, KeyEvent.VK_E);
		menu.add(startSimulation);
	}

	private void createMenuItemViewStatusBar(final JMenu menu) {
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
		menuItem.addActionListener(action(DisplayStatusBarCommand.class));
		menuItem.setText(STRINGLIST.getString("MENU_ITEM_TEXT_VIEW_STATUS_BAR"));
		menuItem.setToolTipText(STRINGLIST.getString("MENU_ITEM_TOOLTIP_VIEW_STATUS_BAR"));
		menuItem.setMnemonic(KeyEvent.VK_S);
		menuItem.setSelected(true);
		menu.add(menuItem);
	}

	private void createMenuItemNewWorkspace(final JMenu menu) {
		menu.add(item("MENU_ITEM_TEXT_NEW_WORKSPACE", "SEAMCAT_ICON_WORKSPACE_NEW", "MENU_ITEM_TOOLTIP_NEW_WORKSPACE", NewWorkspaceCommand.class, (int) 'N', InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_N));
	}

	private void createMenuItemOpenWorkspace(final JMenu menu) {
		openWorkspace = item("MENU_ITEM_TEXT_OPEN_WORKSPACE", "SEAMCAT_ICON_WORKSPACE_OPEN", "MENU_ITEM_TOOLTIP_OPEN_WORKSPACE", OpenWorkspaceCommand.class, (int) 'O', InputEvent.CTRL_MASK, KeyEvent.VK_O);
		menu.add(openWorkspace);
	}

	private void createMenuItemSaveWorkspace(final JMenu menu) {
		saveWorkspace = item("MENU_ITEM_TEXT_SAVE_WORKSPACE", "SEAMCAT_ICON_WORKSPACE_SAVE", "MENU_ITEM_TOOLTIP_SAVE_WORKSPACE", SaveWorkspaceCommand.class, (int) 'S', InputEvent.CTRL_MASK, KeyEvent.VK_S);
		menu.add(saveWorkspace);
	}

	private void createMenuItemSaveWorkspaceAs(final JMenu menu) {
		saveWorkspaceAs = item("MENU_ITEM_TEXT_SAVE_WORKSPACE_AS", "SEAMCAT_ICON_WORKSPACE_SAVEAS", "MENU_ITEM_TOOLTIP_SAVE_WORKSPACE_AS", SaveWorkspaceAsCommand.class);
		menu.add(saveWorkspaceAs);
	}

	private void createMenuItemSaveAllWorkspaces(final JMenu menu) {
		saveAllWorkspaces = item("MENU_ITEM_TEXT_SAVE_ALL_WORKSPACES", "SEAMCAT_ICON_WORKSPACE_SAVEALL", "MENU_ITEM_TOOLTIP_SAVE_ALL_WORKSPACES", SaveAllWorkspacesCommand.class);
        menu.add(saveAllWorkspaces);
	}

	private void createMenuItemCloseWorkspace(final JMenu menu) {
		closeWorkspace = item("MENU_ITEM_TEXT_CLOSE_WORKSPACE", "SEAMCAT_ICON_WORKSPACE_CLOSE", "MENU_ITEM_TOOLTIP_CLOSE_WORKSPACE", CloseWorkspaceCommand.class, (int) 'C', InputEvent.CTRL_MASK, KeyEvent.VK_C);
		menu.add(closeWorkspace);

        closeOthers = item("MENU_ITEM_TEXT_CLOSE_OTHERS_WORKSPACE", null, "MENU_ITEM_TOOLTIP_CLOSE_OTHERS_WORKSPACE", CloseOthersWorkspaceCommand.class );
        menu.add(closeOthers);
        closeAll = item("MENU_ITEM_TEXT_CLOSE_ALL_WORKSPACE", null, "MENU_ITEM_TOOLTIP_CLOSE_ALL_WORKSPACE", CloseAllWorkspaceCommand.class, (int) 'A', InputEvent.CTRL_MASK, KeyEvent.VK_A );
        menu.add(closeAll);
        closeUnmodified = item("MENU_ITEM_TEXT_CLOSE_UNMODIFIED_WORKSPACE", null, "MENU_ITEM_TOOLTIP_CLOSE_UNMODIFIED_WORKSPACE", CloseUnmodifiedWorkspaceCommand.class, (int) 'U', InputEvent.CTRL_MASK, KeyEvent.VK_U);
        menu.add(closeUnmodified);
    }

	private void createMenuItemImportLibrary(final JMenu menu) {
		importLibrary = item("MENU_ITEM_TEXT_IMPORT_LIBRARY", "SEAMCAT_ICON_IMPORT_LIBRARY", "MENU_ITEM_TOOLTIP_IMPORT_LIBRARY", ImportLibraryCommand.class, (int) 'I', keyModifier, KeyEvent.VK_I);
		menu.add(importLibrary);
	}

	private void createMenuItemExportLibrary(JMenu menu) {
		exportLibrary = item("MENU_ITEM_TEXT_EXPORT_LIBRARY", "SEAMCAT_ICON_EXPORT_LIBRARY", "MENU_ITEM_TOOLTIP_EXPORT_LIBRARY", ExportLibraryCommand.class, (int) 'E', keyModifier, KeyEvent.VK_E);
		menu.add(exportLibrary);
	}

	private void createMenuItemConfiguration(final JMenu menu) {
		configuration = item("MENU_ITEM_TEXT_CONFIGURATION", "SEAMCAT_ICON_CONFIGURATION", "MENU_ITEM_TOOLTIP_CONFIGURATION", DisplayConfigurationCommand.class, (int) 'G', InputEvent.CTRL_MASK, KeyEvent.VK_G);
		menu.add(configuration);
	}

	private void createMenuItemExit(JMenu menu) {
		menu.add(item("MENU_ITEM_TEXT_EXIT", "SEAMCAT_ICON_EXIT", "MENU_ITEM_TOOLTIP_EXIT", CloseApplicationCommand.class, (int) 'X', InputEvent.CTRL_MASK, KeyEvent.VK_X));
	}

	private void createMenuItemViewToolBar(final JMenu menu) {
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem();
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) e.getSource();
				getEventBus().publish(new ShowToolBarCommand(menuItem.isSelected()));
			}
		});
		menuItem.setText(STRINGLIST.getString("MENU_ITEM_TEXT_VIEW_TOOL_BAR"));
		menuItem.setToolTipText(STRINGLIST.getString("MENU_ITEM_TOOLTIP_VIEW_TOOL_BAR"));
		menuItem.setMnemonic(KeyEvent.VK_T);
		menuItem.setSelected(true);
		menu.add(menuItem);
	}

	private JMenuItem item(String text, String icon, String tooltip, Class<?> command) {
		JMenuItem item = new JMenuItem();
		item.setText(STRINGLIST.getString(text));
		if (command != null) {
			item.addActionListener(action(command));
		}
		if (icon != null) {
			item.setIcon(SeamcatIcons.getImageIcon(icon));
		}
		if (tooltip != null) {
			item.setToolTipText(STRINGLIST.getString(tooltip));
		}
		return item;
	}

	private JMenuItem item(String text, String icon, String toolTip, Class<?> command, Integer key, Integer inputEvent, Integer keyEvent) {
		JMenuItem item = item(text, icon, toolTip, command);
		item.setAccelerator(KeyStroke.getKeyStroke(key, inputEvent));
		item.setMnemonic(keyEvent);
		return item;
	}

	private void enable(boolean enable) {
        startSimulation.setEnabled(enable);
		reportItem.setEnabled( enable );
		checkConsistency.setEnabled( enable);
		workspaceMenu.setEnabled(enable);
		saveWorkspace.setEnabled(enable);
		saveWorkspaceAs.setEnabled(enable);
		saveAllWorkspaces.setEnabled(enable);
		closeWorkspace.setEnabled(enable);
	}

    @UIEventHandler
    public void handleEmptyRoot( WorkspacesPaneEmptyEvent event ) {
        welcomeItem.setEnabled( true );
    }

    @UIEventHandler
    public void handleNonEmptyRoot( WorkspacesPaneNonEmptyEvent event ) {
        welcomeItem.setEnabled( false );;
    }

	@UIEventHandler
	public void handleSimulationComplete(SimulationCompleteEvent event) {
		if ( startSimulation == null ) return;
        enable(true);
	}

	@UIEventHandler
	public void handleSimulationStarted(SimulationStartedEvent event) {
		enable(false);
	}

	public static ActionListener action(final Class<?> commandClass) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					EventBusFactory.getEventBus().publish(commandClass.newInstance());
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		};
	}

	public static ActionListener action(final Object commandInstance) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				EventBusFactory.getEventBus().publish(commandInstance);
			}
		};
	}
}
