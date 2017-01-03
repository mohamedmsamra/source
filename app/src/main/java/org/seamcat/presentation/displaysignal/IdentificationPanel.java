package org.seamcat.presentation.displaysignal;

import org.seamcat.model.types.Unit;
import org.seamcat.presentation.layout.VerticalSubPanelLayoutManager;
import org.seamcat.presentation.propagationtest.PropagationHolder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class IdentificationPanel extends JPanel {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
	
	private JLabel lbMeanDBm = new JLabel("");
	private JLabel lbMedian = new JLabel("");
	private JLabel lbStddev = new JLabel("");
    private JLabel lbVariance = new JLabel("");
	private JLabel lbMin = new JLabel("");
	private JLabel lbMax = new JLabel("");
	private NumberFormat nf = new DecimalFormat("0.00");

	public IdentificationPanel() {
		setLayout(new VerticalSubPanelLayoutManager());
		add(lbMeanDBm);
		add(lbMedian);
		add(lbStddev);
        add(lbVariance);
		add(lbMin);
		add(lbMax);

		setBorder(new TitledBorder(
		      STRINGLIST.getString("RESULTS_VECTOR_IDENTIFICATION_TITLE")));
	}

	public String formatDouble(double value) {
		return nf.format(value);
	}

    public void setModel(PropagationHolder model) {
        setText(lbMeanDBm, "RESULTS_VECTOR_MEAN_PREFIX", model.getAverage(), Unit.dBm.name()  );
        setText(lbMedian,  "RESULTS_VECTOR_MEDIAN_PREFIX", model.getMedian(), Unit.dBm.name());
        setText(lbStddev,  "RESULTS_VECTOR_STD_PREFIX", model.getStandardDeviation(), Unit.dB.name() );
        setText(lbVariance,"RESULTS_VECTOR_VARIANCE_PREFIX", model.getVariance(), Unit.dB.name());
		setText(lbMin,     "RESULTS_VECTOR_MIN_PREFIX", model.getMin(), Unit.dBm.name());
        setText(lbMax,     "RESULTS_VECTOR_MAX_PREFIX", model.getMax(), Unit.dBm.name() );
    }

    private void setText( JLabel label, String pre, double number, String unit) {
        label.setText(STRINGLIST.getString(pre) + nf.format(number) +" "+ unit);
    }
}
