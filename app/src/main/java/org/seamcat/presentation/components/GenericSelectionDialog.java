package org.seamcat.presentation.components;

import com.rits.cloning.Cloner;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.library.LibraryItemWrapper;

import javax.swing.*;
import java.awt.*;

public class GenericSelectionDialog<T> extends EscapeDialog {

    private JList list;
    private boolean clone;

    public GenericSelectionDialog(JFrame parent, String windowTitle, Iterable<T> elements ) {
        this(parent, windowTitle, elements, true);
    }

	public GenericSelectionDialog(JFrame parent, String windowTitle, Iterable < T > elements, boolean clone) {
		super(parent, true);
        this.clone = clone;
		setTitle(windowTitle);

        DefaultListModel model = new DefaultListModel();
        list = new JList(model);
        for (T element : elements) {
            if ( element instanceof LibraryItem) {
                model.addElement( new LibraryItemWrapper(model.getSize(), (LibraryItem) element));
            } else {
                model.addElement( element );
            }
        }
        JScrollPane pane = new JScrollPane();
        pane.setViewportView( list );

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add( pane, BorderLayout.CENTER);
		getContentPane().add(new NavigateButtonPanel(this), BorderLayout.SOUTH);
		setSize( new Dimension(450,450));
		setLocationRelativeTo(parent);
        list.setSelectedIndex(0);
	}

	public T getSelectedValue() {
        Object value = list.getSelectedValue();
        if ( !clone ) {
            if ( value instanceof LibraryItemWrapper ) {
                return (T) ((LibraryItemWrapper) value).getItem();
            } else {
                return (T) value;
            }
        }
        if ( value instanceof PluginConfiguration) {
            return (T) ((PluginConfiguration) value).deepClone();
        } else if ( value instanceof LibraryItemWrapper ) {
            return new Cloner().deepClone((T)((LibraryItemWrapper) value).getItem());
        }

        return new Cloner().deepClone((T) list.getSelectedValue());
    }

    public int getSelectedIndex() {
        return list.getSelectedIndex();
    }
}
