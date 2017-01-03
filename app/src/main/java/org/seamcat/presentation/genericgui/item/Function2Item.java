package org.seamcat.presentation.genericgui.item;

import org.seamcat.model.functions.EmissionMask;
import org.seamcat.presentation.DialogFunction2Define;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.model.VictimCharacteristics;
import org.seamcat.presentation.valuepreview.ButtonWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.LabelWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.ValuePreviewTextUtil;
import org.seamcat.presentation.valuepreview.ValuePreviewableFunction2Adapter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Function2Item extends AbstractItem<EmissionMask, Object> {

	private String functionDialogTitle;
	private EmissionMask function;
	private JFrame parent;
	private String xAxisName;
	private String yAxisName;
	private LabelWithValuePreviewTip valuePreviewLabel;
	private ButtonWithValuePreviewTip functionButton;
	private VictimCharacteristics victimCharacteristics;
    private ActionListener actionListener;

	public Function2Item(JFrame parent) {
		this.parent = parent;
	}

    public Function2Item(JFrame parent, String xAxisName, String yAxisName) {
        this.parent = parent;
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
    }

	public Function2Item label(String label) {
		super.label(label);
		return this;
	}

	public Function2Item unit(String unit) {
		super.unit(unit);
		return this;
	}

	public Function2Item axisNames(String xAxisName, String yAxisName) {
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;
		return this;
	}

	public void setVictimCharacteristics(VictimCharacteristics victimCharacteristics) {
		this.victimCharacteristics = victimCharacteristics;
	}

	@Override
	public EmissionMask getValue() {
		return function;
	}

	@Override
	public void setValue(EmissionMask value) {
		function = value;
		updateValuePreview(function);
	}

	@Override
	public List<WidgetAndKind> createWidgets() {
		List<WidgetAndKind> widgets = super.createWidgets();

		functionButton = new ButtonWithValuePreviewTip("Edit");
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

		return widgets;
	}

	private void showFunctionDialog() {
		DialogFunction2Define dialog = new DialogFunction2Define(parent, true);

		boolean okFromDialog;
		if (victimCharacteristics != null) {
			okFromDialog = dialog.show(function, functionDialogTitle, victimCharacteristics.getVictimBandwidth(), victimCharacteristics.getFrequencyOffset(), victimCharacteristics.isShowACLR(), victimCharacteristics.getInterfererBandwidth(), xAxisName, yAxisName);
		} else {
			okFromDialog = dialog.show(function, functionDialogTitle, xAxisName, yAxisName);
		}

		if (okFromDialog) {
			function = dialog.getFunction();
			updateValuePreview(function);
			fireItemChanged();
		}
	}

	private void updateValuePreview(EmissionMask function) {
		valuePreviewLabel.setText(ValuePreviewTextUtil.previewLabelText(function.toString()));
		ValuePreviewableFunction2Adapter previewable = new ValuePreviewableFunction2Adapter(function).axisNames(xAxisName, yAxisName);
		previewable.setVictimCharacteristics(victimCharacteristics);
		valuePreviewLabel.setPreviewable(previewable);
		functionButton.setPreviewable(previewable);
	}

    public void dispose() {
        super.dispose();
        functionButton.removeActionListener(actionListener);
    }
}
