package org.seamcat.presentation.genericgui.item;

import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.presentation.DistributionDialog;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.valuepreview.ButtonWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.LabelWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.ValuePreviewTextUtil;
import org.seamcat.presentation.valuepreview.ValuePreviewableDistributionAdapter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class OptionalDistributionItem extends AbstractItem<ValueWithUsageFlag<AbstractDistribution>, Object> {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private String distributionDialogTitle = "Distribution";
    private AbstractDistribution distribution;
    private JFrame parent;

    private LabelWithValuePreviewTip valuePreviewLabel;
    private JCheckBox conditionCheckBox;
    private ButtonWithValuePreviewTip distributionButton;

    public OptionalDistributionItem(JFrame parent) {
        this.parent = parent;
    }

    public OptionalDistributionItem label(String label) {
        super.label(label);
        return this;
    }

    public OptionalDistributionItem unit(String unit) {
        super.unit(unit);
        return this;
    }

    @Override
    public ValueWithUsageFlag<AbstractDistribution> getValue() {
        return new ValueWithUsageFlag<AbstractDistribution>(conditionCheckBox.isSelected(), distribution);
    }

    @Override
    public void setValue(ValueWithUsageFlag<AbstractDistribution> value) {
        conditionCheckBox.setSelected(value.useValue);
        distribution = value.value;
        updateValuePreview(distribution);
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

        distributionButton = new ButtonWithValuePreviewTip(STRINGLIST.getString("BTN_CAPTION_DISTRIBUTION"));
        distributionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDistributionDialog();
            }
        });
        widgets.add(new WidgetAndKind(distributionButton, WidgetKind.VALUE));

        valuePreviewLabel = new LabelWithValuePreviewTip();
        widgets.add(new WidgetAndKind(valuePreviewLabel, WidgetKind.VALUE_PREVIEW));

        JLabel unitLabel = new JLabel(getUnit());
        widgets.add(new WidgetAndKind(unitLabel, WidgetKind.UNIT));

        return widgets;
    }

    @Override
    public void setRelevant(boolean relevant) {
        super.setRelevant(relevant);
        updateWidgetRelevance();
    }

    private void showDistributionDialog() {
        DistributionDialog dialog = new DistributionDialog(parent, true);
        if (distribution == null) {
            if (dialog.showDistributionDialog(distributionDialogTitle)) {
                distribution = dialog.getDistribution();
                updateValuePreview(distribution);
                fireItemChanged();
            }
        }
        else {
            if (dialog.showDistributionDialog(distribution, distributionDialogTitle)) {
                distribution = dialog.getDistribution();
                updateValuePreview(distribution);
                fireItemChanged();
            }
        }
    }

    private void updateValuePreview(AbstractDistribution distribution) {
        valuePreviewLabel.setText(ValuePreviewTextUtil.previewLabelText(distribution.toString()));
        valuePreviewLabel.setPreviewable(new ValuePreviewableDistributionAdapter(distribution));
        distributionButton.setPreviewable(new ValuePreviewableDistributionAdapter(distribution));
    }

    private void updateWidgetRelevance() {
        distributionButton.setEnabled(isRelevant() && conditionCheckBox.isSelected());
    }
}
