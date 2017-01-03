package org.seamcat.presentation.genericgui.item;

import org.seamcat.function.FunctionType;
import org.seamcat.model.functions.Function;
import org.seamcat.presentation.DialogFunctionDefine;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.valuepreview.ButtonWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.LabelWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.ValuePreviewTextUtil;
import org.seamcat.presentation.valuepreview.ValuePreviewableFunctionAdapter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class OptionalFunctionItem extends AbstractItem<ValueWithUsageFlag<Function>, Object> {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private String functionDialogTitle;
    private Function function;
    private FunctionType functionType = FunctionType.none();
    private JFrame parent;
    private JCheckBox conditionCheckBox;
    private ButtonWithValuePreviewTip functionButton;
    private LabelWithValuePreviewTip valuePreviewLabel;
    private String xAxis ="X", yAxis="Y";

    public OptionalFunctionItem(JFrame parent) {
        this.parent = parent;
    }

    public OptionalFunctionItem functionType( FunctionType functionType ) {
        this.functionType = functionType;
        functionDialogTitle = functionType.getTitle();
        return this;
    }

    public OptionalFunctionItem axis( String xAxis, String yAxis ) {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        return this;
    }

    @Override
    public ValueWithUsageFlag<Function> getValue() {
        return new ValueWithUsageFlag<Function>(conditionCheckBox.isSelected(), function);
    }

    @Override
    public void setValue(ValueWithUsageFlag<Function> value) {
        conditionCheckBox.setSelected(value.useValue);
        function = value.value;
        if ( !functionType.isNone() ) {
            functionButton.setText("Pattern");
        }
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
        DialogFunctionDefine dialog = new DialogFunctionDefine(parent, function, functionType, xAxis, yAxis);
        if (dialog.show(function, functionDialogTitle)) {
            function = dialog.getFunction();
            updateValuePreview(function);
            fireItemChanged();
        }
        dialog.destroy();
    }

    private void updateValuePreview(Function function) {
        ValuePreviewableFunctionAdapter previewable = new ValuePreviewableFunctionAdapter(function).axisNames("Degree", "Attenuation (dB)");
        if ( functionType.isNone() ) {
            valuePreviewLabel.setText(ValuePreviewTextUtil.previewLabelText(function.toString()));
            valuePreviewLabel.setPreviewable(previewable);
        } else {
            valuePreviewLabel.setText("");
        }
        functionButton.setPreviewable(previewable);
    }

    private void updateWidgetRelevance() {
        functionButton.setEnabled(isRelevant() && conditionCheckBox.isSelected());
        valuePreviewLabel.setEnabled(isRelevant() && conditionCheckBox.isSelected());
    }
}
