package org.seamcat.presentation.components.interferencecalc;

import org.seamcat.model.engines.ICEConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CalculationModePanel extends JPanel implements ActionListener {

	private JRadioButton compatibility = new JRadioButton("Compatibility");
	private ButtonGroup group = new ButtonGroup();
	private ICEConfiguration iceconf;

	private JRadioButton translation = new JRadioButton("Translation");

	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	public CalculationModePanel() {
		compatibility.addActionListener(this);
		translation.addActionListener(this);

		group.add(compatibility);
		group.add(translation);
		setLayout(new GridLayout(2, 1));
		add(compatibility);
		add(translation);
		compatibility.setSelected(true);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			iceconf.setCalculationModeIsTranslation(translation.isSelected());
			for (ActionListener a : actionListeners) {
				a.actionPerformed(e);
			}
		} catch (NullPointerException ne) { }
	}

	public void addModeListener(ActionListener acc) {
		actionListeners.add(acc);
	}

	public void init(ICEConfiguration _iceconf) {
		enableButtons(true);
        this.iceconf = _iceconf;
		setModeIsCompability(!iceconf.calculationModeIsTranslation());
        if (iceconf.getHasBeenCalculated()) {
            enableButtons(false);
        }
    }

    private void enableButtons( boolean enable ) {
        compatibility.setEnabled(enable);
        translation.setEnabled(enable);
    }

	public boolean modeIsTranslation() {
		return translation.isSelected();
	}

	public void setModeIsCompability(boolean comp) {
		compatibility.setSelected(comp);
		translation.setSelected(!comp);
	}

}
