package org.seamcat.presentation.components;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.presentation.menu.ToolBar;
import org.seamcat.scenario.MutableLocalEnvironment;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static org.seamcat.model.factory.Factory.prototype;
import static org.seamcat.model.factory.Factory.when;

public class LocalEnvironmentsPanel extends JPanel {

    private final JButton add;
    private final JButton delete;
    private JList list;
    private DefaultListModel listModel;
    private JToolBar toolBar;
    private List<LocalEnvironment> localEnvironments;

    public LocalEnvironmentsPanel(String title) {
        list = new JList();
        listModel = new DefaultListModel();
        list.setModel( listModel );

        toolBar = new JToolBar();
        toolBar.setFocusable(false);
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.add( new JLabel(title));
        add = ToolBar.button("SEAMCAT_ICON_ADD", "TOOLBAR_LOCALENVIRONMENT_ADD", null);
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleAdd();
            }
        });
        toolBar.add(add);
        delete = ToolBar.button("SEAMCAT_ICON_DELETE_TRASH","TOOLBAR_LOCALENVIRONMENT_REMOVE", null);
        delete.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleDelete();
            }
        });
        toolBar.add(delete);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(toolBar, BorderLayout.NORTH);
        JScrollPane pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(350, 75));
        panel.add(pane, BorderLayout.CENTER);

        list.addListSelectionListener( new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if ( !event.getValueIsAdjusting() ) {
                    // update relevance of buttons
                }
            }
        });
        list.addMouseListener( new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ( e.getClickCount() > 1 ) {
                    MutableLocalEnvironment value = (MutableLocalEnvironment) list.getSelectedValue();
                    if ( value != null && value.getEnvironment() != LocalEnvironment.Environment.Outdoor ) {
                        LocalEnvironmentUI proto = prototype(LocalEnvironmentUI.class);
                        when( proto.probability() ).thenReturn( value.getProbability()*100 );
                        when( proto.wallLoss() ).thenReturn( value.getWallLoss() );
                        when( proto.stdDev()).thenReturn( value.getWallLossStdDev());
                        LocalEnvironmentDialog dialog = new LocalEnvironmentDialog(Factory.build(proto));
                        if (dialog.display()) {
                            LocalEnvironmentUI model = dialog.getModel();
                            value.setProbability( model.probability() / 100);
                            value.setWallLoss( model.wallLoss());
                            value.setWallLossStdDev(model.stdDev());
                            calculatePercentages( value );
                            refreshFromModel();
                        }
                    }
                }
            }
        });

        add(panel);
    }


    public void dispose() {
        for (ListSelectionListener listener : list.getListSelectionListeners()) {
            list.removeListSelectionListener(listener );
        }
        for (MouseListener listener : list.getMouseListeners()) {
            list.removeMouseListener(listener);
        }
        for (ActionListener listener : add.getActionListeners()) {
            add.removeActionListener( listener );
        }
        for (ActionListener listener : delete.getActionListeners()) {
            delete.removeActionListener(listener);
        }
        toolBar.removeAll();
        removeAll();
    }

    private void handleAdd() {
        MutableLocalEnvironment environment = new MutableLocalEnvironment();
        environment.setProbability(0);
        environment.setEnvironment( LocalEnvironment.Environment.Indoor );
        getModel().add(environment);
        refreshFromModel();
    }

    private void handleDelete() {
        MutableLocalEnvironment env = (MutableLocalEnvironment) list.getSelectedValue();
        if ( env != null && env.getEnvironment() != LocalEnvironment.Environment.Outdoor ) {
            getModel().remove( env );
            calculatePercentages(null);
            refreshFromModel();
        }
    }

    public void setModel(List<LocalEnvironment> localEnvironments) {
        this.localEnvironments = localEnvironments;
        refreshFromModel();
    }

    public List<LocalEnvironment> getModel() {
        return localEnvironments;
    }

    public void refreshFromModel() {
        listModel.clear();
        LocalEnvironment outdoor = null;
        for (LocalEnvironment environment : getModel()) {
            if ( environment.getEnvironment() == LocalEnvironment.Environment.Outdoor ) {
                outdoor = environment;
            } else {
                listModel.addElement(environment);
            }
        }
        if ( outdoor != null ) {
            listModel.addElement(outdoor);
        }
        list.updateUI();
    }

    private void calculatePercentages(MutableLocalEnvironment selected) {
        double cumulative = 0;
        double reserved = selected == null ? 0 : selected.getProbability();
        MutableLocalEnvironment outDoor = null;
        for (LocalEnvironment environment : getModel()) {
            if ( environment.getEnvironment() == LocalEnvironment.Environment.Outdoor ) {
                outDoor = (MutableLocalEnvironment) environment;
                continue;
            }
            if ( environment == selected ) {
                cumulative += reserved;
            } else if ( cumulative + environment.getProbability() + reserved > 1 ) {
                ((MutableLocalEnvironment)environment).setProbability( 1 - (cumulative+reserved) );
                cumulative = 1-reserved;
            } else {
                cumulative += environment.getProbability();
            }
        }
        if ( outDoor != null ) {
            outDoor.setProbability(1 - cumulative);
        }
    }

    public void updateModel() {
        List<LocalEnvironment> model = getModel();
        model.clear();
        for ( int i=0; i<listModel.size(); i++) {
            model.add((MutableLocalEnvironment) listModel.get(i ));
        }
    }

    public LocalEnvironmentsPanel setEnvironments( List<LocalEnvironment> environments ) {
        setModel(environments);
        return this;
    }
}
