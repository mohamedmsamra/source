package org.seamcat.presentation.systems;

import org.seamcat.commands.DisplayHelpNameCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.model.IdElement;
import org.seamcat.model.InterferenceLinkElement;
import org.seamcat.model.Library;
import org.seamcat.model.Workspace;
import org.seamcat.model.factory.Model;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.SystemModelGeneric;

import org.seamcat.model.systems.generic.TransmitterReceiverPathModel;
import org.seamcat.model.types.CoverageRadius;
import org.seamcat.model.types.Description;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.WorkspaceView;
import org.seamcat.presentation.components.GenericSelectionDialog;
import org.seamcat.presentation.genericgui.panelbuilder.CompositeEditor;
import org.seamcat.presentation.library.ChangeNotifier;
import org.seamcat.presentation.library.LibraryDetailPanel;
import org.seamcat.presentation.library.LibraryItemWrapper;
import org.seamcat.presentation.menu.ToolBar;
import org.seamcat.util.StringHelper;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.UUID;

import static org.seamcat.presentation.systems.Helper.changeName;

public class SystemsPanel extends JPanel {

    private final JSplitPane splitPane;
    public int indexDeleted;
    private JPanel detail = new JPanel( new BorderLayout());
    private LibraryDetailPanel<? extends SystemModel> detailPanel;
    private DefaultListModel listModel;
    private JList list;
    private JButton delete, duplicate, export;
    private JToolBar toolBar = new JToolBar();
    private ChangeNotifier notifier;
    private List<IdElement<SystemModel>> model;
    private WorkspaceView parent;
    private Workspace workspace;
    private int index;

    public SystemsPanel( final WorkspaceView parent, Workspace workspace) {
        this.parent = parent;
        this.workspace = workspace;
        index = 0;
        this.model = workspace.getSystemModels();
        toolBar.setFocusable(false);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        notifier = new ChangeNotifier() {
            public void changed() {
                updateModel();
                parent.updatePanels();
            }
        };
        setupToolbar( null );
        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setViewportView(list);
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(toolBar, BorderLayout.NORTH);
        leftPanel.add(jScrollPane, BorderLayout.CENTER);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.add(leftPanel);
        splitPane.add(detail);
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
                    detail.removeAll();
                    detail.add( new JPanel(), BorderLayout.CENTER);
                }

                if (!listModel.isEmpty() && list.getSelectedIndex() != -1) {
                    updateModel();
                    index = list.getSelectedIndex();
                    showDetail();
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
        setLayout(new BorderLayout());
        add( splitPane, BorderLayout.CENTER );
        refreshFromModel();
        EventBusFactory.getEventBus().subscribe( this );
    }

    private void showDetail() {
        SystemListItem item = (SystemListItem) listModel.get(list.getSelectedIndex());
        SystemModel sysModel = item.getElement().getElement();
        detailPanel = new LibraryDetailPanel<>(MainWindow.getInstance(), Library.getSystemModelClass(sysModel),
                new LibraryItemWrapper<>(list.getSelectedIndex(), sysModel), notifier, workspace);
        detail.removeAll();
        detail.add( detailPanel, BorderLayout.CENTER );
        detail.revalidate();
        detail.repaint();
    }


