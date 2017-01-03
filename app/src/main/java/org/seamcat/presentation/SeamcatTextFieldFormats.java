/*
 * DialogFormats.java
 *
 * Created on 5. marts 2004, 14:59
 */

package org.seamcat.presentation;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

import org.seamcat.presentation.components.DoubleCellEditor2;

public class SeamcatTextFieldFormats {

	public static class DoubleCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;

		NumberFormat doubleFormat;

		public DoubleCellRenderer(NumberFormat nf) {
			doubleFormat = nf;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			String _formattedValue;
			Double _value = (Double) value;
			if (value == null) {
				_formattedValue = "Undefined";
			} else {
				_formattedValue = doubleFormat.format(_value);
			}
			JLabel testLabel = (JLabel) super.getTableCellRendererComponent(table,
			      _formattedValue, isSelected, hasFocus, row, column);
			testLabel.setHorizontalAlignment(SwingConstants.RIGHT);

			if (isSelected) {
				testLabel.setBackground(table.getSelectionBackground());
				testLabel.setOpaque(true);
				testLabel.setForeground(table.getSelectionForeground());
			}
			if (hasFocus) {
				testLabel.setForeground(table.getSelectionBackground());
				testLabel.setBackground(table.getSelectionForeground());
				testLabel.setOpaque(true);
			}
			return testLabel;
		}
	}

	private static final class DoubleJTextFieldVerifier extends InputVerifier {

		private final double max;

		private final double min;

		public DoubleJTextFieldVerifier() {
			this.min = Double.MIN_VALUE;
			this.max = Double.MAX_VALUE;
		}

		@Override
		public final boolean verify(final JComponent input) {
			boolean valid;
			try {
				if (input instanceof JTextField) {
					final JTextField inputTextField = (JTextField) input;
					final double value = Double
							.parseDouble(inputTextField.getText());
					valid = (value >= min) && (value <= max);
				} else {
					valid = false;
				}
			} catch (Exception e) {
				valid = false;
			}
			return valid;
		}
	}

	private static final class IntegerJTextFieldVerifier extends InputVerifier {

		private final int max;

		private final int min;

		public IntegerJTextFieldVerifier() {
			this.min = Integer.MIN_VALUE;
			this.max = Integer.MAX_VALUE;
		}

		@Override
		public final boolean verify(JComponent input) {
			try {
				final int value = Integer.parseInt(((JTextComponent) input)
						.getText());
				return (value >= min) && (value <= max);
			} catch (NumberFormatException e) {
				return false;
			}
		}

	}

	public static final DoubleCellEditor2 DOUBLE_EDITOR;

	/* Formats */
	private static final DefaultFormatter DOUBLE_FORMAT;
	/* Factories */
	public static final DefaultFormatterFactory DOUBLE_FACTORY;


	public static final InputVerifier DOUBLE_JTEXTFIELD_VERIFIER = new DoubleJTextFieldVerifier();

	public static final DoubleCellRenderer DOUBLE_RENDERER;

	private static final String INTEGER_FORMAT_STRING = "#0";

	private static final DefaultFormatter INTEGER_FORMAT = new NumberFormatter(
			new DecimalFormat(INTEGER_FORMAT_STRING));

	public static final DefaultFormatterFactory INTEGER_FACTORY = new DefaultFormatterFactory(
			INTEGER_FORMAT);

	public static final InputVerifier INTEGER_JTEXTFIELD_VERIFIER = new IntegerJTextFieldVerifier();

	public static final FocusListener SELECTALL_FOCUSHANDLER = new FocusListener() {

		@Override
		public void focusGained(final FocusEvent e) {
			if (e.getComponent() instanceof JTextComponent) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						((JTextComponent) e.getComponent()).selectAll();
					}
				});
			}
		}

		@Override
		public void focusLost(FocusEvent e) {
			// System.out.println("Lost focus");
		}

	};

	private static final DefaultFormatter STRING_FORMAT = new DefaultFormatter();

	public static final DefaultFormatterFactory STRING_FACTORY = new DefaultFormatterFactory(
			STRING_FORMAT, STRING_FORMAT, STRING_FORMAT);


	static {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.ENGLISH);

		// dfs.setDecimalSeparator(',');

		DecimalFormat format = new DecimalFormat();// (DOUBLE_FORMAT_STRING,
		// dfs);
		format.setDecimalFormatSymbols(dfs);
		format.setMinimumFractionDigits(1);
		format.setMaximumFractionDigits(15);

		DOUBLE_FORMAT = new NumberFormatter(format);



		DOUBLE_RENDERER = new DoubleCellRenderer(format);
		DOUBLE_EDITOR = new DoubleCellEditor2();

		DOUBLE_FORMAT.setAllowsInvalid(false);
		INTEGER_FORMAT.setAllowsInvalid(false);

		DOUBLE_FACTORY = new DefaultFormatterFactory(DOUBLE_FORMAT, DOUBLE_FORMAT, DOUBLE_FORMAT);
	}

	public static DefaultFormatterFactory getDoubleFactory() {
		return DOUBLE_FACTORY;
	}

	public static DefaultFormatter getDoubleFormatter() {
		return DOUBLE_FORMAT;
	}

	public static DefaultFormatterFactory getIntegerFactory() {
		return INTEGER_FACTORY;
	}

	public static DefaultFormatter getIntegerFmt() {
		return INTEGER_FORMAT;
	}

	public static DefaultFormatterFactory getStringFactory() {
		return STRING_FACTORY;
	}

	public static DefaultFormatter getStringFmt() {
		return STRING_FORMAT;
	}

	public SeamcatTextFieldFormats() {
	}

}
