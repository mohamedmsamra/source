package org.seamcat.presentation.genericgui.item;

import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.presentation.genericgui.IdentityMapper;
import org.seamcat.presentation.genericgui.ValueMapper;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.util.Assert;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


public class OptionalDoubleItem extends AbstractItem<ValueWithUsageFlag<Double>, Object> {

    private CalculatorInputField valueWidget;
    private JCheckBox conditionCheckBox;
    private ValueMapper<Double, Double> valueMapper;

    public OptionalDoubleItem() {
        valueMapper = new IdentityMapper<Double>();
    }

    public OptionalDoubleItem label(String label) {
        super.label(label);
        return this;
    }

    public OptionalDoubleItem unit(String unit) {
        super.unit(unit);
        return this;
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = new ArrayList<WidgetAndKind>();

        conditionCheckBox = new JCheckBox(getLabel());
        if ( getToolTipText() != null ) {
            conditionCheckBox.setToolTipText( getToolTipText() );
        }
        conditionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateWidgetRelevance();
                fireItemChanged();
            }
        });
        widgets.add(new WidgetAndKind(conditionCheckBox, WidgetKind.LABEL));

        valueWidget = new CalculatorInputField();
        valueWidget.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fireItemChanged();
            }
        });
        widgets.add(new WidgetAndKind(valueWidget, WidgetKind.VALUE));

        widgets.add(new WidgetAndKind(new JLabel(getUnit()), WidgetKind.UNIT));

        return widgets;
    }

    @Override
    public ValueWithUsageFlag<Double> getValue() {
        return new ValueWithUsageFlag<Double>(
                conditionCheckBox.isSelected(),
                valueMapper.mapToModelValue(valueWidget.getValueAsDouble()));
    }

    @Override
    public void setValue(ValueWithUsageFlag<Double> value) {
        Assert.notNull("Value is null", value);
        valueWidget.setValue(valueMapper.mapToWidgetValue(value.value));
        conditionCheckBox.setSelected(value.useValue);
        updateWidgetRelevance();
    }

    private void updateWidgetRelevance() {
        valueWidget.setEnabled(conditionCheckBox.isSelected());
    }
}
