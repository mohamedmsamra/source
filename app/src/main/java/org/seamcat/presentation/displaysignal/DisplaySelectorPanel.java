package org.seamcat.presentation.displaysignal;

import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.presentation.DisplaySignalPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.ResourceBundle;


public class DisplaySelectorPanel extends JPanel {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
	
	private final ButtonGroup buttons = new ButtonGroup();
	private final ButtonGroup vectorButtons = new ButtonGroup();
	private final JRadioButton cdf = new JRadioButton(STRINGLIST.getString("RESULTS_VECTOR_GRAPH_CDF_BUTTON_TITLE"));
	private final JRadioButton density = new JRadioButton(STRINGLIST.getString("RESULTS_VECTOR_GRAPH_DENSITY_BUTTON_TITLE"));
	private final JRadioButton vector = new JRadioButton(STRINGLIST.getString("RESULTS_VECTOR_GRAPH_VECTOR_BUTTON_TITLE"));
	private final JRadioButton vectorLinear = new JRadioButton(STRINGLIST.getString("RESULTS_VECTOR_GRAPH_VECTOR_LINEAR_BUTTON_TITLE"));
	private final JRadioButton vectorLog = new JRadioButton(STRINGLIST.getString("RESULTS_VECTOR_GRAPH_VECTOR_LOG_BUTTON_TITLE"));
    private final CalculatorInputField binCount = new CalculatorInputField();

    public DisplaySelectorPanel(final DisplaySignalPanel parent) {
        final JPanel bCount = new JPanel(new GridLayout(1,2));
        final JLabel size = new JLabel("Bin number: ");
        bCount.add(size);
        bCount.add(binCount);
        binCount.setIntegerMode(true);
        binCount.setAllowNegatives(false);
        binCount.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                Object val = event.getNewValue();
                if ( val != null ) {
                    parent.density();
                }
            }
        });

        setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		vector.setSelected(true);
		gc.anchor = GridBagConstraints.WEST;
		gc.weighty = 1.0;
		gc.weightx = 1.0;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridheight = 1;
		gc.insets = new Insets(0, 0, 0, 50);
		add(vector, gc);

		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridheight = 2;
		gc.insets = new Insets(0, 20, 0, 0);
		add(vectorLinear, gc);

		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridheight = 2;
		gc.insets = new Insets(0, 80, 0, 0);
		add(vectorLog, gc);

		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridheight = 1;
		gc.insets = new Insets(0, 0, 0, 50);
		add(cdf, gc);

		gc.gridx = 0;
		gc.gridy = 4;
		gc.gridheight = 1;
		gc.insets = new Insets(0, 0, 0, 50);
		add(density, gc);

        gc.gridx = 0;
        gc.gridy = 5;
        gc.gridheight = 1;
        gc.insets = new Insets(0, 10, 0, 0);
        add(bCount, gc);

		buttons.add(vector);
		buttons.add(cdf);
		buttons.add(density);
		vectorButtons.add(vectorLinear);
		vectorButtons.add(vectorLog);

		setBorder(new TitledBorder(STRINGLIST.getString("RESULTS_VECTOR_BUTTONS_TITLE")));
        size.setEnabled(false);
        binCount.setEnabled( false);

        density.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                boolean selected = density.isSelected();
                size.setEnabled(selected);
                binCount.setEnabled( selected );
            }
        });
	}

	
   public JRadioButton getVectorLinear() {
   	return vectorLinear;
   }


	
   public JRadioButton getVectorLog() {
   	return vectorLog;
   }


	
   public JRadioButton getVector() {
   	return vector;
   }


	
   public JRadioButton getCdf() {
   	return cdf;
   }


	
   public JRadioButton getDensity() {
   	return density;
   }

    public int getBinSize() {
        return binCount.getValueAsInteger();
    }

    public void setBinSize( int binSize ) {
        binCount.setValue( binSize );
    }
}