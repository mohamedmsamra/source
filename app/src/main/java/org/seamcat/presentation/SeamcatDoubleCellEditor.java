package org.seamcat.presentation;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public class SeamcatDoubleCellEditor extends DefaultCellEditor {

	private DecimalFormat format;
	JFormattedTextField ftf;
	private Double minimum, maximum;

	public SeamcatDoubleCellEditor() {
		this(Double.MIN_VALUE, Double.MAX_VALUE);
	}

	public SeamcatDoubleCellEditor(double min, double max) {
		super(new JFormattedTextField());
		ftf = (JFormattedTextField) getComponent();
		minimum = min;
		maximum = max;

		DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.getDefault());

		dfs.setDecimalSeparator(',');

		format = new DecimalFormat();
		format.setDecimalFormatSymbols(dfs);
		format.setMinimumFractionDigits(1);
		format.setMaximumFractionDigits(15);
		NumberFormatter dobFormatter = new NumberFormatter(format);

		dobFormatter.setMinimum(minimum);
		dobFormatter.setMaximum(maximum);

		ftf.setFormatterFactory(new DefaultFormatterFactory(dobFormatter));
		ftf.setValue(minimum);
		ftf.setHorizontalAlignment(SwingConstants.TRAILING);
		ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

		// React when the user presses Enter while the editor is
		// active. (Tab is handled as specified by
		// JFormattedTextField's focusLostBehavior property.)
		ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
		      "check");
		ftf.getActionMap().put("check", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				if (!ftf.isEditValid()) { // The text is invalid.
					if (userSaysRevert()) { // reverted
						ftf.postActionEvent(); // inform the editor
					}
				} else {
					try { // The text is valid,
						ftf.commitEdit(); // so use it.
						ftf.postActionEvent(); // stop editing
					} catch (java.text.ParseException exc) {
					}
				}
			}
		});
	}

	// Override to ensure that the value remains an Integer.
	@Override
	public Object getCellEditorValue() {
		JFormattedTextField ftf = (JFormattedTextField) getComponent();
		Object o = ftf.getValue();
		if (o instanceof Number) {
			return o;
		}
		// CHP 27/07-2005: Integer extends Number! So this else if will NEVER be
		// reached.
		// else if (o instanceof Integer) {
		// return new Double(((Integer)o).intValue());
		// }
		else {
			try {
				return format.parse(o.toString());
			} catch (Exception exc) {
				return null;
			}
		}
	}

	// Override to invoke setValue on the formatted text field.
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
	      boolean isSelected, int row, int column) {
		JFormattedTextField ftf = (JFormattedTextField) super
		      .getTableCellEditorComponent(table, value, isSelected, row, column);
		ftf.setValue(value);
		return ftf;
	}

	// Override to check whether the edit is valid,
	// setting the value if it is and complaining if
	// it isn't. If it's OK for the editor to go
	// away, we need to invoke the superclass's version
	// of this method so that everything gets cleaned up.
	@Override
	public boolean stopCellEditing() {
		JFormattedTextField ftf = (JFormattedTextField) getComponent();
		if (ftf.isEditValid()) {
			try {
				ftf.commitEdit();
			} catch (java.text.ParseException exc) {
			}

		} else { // text is invalid
			if (!userSaysRevert()) { // user wants to edit
				return false; // don't let the editor go away
			}
		}

		return super.stopCellEditing();
	}

	/**
	 * Lets the user know that the text they entered is bad. Returns true if the
	 * user elects to revert to the last good value. Otherwise, returns false,
	 * indicating that the user wants to continue editing.
	 */
	protected boolean userSaysRevert() {
		Toolkit.getDefaultToolkit().beep();
		ftf.selectAll();
		Object[] options = { "Edit", "Revert" };
		int answer = JOptionPane.showOptionDialog(SwingUtilities
		      .getWindowAncestor(ftf), "The value must be a number between "
		      + minimum + " and " + maximum + ".\n"
		      + "You can either continue editing "
		      + "or revert to the last valid value.", "Invalid Text Entered",
		      JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
		      options, options[1]);

		if (answer == 1) { // Revert!
			ftf.setValue(ftf.getValue());
			return true;
		}
		return false;
	}
}
