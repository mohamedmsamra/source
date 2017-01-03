package org.seamcat.presentation;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.SelectionMadeEvent;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class SelectionPanel extends JPanel {

    private List<JRadioButton> selections = new ArrayList<JRadioButton>();
    private JRadioButton selection;

    public SelectionPanel( final Object context, String... options ) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        selections = new ArrayList<JRadioButton>();
        ButtonGroup buttonGroupType = new ButtonGroup();
        for (String option : options) {
            final JRadioButton button = new JRadioButton(option);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    EventBusFactory.getEventBus().publish( new SelectionMadeEvent(context, button.getText()));
                    selection = button;
                }
            });
            selections.add( button );
            buttonGroupType.add( button );
            add(button);
        }
        selection = selections.get(0);
        setBorder(new TitledBorder("Type"));
    }

    public String getSelection() {
        return selection.getText();
    }

    public void setSelection( String selection ) {
        for (JRadioButton button : selections) {
            if ( button.getText().equals( selection ) ) {
                button.setSelected(true);
                button.requestFocus();
                this.selection = button;
                return;
            }
        }
    }
}