    private void setupToolbar( String helpClass) {
        JButton button = ToolBar.button("SEAMCAT_ICON_IMPORT_LIBRARY", "TOOLBAR_SYSTEMS_PANEL_IMPORT", null);
        button.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                handleImport();
            }
        });
        toolBar.add(button);

        export = ToolBar.button("SEAMCAT_ICON_EXPORT_LIBRARY", "TOOLBAR_SYSTEMS_PANEL_EXPORT", null);
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handleExport();
            }
        });
        toolBar.add( export );

        duplicate = ToolBar.button("SEAMCAT_ICON_DUPLICATE","TOOLBAR_LIBRARY_DUPLICATE_TOOLTIP", null);
        duplicate.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                handleDuplicate();
            }
        });
        toolBar.add(duplicate);

        delete = ToolBar.button("SEAMCAT_ICON_DELETE_TRASH","TOOLBAR_LIBRARY_DELETE_TOOLTIP", null);
        delete.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                handleDelete();
            }
        });
        toolBar.add( delete );

        toolBar.addSeparator();
        toolBar.add(ToolBar.button("SEAMCAT_ICON_HELP", "HELP_CONTENTS_MENU_ITEM_TEXT",
                new DisplayHelpNameCommand( helpClass )));
    }


    private void updateButton() {
        boolean someSelected = list.getSelectedValue() != null;

        duplicate.setEnabled(someSelected);
        delete.setEnabled(someSelected);
        export.setEnabled(someSelected);
    }

    public void refreshFromModel(  ) {
        detailPanel = null;
        listModel.clear();
        for ( IdElement<SystemModel> model : this.model ) {
            listModel.addElement( new SystemListItem(model));
        }
        list.setSelectedIndex( index );
        updateButton();
    }

    public void handleImport() {
        List<SystemModel> group = Model.getInstance().getLibrary().getGroup(SystemModel.class);
        GenericSelectionDialog<SystemModel> dialog = new GenericSelectionDialog<SystemModel>(MainWindow.getInstance(),
                "Select library system", group, false);
        if ( dialog.display() ) {
            index = model.size();
            model.add( new IdElement<SystemModel>(UUID.randomUUID().toString(), dialog.getSelectedValue()) );
            parent.updatePanels();
            updateModel();
            refreshFromModel();
        }
    }

    public void handleExport() {
        SystemModel model = detailPanel.getModel();
        if ( !Model.getInstance().getLibrary().addSystem(model)) {
            if ( DialogHelper.overrideInLibrary(parent, model.description().name()) ) {
                Model.getInstance().getLibrary().replaceNamedSystem(model);
                Model.getInstance().persist();
                EventBusFactory.getEventBus().publish( new InfoMessageEvent(String.format("'%s' overridden in system library", model.description().name())));
            }
        } else {
            Model.getInstance().persist();
            EventBusFactory.getEventBus().publish( new InfoMessageEvent(String.format("'%s' added to system library", model.description().name())));
        }
    }

    private void handleDuplicate() {
        SystemModel model = detailPanel.getModel();
        Description name = changeName( model.description(), getDuplicatedName(model.description().name()));
        SystemModel clone = ProxyHelper.deepCloneComposite(Library.getSystemModelClass(model), model, Description.class, name );
        index = this.model.size();
        this.model.add( new IdElement<>(UUID.randomUUID().toString(), clone) );
        parent.updatePanels();
        updateModel();
        refreshFromModel();
    }

    private void handleDelete() {
        SystemListItem model = (SystemListItem) list.getSelectedValue();
        // make sure system is not referenced
        if ( workspace.getVictimSystemId().equals( model.getElement().getId() ) ) {
            DialogHelper.cannotDeleteUsedSystem( model.getElement().getElement().description().name() );
            return;
        }

        for (InterferenceLinkElement element : workspace.getInterferenceLinkUIs()) {
            if ( element.getInterferingSystemId().equals( model.getElement().getId()) ) {
                DialogHelper.cannotDeleteUsedSystem( model.getElement().getElement().description().name() );
                return;
            }
        }

        indexDeleted = list.getSelectedIndex();
        detailPanel = null;
        this.model.remove( indexDeleted);
        if (this.model.size() == indexDeleted) {
            index = indexDeleted - 1;
        } else {
            index = indexDeleted;
        }
        parent.updatePanels();
        updateModel();
        refreshFromModel();
    }

    public void setModel( int index, SystemModel model ) {
        if ( index < listModel.size() ) {
            SystemListItem current = (SystemListItem) listModel.getElementAt(index);
            IdElement<SystemModel> updated = new IdElement<SystemModel>(current.getElement().getId(), model);
            listModel.setElementAt( new SystemListItem(updated), index );
            this.model.set( index, updated);
            refreshFromModel();
        }
    }

    public void updateModel() {
        if ( detailPanel != null ) {
            SystemModel model = detailPanel.getModel();
            int index = detailPanel.getIndex();
            if ( index < listModel.size() ) {
                SystemListItem current = (SystemListItem) listModel.getElementAt(index);
                IdElement<SystemModel> updated = new IdElement<SystemModel>(current.getElement().getId(), model);
                listModel.setElementAt( new SystemListItem(updated), index );
                this.model.set( index, updated);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        list.updateUI();
                    }
                });
            }
        }
    }

    protected String getDuplicatedName(String original) {
        return StringHelper.getDuplicatedName(original, listModel);
    }

    public void handleEnablement() {
        SystemModel model = detailPanel.getModel();
        if ( model instanceof SystemModelGeneric ) {
            TransmitterReceiverPathModel path = ((SystemModelGeneric) model).path();
            boolean corrEnabled = path.relativeLocation().useCorrelatedDistance();

            Component component = detailPanel.getComponent();
            if ( component instanceof CompositeEditor ) {
                ((CompositeEditor) component).enableWidget(CoverageRadius.class, !corrEnabled);
            }
        }
    }
}
