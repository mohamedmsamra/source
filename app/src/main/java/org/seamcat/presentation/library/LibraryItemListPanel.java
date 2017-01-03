package org.seamcat.presentation.library;

import com.rits.cloning.Cloner;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.commands.DisplayHelpNameCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.TextWidgetValueUpdatedEvent;
import org.seamcat.exception.SeamcatErrorException;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.Library;
import org.seamcat.model.factory.Model;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.types.*;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.plugin.JarConfigurationModel;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.FileDialogHelper;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.NavigateButtonPanel;
import org.seamcat.presentation.menu.ToolBar;
import org.seamcat.presentation.systems.Helper;
import org.seamcat.util.StringHelper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class LibraryItemListPanel<M extends LibraryItem> extends EscapeDialog {

    private static final Logger LOG = Logger.getLogger(LibraryItemListPanel.class);
    private static final Set<Class<? extends LibraryItem>> addClasses;
    private static final Set<Class<? extends LibraryItem>> duplicateClasses;
    private static final Set<Class<? extends LibraryItem>> restoreClasses;

    static {
        addClasses = new HashSet<>(Arrays.asList(BlockingMask.class, EmissionMask.class,
                CDMALinkLevelData.class, ReceiverModel.class, TransmitterModel.class,
                JarConfigurationModel.class));

        duplicateClasses = new HashSet<>(Arrays.asList(BlockingMask.class, EmissionMask.class,
                CDMALinkLevelData.class, ReceiverModel.class, TransmitterModel.class,
                AntennaGain.class, CoverageRadius.class, PropagationModel.class,
                EventProcessing.class, SystemModel.class));

        restoreClasses = new HashSet<>(Arrays.asList(SystemModel.class, BlockingMask.class, EmissionMask.class,
                CDMALinkLevelData.class, ReceiverModel.class, TransmitterModel.class, AntennaGain.class,
                CoverageRadius.class, PropagationModel.class, EventProcessing.class));
    }


    private final JFrame dialog;
    private final int minimumSize;
    public int indexDeleted;
    private Class<M> clazz;
    private LibraryDetailPanel<M> detailPanel;
    private DefaultListModel listModel;
    private JList list;
    private JButton delete;
    private JButton duplicate;
    private JToolBar toolBar = new JToolBar();
    private JFrame ownerFrame;
    private ChangeNotifier notifier;

    public LibraryItemListPanel(JFrame owner, Class<M> clazz, String title, String helpClass, int width, int height, int minimumSize) {
        super(owner, true);
        setTitle( title );
        this.clazz = clazz;
        ownerFrame = owner;
        this.dialog = owner;
        this.minimumSize = minimumSize;
        toolBar.setFocusable(false);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        notifier = new ChangeNotifier() {
            public void changed() {
                updateModel();
            }
        };
        setupToolbar( helpClass);
        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(list);
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(toolBar, BorderLayout.NORTH);
        leftPanel.add(jScrollPane, BorderLayout.CENTER);
        leftPanel.add(new NavigateButtonPanel(this, false) {
            @Override
            public void btnOkActionPerformed() {
                super.btnOkActionPerformed();
                closing();
                destroy();
            }

            @Override
            public void btnCancelActionPerformed() {
                super.btnCancelActionPerformed();
                destroy();
            }
        }, BorderLayout.SOUTH);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.add(leftPanel);
        splitPane.add(new JPanel());
        splitPane.setDividerLocation(180);

        listModel = new DefaultListModel();
        list.setModel(listModel);
        list.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                if (list.getModel().getSize() == 0) {
                    splitPane.setRightComponent(new JPanel());
                }

                if (!listModel.isEmpty() && list.getSelectedIndex() != -1) {
                    updateModel();
                    M model = ((LibraryItemWrapper<M>) listModel.get(list.getSelectedIndex())).getItem();
                    detailPanel = new LibraryDetailPanel<M>(ownerFrame, LibraryItemListPanel.this.clazz, new LibraryItemWrapper<M>(list.getSelectedIndex(), model), notifier);
                    splitPane.setRightComponent(detailPanel);
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyChar() == KeyEvent.VK_DELETE) {
                    handleDelete();
                }
            }
        });
        setModel(Model.getInstance().getLibrary().getGroup(clazz));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(splitPane, BorderLayout.CENTER);
        setSize( width, height);
        setLocationRelativeTo(owner);

        // Disable resizing if METAL L&F
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf != null) {
            Class<? extends LookAndFeel> lafClass = laf.getClass();
            if (lafClass != null) {
                if (lafClass.toString().contains("MetalLookAndFeel")) {
                    setResizable(false);
                    if (Log.isDebugEnabled()) {
                        Log.debug("Metal LAF detected. Resizing of interfering link dialog disabled");
                    }
                }
            }
        }
        EventBusFactory.getEventBus().subscribe( this );
    }

  

	@UIEventHandler
    public void handle(TextWidgetValueUpdatedEvent event) {
        if ( detailPanel != null ) {
            if ( detailPanel.match( event.getContext() ) ) {
                updateModel();
            }
        }
    }


    private void setupToolbar( String helpClass) {
        JButton button;
        if ( addClasses.contains( clazz )) {
            button = ToolBar.button("SEAMCAT_ICON_ADD", "TOOLBAR_LIBRARY_ADD_TOOLTIP", null);
            button.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    handleAdd();
                }
            });
            toolBar.add(button);
        }

        if ( duplicateClasses.contains( clazz )) {
            duplicate = ToolBar.button("SEAMCAT_ICON_DUPLICATE","TOOLBAR_LIBRARY_DUPLICATE_TOOLTIP", null);
            duplicate.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    handleDuplicate();
                }
            });
            toolBar.add(duplicate);
        }

        delete = ToolBar.button("SEAMCAT_ICON_DELETE_TRASH","TOOLBAR_LIBRARY_DELETE_TOOLTIP", null);
        delete.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                handleDelete();
            }
        });
        toolBar.add( delete );

        if ( restoreClasses.contains(clazz)) {
            button = ToolBar.button("SEAMCAT_ICON_RESTORE_DEFAULTS","TOOLBAR_LIBRARY_RESTORE_DEFAULTS_TOOLTIP", null);
            button.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    if ( DialogHelper.restoreLibrary(dialog) ) {
                        resetToDefaults();
                    }
                }
            });
            toolBar.add( button );
        }

        toolBar.addSeparator();
        toolBar.add(ToolBar.button("SEAMCAT_ICON_HELP", "HELP_CONTENTS_MENU_ITEM_TEXT",
                new DisplayHelpNameCommand( helpClass )));
    }


    private void updateButton() {
        boolean someSelected = list.getSelectedValue() != null && list.getModel().getSize() >minimumSize;

        delete.setEnabled(someSelected);
        if ( duplicate != null )   {
            duplicate.setEnabled( someSelected );
        }
    }

    private void setModel( Collection<M> elements ) {
        listModel.clear();
        for ( M model : elements ) {
            listModel.addElement( new LibraryItemWrapper<M>(listModel.size(), model) );
        }
        list.setSelectedIndex(0);
        updateButton();
    }
    public void handleAdd() {
        if ( clazz == EmissionMask.class ) {
            add((M) new EmissionMaskImpl());
        } else if ( clazz == BlockingMask.class) {
            add((M) new BlockingMaskImpl());
        } else if ( clazz == JarConfigurationModel.class ) {
            FileDialogHelper helper = new FileDialogHelper();
            if ( helper.chooseJar(this).selectionMade() ){
                File selectedJarFile = helper.getSelectedFile();
                try {
                    if (selectedJarFile.exists() && selectedJarFile.canRead()) {
                        final JarConfigurationModel model = new JarConfigurationModel(selectedJarFile);
                        if ( !getModel().contains( model) ) {
                            add((M)model);
                        }
                    } else {
                        LOG.warn(String.format("File %s does not exist or cannot be read", selectedJarFile.getName()));
                    }

                } catch (SeamcatErrorException e ){
                    DialogHelper.generalSeamcatError(e.getMessage());
                }
            }
        } else if ( clazz == CDMALinkLevelData.class ) {
            add((M) new CDMALinkLevelData());
        } else {
            String title = "DEFAULT_TX";
            if ( ReceiverModel.class.isAssignableFrom(clazz)) {
                title = "DEFAULT_RX";
            }
            M model = (M) ProxyHelper.newComposite(clazz, getDuplicatedName( title ));
            add(model);
        }
    }

    public void resetToDefaults() {
        detailPanel = null;
        List<M> defaults = Model.getDefaultsForType(clazz);
        setModel( defaults );
    }

    private void handleDuplicate() {
        M model = detailPanel.getModel();
        M clone;
        if ( model instanceof PluginConfiguration) {
            PluginConfiguration configuration = ((PluginConfiguration) model).deepClone();
            configuration.setName( getDuplicatedName(model.description().name()));
            clone = (M) configuration;
        } else if ( model instanceof BlockingMaskImpl ) {
            BlockingMaskImpl mask = (BlockingMaskImpl) new Cloner().deepClone( model );
            mask.setDescription(new DescriptionImpl(getDuplicatedName(mask.description().name()), mask.description().description()));
            clone = (M) mask;
        } else if ( model instanceof EmissionMaskImpl ) {
            EmissionMaskImpl mask = (EmissionMaskImpl) new Cloner().deepClone(model );
            mask.setDescription(new DescriptionImpl(getDuplicatedName(mask.description().name()), mask.description().description()));
            clone = (M) mask;
        } else if ( model instanceof CDMALinkLevelData ) {
            CDMALinkLevelData lld = (CDMALinkLevelData) new Cloner().deepClone( model );
            lld.setSystem( getDuplicatedName(lld.getSystem()) );
            clone = (M) lld;
        } else {
            Description name = Helper.changeName(model.description(), getDuplicatedName(model.description().name()));
            if ( model instanceof SystemModel ) {
                SystemModel sys = (SystemModel) model;
                clone = (M) ProxyHelper.deepCloneComposite( Library.getSystemModelClass(sys), sys, Description.class, name );
            } else {
                clone = ProxyHelper.deepCloneComposite( clazz, model, Description.class, name );
            }
        }
        add(clone);
    }

    protected void add( M model ) {
        listModel.addElement( new LibraryItemWrapper<M>(listModel.size(), model));
        list.setSelectedIndex(listModel.getSize() - 1);
        updateButton();
    }

    private void handleDelete() {
        LibraryItemWrapper<M> model = (LibraryItemWrapper<M>) list.getSelectedValue();
        indexDeleted = list.getSelectedIndex();
        if (model != null && confirmDelete(model.getItem())) {
            detailPanel = null;
            listModel.removeElement(model);
            if (list.getModel().getSize() == indexDeleted) {
                list.setSelectedIndex(indexDeleted - 1);
            } else {
                list.setSelectedIndex(indexDeleted);
            }
            updateButton();
        }
    }

    public boolean confirmDelete(M selected) {
        if ( selected instanceof JarConfigurationModel ) {
            JarConfigurationModel toBeDeleted = (JarConfigurationModel) selected;
            if ( toBeDeleted.getPluginClasses() != null ) {
                List<PluginConfiguration> instances = Model.getInstance().getLibrary().getConfigurationsForJar(toBeDeleted.getHash());
                if ( !instances.isEmpty() ) {
                    StringBuilder sb = new StringBuilder("To delete selected jar-file the following library configuration(s) will also be deleted:\n");
                    for (PluginConfiguration instance : instances) {
                        if ( instance != null && instance.description() !=  null ) {
                            sb.append("* ").append(instance.description().name()).append("\n");
                        }
                    }
                    sb.append("\nProceed?");

                    return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                            sb.toString(), "Deleting configurations",
                            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            // check if deleted item is the last of its kind
            if (selected instanceof SystemModel ) {
                Class<SystemModel> aClass = Library.getSystemModelClass((SystemModel) selected);
                int hits = 0;
                for (M m : getModel()) {
                    if ( aClass.isAssignableFrom( m.getClass() )) {
                        hits++;
                    }
                }
                if ( hits < 2 ) {
                    int result = JOptionPane.showConfirmDialog(this, "You are about to delete the last system of this type.\n" +
                            "SEAMCAT will automatically create a default instance when closing the library.",
                            "Removing last of it kind", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                    return ( result == JOptionPane.OK_OPTION );
                }
            }
        }
        return true;
    }

    protected void updateModel() {
        if ( detailPanel != null ) {
            M model = detailPanel.getModel();
            int index = detailPanel.getIndex();
            if ( index < listModel.size() ) {
                listModel.setElementAt( new LibraryItemWrapper<M>(index, model), index );

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        list.updateUI();
                    }
                });
            }
        }
    }

    void closing() {
        updateModel();
        Library library = Model.getInstance().getLibrary();
        library.writeAll(clazz, getModel());
        library.ensureConsistentLibrary();
    }

    void destroy() {
        EventBusFactory.getEventBus().unsubscribe( this );
    }
    public List<M> getModel() {
        List<M> elements = new ArrayList<M>();
        for (int i=0; i<listModel.size(); i++) {
            elements.add(((LibraryItemWrapper<M>) listModel.get(i)).getItem());
        }
        return elements;
    }

    protected String getDuplicatedName(String original) {
        return StringHelper.getDuplicatedName(original, listModel );
    }
}
