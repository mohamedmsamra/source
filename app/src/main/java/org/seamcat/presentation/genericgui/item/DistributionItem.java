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
import java.util.List;
import java.util.ResourceBundle;


public class DistributionItem extends AbstractItem<AbstractDistribution, Object> {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

	private String distributionDialogTitle = "Distribution";
	private AbstractDistribution distribution;
	private JFrame parent;
	private LabelWithValuePreviewTip valuePreviewLabel;
	private ButtonWithValuePreviewTip distributionButton;
	private ItemChanged<AbstractDistribution> handler;


	public DistributionItem(JFrame owner) {
		parent = owner;
	}

	public DistributionItem label(String label) {
		super.label(label);
		return this;
	}

	public DistributionItem unit(String unit) {
		super.unit(unit);
		return this;
	}

	@Override
	public AbstractDistribution getValue() {
		return distribution;
	}

	@Override
	public void setValue(AbstractDistribution value) {
		distribution = value;
		updateValuePreview(distribution);
	}

	@Override
	public List<WidgetAndKind> createWidgets() {
		List<WidgetAndKind> widgets = super.createWidgets();

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

		return widgets;
	}

	private void showDistributionDialog() {
		DistributionDialog dialog = new DistributionDialog(parent, true);
		if (distribution == null) {
			if (dialog.showDistributionDialog(distributionDialogTitle)) {
				distribution = dialog.getDistribution();
				updateValuePreview(distribution);
				fireItemChanged();
				if ( handler != null ) {
					handler.itemChanged( distribution );
				}
			}
		} else {
			if (dialog.showDistributionDialog(distribution, distributionDialogTitle)) {
				distribution = dialog.getDistribution();
				updateValuePreview(distribution);
				fireItemChanged();
				if ( handler != null ) {
					handler.itemChanged( distribution );
				}
			}
		}
	}

	private void updateValuePreview(AbstractDistribution distribution) {
		valuePreviewLabel.setText(ValuePreviewTextUtil.previewLabelText(distribution.toString()));
		ValuePreviewableDistributionAdapter previewable = new ValuePreviewableDistributionAdapter(distribution);
		valuePreviewLabel.setPreviewable(previewable);
		distributionButton.setPreviewable(previewable);
	}

	public void dispose() {
		super.dispose();

	}

	public void addItemChangedHandler(ItemChanged<AbstractDistribution> handler ) {
		this.handler = handler;
	}
}
