package org.seamcat.presentation.library;

import org.seamcat.function.MutableLibraryItem;
import org.seamcat.model.Library;
import org.seamcat.model.factory.Model;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.presentation.builder.SelectableTablePanel;
import org.seamcat.util.StringHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.presentation.builder.PanelBuilder.panel;

public class LibraryImportGroup extends SelectableTablePanel {

    public LibraryImportGroup( List<? extends LibraryItem> imports, List<? extends LibraryItem> existing ) {
        super(2);
        setLayout(new BorderLayout());
        JTable table = new JTable();
        table.setFillsViewportHeight(true);
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

        final DefaultTableModel model = new DefaultTableModel() {
            public Class<?> getColumnClass(int columnIndex) {
                if ( columnIndex == 0 ) return String.class;
                if ( columnIndex == 1 ) return String.class;
                if ( columnIndex == 2 ) return Boolean.class;
                if ( columnIndex == 3 ) return Boolean.class;
                return String.class;
            }

            public boolean isCellEditable(int row, int column) {
                if ( column == 3 ) {
                    return enabled(this, row, 2);
                }
                if ( column == 4 ) {
                    return enabled(this, row, 2) && !enabled(this, row, 3);
                }
                return column != 0 && column != 1;
            }

            public String getColumnName(int column) {
                if ( column == 0 ) return "Name";
                if ( column == 1 ) return "Type";
                if ( column == 2 ) return "Import";
                if ( column == 3 ) return "Override";
                return "Rename (only editable when override deselected)";
            }
        };

        table.setModel(model);
        this.model = model;
        model.setColumnCount(5);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);

        addLibraryList(imports, existing, model);

        JPanel control = panel(new FlowLayout(FlowLayout.LEFT))
                .button("Select All", this, "select" )
                .button("Deselect All", this, "deselect").get();

        Dimension d = table.getPreferredSize();
        scrollPane.setPreferredSize(
                new Dimension(d.width-15,table.getRowHeight()*(imports.size()+2)));

        add(scrollPane, BorderLayout.CENTER);
        add(control, BorderLayout.SOUTH);
    }

    private boolean enabled( DefaultTableModel model, int row, int column ) {
        return (Boolean) model.getValueAt( row, column );
    }

    private void addLibraryList( List<? extends LibraryItem> list, List<? extends LibraryItem> library, DefaultTableModel model ) {
        List<String> existing = new ArrayList<String>();
        for (LibraryItem identifiable : library) {
            existing.add(identifiable.description().name());
        }
        for (LibraryItem item : list) {
            String suggestName = "";
            if ( existing.contains( item.description().name() ) ) {
                // suggest a rename title
                suggestName = StringHelper.getDuplicatedName(item.description().name(), existing);
            }
            model.addRow(new Object[]{ new LibraryItemWrapper(0, item), Model.getInstance().getLibrary().typeName(item), true, false, suggestName});
        }
    }

    public List<LibraryItem> selectedItems() {
        ArrayList<LibraryItem> list = new ArrayList<LibraryItem>();
        for ( int i=0; i<model.getRowCount(); i++) {
            if ( selected(i) ) {
                LibraryItem id = ((LibraryItemWrapper) model.getValueAt(i, 0)).getItem();
                if ( !((Boolean) model.getValueAt(i, 3))) {
                    // rename selected but only take it if rename value is non-empty
                    String reference = model.getValueAt(i, 4).toString();
                    if ( reference != null && !reference.isEmpty()) {
                        if ( id instanceof SystemModel ) {
                            Class<SystemModel> aClass = Library.getSystemModelClass((SystemModel) id);
                            id = ProxyHelper.deepCloneComposite(aClass, (SystemModel)id, Description.class, new DescriptionImpl(reference, id.description().description()));
                        } else if (id instanceof TransmitterModel ) {
                            id = ProxyHelper.deepCloneComposite(TransmitterModel.class, (TransmitterModel)id, Description.class, new DescriptionImpl(reference, id.description().description()));
                        } else if ( id instanceof ReceiverModel ) {
                            id = ProxyHelper.deepCloneComposite(ReceiverModel.class, (ReceiverModel)id, Description.class, new DescriptionImpl(reference, id.description().description()));
                        }
                           else if ( id instanceof T_ReceiverModel ) {
                                id = ProxyHelper.deepCloneComposite(T_ReceiverModel.class, (T_ReceiverModel)id, Description.class, new DescriptionImpl(reference, id.description().description()));
                        } else {
                            ((MutableLibraryItem) id).setDescription(new DescriptionImpl(reference, id.description().description()));
                        }
                    }
                }
                list.add(id);
            }
        }
        return list;
    }
}
