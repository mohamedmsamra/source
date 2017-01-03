package org.seamcat.presentation.genericgui.item;

import org.seamcat.function.DiscreteFunction;
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
import java.util.List;
import java.util.ResourceBundle;


public class FunctionItem extends AbstractItem<Function, Object> {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private FunctionType functionType = FunctionType.none();
    private Function function;
    private JFrame parent;
    private String xAxisName;
    private String yAxisName;
    private LabelWithValuePreviewTip valuePreviewLabel;
    private ButtonWithValuePreviewTip functionButton;
    private JLabel unitPreviewLabel;
    private ActionListener actionListener;


    public FunctionItem(JFrame parent) {
        this.parent = parent;
    }

    public FunctionItem functionType( FunctionType functionType ) {
        this.functionType = functionType;
        return this;
    }


    public FunctionItem label(String label) {
        super.label(label);
        return this;
    }

    public FunctionItem unit(String unit) {
        super.unit(unit);
        yAxisName = unit;
        return this;
    }

    public FunctionItem axisNames(String xAxisName, String yAxisName) {
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
        return this;
    }

    @Override
    public Function getValue() {
        return function;
    }

    @Override
    public void setValue(Function value) {
        function = value;
        updateValuePreview(function);
        if ( !functionType.isNone() ) {
            functionButton.setText("Pattern");
        }
    }

    public void setUnit(String unit){
        updateUnit(unit);
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();
        functionButton = new ButtonWithValuePreviewTip(STRINGLIST.getString("BTN_CAPTION_FUNCTION"));
        actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFunctionDialog();
            }
        };
        functionButton.addActionListener(actionListener);
        widgets.add(new WidgetAndKind(functionButton, WidgetKind.VALUE));

        valuePreviewLabel = new LabelWithValuePreviewTip();
        widgets.add(new WidgetAndKind(valuePreviewLabel, WidgetKind.VALUE_PREVIEW));

        unitPreviewLabel = new JLabel();
        widgets.add(new WidgetAndKind(unitPreviewLabel, WidgetKind.UNIT));

        return widgets;
    }

    private void showFunctionDialog() {
        DialogFunctionDefine dialog = new DialogFunctionDefine(parent, function, functionType, xAxisName, yAxisName);

        if (dialog.show(function, getLabel())) {
            function = dialog.getFunction();
            updateValuePreview(function);
            fireItemChanged();
        }
        dialog.destroy();
    }

    private void updateValuePreview(Function function) {
        ValuePreviewableFunctionAdapter previewable = new ValuePreviewableFunctionAdapter(function).axisNames(xAxisName, yAxisName);
        if ( functionType.isNone())  {
            valuePreviewLabel.setText(ValuePreviewTextUtil.previewLabelText(DiscreteFunction.pretty(function)));
            valuePreviewLabel.setPreviewable(previewable);
        } else {
            valuePreviewLabel.setText("");
        }
        functionButton.setPreviewable(previewable);
    }

    private void updateUnit(String string) {
        unitPreviewLabel.setText(string);
    }

    public void dispose() {
        super.dispose();
        functionButton.removeActionListener(actionListener);
        valuePreviewLabel.dispose();
        functionButton.dispose();
    }

}
