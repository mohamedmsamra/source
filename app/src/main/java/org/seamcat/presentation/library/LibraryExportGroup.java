package org.seamcat.presentation.library;

import org.seamcat.model.factory.Model;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.presentation.builder.PanelBuilder;
import org.seamcat.presentation.builder.SelectableTablePanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LibraryExportGroup extends SelectableTablePanel {

    public LibraryExportGroup(List<? extends LibraryItem> group ) {
        super(2);
        setLayout(new BorderLayout());
        JTable table = new JTable();
        table.setFillsViewportHeight(true);
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

        DefaultTableModel model = new DefaultTableModel() {
            public Class<?> getColumnClass(int columnIndex) {
                if ( columnIndex == 0 ) return String.class;
                if ( columnIndex == 1 ) return String.class;
                return Boolean.class;
            }
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 1;
            }
            public String getColumnName(int column) {
                if ( column == 0 ) return "Name";
                if ( column == 1 ) return "Type";
                return "Export";
            }
        };
        table.setModel(model);
        this.model = model;

        model.setColumnCount(3);
        table.getColumnModel().getColumn(0).setPreferredWidth(500);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        JPanel control = PanelBuilder.panel(new FlowLayout(FlowLayout.LEFT))
                .button("Select All", this, "select")
                .button("Deselect", this, "deselect").get();

        addLibraryList(group, model );
        Dimension d = table.getPreferredSize();
        scrollPane.setPreferredSize(
                new Dimension(d.width-15,table.getRowHeight()*(group.size()+2)));

        add(scrollPane, BorderLayout.CENTER);
        add(control, BorderLayout.SOUTH);

    }
    private void addLibraryList( List<? extends LibraryItem> list, DefaultTableModel model ) {
        for (LibraryItem item : list) {
            model.addRow(new Object[]{new LibraryItemWrapper(0, item), Model.getInstance().getLibrary().typeName(item), true});
        }
    }
}
