package org.seamcat.presentation.genericgui.item;

import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.presentation.genericgui.IdentityMapper;
import org.seamcat.presentation.genericgui.ValueMapper;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.util.Assert;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;


public class DoubleItem extends AbstractItem<Double, Object> {

    private CalculatorInputField valueWidget;
    private ValueMapper<Double, Double> valueMapper;
    private FocusAdapter listener;

    public DoubleItem() {
        valueMapper = new IdentityMapper<Double>();
    }

    public DoubleItem label(String label) {
        super.label(label);
        return this;
    }

    public DoubleItem unit(String unit) {
        super.unit(unit);
        return this;
    }

    public DoubleItem mapper(ValueMapper<Double, Double> mapper) {
        this.valueMapper = mapper;
        return this;
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();
        valueWidget = new CalculatorInputField();
        listener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                fireItemChanged();
            }
        };
        valueWidget.addFocusListener(listener);
        widgets.add(new WidgetAndKind(valueWidget, WidgetKind.VALUE));
        return widgets;
    }

    @Override
    public Double getValue() {
        return valueMapper.mapToModelValue(valueWidget.getValueAsDouble());
    }

    @Override
    public void setValue(Double value) {
        Assert.notNull("Value is null", value);
        valueWidget.setValue(valueMapper.mapToWidgetValue(value));
    }

    public void dispose() {
        super.dispose();
        valueWidget.removeFocusListener(listener);
        valueWidget.dispose();
    }
}
