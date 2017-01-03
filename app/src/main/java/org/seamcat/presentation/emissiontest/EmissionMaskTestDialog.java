package org.seamcat.presentation.emissiontest;

import org.apache.log4j.Logger;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.FunctionException;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.presentation.DialogDisplaySignal;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EmissionMaskTestDialog extends EscapeDialog {

    private static final Logger LOG = Logger.getLogger(EmissionMaskTestDialog.class);
	private static final String TITLE = "Emission Mask Test";
    private DialogDisplaySignal displaySignal;

	public EmissionMaskTestDialog(JFrame owner) {
		super(owner, TITLE);
		getContentPane().setLayout(new BorderLayout());
        final GenericPanelEditor<EmissionTestPanel> editor = new GenericPanelEditor<EmissionTestPanel>(owner, EmissionTestPanel.class, ProxyHelper.newInstance(EmissionTestPanel.class));

        JPanel south = new JPanel();
        JButton btn_Gen = new JButton("Generate and show samples");
        south.add(btn_Gen);
        JButton btn_Close = new JButton("Close");
        south.add(btn_Close);
        JButton help = new JButton("Help");
        south.add(help);


		getContentPane().add(editor, BorderLayout.CENTER);
		getContentPane().add(south, BorderLayout.SOUTH);

		btn_Gen.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                EmissionTestPanel model = editor.getModel();
                int events = model.samples();
                EmissionMask function = model.mask();
                Distribution difference = model.frequencyDiff();

                EmissionMask normalized = function.normalize();
                double ref = model.bandwidth() / 1000;
                double[] samples = new double[events];

                for (int i = 0; i < samples.length; i++) {
                    try {
                        samples[i] = normalized.integrate(difference.trial(), ref);
                    } catch (FunctionException ex) {
                        LOG.error("An Error occured", ex);
                    }
                }
                displaySignal = new DialogDisplaySignal(EmissionMaskTestDialog.this, "Event", "Function Value");
                displaySignal.show(samples, events + " samples from specified function", "");
            }
        });

		btn_Close.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
		((JComponent) getContentPane()).setBorder(new TitledBorder(
		      "Configuration"));
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                SeamcatHelpResolver.showHelp(EmissionMaskTestDialog.this);
            }
        });
		pack();
		setLocationRelativeTo(owner);
	}

}
