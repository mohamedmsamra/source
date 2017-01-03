package org.seamcat.calculator;

import org.apache.log4j.Logger;
import org.seamcat.presentation.SeamcatIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class MemoryButton extends JButton {

	private static final String KEYMAP_PREFIX = "Store-f";

	private static final Logger LOG = Logger.getLogger(MemoryButton.class);

	private static final HashMap<Integer, Double> VALUES = new HashMap<Integer, Double>(
	      5, 1.0f);
	private Calculator calc;
	private int index;
	private String no_value_tool_tip;

	private boolean valueSet = false;

	public MemoryButton(int _index, Calculator _calc, int keyevent) {
		super(String.valueOf(_index), SeamcatIcons
		      .getImageIcon("SEAMCAT_ICON_CALCULATOR_MEMORY_EMPTY"));
		index = _index;
		no_value_tool_tip = "<html>Memory space " + index
		      + ": No value stored<br>" + "Click or press F" + index
		      + " to store value";
		setToolTipText(no_value_tool_tip);
		calc = _calc;
		setMargin(new Insets(2, 2, 2, 2));
		addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (!valueSet
				      || (e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
					storeValue();
				} else if ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
					resetValue();
				} else {
					retrieveValue();
				}
				calc.resetFocus();
			}

		});
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		      KeyStroke.getKeyStroke(keyevent, 0), KEYMAP_PREFIX + index);
		getActionMap().put(KEYMAP_PREFIX + index, new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				doClick();
			}
		});

	}

	@Override
	public void repaint() {
		setValueSet(VALUES.containsKey(index));
		super.repaint();
	}

	public void resetValue() {
		VALUES.remove(index);
		setValueSet(false);
	}

	public void retrieveValue() {
		if (valueSet) {
			calc.setValue(VALUES.get(index));
			LOG.debug("Retrieved value [" + VALUES.get(index)
			      + "] from memory position " + index);
		}
	}

	private void setValueSet(boolean value) {
		valueSet = value;
		if (valueSet) {
			setIcon(SeamcatIcons
			      .getImageIcon("SEAMCAT_ICON_CALCULATOR_MEMORY_FULL"));
			setToolTipText("<html>Memory space " + index + ": "
			      + VALUES.get(index) + "<br>" + "Click or press F" + index
			      + " to use value<br>" + "Shift + Click to store new value<br>"
			      + "Control + Click to clear value");
		} else {
			valueSet = false;
			setIcon(SeamcatIcons
			      .getImageIcon("SEAMCAT_ICON_CALCULATOR_MEMORY_EMPTY"));
			setToolTipText(no_value_tool_tip);
		}
	}

	public void storeValue() {
		try {
			double value = calc.getValue();
			VALUES.put(index, value);
			setValueSet(true);
			LOG.debug("Stored value [" + value + "] in memory position " + index);
		} catch (Exception e) {
			// Do nothing
		}
	}

}
