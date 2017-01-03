package org.seamcat.presentation;

import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.UniformDistributionImpl;
import org.seamcat.help.SeamcatHelpResolver;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class DistributionTestDialog extends EscapeDialog {

	private static final String TITLE = "Seamcat Distribution Test";
	private JButton btn_Close = new JButton("Close");
	private JButton btn_Dist = new JButton("Distribution");
	private JButton btn_Gen = new JButton("Generate and show samples");
	private JCheckBox btn_Java_Random = new JCheckBox("Test Java random");

	private DialogDisplaySignal displaySignal;
	private AbstractDistribution distribution = new UniformDistributionImpl(0, 1);
	private JLabel lbl_Dist = new JLabel("Click to configure distribution:");
	private JLabel lbl_Events = new JLabel("Specify number of samples:");
	private DistributionDialog powerDistDialog;

	private Random random = new Random();
	private JFormattedTextField tf_Events = new JFormattedTextField(
	      SeamcatTextFieldFormats.getIntegerFactory(), 1000);

	public DistributionTestDialog(Frame owner) {
		super(owner, TITLE);
		powerDistDialog = new DistributionDialog(this, true);

		getContentPane().setLayout(new BorderLayout());

		JPanel center = new JPanel(new LabeledPairLayout());
		center.add(lbl_Dist, LabeledPairLayout.LABEL);
		center.add(btn_Dist, LabeledPairLayout.FIELD);
		center.add(new JLabel(""), LabeledPairLayout.LABEL);
		center.add(btn_Java_Random, LabeledPairLayout.FIELD);

		center.add(lbl_Events, LabeledPairLayout.LABEL);
		center.add(tf_Events, LabeledPairLayout.FIELD);

		JPanel south = new JPanel();
		south.add(btn_Gen);
		south.add(btn_Close);
        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                SeamcatHelpResolver.showHelp(DistributionTestDialog.this);
            }
        });
        south.add(help);

		getContentPane().add(center, BorderLayout.CENTER);
		getContentPane().add(south, BorderLayout.SOUTH);

		btn_Dist.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				boolean accept = powerDistDialog.showDistributionDialog(
				      distribution, "Configure Distribution");
				if (accept) {
					distribution = powerDistDialog.getDistribution();
				}
			}
		});

		btn_Java_Random.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				btn_Dist.setEnabled(!btn_Java_Random.isSelected());
			}

		});

		btn_Gen.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				int events = ((Number) tf_Events.getValue()).intValue();
				double[] samples = new double[events];
				boolean intJava = btn_Java_Random.isSelected();
				for (int i = 0; i < samples.length; i++) {
					if (intJava) {
						samples[i] = random.nextDouble();
					} else {
						samples[i] = distribution.trial();
					}
				}
				displaySignal = new DialogDisplaySignal(DistributionTestDialog.this, "Trial Number",
				      "Trialed Value");
				
				displaySignal.show(samples,
				      events
				            + " samples from "
				            + (intJava ? "internal java random" : distribution
				                  .toString()), "");
			}
		});

		btn_Close.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		((JComponent) getContentPane()).setBorder(new TitledBorder(
		      "Configure Distribution"));
		pack();
		setSize(400, 200);
		setLocationRelativeTo(owner);

	}

}
