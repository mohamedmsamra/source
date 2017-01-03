package org.seamcat.presentation.components.interferencecalc;

import org.seamcat.model.engines.ICEConfiguration;

import javax.swing.*;
import java.awt.*;

public class AlgorithmSamplesPanel extends JPanel {

	private JTextField samples = new JTextField();
	private JTextField probabilityN = new JTextField();
	private JTextField tfSensitivity = new JTextField();
	
	public AlgorithmSamplesPanel() {
		this.setLayout(new GridLayout(5, 2));
		
		samples.setColumns(10);
		samples.setHorizontalAlignment(SwingConstants.LEFT);
		samples.setEditable(false);
		samples.setEnabled(false);

		probabilityN.setColumns(10);
		probabilityN.setHorizontalAlignment(SwingConstants.LEFT);
		probabilityN.setEnabled(false);
		probabilityN.setEditable(false);

		tfSensitivity.setColumns(10);
		tfSensitivity.setHorizontalAlignment(SwingConstants.LEFT);
		tfSensitivity.setEnabled(false);
		tfSensitivity.setEditable(false);

		add(new JLabel("Events"));
		add(samples);
		add(new JLabel("Events (dRSS > sensitivity)"));
		add(probabilityN);
		add(new JLabel("Sensitivity (dBm)"));
		add(tfSensitivity);
	}

	public void init(ICEConfiguration iceconf) {
		String totalNumber = iceconf.getNumberOfTotalEvents() == Integer.MIN_VALUE ? "" :Integer.toString((int)iceconf.getNumberOfTotalEvents());
		samples.setText(totalNumber);
		String probN = iceconf.getProbabilityTotalN() == Double.MIN_VALUE ? "" : Integer.toString((int)iceconf.getProbabilityTotalN());
		probabilityN.setText(probN);
		String sensitivity = iceconf.getSensitivity() == Double.MIN_VALUE ? "" : Double.toString(iceconf.getSensitivity());
		tfSensitivity.setText(sensitivity);
	}
}
