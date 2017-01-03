package org.seamcat.presentation.library;

import org.seamcat.interfaces.Dispatcher;
import org.seamcat.interfaces.DuplicateVisitor;
import org.seamcat.model.Library;
import org.seamcat.model.factory.Model;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.builder.AsActionListener;
import org.seamcat.presentation.builder.SelectableTablePanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryImportExportDialog extends EscapeDialog {

    protected final Library library;
    protected final ArrayList<SelectableTablePanel> groups;

    public LibraryImportExportDialog(JFrame owner, String title, int width, int height ) {
        super(owner,true );
        setTitle( title );
        setSize( width, height );
        library = Model.getInstance().getLibrary();
        groups = new ArrayList<SelectableTablePanel>();
    }

    public List<LibraryItem> findDuplicateNames() {
        List<LibraryItem> duplicates = new ArrayList<LibraryItem>();
        List<LibraryItem> selected = selectedItems();
        DuplicateVisitor visitor = new DuplicateVisitor(library);
        for (LibraryItem identifiable : selected) {
            if (Dispatcher.dispatch(visitor, identifiable) ) {
                duplicates.add( identifiable );
            }
        }
        return duplicates;
    }

    public List<LibraryItem> selectedItems() {
        List<LibraryItem> result = new ArrayList<LibraryItem>();
        for (SelectableTablePanel group : groups) {
            result.addAll(group.selectedItems());
        }
        return result;
    }

    @AsActionListener("deselect")
    private void deSelectAll() {
        for (SelectableTablePanel group : groups) {
            group.setAll( false );
        }
    }

    @AsActionListener("select")
    private void selectAll() {
        for (SelectableTablePanel group : groups) {
            group.setAll( true );
        }
    }

}
