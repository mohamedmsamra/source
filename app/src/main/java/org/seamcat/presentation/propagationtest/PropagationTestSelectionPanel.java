package org.seamcat.presentation.propagationtest;

import org.seamcat.presentation.propagationtest.AddRemovePanel.AddRemoveListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PropagationTestSelectionPanel extends JPanel {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);

    private JList list;
    private DefaultListModel listModel;
    private AddRemovePanel propagationTestAddRemovePanel;
    public interface SelectedModel {
        void selected( PropagationTestModel model);
    }

    public PropagationTestSelectionPanel(final SelectedModel selectedModel) {
        setLayout(new BorderLayout());
        listModel = new DefaultListModel();
        list = new JList(listModel);
        JScrollPane scrollPane = new JScrollPane(list);

        list.setBorder(BorderFactory.createEmptyBorder());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if ( !event.getValueIsAdjusting() ) {
                    int index = list.getSelectedIndex();
                    if (index != -1) {
                        selectedModel.selected((PropagationTestModel) listModel.get(index));
                    }
                }
            }
        });
        list.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    removeSelectedItem();
                }
            }
        });
        propagationTestAddRemovePanel = new AddRemovePanel();
        propagationTestAddRemovePanel.setLabelText(STRINGLIST.getString("LBL_ADD_REMOVE"));
        add(scrollPane, BorderLayout.CENTER);
        add(propagationTestAddRemovePanel, BorderLayout.NORTH);
        setBorder(new TitledBorder("Propagation list"));

    }

    public void addModel(PropagationTestModel model) {
        listModel.addElement(model);
        list.setSelectedIndex(listModel.getSize() - 1);
    }

    public void refreshFromModel() {
        list.updateUI();
        revalidate();
        repaint();
    }

    public void removeSelectedItem() {
        int index = list.getSelectedIndex();
        int size = listModel.getSize();
        if (index >= 0 && size > 1) {
            listModel.remove(index);

            if (index < listModel.getSize()) {
                list.setSelectedIndex(index);
            } else {
                list.setSelectedIndex(index - 1);
            }

        }
    }

    public PropagationTestModel getSelected() {
        return (PropagationTestModel) list.getSelectedValue();
    }

    public void addAddRemoveListener(AddRemoveListener addRemoveListener) {
        propagationTestAddRemovePanel.addAddRemoveListener(addRemoveListener);
    }

    public List<PropagationTestModel> getModels() {
        PropagationTestModel[] models = new PropagationTestModel[listModel.size()];
        listModel.copyInto(models);
        return Arrays.asList( models );
    }
}
