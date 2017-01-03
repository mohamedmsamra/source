package org.seamcat.presentation.genericgui.item;

import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.util.Assert;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


public class IntegerItem extends AbstractItem<Integer, Object> {

    private CalculatorInputField valueWidget;

    public IntegerItem label(String label) {
        super.label(label);
        return this;
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();
        valueWidget = new CalculatorInputField();
        valueWidget.setIntegerMode(true);
        valueWidget.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireItemChanged();
            }
        });
        widgets.add(new WidgetAndKind(valueWidget, WidgetKind.VALUE));
        return widgets;
    }

    @Override
    public Integer getValue() {
        return valueWidget.getValueAsInteger();
    }

    @Override
    public void setValue(Integer value) {
        Assert.notNull("Value is null", value);
        valueWidget.setValue(value);
    }

    @Override
    public IntegerItem unit(String unit) {
        super.unit(unit);	 //To change body of overridden methods use File | Settings | File Templates.
        return this;
    }
}
