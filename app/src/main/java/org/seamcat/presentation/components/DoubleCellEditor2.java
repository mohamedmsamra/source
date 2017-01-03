package org.seamcat.presentation.components;

import org.seamcat.presentation.components.ValidatorDocument.Type;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.text.ParseException;

public class DoubleCellEditor2 extends DefaultCellEditor {

    static class DoubleFormatter extends DefaultFormatter {

        @Override
        public Object stringToValue(String string) {
            Double n;
            try {
                n = new Double(string.replace(',', '.')); // Convert
                // commas to
                // periods
                // System.out.println("Parsed "+string+" to "+n);
            } catch (NumberFormatException e) {
                // System.out.println("Number did not parse");
                n = null;
            }
            return n;
        }

        @Override
        public String valueToString(Object value) throws ParseException {
            return value != null ? ((Number) value).toString() : null;
        }

    }

    public DoubleCellEditor2() {
        super(new JTextField());
        final JTextField textField = (JTextField) editorComponent;

        // Remove delegate listener that was added in super class
        textField.removeActionListener(delegate);

        final DoubleFormatter formatter = new DoubleFormatter();

        // Make new delegate
        // final BigDecimal ZERO=new BigDecimal(0);
        delegate = new EditorDelegate() {

            @Override
            public Object getCellEditorValue() {
                return formatter.stringToValue(textField.getText());
            }

            @Override
            public void setValue(Object value) {
                textField.setText(value != null ? value.toString() : "");
            }
        };

        // Set validating document
        textField.setDocument(new ValidatorDocument(Type.INTEGERS,Type.FLOAT_DELIMITERS, Type.NEGATE));

        // Add new delegate as actionlistener
        textField.addActionListener(delegate);
    }

}