package org.seamcat.calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CalculatorButton extends JButton {

	private static final String ACTION = "action";
	private static final String NOACTION = "seamcat-no-action";

	private String key;

	public CalculatorButton(String _key) {
		super();
		key = _key;

		setMargin(new Insets(2, 2, 2, 2));

		getInputMap(JComponent.WHEN_FOCUSED).put(
		      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), NOACTION);
		getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
		      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), NOACTION);
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), NOACTION);

		if (key != null) {
			getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
			      KeyStroke.getKeyStroke(key), ACTION);

			getActionMap().put(ACTION, new AbstractAction() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent event) {
					doClick();
				}
			});
		}
	}

	@Override
	public void setText(String s) {
		if (key == null) {
			super.setText(s);
		} else {
			super.setText(s + " [" + key + "]");
		}
	}

}
