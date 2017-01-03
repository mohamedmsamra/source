package org.seamcat.presentation.components;

import com.rits.cloning.Cloner;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.EscapeDialog;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public abstract class GenericListDetailDialog<T> extends EscapeDialog {

    private final JList list;
    private JComponent detail;
    private JPanel detailPanel = new JPanel(new BorderLayout());

	public GenericListDetailDialog(JFrame parent, String windowTitle, Iterable<T> elements) {
		super(parent, true);
		setTitle(windowTitle);

		list = new JList();
        DefaultListModel model = new DefaultListModel();
        list.setModel(model);
        for (T element : elements) {
            model.addElement( element );
        }

        list.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if ( !event.getValueIsAdjusting() ) {
                    selectedElement((T)list.getSelectedValue());
                }
            }
        });
		getContentPane().setLayout(new BorderLayout());
        JScrollPane pane = new JScrollPane();
        pane.setViewportView( list );
        JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        jSplitPane.add(pane);
        jSplitPane.add(new JScrollPane(detailPanel));
        jSplitPane.setDividerLocation(200);

        getContentPane().add(jSplitPane, BorderLayout.CENTER);
		getContentPane().add( new NavigateButtonPanel(this), BorderLayout.SOUTH);
		setSize( new Dimension(800, 600));
		setLocationRelativeTo( parent );
        list.setSelectedIndex(0);
	}

	public T getSelectedValue() {
        T selectedValue = (T) list.getSelectedValue();
        if ( selectedValue instanceof PluginConfiguration) {
            return (T) ((PluginConfiguration) selectedValue).deepClone();
        }
        return new Cloner().deepClone( (T)list.getSelectedValue() );
	}

    public void setDetail( JComponent detail ) {
        if ( this.detail != null) {
            detailPanel.remove(this.detail);
        }
        this.detail = detail;
        detailPanel.add(detail);
        detailPanel.updateUI();
    }

    public abstract void selectedElement(T t);
}
