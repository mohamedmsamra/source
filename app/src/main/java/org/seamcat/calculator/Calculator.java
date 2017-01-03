package org.seamcat.calculator;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.presentation.SeamcatIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * this is the pocket calculator of SEAMCAT
 */
public class Calculator extends JDialog {

	private enum BinaryOperation {
		Add, Divide, Multiply, Substract
	}

	private static final String B = "constant-B";
	private static final String CANCEL_DIALOG = "cancel-dialog";

	private static final String CLEAR = "C";
	private static final String CLEARALL = "CA";

	public static final String COMMA = ",";
	private static final String DIVIDE = "/";
	private static final String E = "constant-E";
	private static final String EQUALS = "==";
	private static final String LIST = ";";
	private static final Logger LOG = Logger.getLogger(Calculator.class);
	private static final String MINUS = "-";
	private static final String MULTIPLY = "*";
	private static final String SIGN = "--";
	private static final double SQRT2 = Math.sqrt(2);
	private static final double SQRT3 = Math.sqrt(3);
	private static final String TRANSFER = "transfer";

	private static double fromdBm2Watt(double dbm) {
		return Math.pow(10, (dbm - 30) / 10);
	}

	private static double fromWatt2dBm(double watt) {
		if (watt == 0) {
			LOG.warn("fromWatt2dBm - Returned -1000 instead of NaN");
			return -1000;
		}
		return 10 * Math.log10(watt) + 30;
	}

