package org.seamcat.presentation.builder;

import org.seamcat.model.types.LibraryItem;
import org.seamcat.presentation.library.LibraryItemWrapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class SelectableTablePanel extends JPanel {

    protected DefaultTableModel model;
    protected int selectionRow;

    public SelectableTablePanel( int selectionRow ) {
        this.selectionRow = selectionRow;
    }

    protected boolean selected( int row ) {
        return  (Boolean) model.getValueAt( row, selectionRow );
    }

    public void setAll( boolean enable ) {
        for ( int i=0; i<model.getRowCount(); i++) {
            model.setValueAt( enable, i, selectionRow );
        }
    }

    public List<LibraryItem> selectedItems() {
        ArrayList<LibraryItem> list = new ArrayList<LibraryItem>();
        for ( int i=0; i<model.getRowCount(); i++) {
            if ( selected(i) ) {
                list.add(((LibraryItemWrapper) model.getValueAt(i, 0)).getItem());
            }
        }
        return list;
    }

    @AsActionListener("select")
    private void select() {
        setAll(true);
    }

    @AsActionListener("deselect")
    private void deselect() {
        setAll(false);
    }
}
