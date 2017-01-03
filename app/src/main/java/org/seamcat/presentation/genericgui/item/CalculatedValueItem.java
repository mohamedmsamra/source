package org.seamcat.presentation.genericgui.item;

import org.seamcat.model.systems.CalculatedValue;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;

import javax.swing.*;
import java.util.List;

public class CalculatedValueItem extends AbstractItem<CalculatedValue, Object> {

    private JButton evaluateButton;
    private JLabel result;

    public JButton getEvaluateButton() {
        return evaluateButton;
    }

    public CalculatedValueItem label(String label) {
        super.label(label);
        return this;
    }

    public CalculatedValueItem unit(String unit) {
        super.unit(unit);
        return this;
    }

    public void setResult( String result ) {
        this.result.setText( result );
        this.result.revalidate();
        this.result.repaint();
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();
        WidgetAndKind label = null;
        for (WidgetAndKind widget : widgets) {
            if ( widget.getKind() == WidgetKind.LABEL ) {
                label = widget;
            }
        }
        widgets.remove( label );
        result = new JLabel("", SwingConstants.RIGHT);
        evaluateButton = new JButton(getLabel());
        widgets.add(new WidgetAndKind(evaluateButton, WidgetKind.LABEL));
        widgets.add(new WidgetAndKind(result, WidgetKind.VALUE));
        return widgets;
    }

    @Override
    public CalculatedValue getValue() {
        return new CalculatedValue(null);
    }

    @Override
    public void setValue(CalculatedValue calculatedValue) {}
}
