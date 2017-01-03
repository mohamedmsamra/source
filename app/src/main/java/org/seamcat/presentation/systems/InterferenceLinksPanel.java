package org.seamcat.presentation.systems;

import org.seamcat.commands.DisplayHelpNameCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.Subscriber;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.CorrelationModelChanged;
import org.seamcat.model.IdElement;
import org.seamcat.model.InterferenceLinkElement;
import org.seamcat.model.Library;
import org.seamcat.model.Workspace;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.generic.InterferersDensityUI;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.generic.PathLossCorrelationUI;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.scenariocheck.ScenarioCheckResult;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink;
import org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink;
import org.seamcat.model.workspace.CustomPanelBuilder;
import org.seamcat.model.workspace.InterferenceLinkUI;
import org.seamcat.model.workspace.RelativeLocationInterferenceUI;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.GenericSelectionDialog;
import org.seamcat.presentation.components.ScrollingBorderPanel;
import org.seamcat.presentation.genericgui.GenericPanel;
import org.seamcat.presentation.genericgui.item.DistributionItem;
import org.seamcat.presentation.genericgui.item.Item;
import org.seamcat.presentation.genericgui.item.ItemChanged;
import org.seamcat.presentation.genericgui.panelbuilder.CompositeEditor;
import org.seamcat.presentation.genericgui.panelbuilder.PanelModelEditor;
import org.seamcat.presentation.menu.ToolBar;
import org.seamcat.presentation.multiple.GenerateMultipleInterferersDialog;
import org.seamcat.presentation.systems.generic.RelativeLocationInterferingLinkPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterferenceLinksPanel extends JPanel {

    private Workspace workspace;
    private List<InterferenceLinkElement> model;

    public int indexDeleted;
    private JPanel detail = new JPanel(new BorderLayout());
    private DefaultListModel listModel;
    private JList list;
    private JToolBar toolBar;
    private JButton delete;
    private JFrame owner;
    private JPanel frequencySelector = new JPanel(new GridLayout(1,2));
    private CompositeEditor<InterferenceLinkUI> detailPanel;
    private RelativeLocationInterferingLinkPanel relativeLocPanel;

    private ListSelectionListener listListener;
    private KeyListener keyListener;

    public InterferenceLinksPanel(JFrame parent, Workspace workspace) {
        this.workspace = workspace;
        this.model = workspace.getInterferenceLinkUIs();
        this.owner = parent;
        toolBar = new JToolBar();
        toolBar.setFocusable(false);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        setupToolbar();
        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(list);
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(toolBar, BorderLayout.NORTH);
        leftPanel.add(jScrollPane, BorderLayout.CENTER);

        listModel = new DefaultListModel();
        list.setModel(listModel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.add(leftPanel);
        splitPane.add(detail);
        splitPane.setDividerLocation(200);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        frequency = new DistributionItem(MainWindow.getInstance()).label("Frequency");
        frequency.initialize();

        refreshFromModel();
        detail(model.get(list.getSelectedIndex()), list.getSelectedIndex());
        handleEnablement();
    }

    public void register() {

        frequency.addItemChangedHandler(new ItemChanged<AbstractDistribution>() {
            public void itemChanged(AbstractDistribution value) {
                int index = list.getSelectedIndex();
                workspace.getInterferingLinkFrequency().set(index, value);
            }
        });

        listListener = new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!listModel.isEmpty()) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    if (list.getSelectedIndex() != -1) {
                        updateModel();
                        detail(model.get(list.getSelectedIndex()), list.getSelectedIndex());
                    }
                }
            }
        };
        list.addListSelectionListener(listListener);
        keyListener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyChar() == KeyEvent.VK_DELETE) {
                    deleteCurrentlySelectedInterferenceLink();
                }
            }
        };
        list.addKeyListener(keyListener);
        Subscriber.subscribe(this);
    }

    public void dispose() {
        list.removeListSelectionListener(listListener);
        list.removeKeyListener(keyListener);
        toolBar.removeAll();
        EventBusFactory.getEventBus().unsubscribe( this );
    }

    private void setupToolbar() {
        JButton button = ToolBar.button("SEAMCAT_ICON_ADD", "TOOLBAR_INTERFERING_LINKS_ADD_TOOLTIP", null);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleAddInterferenceLink();
            }
        });
        toolBar.add(button);
        button = ToolBar.button("SEAMCAT_ICON_DUPLICATE", "TOOLBAR_INTERFERING_LINKS_DUPLICATE_TOOLTIP", null);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                handleDupInterferenceLink();
            }
        });
        toolBar.add(button);
        delete = ToolBar.button("SEAMCAT_ICON_DELETE_TRASH", "TOOLBAR_INTERFERING_LINKS_DELETE_TOOLTIP", null);
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                deleteCurrentlySelectedInterferenceLink();
            }
        });
        toolBar.add(delete);

        JButton multiple = ToolBar.button("SEAMCAT_ICON_GENERATE_MULTIPLE", "TOOLBAR_INTERFERING_LINKS_MULTIPLE_TOOLTIP", null);
        multiple.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                updateModel();
                GenerateMultipleInterferersDialog dialog = new GenerateMultipleInterferersDialog(MainWindow.getInstance());
                dialog.setModel( workspace );

                List<InterferenceLinkElement> added = dialog.getAdded();
                if ( added != null ) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    for (InterferenceLinkElement elm : added) {
                        elm.setName( getILName( workspace.getInterferenceLinkUIs().size(), workspace.getSystem(elm.getInterferingSystemId())));
                        workspace.addInterferenceLink( elm, workspace.getInterferingLinkFrequency().get(dialog.getSelectedIndex()));
                    }
                    refreshFromModel();
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        toolBar.add( multiple );
        JButton setSystem = ToolBar.button("SEAMCAT_ICON_IMPORT_LIBRARY", "TOOLBAR_INTERFERING_LINKS_IMPORT", null);
        setSystem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                handleImport();
            }
        });
        toolBar.add( setSystem );

        toolBar.addSeparator();
        toolBar.add(ToolBar.button("SEAMCAT_ICON_HELP", "HELP_CONTENTS_MENU_ITEM_TEXT", new DisplayHelpNameCommand("InterferingLinksEditor")));
    }

    private void detail(final InterferenceLinkElement link, int index) {
        if ( detailPanel != null ) {
            Subscriber.unSubscribeDeep(detailPanel);
        }
        detail.removeAll();
        detailPanel = new CompositeEditor<InterferenceLinkUI>(owner, InterferenceLinkUI.class, link.getSettings(), false, null, new CustomPanelBuilder() {
            public boolean canBuild(Class<?> modelClass) {
                return RelativeLocationInterferenceUI.class == modelClass;
            }

            @Override
            public <T> PanelModelEditor<T> build(Class<T> modelClass, T mod, final String name) {
                relativeLocPanel = new RelativeLocationInterferingLinkPanel(MainWindow.getInstance(), link, workspace);
                return new PanelModelEditor<T>() {
                    public JPanel getPanel() {
                        return  new ScrollingBorderPanel(relativeLocPanel, name, "Relative position of interfering link help", "InterferingLinkRelativePositioningOfInterferingLink");
                    }
                    public T getModel() {
                        return (T) relativeLocPanel.getModel();
                    }
                };
            }
        }, null, null);

        detailPanel.setIndex( index );

        JPanel interferer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        interferer.add( Box.createHorizontalStrut(5) );
        interferer.add( single(frequency));
        frequency.setValue((AbstractDistribution) workspace.getInterferingLinkFrequency().get(index));
        frequencySelector.removeAll();
        frequencySelector.add(interferer);

        detail.add(frequencySelector, BorderLayout.NORTH);
        detail.add(detailPanel, BorderLayout.CENTER);
        detail.revalidate();
        detail.repaint();
    }

    private static Set<InterferingLinkRelativePosition.CorrelationMode> densityModes = new HashSet<>();

    static {
        densityModes.add(InterferingLinkRelativePosition.CorrelationMode.VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM);
        densityModes.add(InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR);
        densityModes.add(InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT);
        densityModes.add(InterferingLinkRelativePosition.CorrelationMode.VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST);
        densityModes.add(InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR);
        densityModes.add(InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT);
    }

    @UIEventHandler
    public void handle(CorrelationModelChanged changed ) {
        handleEnablement();
    }

    public void handleEnablement() {
        InterferenceLinkUI model = detailPanel.getModel();
        InterferingLinkRelativePosition.CorrelationMode mode = model.path().relativeLocation().mode();
        boolean enable = densityModes.contains(mode);
        detailPanel.enableWidget(InterferersDensityUI.class, enable );


        SystemModel victim = workspace.getVictimSystem();
        int index = list.getSelectedIndex();
        SystemModel interferer = workspace.getSystem(workspace.getInterferenceLinkUIs().get(index).getInterferingSystemId());

        boolean crMode = cr(victim, interferer);

        boolean enablePLCorrelation = victim instanceof SystemModelOFDMADownLink ||
                victim instanceof SystemModelOFDMAUpLink;

        detailPanel.enableWidget(PathLossCorrelationUI.class, enablePLCorrelation );

        frequency.setRelevant(!crMode);
    }

    private boolean cr( SystemModel victim, SystemModel interferer) {
        return victim instanceof SystemModelGeneric
            &&
               interferer instanceof SystemModelGeneric && ((SystemModelGeneric) interferer).transmitter().emissionCharacteristics().cognitiveRadio();

    }

    private GenericPanel single(Item item) {
        GenericPanel gp = new GenericPanel();
        gp.addItem(item);
        gp.initializeWidgets();
        return gp;
    }

    public void refreshFromModel() {
        listModel.clear();
        for (int i = 0; i < model.size(); i++) {
            InterferenceLinkElement link = model.get(i);
            link.setName(getILName(i+1, workspace.getSystem(link.getInterferingSystemId())));
            listModel.addElement(link.getName());
        }
        updateButton();
        int index = 0;
        if ( detailPanel != null ) {
            index = detailPanel.getIndex();
        }
        list.setSelectedIndex(index);
        frequency.setValue((AbstractDistribution) workspace.getInterferingLinkFrequency().get(index));
    }

    private void updateButton() {
        delete.setEnabled(listModel.size() > 1);
    }

    public void updateModel() {
        if ( detailPanel != null) {
            int index = detailPanel.getIndex();
            InterferenceLinkElement link = model.get(index);
            model.set( index, new InterferenceLinkElement(link.getId(), link.getInterferingSystemId(), link.getName(), detailPanel.getModel()));
        }
    }

    private void handleImport() {
        List<IdElement<SystemModel>> systemModels = workspace.getSystemModels();
        List<SystemListItem> items = new ArrayList<SystemListItem>();
        for (IdElement<SystemModel> model : systemModels) {
            items.add( new SystemListItem(model));
        }
        GenericSelectionDialog<SystemListItem> dialog = new GenericSelectionDialog<SystemListItem>(MainWindow.getInstance(),
                "Select interfering system", items, false);
        if ( dialog.display() ) {
            SystemListItem selectedValue = dialog.getSelectedValue();
            int index = dialog.getSelectedIndex();
            String name = getILName(index + 1, selectedValue.getElement().getElement());
            int selectedIndex = list.getSelectedIndex();
            detailPanel.setIndex( selectedIndex );
            model.set(selectedIndex, new InterferenceLinkElement(selectedValue.getElement().getId(), name, detailPanel.getModel()));
            SystemModel system = workspace.getSystemModels().get(index).getElement();
            workspace.getInterferingLinkFrequency().set(selectedIndex, Library.getFrequency(system));
            refreshFromModel();
        }
    }

    private void handleAddInterferenceLink() {
        updateModel();
        List<IdElement<SystemModel>> systemModels = workspace.getSystemModels();
        int size = model.size();
        if (systemModels.size() == 1) {
            String name = getILName(size+1, systemModels.get(0).getElement());
            InterferenceLinkElement element = new InterferenceLinkElement(model.get(0).getInterferingSystemId(), name, ProxyHelper.newComposite(InterferenceLinkUI.class));
            workspace.addInterferenceLink(element, workspace.getInterferingLinkFrequency().get(0));
            model = workspace.getInterferenceLinkUIs();
            listModel.addElement( name );
            list.setSelectedIndex(listModel.getSize() - 1);
        } else {
            List<SystemListItem> items = new ArrayList<SystemListItem>();
            for (IdElement<SystemModel> model : systemModels) {
                items.add( new SystemListItem(model));
            }
            GenericSelectionDialog<SystemListItem> dialog = new GenericSelectionDialog<>(MainWindow.getInstance(),
                    "Select interfering system", items, false);
            if ( dialog.display() ) {
                SystemListItem selectedValue = dialog.getSelectedValue();
                String name = getILName(size+1, selectedValue.getElement().getElement());
                InterferenceLinkElement element = new InterferenceLinkElement(selectedValue.getElement().getId(), name, ProxyHelper.newComposite(InterferenceLinkUI.class));
                workspace.addInterferenceLink(element, Library.getFrequency(selectedValue.getElement().getElement()));
                model = workspace.getInterferenceLinkUIs();
                listModel.addElement( name );
                list.setSelectedIndex(listModel.getSize() - 1);
            }
        }

        updateButton();
    }

    public static String getILName(int number, SystemModel interferer) {
        return "Link "+number+ " (" +interferer.description().name()+")";
    }

    public void handleDupInterferenceLink() {
        InterferenceLinkElement link = model.get(list.getSelectedIndex());
        SystemModel system = workspace.getSystem(link.getInterferingSystemId());
        String name = getILName(model.size()+1, system);
        InterferenceLinkElement dup = new InterferenceLinkElement(link.getInterferingSystemId(), name, detailPanel.getModel());
        workspace.addInterferenceLink( dup, Library.getFrequency(system));
        model = workspace.getInterferenceLinkUIs();
        listModel.addElement( name );
        list.setSelectedIndex(listModel.getSize() - 1);
        updateButton();
    }

    private void deleteCurrentlySelectedInterferenceLink() {
        detailPanel = null;
        indexDeleted = list.getSelectedIndex();
        if (listModel.size() > 1) {
            listModel.removeElementAt( indexDeleted );
            model.remove( indexDeleted );
            workspace.getInterferingLinkFrequency().remove( indexDeleted );
            updateButton();
        } else {
            return;
        }
        List<ScenarioCheckResult> verify = consistencyCheck();
        if (!verify.isEmpty()) {
            MainWindow.displayScenarioCheckResults(verify, true, false, this);
        }
        // may be wrap SwingUtilities.invokeLater to be used if problem with
        // the list layout
        if (list.getModel().getSize() == indexDeleted) {
            list.setSelectedIndex(indexDeleted - 1);
        } else {
            list.setSelectedIndex(indexDeleted);
        }
        refreshFromModel();
    }

    /**
     * Check the list of interference links. Some links has been deleted so make
     * sure no other link is co-located with that
     *
     * @return potential list of errors. Empty list of there were no errors
     */
    private List<ScenarioCheckResult> consistencyCheck() {
        List<ScenarioCheckResult> warnings = new ArrayList<ScenarioCheckResult>();
        for (int i = 0; i < listModel.size(); i++) {
        }
        return warnings;
    }


    private DistributionItem frequency;
}
