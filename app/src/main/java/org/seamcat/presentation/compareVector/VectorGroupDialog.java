package org.seamcat.presentation.compareVector;

import org.seamcat.model.types.result.NamedVectorResult;
import org.seamcat.model.types.result.VectorGroupResultType;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.propagationtest.PropagationHolder;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VectorGroupDialog extends EscapeDialog {

    private CompareVectorPanel compareVectorPanel;
    private JList list;
    private DefaultListModel listModel;

    public VectorGroupDialog( final VectorGroupResultType group ) {
        compareVectorPanel = new CompareVectorPanel(this);

        list = new JList();
        listModel = new DefaultListModel();
        for (NamedVectorResult vector : group.getVectorGroup()) {
            listModel.addElement(vector);
        }
        list.setModel( listModel );
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if ( !event.getValueIsAdjusting() ) {
                    List selected = list.getSelectedValuesList();
                    List<PropagationHolder> result = new ArrayList<PropagationHolder>();
                    for (Object o : selected) {
                        NamedVectorResult v = (NamedVectorResult) o;
                        PropagationHolder holder = new PropagationHolder();
                        holder.setTitle( v.getName() );
                        holder.setData(v.getVector().asArray());
                        result.add( holder );
                    }
                    compareVectorPanel.show(result, group.getUnit());
                }
            }
        });
        if ( listModel.size() > 0 ) {
            list.setSelectedIndex(0);
        }

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add( new JScrollPane(list), BorderLayout.CENTER );
        setLayout(new BorderLayout());
        getContentPane().add( new BorderPanel(listPanel,"Vector group"), BorderLayout.WEST );
        getContentPane().add( compareVectorPanel, BorderLayout.CENTER );

        setTitle(group.getName());

        JDialog.setDefaultLookAndFeelDecorated(true);
        pack();
        setLocationRelativeTo(MainWindow.getInstance());
        setVisible(true);
    }



}
