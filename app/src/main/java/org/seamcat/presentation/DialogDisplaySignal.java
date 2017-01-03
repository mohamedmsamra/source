package org.seamcat.presentation;

import org.seamcat.presentation.propagationtest.PropagationHolder;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DialogDisplaySignal extends EscapeDialog {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
	private DisplaySignalPanel displaySignalPanel;

	public DialogDisplaySignal(Frame owner, String xTitle, String yTitle) {
		super(owner, true);
		displaySignalPanel = new DisplaySignalPanel(this, xTitle, yTitle);

	}

	public DialogDisplaySignal(JDialog owner, String xTitle, String yTitle) {
		super(owner, true);
		displaySignalPanel = new DisplaySignalPanel(this, xTitle, yTitle);

	}

	public void show(double[] _data, String title, String unit) {
		displaySignalPanel.show(_data, title, unit);
		showDialog(title);

	}

	public void show(List<PropagationHolder> propagations, String title,
	      String unit) {
		
		displaySignalPanel.show(propagations, title, unit);
		showDialog(title);

	}

	public void show(double[] _data, double[] sortedDistributions, String title,
	      String unit) {
		displaySignalPanel.show(_data, sortedDistributions, title, unit);
		showDialog(title);
	}

	public void show(double[] _data, String title, String unit,
	      double minDataValue, double maxDataValue) {
		displaySignalPanel.show(_data, title, unit, minDataValue, maxDataValue);
		showDialog(title);
	}

	public void show(double[] _data, double[] sortedDistributions, String title,
	      String unit, double minDataValue, double maxDataValue) {
		displaySignalPanel.show(_data, sortedDistributions, title, unit, minDataValue, maxDataValue);
		showDialog(title);
	}

	public void show(double[] _data, String title, String unit, String label) {
		displaySignalPanel.show(_data, title, unit, label);
		showDialog(title);

	}

	public void show(double[] _data, double[] sortedDistributions, String title,
	      String unit, String label) {
		displaySignalPanel.show(_data, sortedDistributions, title, unit, label);
		showDialog(title);


	}

	public void show(List<PropagationHolder> propagations, String title,
	      String unit, String label, double max, double min) {
		displaySignalPanel.show(propagations, title, unit, label, max, min);
		showDialog(title);
	}

	public void show(List<PropagationHolder> propagations, String title,
					 String unit, String label, double max, double min, int limit) {
		displaySignalPanel.show(propagations, title, unit, label, max, min, limit);
		showDialog(title);
	}

	public void displayDataSelectionPanel(boolean displayDataSelectionPanel) {
		displaySignalPanel.displayDataSelectionPanel(displayDataSelectionPanel);
	}

	public void showDialog(String title) {

		getContentPane().add(displaySignalPanel, BorderLayout.CENTER);

		setTitle(STRINGLIST.getString("RESULTS_VECTOR_GRAPH_TITLE_PREFIX")
		      + title);

		JDialog.setDefaultLookAndFeelDecorated(true);
		setPreferredSize(new Dimension(getPreferredSize().width, 550));
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}
}