	public static void main(String[] args) {
		try {
			if (UIManager.getSystemLookAndFeelClassName().equalsIgnoreCase(
			      "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
				UIManager.setLookAndFeel(UIManager
				      .getCrossPlatformLookAndFeelClassName());
			} else {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			}
		} catch (Exception exception) {
			System.err.println(exception.getLocalizedMessage());
		}
		BasicConfigurator.configure();
		JFrame main = new JFrame("Calculator");
		main.setIconImage(SeamcatIcons.getImage("SEAMCAT_ICON_CALCULATOR",
		      SeamcatIcons.IMAGE_SIZE_32x32));
		JDialog calc = new Calculator(main);
		main.setContentPane(calc.getContentPane());
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setSize(calc.getSize());
		main.setLocation(calc.getLocation());
		main.setResizable(false);
		main.setVisible(true);

	}

	private static double powerSummation(double... powers) {
		double total = 0;
		for (double power : powers) {
			total += Math.pow(10, (power - 30) / 10);
		}

		return 10 * Math.log10(total) + 30;
	}

	private JButton aCosD = new CalculatorButton("I");

	protected JButton add = new CalculatorButton(null);
	private JButton aSinD = new CalculatorButton("X");
	private JButton aTanD = new CalculatorButton("P");
	private JButton average = new CalculatorButton("Z");
	protected JButton cbrt = new CalculatorButton("O");
	private JButton ceil = new CalculatorButton(null);

	private JButton change_sign = new CalculatorButton(null);
	private JButton clear = new CalculatorButton(null);
	protected JButton clearAll = new CalculatorButton(null);
	private JButton comma = new CalculatorButton(null);
	protected JButton constant_boltzmann = new CalculatorButton(null);

	protected JButton constant_e = new CalculatorButton(null);
	protected JButton constant_pi = new CalculatorButton(null);
	protected JButton constant_sqrt2 = new CalculatorButton(null);
	protected JButton constant_sqrt3 = new CalculatorButton(null);
	protected JButton cos = new CalculatorButton("R");
	private JButton cosH = new CalculatorButton("D");
	private BinaryOperation currentOperation;
	protected JButton dbm2watt = new CalculatorButton("Q");
	protected JButton dgr2rad = new CalculatorButton(null);
	private JFormattedTextField display = new JFormattedTextField();
	protected JButton divide = new CalculatorButton(null);
	private JButton ee = new CalculatorButton("J");
	protected JButton equals = new CalculatorButton(null);
	private boolean firstEntryOfBinaryOperation = false;
	private JButton floor = new CalculatorButton("N");
	private boolean insideBinaryOperation = false;
	private boolean lastActionWasEquals = false;
	protected JButton list_separate = new CalculatorButton(null);
	private JButton log10 = new CalculatorButton("U");
	private JButton min = new CalculatorButton("K");
	protected JButton minus = new CalculatorButton(null);
	protected JButton multiply = new CalculatorButton(null);
	private final ActionListener numberAction = new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			if (firstEntryOfBinaryOperation) {
				firstEntryOfBinaryOperation = false;
				lastActionWasEquals = false;
				removeLastEntry();
			}
			numberActionPerformed(e.getActionCommand());

			equals.requestFocusInWindow();
		}
	};
	private JButton pow = new CalculatorButton("L");

	private JButton powerSum = new CalculatorButton("V");
	protected JButton rad2dgr = new CalculatorButton(null);

	protected JButton random = new CalculatorButton("G");
	protected JButton sin = new CalculatorButton("M");
	private JButton sinH = new CalculatorButton("S");
	protected JButton sqrt = new CalculatorButton("T");
    private JLabel statusLabel = new JLabel("SEAMCAT Calculator");

	private JButton stdDev = new CalculatorButton("L");

	private JButton tan = new CalculatorButton("Y");

	private JButton tanH = new CalculatorButton("F");

	private double tempBinaryOperatorValue = 0;

	private double tempEqualsValue = 0;

	protected JButton transfer = new CalculatorButton(null);

	private boolean transferValue = false;

	protected JButton watt2dbm = new CalculatorButton("W");

	public Calculator(Frame owner) {
		super(owner, true);
		init();
		setLocationRelativeTo(owner);
	}

	public Calculator(JDialog owner) {
		super(owner, true);
		init();
		setLocationRelativeTo(owner);
	}

	/**
	 * Adds fill components to empty cells in the first row and first column of
	 * the grid. This ensures that the grid spacing will be the same as shown in
	 * the designer.
	 * 
	 * @param cols
	 *           an array of column indices in the first row where fill
	 *           components should be added.
	 * @param rows
	 *           an array of row indices in the first column where fill
	 *           components should be added.
	 */
	void addFillComponents(Container panel, int[] cols, int[] rows) {
		Dimension filler = new Dimension(10, 10);

		boolean filled_cell_11 = false;
		CellConstraints cc = new CellConstraints();
		if (cols.length > 0 && rows.length > 0) {
			if (cols[0] == 1 && rows[0] == 1) {
				/** add a rigid area */
				panel.add(Box.createRigidArea(filler), cc.xy(1, 1));
				filled_cell_11 = true;
			}
		}

        for (int col : cols) {
            if (col == 1 && filled_cell_11) {
                continue;
            }
            panel.add(Box.createRigidArea(filler), cc.xy(col, 1));
        }

        for (int row : rows) {
            if (row == 1 && filled_cell_11) {
                continue;
            }
            panel.add(Box.createRigidArea(filler), cc.xy(1, row));
        }

	}

	private void clearDisplay() {
		display.setText("");
	}

	private void completeBinaryOperation() throws Exception {
		switch (currentOperation) {
			case Add: {
				tempBinaryOperatorValue += getValue();
				break;
			}
			case Substract: {
				tempBinaryOperatorValue -= getValue();
				break;
			}
			case Multiply: {
				tempBinaryOperatorValue *= getValue();
				break;
			}
			case Divide: {
				tempBinaryOperatorValue /= getValue();
				break;
			}
		}
	}

	public JPanel createPanel() {
		JPanel mainPanel = new JPanel();
		FormLayout formlayout = new FormLayout(
		      "FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,"
		            + "FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,"
		            + "FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,"
		            + "FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,"
		            + "FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,"
		            + "FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE",
		      "CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,"
		            + "CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,"
		            + "CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,"
		            + "CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,"
		            + "CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		mainPanel.setLayout(formlayout);

		mainPanel.add(generateNumericButton("7"), cc.xy(13, 7));
		mainPanel.add(generateNumericButton("8"), cc.xy(14, 7));
		mainPanel.add(generateNumericButton("9"), cc.xy(15, 7));
		mainPanel.add(generateNumericButton("4"), cc.xy(13, 9));
		mainPanel.add(generateNumericButton("5"), cc.xy(14, 9));
		mainPanel.add(generateNumericButton("6"), cc.xy(15, 9));
		mainPanel.add(generateNumericButton("1"), cc.xy(13, 11));
		mainPanel.add(generateNumericButton("2"), cc.xy(14, 11));
		mainPanel.add(generateNumericButton("3"), cc.xy(15, 11));
		mainPanel.add(generateNumericButton("0"), cc.xy(13, 13));

		comma.setActionCommand(".");
		comma.setText(".");
		comma.setMargin(new Insets(2, 5, 2, 5));
		comma.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String str = display.getText();
				// If multiple entries are visible we find the last one:
				if (display.getText().indexOf(";") > -1) {
					str = str.substring(str.lastIndexOf(";"));
				}
				// Only add decimal separator if it is not already present
				if (str.indexOf(".") < 0) {
					if (str.length() == 0) {
						display.setText(display.getText() + "0.");
					} else {
						display.setText(display.getText() + ".");
					}

				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(comma, cc.xy(14, 13));

		transfer.setIcon(SeamcatIcons
		      .getImageIcon("SEAMCAT_ICON_TRANSFER_RESULT"));
		transfer.setToolTipText("Close and copy result to SEAMCAT [INSERT]");
		transfer.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				transferValue = true;
				Calculator.this.setVisible(false);
			}

		});

		mainPanel.add(transfer, cc.xy(18, 15));

		change_sign.setActionCommand("+ / -");
		change_sign.setText("+ / -");
		change_sign
		      .setToolTipText("Change sign of current input [SHIFT + MINUS]");
		change_sign.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (display.getText().indexOf(";") > -1) {
					String str = display.getText();
					String nmb = str.substring(str.lastIndexOf(";") + 1);
					if (nmb.indexOf("-") > -1) {
						nmb = nmb.substring(1);
					} else {
						nmb = "-" + nmb;
					}
					display
					      .setText(str.substring(0, str.lastIndexOf(";") + 1) + nmb);
				} else {
					String str = display.getText();
					if (str.length() > 0 && str.charAt(0) == '-') {
						str = str.substring(1);
					} else {
						str = "-" + str;
					}
					display.setText(str);
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(change_sign, cc.xy(18, 5));

		display.setFocusLostBehavior(JFormattedTextField.COMMIT);
		display.setHorizontalAlignment(JFormattedTextField.RIGHT);
		display.setEditable(false);
		display.setEnabled(true);
		mainPanel.add(display, cc.xywh(2, 3, 21, 1));

		tan.setActionCommand("tanD");
		tan.setText("tanD");
		tan.setToolTipText("Internal SEAMCAT function: Mathematics.tanD(double)");
		tan.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.tanD(value);
					setValue(result);
					LOG.debug("Result of Mathematics.tanD (" + value + ") = "
					      + result);
					equals.requestFocusInWindow();
				} catch (Exception ex) {
				}
			}

		});
		mainPanel.add(tan, cc.xy(2, 7));

		log10.setActionCommand("Log10");
		log10.setText("Log10");
		log10.setToolTipText("Internal java function: Math.log10(double)");
		log10.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Math.log10(value);
					setValue(result);
					LOG.debug("Result of Math.log10 (" + value + ") = " + result);
					equals.requestFocusInWindow();
				} catch (Exception ex) {
				}
			}

		});
		mainPanel.add(log10, cc.xy(4, 7));

		aCosD.setActionCommand("acosD");
		aCosD.setText("acosD");
		aCosD
		      .setToolTipText("Internal SEAMCAT function: Mathematics.acosD(double)");
		aCosD.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.acosD(value);
					setValue(result);
					LOG.debug("Result of Mathematics.acosD (" + value + ") = "
					      + result);
					equals.requestFocusInWindow();
				} catch (Exception ex) {
				}
			}

		});
		mainPanel.add(aCosD, cc.xy(6, 7));

		aSinD.setActionCommand("asinD");
		aSinD.setText("asinD");
		aSinD
		      .setToolTipText("Internal SEAMCAT function: Mathematics.asinD(double)");
		aSinD.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.asinD(value);
					setValue(result);
					LOG.debug("Result of Mathematics.asinD (" + value + ") = "
					      + result);
					equals.requestFocusInWindow();
				} catch (Exception ex) {
				}
			}

		});
		mainPanel.add(aSinD, cc.xy(8, 7));

		aTanD.setActionCommand("atanD");
		aTanD.setText("atanD");
		aTanD
		      .setToolTipText("Internal SEAMCAT function: Mathematics.atanD(double)");
		aTanD.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.atanD(value);
					setValue(result);
					LOG.debug("Result of Mathematics.atanD (" + value + ") = "
					      + result);
					equals.requestFocusInWindow();
				} catch (Exception ex) {
				}
			}

		});
		mainPanel.add(aTanD, cc.xy(2, 9));

		sinH.setActionCommand("sinh");
		sinH.setText("sinh");
		sinH
		      .setToolTipText("Internal SEAMCAT function: Mathematics.sinh(double)");
		sinH.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.sinh(value);
					setValue(result);
					LOG.debug("Result of Mathematics.sinh (" + value + ") = "
					      + result);
					equals.requestFocusInWindow();
				} catch (Exception ex) {
				}
			}

		});
		mainPanel.add(sinH, cc.xy(4, 9));

		cosH.setActionCommand("cosh");
		cosH.setText("cosh");
		cosH
		      .setToolTipText("Internal SEAMCAT function: Mathematics.cosh(double)");
		cosH.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.cosh(value);
					setValue(result);
					LOG.debug("Result of Mathematics.cosh (" + value + ") = "
					      + result);
					equals.requestFocusInWindow();
				} catch (Exception ex) {
				}
			}

		});
		mainPanel.add(cosH, cc.xy(6, 9));

		tanH.setActionCommand("tanh");
		tanH.setText("tanh");
		tanH
		      .setToolTipText("Internal SEAMCAT function: Mathematics.tanhD(double)");
		tanH.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.tanh(value);
					setValue(result);
					LOG.debug("Result of Mathematics.tanh (" + value + ") = "
					      + result);
					equals.requestFocusInWindow();
				} catch (Exception ex) {
				}
			}

		});
		mainPanel.add(tanH, cc.xy(8, 9));

		ee.setActionCommand("Max");
		ee.setText("<html>X * 10<sup>y</sup>");
		ee.setToolTipText("Internal SEAMCAT function: X * 10^Y");
		ee.setForeground(Color.RED);
		ee.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				double[] val = getValues();
				if (val.length == 2) {
					double result = val[0] * Math.pow(10, val[1]);
					clearDisplay();
					setValue(result);
					LOG.debug("Result of " + val[0] + " * 10 ^ " + val[1] + " = "
					      + result);
				} else {
					JOptionPane.showMessageDialog(Calculator.this,
					      "XeY is only applicable to lists with 2 elements");
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(ee, cc.xy(2, 11));

		min.setActionCommand("Min");
		min.setText("<html>X<sup>y</sup>");
		min.setToolTipText("Internal SEAMCAT function: Math.pow(double, double)");
		min.setForeground(Color.RED);
		min.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				double[] values = getValues();
				if (values.length == 2) {
					double result = Math.pow(values[0], values[1]);
					clearDisplay();
					setValue(result);
					StringBuffer val = new StringBuffer("Result of Math.pow ({");
					for (int i = 0; i < values.length; i++) {
						val.append(values[i]);
						if (i + 1 < values.length) {
							val.append(";");
						}
					}
					val.append("}) = " + result);
					LOG.debug(val.toString());

				} else {
					JOptionPane.showMessageDialog(Calculator.this,
					      "Pow (x;y) is only applicable to lists with 2 elements");
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(min, cc.xy(4, 11));

		stdDev.setActionCommand("Std. Dev.");
		stdDev.setText("Std. Dev.");
		stdDev
		      .setToolTipText("Internal SEAMCAT function: Mathematics.getStdDev(double[])");
		stdDev.setForeground(Color.RED);
		stdDev.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				double[] values = getValues();
				double result = Mathematics.getStdDev(values);
				clearDisplay();
				setValue(result);
				StringBuffer val = new StringBuffer(
				      "Result of Mathematics.getStdDev ({");
				for (int i = 0; i < values.length; i++) {
					val.append(values[i]);
					if (i + 1 < values.length) {
						val.append(";");
					}
				}
				val.append("}) = " + result);
				LOG.debug(val.toString());
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(stdDev, cc.xy(6, 11));

		average.setActionCommand("Average");
		average.setText("Average");
		average
		      .setToolTipText("Internal SEAMCAT function: Mathematics.getAverage(double[])");
		average.setForeground(Color.RED);
		average.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				double[] values = getValues();
				double result = Mathematics.getAverage(values);
				clearDisplay();
				setValue(result);
				StringBuffer val = new StringBuffer(
				      "Result of Mathematics.getAverage ({");
				for (int i = 0; i < values.length; i++) {
					val.append(values[i]);
					if (i + 1 < values.length) {
						val.append(";");
					}
				}
				val.append("}) = " + result);
				LOG.debug(val.toString());
				equals.requestFocusInWindow();

			}

		});
		mainPanel.add(average, cc.xy(8, 11));

		pow.setActionCommand("Pow (x;y)");
		pow.setText("Pow (x;y)");
		pow.setToolTipText("Internal Java function: Math.pow(x, y)");
		pow.setForeground(Color.RED);
		pow.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				double[] val = getValues();
				if (val.length == 2) {
					double result = Math.pow(val[0], val[1]);
					clearDisplay();
					setValue(result);
					LOG.debug("Result of Math.pow (" + val[0] + ", " + val[1]
					      + ") = " + result);
				} else {
					JOptionPane.showMessageDialog(Calculator.this,
					      "Pow (x;y) is only applicable to lists with 2 elements");
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(pow, cc.xy(2, 13));

		powerSum.setActionCommand((char) 0x03A3 + " Power");
		powerSum.setText((char) 0x03A3 + " Power");
		powerSum
		      .setToolTipText("Internal SEAMCAT function: CDMASystem.powerSummation(double[])");
		powerSum.setForeground(Color.RED);
		powerSum.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				double[] values = getValues();
				double result = Calculator.powerSummation(values);
				clearDisplay();
				setValue(result);
				StringBuffer val = new StringBuffer(
				      "Result of CDMASystem.powerSummation ({");
				for (int i = 0; i < values.length; i++) {
					val.append(values[i]);
					if (i + 1 < values.length) {
						val.append(";");
					}
				}
				val.append("}) = " + result);
				LOG.debug(val.toString());
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(powerSum, cc.xy(4, 13));

		floor.setActionCommand("Floor");
		floor.setText("Floor");
		floor.setToolTipText("Internal Java function: Math.floor(double)");
		floor.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Math.floor(value);
					setValue(result);
					LOG.debug("Result of Math.floor (" + value + ") = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(floor, cc.xy(6, 13));

		ceil.setActionCommand("Ceil");
		ceil.setText("Ceil");
		ceil.setToolTipText("Internal Java function: Math.ceil(double)");
		ceil.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Math.ceil(value);
					setValue(result);
					LOG.debug("Result of Math.ceil (" + value + ") = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(ceil, cc.xy(8, 13));

		sqrt.setActionCommand("sqrt");
		sqrt.setText("SQRT");
		sqrt.setToolTipText("Squareroot");
		sqrt.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Math.sqrt(value);
					setValue(result);
					LOG.debug("Result of Math.sqrt (" + value + ") = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(sqrt, cc.xy(10, 5));

		cbrt.setActionCommand("cbrt");
		cbrt.setText("CBRT");
		cbrt.setToolTipText("Cubicroot");
		cbrt.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Math.cbrt(value);
					setValue(result);
					LOG.debug("Result of Math.cbrt (" + value + ") = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(cbrt, cc.xy(10, 7));

		random.setActionCommand("random");
		random.setText("Random");
		random
		      .setToolTipText("Generates a random number between 0 and 1. Number is inserted at current cursor location.");
		random.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double result = Math.random();
					setValue(result);
					LOG.debug("Result of Math.random () = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(random, cc.xy(10, 9));

		dgr2rad.setActionCommand("Dgr->Rad");
		dgr2rad.setText("Dgr" + (char) 0x21d2 + "Rad");
		dgr2rad.setToolTipText("Convert input from Degrees to Radians");
		dgr2rad.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Math.toRadians(value);
					setValue(result);
					LOG
					      .debug("Result of Math.toRadians (" + value + ") = "
					            + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(dgr2rad, cc.xy(10, 11));

		rad2dgr.setActionCommand("rad2dgr");
		rad2dgr.setText("Rad" + (char) 0x21d2 + "Dgr");
		rad2dgr.setToolTipText("Convert input from Radians to Degrees");
		rad2dgr.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Math.toDegrees(value);
					setValue(result);
					LOG
					      .debug("Result of Math.toDegrees (" + value + ") = "
					            + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(rad2dgr, cc.xy(10, 13));

		clear.setActionCommand("Clear");
		clear.setText("C");
		clear.setToolTipText("Clear last entry [BACKSPACE]");
		clear.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if (firstEntryOfBinaryOperation) {
						removeLastEntry();
						firstEntryOfBinaryOperation = false;
					} else {
						display.setText(display.getText().substring(0,
						      display.getText().length() - 1));
						statusLabel.setText("");
					}
					equals.requestFocusInWindow();
				} catch (Exception ex) {

				}
			}

		});
		mainPanel.add(clear, cc.xy(15, 5));

		clearAll.setActionCommand("CA");
		clearAll.setText("A");
		clearAll.setToolTipText("Clear All [DELETE]");
		clearAll.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				clearDisplay();
				insideBinaryOperation = false;
				statusLabel.setText("");
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(clearAll, cc.xy(14, 5));

		list_separate.setActionCommand(";");
		list_separate.setName("List separate");
		list_separate.setText(";");
		list_separate.setForeground(Color.RED);
		list_separate.setToolTipText("Separate list entries [SHIFT + ADD]");
		list_separate.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				firstEntryOfBinaryOperation = false;
				numberActionPerformed(e.getActionCommand());
				insideBinaryOperation = false;
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(list_separate, cc.xy(13, 5));

		dbm2watt.setActionCommand("dBm" + (char) 0x21d2 + "Watt");
		dbm2watt.setText("dBm" + (char) 0x21d2 + "Watt");
		dbm2watt
		      .setToolTipText("Internal SEAMCAT function: CDMASystem.fromdBm2Watt(double)");
		dbm2watt.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Calculator.fromdBm2Watt(value);
					setValue(result);
					LOG.debug("Result of CDMASystem.fromdBm2Watt (" + value + ") = "
					      + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(dbm2watt, cc.xy(2, 5));

		watt2dbm.setActionCommand("Watt" + (char) 0x21d2 + "dBm");
		watt2dbm.setText("Watt" + (char) 0x21d2 + "dBm");
		watt2dbm
		      .setToolTipText("Internal SEAMCAT function: CDMASystem.fromWatt2dBm(double)");
		watt2dbm.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Calculator.fromWatt2dBm(value);
					setValue(result);
					LOG.debug("Result of CDMASystem.fromWatt2dBm (" + value + ") = "
					      + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(watt2dbm, cc.xy(4, 5));

		sin.setActionCommand("sinD");
		sin.setText("sinD");
		sin.setToolTipText("Internal SEAMCAT function: Mathematics.sinD(double)");
		sin.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.sinD(value);
					setValue(result);
					LOG.debug("Result of Mathematics.sinD (" + value + ") = "
					      + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(sin, cc.xy(6, 5));

		cos.setActionCommand("cosD");
		cos.setText("cosD");
		cos.setToolTipText("Internal SEAMCAT function: Mathematics.cosD(double)");
		cos.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double value = getValue();
					double result = Mathematics.cosD(value);
					setValue(result);
					LOG.debug("Result of Mathematics.cosD (" + value + ") = "
					      + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(cos, cc.xy(8, 5));

		minus.setText("-");
		minus.setToolTipText("Substraction");
		minus.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if (insideBinaryOperation) {
						if (getValueString().equals("")
						      || firstEntryOfBinaryOperation) {
							currentOperation = BinaryOperation.Substract;
							return;
						}
						completeBinaryOperation();
					} else {
						tempBinaryOperatorValue = getValue();
					}
					currentOperation = BinaryOperation.Substract;
					insideBinaryOperation = true;
					firstEntryOfBinaryOperation = true;
					setValue(tempBinaryOperatorValue);
					LOG.debug("Enter value to substract from "
					      + tempBinaryOperatorValue);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}
		});
		mainPanel.add(minus, cc.xy(18, 7));

		add.setText("+");
		add.setToolTipText("Addition");
		add.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if (insideBinaryOperation) {
						if (getValueString().equals("")
						      || firstEntryOfBinaryOperation) {
							currentOperation = BinaryOperation.Add;
							return;
						}
						completeBinaryOperation();
					} else {
						tempBinaryOperatorValue = getValue();
					}
					currentOperation = BinaryOperation.Add;
					insideBinaryOperation = true;
					setValue(tempBinaryOperatorValue);
					firstEntryOfBinaryOperation = true;
					LOG.debug("Enter value to add to " + tempBinaryOperatorValue);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}
		});
		mainPanel.add(add, cc.xy(18, 9));

		multiply.setText("*");
		multiply.setToolTipText("Multiplication");
		multiply.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if (insideBinaryOperation) {
						if (getValueString().equals("")
						      || firstEntryOfBinaryOperation) {
							currentOperation = BinaryOperation.Multiply;
							return;
						}
						completeBinaryOperation();
					} else {
						tempBinaryOperatorValue = getValue();
					}
					currentOperation = BinaryOperation.Multiply;
					insideBinaryOperation = true;
					firstEntryOfBinaryOperation = true;
					setValue(tempBinaryOperatorValue);
					LOG.debug("Enter value to multiply with "
					      + tempBinaryOperatorValue);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}
		});
		mainPanel.add(multiply, cc.xy(18, 11));

		divide.setText("/");
		divide.setToolTipText("Division");
		divide.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if (insideBinaryOperation) {
						if (getValueString().equals("")
						      || firstEntryOfBinaryOperation) {
							currentOperation = BinaryOperation.Divide;
							return;
						}
						completeBinaryOperation();
					} else {
						tempBinaryOperatorValue = getValue();
					}
					currentOperation = BinaryOperation.Divide;
					insideBinaryOperation = true;
					firstEntryOfBinaryOperation = true;
					setValue(tempBinaryOperatorValue);
					LOG.debug("Enter value to divide " + tempBinaryOperatorValue
					      + " with");
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}
		});
		mainPanel.add(divide, cc.xy(18, 13));

		constant_pi.setText("pi");// String.valueOf((char) 0x03C0));
		constant_pi
		      .setToolTipText("The double value that is closer than any other to pi, the ratio of the circumference of a circle to its diameter.");
		constant_pi.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double result = Math.PI;
					setValue(result);
					LOG.debug("Inserted Math.PI = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(constant_pi, cc.xy(20, 5));
		constant_e.setText("e");
		constant_e
		      .setToolTipText("The double value that is closer than any other to e, the base of the natural logarithms.");
		constant_e.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double result = Math.E;
					setValue(result);
					LOG.debug("Inserted Math.E = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(constant_e, cc.xy(20, 7));

		constant_sqrt2.setText("SQRT2");
		constant_sqrt2
		      .setToolTipText("The double value that is closer than any other to sqrt(2)");
		constant_sqrt2.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double result = SQRT2;
					setValue(result);
					LOG.debug("Inserted CDMASystem.SQRT2 = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(constant_sqrt2, cc.xy(20, 9));

		constant_sqrt3.setText("SQRT3");
		constant_sqrt3
		      .setToolTipText("The double value that is closer than any other to sqrt(3)");
		constant_sqrt3.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double result = SQRT3;
					setValue(result);
					LOG.debug("Inserted CDMASystem.SQRT3 = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(constant_sqrt3, cc.xy(20, 11));

		constant_boltzmann.setText("B");
		constant_boltzmann
		      .setToolTipText("Boltzmann\'s constant, k = 1.38x10^-23 (Joules/K)");
		constant_boltzmann.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double result = 1.38e-23d;
					setValue(result);
					LOG.debug("Inserted 1.38e-23d = " + result);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}

		});
		mainPanel.add(constant_boltzmann, cc.xy(20, 13));

		equals.setText("=");
		equals.setToolTipText("Equals [ENTER]");
		equals.setMargin(new Insets(2, 5, 2, 5));
		equals.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if (insideBinaryOperation) {
						if (getValueString().equals("")) {
							return;
						}
						tempEqualsValue = getValue();
						completeBinaryOperation();
					} else if (lastActionWasEquals) {
						switch (currentOperation) {
							case Add: {
								tempBinaryOperatorValue += tempEqualsValue;
								break;
							}
							case Substract: {
								tempBinaryOperatorValue -= tempEqualsValue;
								break;
							}
							case Multiply: {
								tempBinaryOperatorValue *= tempEqualsValue;
								break;
							}
							case Divide: {
								tempBinaryOperatorValue /= tempEqualsValue;
								break;
							}
						}
					} else {
						currentOperation = BinaryOperation.Add;
						tempEqualsValue = 0;
						tempBinaryOperatorValue = 0;
						return;
					}
					setValue(tempBinaryOperatorValue);
					insideBinaryOperation = false;
					lastActionWasEquals = true;

					LOG.debug("Result is " + tempBinaryOperatorValue);
				} catch (Exception ex) {
				}
				equals.requestFocusInWindow();
			}
		});
		mainPanel.add(equals, cc.xy(15, 13));

		mainPanel.add(new MemoryButton(1, this, KeyEvent.VK_F1), cc.xy(22, 5));
		mainPanel.add(new MemoryButton(2, this, KeyEvent.VK_F2), cc.xy(22, 7));
		mainPanel.add(new MemoryButton(3, this, KeyEvent.VK_F3), cc.xy(22, 9));
		mainPanel.add(new MemoryButton(4, this, KeyEvent.VK_F4), cc.xy(22, 11));
		mainPanel.add(new MemoryButton(5, this, KeyEvent.VK_F5), cc.xy(22, 13));

		mainPanel.add(statusLabel, cc.xywh(2, 15, 17, 1));

		addFillComponents(mainPanel, new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
		      11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23 }, new int[] { 1, 2,
		      3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
		return mainPanel;
	}

	protected JButton generateModifyButton(final String value) {
		JButton button = new JButton(value);
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				display.setText(display.getText() + value);

				equals.requestFocusInWindow();
			}
		});
		return button;
	}

	protected JButton generateNumericButton(final String str) {
		final JButton but = new CalculatorButton(null);
		but.setActionCommand(str);
		but.setText(str);
		but.setForeground(Color.BLUE);
		but.addActionListener(numberAction);
		but.setMargin(new Insets(2, 5, 2, 5));

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(str), str);
		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke("NUMPAD" + str), str);
		((JPanel) getContentPane()).getActionMap().put(str, new AbstractAction() {

			public void actionPerformed(ActionEvent event) {
				but.doClick();
			}
		});
		return but;
	}

	public double getValue() throws Exception {
		try {
			double value = Double.parseDouble(getValueString());
			return value;
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(Calculator.this,
			      "Input does not appear to be a valid number!", "Input error",
			      JOptionPane.WARNING_MESSAGE);
			throw ex;
		}
	}

	@SuppressWarnings("boxing")
	private double[] getValues() {
		double[] values;
		String str = display.getText();
		if (str.charAt(str.length() - 1) == ';') {
			str = str.substring(0, str.length() - 1);
		}
		if (str.indexOf(';') > -1) {
			Scanner scanner = new Scanner(str.replace(".", ","));
			scanner.useDelimiter(";");
			ArrayList<Double> list = new ArrayList<Double>();
			while (scanner.hasNextDouble()) {
				try {
					list.add(scanner.nextDouble());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			values = new double[list.size()];
			for (int i = 0; i < list.size(); i++) {
				values[i] = list.get(i);
			}
		} else {
			values = new double[1];
			try {
				values[0] = getValue();
			} catch (Exception ex) {
				LOG.error(ex);
			}
		}

		return values;
	}

	private String getValueString() {
		String str = null;
		str = display.getText();
		if (display.getText().indexOf(";") > -1) {
			str = str.substring(str.lastIndexOf(";") + 1);
		}
		return str;
	}

	private void init() {
		setTitle("SEAMCAT Calculator");
		initializePanel();
		pack();
		transfer.setVisible(false);
		setResizable(false);
		setLocation(100, 100);
		registerKeyListeners();
	}

	/**
	 * Initializer
	 */
	protected void initializePanel() {
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createPanel(), BorderLayout.CENTER);
	}

	private void numberActionPerformed(String s) {
		display.setText(display.getText() + s);
	}

	private void registerKeyListeners() {
		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_DIALOG);
		((JPanel) getContentPane()).getActionMap().put(CANCEL_DIALOG,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      setVisible(false);
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), MINUS);
		((JPanel) getContentPane()).getActionMap().put(MINUS,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      minus.doClick();
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "+");
		((JPanel) getContentPane()).getActionMap().put("+", new AbstractAction() {

			public void actionPerformed(ActionEvent event) {
				add.doClick();
			}
		});

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0), MULTIPLY);
		((JPanel) getContentPane()).getActionMap().put(MULTIPLY,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      multiply.doClick();
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0), DIVIDE);
		((JPanel) getContentPane()).getActionMap().put(DIVIDE,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      divide.doClick();
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, 0), LIST);
		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.SHIFT_MASK),
		            LIST);
		((JPanel) getContentPane()).getActionMap().put(LIST,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      list_separate.doClick();
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,
		                  KeyEvent.SHIFT_MASK), SIGN);
		((JPanel) getContentPane()).getActionMap().put(SIGN,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      change_sign.doClick();
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), B);
		((JPanel) getContentPane()).getActionMap().put(B, new AbstractAction() {

			public void actionPerformed(ActionEvent event) {
				constant_boltzmann.doClick();
			}
		});

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), E);
		((JPanel) getContentPane()).getActionMap().put(E, new AbstractAction() {

			public void actionPerformed(ActionEvent event) {
				constant_e.doClick();
			}
		});

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, 0), COMMA);
		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0), COMMA);
		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0), COMMA);
		((JPanel) getContentPane()).getActionMap().put(COMMA,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      if (firstEntryOfBinaryOperation) {
					      firstEntryOfBinaryOperation = false;
					      lastActionWasEquals = false;
					      removeLastEntry();
				      }
				      comma.doClick();
			      }
		      });

		display.getInputMap(JComponent.WHEN_FOCUSED).put(
		      KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), CLEAR);

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), CLEAR);
		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), CLEAR);

		((JPanel) getContentPane()).getActionMap().put(CLEAR,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      clear.doClick();
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), CLEARALL);
		display.getInputMap(JComponent.WHEN_FOCUSED).put(
		      KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), CLEARALL);
		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), CLEARALL);
		((JPanel) getContentPane()).getActionMap().put(CLEARALL,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      clearAll.doClick();
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), EQUALS);

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), EQUALS);

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), EQUALS);
		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), EQUALS);

		((JPanel) getContentPane()).getActionMap().put(EQUALS,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      equals.doClick();
			      }
		      });

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
		      .put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_MASK),
		            TRANSFER);

		((JPanel) getContentPane())
		      .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		            KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), TRANSFER);

		((JPanel) getContentPane()).getActionMap().put(TRANSFER,
		      new AbstractAction() {

			      public void actionPerformed(ActionEvent event) {
				      transfer.doClick();
			      }
		      });

		display.getInputMap(JComponent.WHEN_FOCUSED).put(
		      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), EQUALS);

	}

	private void removeLastEntry() {
		String str = null;
		str = display.getText();
		if (display.getText().indexOf(";") > -1) {
			str = str.substring(0, str.lastIndexOf(";") + 1);
			display.setText(str);
		} else {
			display.setText("");
		}
	}

	public void resetFocus() {
		equals.requestFocusInWindow();
	}

	public void setValue(double result) {
		String str = Double.toString(result);
		if (display.getText().indexOf(";") > -1) {
			display.setText(display.getText().substring(0,
			      display.getText().lastIndexOf(";") + 1)
			      + str);
		} else {
			display.setText(str);
		}
		firstEntryOfBinaryOperation = true;
		lastActionWasEquals = false;
	}

	public void show(CalculatorInputField field) {
		clearDisplay();
		lastActionWasEquals = false;
		transferValue = false;
		try {
			setValue(((Number) field.getValue()).doubleValue());
		} catch (Exception e) {
			// Do nothing - we leave display cleared
		}
		transfer.setVisible(true);
		setVisible(true);
		statusLabel.setText("");
		try {
			if (transferValue) {
				if (!getValueString().equals("")) {
					field.setValue(getValue());
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this,
			      "Unable to transfer result to field!", "Field error",
			      JOptionPane.WARNING_MESSAGE);
		}
		transfer.setVisible(false);
	}

}
