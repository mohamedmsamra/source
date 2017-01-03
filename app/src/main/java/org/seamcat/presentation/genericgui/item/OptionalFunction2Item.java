package org.seamcat.presentation.genericgui.item;

import org.seamcat.model.functions.EmissionMask;
import org.seamcat.presentation.DialogFunction2Define;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.valuepreview.ButtonWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.LabelWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.ValuePreviewTextUtil;
import org.seamcat.presentation.valuepreview.ValuePreviewableFunction2Adapter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class OptionalFunction2Item extends AbstractItem<ValueWithUsageFlag<EmissionMask>, Object> {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private String functionDialogTitle;
    private EmissionMask function;
    private JFrame parent;
    private String xAxisName;
    private String yAxisName;
    private JCheckBox conditionCheckBox;
    private ButtonWithValuePreviewTip functionButton;
    private LabelWithValuePreviewTip valuePreviewLabel;

    public OptionalFunction2Item(JFrame parent, String name, String xAxisName, String yAxisName) {
        this.parent = parent;
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
        functionDialogTitle = name + " X(MHz) / Y(dBm)";
    }

    public OptionalFunction2Item label(String label) {
        super.label(label);
        return this;
    }

    public OptionalFunction2Item unit(String unit) {
        super.unit(unit);
        return this;
    }

    public OptionalFunction2Item axisNames(String xAxisName, String yAxisName) {
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
        return this;
    }

    @Override
    public ValueWithUsageFlag<EmissionMask> getValue() {
        return new ValueWithUsageFlag<EmissionMask>(conditionCheckBox.isSelected(), function);
    }

    @Override
    public void setValue(ValueWithUsageFlag<EmissionMask> value) {
        conditionCheckBox.setSelected(value.useValue);
        function = value.value;
        updateValuePreview(function);
        updateWidgetRelevance();
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = new ArrayList<WidgetAndKind>();

        conditionCheckBox = new JCheckBox(getLabel());
        conditionCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateWidgetRelevance();
                fireItemChanged();
            }
        });
        widgets.add(new WidgetAndKind(conditionCheckBox, WidgetKind.LABEL));

        functionButton = new ButtonWithValuePreviewTip(STRINGLIST.getString("BTN_CAPTION_FUNCTION"));
        functionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFunctionDialog();
            }
        });
        widgets.add(new WidgetAndKind(functionButton, WidgetKind.VALUE));
        valuePreviewLabel = new LabelWithValuePreviewTip();
        widgets.add(new WidgetAndKind(valuePreviewLabel, WidgetKind.VALUE_PREVIEW));
        widgets.add(new WidgetAndKind(new JLabel(getUnit()), WidgetKind.UNIT));

        return widgets;
    }

    @Override
    public void setRelevant(boolean relevant) {
        super.setRelevant(relevant);
        updateWidgetRelevance();
    }

    private void showFunctionDialog() {
        DialogFunction2Define dialog = new DialogFunction2Define(parent, true);
        if (dialog.show(function, functionDialogTitle, xAxisName, yAxisName)) {
            function = dialog.getFunction();
            updateValuePreview(function);
            fireItemChanged();
        }
    }

    private void updateValuePreview(EmissionMask function) {
        valuePreviewLabel.setText(ValuePreviewTextUtil.previewLabelText(function.toString()));
        ValuePreviewableFunction2Adapter previewable = new ValuePreviewableFunction2Adapter(function).axisNames(xAxisName, yAxisName);
        valuePreviewLabel.setPreviewable(previewable);
        functionButton.setPreviewable(previewable);
    }

    private void updateWidgetRelevance() {
        functionButton.setEnabled(isRelevant() && conditionCheckBox.isSelected());
        valuePreviewLabel.setEnabled(isRelevant() && conditionCheckBox.isSelected());
    }
}
