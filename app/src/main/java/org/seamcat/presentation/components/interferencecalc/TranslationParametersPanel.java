package org.seamcat.presentation.components.interferencecalc;

import org.seamcat.model.Scenario;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.presentation.LabeledPairLayout;
import org.seamcat.presentation.SeamcatTextFieldFormats;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TranslationParametersPanel extends JPanel {

	private ICEConfiguration iceconf;
	private DefaultListModel listModel = new DefaultListModel();
	private JLabel maxLabel = new JLabel("Max (dBm or dB)");
	private JFormattedTextField maxValue = new JFormattedTextField();

	private JLabel minLabel = new JLabel("Min (dBm or dB)");
	private JFormattedTextField minValue = new JFormattedTextField();
	private JList parameters = new JList(listModel);

	private JLabel pointsLabel = new JLabel("# Points");

	private JFormattedTextField pointsValue = new JFormattedTextField();

	public TranslationParametersPanel() {
		super();
		this.setLayout(new BorderLayout());
		parameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		this.add(new JScrollPane(parameters), BorderLayout.CENTER);

		minValue.setColumns(5);
		minValue.setHorizontalAlignment(SwingConstants.RIGHT);
		minValue.setFormatterFactory(SeamcatTextFieldFormats.getIntegerFactory());
		minValue.setValue(new Integer(0));

		maxValue.setColumns(5);
		maxValue.setHorizontalAlignment(SwingConstants.RIGHT);
		maxValue.setFormatterFactory(SeamcatTextFieldFormats.getIntegerFactory());
		maxValue.setValue(new Integer(100));

		pointsValue.setColumns(5);
		pointsValue.setHorizontalAlignment(SwingConstants.RIGHT);
		pointsValue.setFormatterFactory(SeamcatTextFieldFormats
		      .getIntegerFactory());
		pointsValue.setValue(new Integer(100));

		JPanel bottom = new JPanel(new LabeledPairLayout());
		bottom.add(minLabel, LabeledPairLayout.LABEL);
		bottom.add(minValue, LabeledPairLayout.FIELD);
		bottom.add(maxLabel, LabeledPairLayout.LABEL);
		bottom.add(maxValue, LabeledPairLayout.FIELD);
		bottom.add(pointsLabel, LabeledPairLayout.LABEL);
		bottom.add(pointsValue, LabeledPairLayout.FIELD);

		this.add(bottom, BorderLayout.SOUTH);

	}

	public void init(ICEConfiguration _iceconf, Scenario scenario) {
		this.iceconf = _iceconf;

		listModel.clear();
		listModel.addElement("Blocking response level / Victim link");
		listModel.addElement("Intermodulation response level / Victim link");

		List<InterferenceLink> interferenceLinks = (List<InterferenceLink>) scenario.getInterferenceLinks();
        for ( InterferenceLink link : interferenceLinks) {
			listModel.addElement("Power supplied / " + link.getInterferingSystem().getName());
		}

		parameters.setSelectedIndex(iceconf.getTranslationParameter());
		minValue.setValue(new Double(iceconf.getTranslationMin()));
		maxValue.setValue(new Double(iceconf.getTranslationMax()));
		pointsValue.setValue(new Double(iceconf.getTranslationPoints()));
	}

	public void setElementStatusEnabled(boolean value) {
		parameters.setEnabled(value);
		minValue.setEnabled(value);
		minLabel.setEnabled(value);
		maxValue.setEnabled(value);
		maxLabel.setEnabled(value);
		pointsValue.setEnabled(value);
		pointsLabel.setEnabled(value);
	}

	public void updateModel() {
		try {
			iceconf.setTranslationParameter(parameters.getSelectedIndex());
			iceconf
			      .setTranslationMin(((Number) minValue.getValue()).doubleValue());
			iceconf
			      .setTranslationMax(((Number) maxValue.getValue()).doubleValue());
			iceconf.setTranslationPoints(((Number) pointsValue.getValue())
			      .doubleValue());
		} catch (NullPointerException ne) {
		}
	}

}
