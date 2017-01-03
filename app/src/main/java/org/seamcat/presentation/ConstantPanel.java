package org.seamcat.presentation;

import org.seamcat.calculator.CalculatorInputField;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ResourceBundle;

public class ConstantPanel extends JPanel {

    private static final ResourceBundle stringlist = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private JFormattedTextField tfConstant;

    public ConstantPanel() {
        super(new FlowLayout(FlowLayout.LEFT));
        tfConstant = new CalculatorInputField(0);

        JLabel lblConstant = new JLabel(stringlist.getString("FUNCTION_CONSTANT"));
        lblConstant.setLabelFor(tfConstant);

        add(lblConstant);
        add(tfConstant);
        setBorder(new TitledBorder("Constant"));
    }

    public void clear() {
        setConstant(30);
    }

    public double getConstant() {
        return ((Number) tfConstant.getValue()).doubleValue();
    }

    public void setConstant (double c) {
        tfConstant.setValue(c);
    }
}
