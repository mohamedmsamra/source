package org.seamcat.presentation.genericgui.item;

import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/** Item for selecting a value from a set of possible values
 */
public class SelectionItem<ValueType> extends AbstractItem<ValueType, Object> {

    private static class ComboBoxElement<ValueType> {
        ValueType value;
        String selectionString;

        ComboBoxElement(ValueType value, String selectionString) {
            this.value = value;
            this.selectionString = selectionString;
        }


        ComboBoxElement(ValueType value) {
            this(value, value.toString());
        }

        @Override
        public String toString() {
            return selectionString;
        }
    }

    private List<ComboBoxElement<ValueType>> values = new ArrayList<ComboBoxElement<ValueType>>();
    private JComboBox valueWidget;

    public SelectionItem<ValueType> label(String label) {
        super.label(label);
        return this;
    }

    public SelectionItem<ValueType> values(Iterable<ValueType> values) {
        for (ValueType value: values) {
            this.values.add(new ComboBoxElement<ValueType>(value));
        }
        return this;
    }

    public SelectionItem<ValueType> value(ValueType value, String selectionString) {
        this.values.add(new ComboBoxElement<ValueType>(value, selectionString));
        return this;
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();
        valueWidget = new JComboBox();
        valueWidget.setModel(new DefaultComboBoxModel(values.toArray()));
        valueWidget.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireItemChanged();
            }
        });
        widgets.add(new WidgetAndKind(valueWidget, WidgetKind.VALUE));
        return widgets;
    }

    private ComboBoxElement<ValueType> findElementForValue(ValueType value) {
        for (ComboBoxElement<ValueType> element: values) {
            if (element.value.equals(value)) {
                return element;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ValueType getValue() {
        return ((ComboBoxElement<ValueType>) valueWidget.getSelectedItem()).value;
    }

    @Override
    public void setValue(ValueType value) {
        if (value == null) {
            valueWidget.setSelectedItem(value);
            return;
        }

        ComboBoxElement<ValueType> element = findElementForValue(value);
        if (element != null) {
            valueWidget.setSelectedItem(element);
        }
        else {
            throw new RuntimeException("Value "+value+" is not valid for object selection");
        }
    }

}
