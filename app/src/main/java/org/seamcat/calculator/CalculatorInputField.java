package org.seamcat.calculator;

import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.SeamcatIcons;
import org.seamcat.presentation.SeamcatTextFieldFormats;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class CalculatorInputField extends JFormattedTextField implements DocumentListener {

	private static final long serialVersionUID = 1L;
	private static Calculator CALC;
    private final MouseAdapter mouseListener;
    private final MouseMotionListener mouseMotionListener;
    private final AbstractAction action;
    private boolean integerMode = false;
	private boolean allowNegatives = true;
	private boolean allowZero = true;
	private boolean fractionMode = false; // Only allow input in 0 - 1 range

	private Color invalid = new Color(255, 120, 120);
	private Color valid;

	private static final AbstractFormatterFactory DOUBLE_FACTORY;

	static {
		DecimalFormat format = new DecimalFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
		format.setMinimumFractionDigits(1);
		format.setMaximumFractionDigits(15);
		NumberFormatter f = new NumberFormatter(format);
		f.setAllowsInvalid(true);
		DOUBLE_FACTORY = new DefaultFormatterFactory(f, f, f);
	}

	public CalculatorInputField() {
		super(DOUBLE_FACTORY);
		DefaultFormatter formatter = (DefaultFormatter) getFormatter();
		formatter.setCommitsOnValidEdit(true);

		setBorder(new ImageIconBorder(this, SeamcatIcons.getImageIcon("SEAMCAT_ICON_CALCULATOR", SeamcatIcons.IMAGE_SIZE_16x16), SeamcatIcons.getImageIcon("SEAMCAT_ICON_CALCULATOR", SeamcatIcons.IMAGE_SIZE_16x16_DISABLED)));
		setColumns(8);
		setHorizontalAlignment(SwingConstants.RIGHT);
		addFocusListener(SeamcatTextFieldFormats.SELECTALL_FOCUSHANDLER);
		valid = getBackground();

		getDocument().addDocumentListener(this);

        mouseListener = new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getPoint().getX() >= getSize().getWidth() - 16) {
                    if (isEnabled()) {
                        try {
                            CalculatorInputField.this.commitEdit();
                        } catch (ParseException ex) {
                            setValue(0);
                        }
                        try {
                            if (CALC == null) {
                                CALC = new Calculator(MainWindow.getInstance());
                            }

                            CALC.show(CalculatorInputField.this);
                        } catch (Exception ex) {
                            // Do nothing
                        }
                    }
                } else {
                    requestFocus();
                }

            }

            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        };
        addMouseListener(mouseListener);

        mouseMotionListener = new MouseMotionListener() {

            public void mouseDragged(MouseEvent e) {

            }

            public void mouseMoved(MouseEvent e) {
                if (e.getPoint().getX() >= getSize().getWidth() - 16) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                } else {
                    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
            }

        };
        addMouseMotionListener(mouseMotionListener);

		getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK), this.getClass().getName());
        action = new AbstractAction() {

            public void actionPerformed(ActionEvent event) {
                try {
                    CalculatorInputField.this.commitEdit();
                } catch (ParseException ex) {
                    setValue(0);
                }
                try {
                    if (CALC == null) {
                        CALC = new Calculator(MainWindow.getInstance());
                    }

                    CALC.show(CalculatorInputField.this);
                } catch (Exception ex) {
                    // Do nothing
                }
            }
        };
        getActionMap().put(this.getClass().getName(), action);

	}

	public CalculatorInputField(double value) {
		this();
		setValue(value);
	}

	public double getValueAsDouble() {
		try {

			return getValueAsNumber().doubleValue();
		} catch (NullPointerException ne) {
			try {
				return Double.parseDouble(getText());
			} catch (Exception e) {
				return 0;
			}
		}
	}

	public int getValueAsInteger() {
		return getValueAsNumber().intValue();
	}

	public Number getValueAsNumber() {
		if ( integerMode ) {
			try {
				return Integer.parseInt( getText() );
			} catch ( NumberFormatException e ) {
				return 0;
			}
		} else {
			String valueText = getText();
			if(valueText.indexOf(",") != -1){
				valueText = valueText.replace(",", "");
				return Double.parseDouble( valueText );
			}
			try {
				return Double.parseDouble( getText() );
			} catch ( NumberFormatException e ) {
				return 0.0;
			}
		}
	}

	public boolean isIntegerMode() {
		return integerMode;
	}

	public void setIntegerMode(boolean integerMode) {
		this.integerMode = integerMode;
		if (integerMode) {
			super.setFormatterFactory(SeamcatTextFieldFormats.INTEGER_FACTORY);
		} else {
			super.setFormatterFactory(DOUBLE_FACTORY);
			fractionMode = false;
		}
	}

	public void setAllowNegatives(boolean allowNegatives) {
		this.allowNegatives = allowNegatives;
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		update(validateValue(getValueAsDouble()));
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		update(validateValue(getValueAsDouble()));
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		update(validateValue(getValueAsDouble()));
	}

	protected boolean validateValue(double value) {
		if (fractionMode) {
			return ((value >= 0) && (value <= 1));
		} else if (!allowNegatives) {
			return (value >= 0);
		}
		return true;
	}

	protected void update(boolean v) {
		setBackground(v ? valid : invalid);
	}

    public void dispose() {
        removeMouseListener( mouseListener );
        removeMouseMotionListener( mouseMotionListener );
        getActionMap().remove(this.getClass().getName());
    }
}
