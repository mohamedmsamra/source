package org.seamcat.presentation.components.interferencecalc;

import com.jgoodies.forms.layout.*;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.ICECriterionChanged;
import org.seamcat.events.ICESignalTypeChanged;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.model.engines.InterferenceCriterionType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ParametersPanel extends JPanel {
	
	private ICEConfiguration iceconf;

	public ParametersPanel() {
        rbCI = new JRadioButton();
        tfCI = new JTextField();
        rbCIN = new JRadioButton();
        tfCIN = new JTextField();
        rbNIN = new JRadioButton();
        tfNIN = new JTextField();
        rbIN = new JRadioButton();
        tfIN = new JTextField();
        CellConstraints cc = new CellConstraints();

        //======== this ========
        setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
            new RowSpec[] {
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.LINE_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.LINE_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.LINE_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,}));

        //---- rbCI ----
        rbCI.setText("C / I");
        add(rbCI, cc.xy(1, 1));

        //---- tfCI ----
        tfCI.setColumns(10);
        tfCI.setEnabled(false);
        add(tfCI, "3, 1");

        add(new JLabel("dB"), "5, 1");

        //---- rbCIN ----
        rbCIN.setText("C / (I + N)");
        add(rbCIN, cc.xy(1, 3));

        //---- tfCIN ----
        tfCIN.setColumns(10);
        tfCIN.setEnabled(false);
        add(tfCIN, cc.xy(3, 3));

        add(new JLabel("dB"), "5, 3");

        //---- rbNIN ----
        rbNIN.setText("(N + I) / N");
        add(rbNIN, cc.xy(1, 5));

        //---- tfNIN ----
        tfNIN.setColumns(10);
        tfNIN.setEnabled(false);
        add(tfNIN, cc.xy(3, 5));

        add(new JLabel("dB"), "5, 5");

        //---- rbIN ----
        rbIN.setText("I / N");
        add(rbIN, cc.xy(1, 7));

        //---- tfIN ----
        tfIN.setColumns(10);
        tfIN.setEnabled(false);
        add(tfIN, cc.xy(3, 7));

        //---- interferenceCriterionGroup ----
        ButtonGroup interferenceCriterionGroup = new ButtonGroup();
        interferenceCriterionGroup.add(rbCI);
        interferenceCriterionGroup.add(rbCIN);
        interferenceCriterionGroup.add(rbNIN);
        interferenceCriterionGroup.add(rbIN);

        add(new JLabel("dB"), "5, 7");

        rbCI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				change(InterferenceCriterionType.CI);
			}
		});

		rbCIN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                change(InterferenceCriterionType.CNI);
			}
		});
		
		rbNIN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                change(InterferenceCriterionType.INI);
			}
		});
		
		rbIN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				change(InterferenceCriterionType.IN);
			}
		});
        EventBusFactory.getEventBus().subscribe( this );
	}

    private void change( int newType ) {
        if ( iceconf != null ) {
            iceconf.setInterferenceCriterionType( newType );
            EventBusFactory.getEventBus().publish( new ICECriterionChanged(iceconf, this));
        }
    }

    @UIEventHandler
    public void handle( ICECriterionChanged event ) {
        if ( event.getOrigin() == this ) return;
        if ( iceconf == event.getIce() ) {
            init( iceconf );
        }
    }

    @UIEventHandler
    public void handle( ICESignalTypeChanged event ) {
        if ( iceconf == event.getIce() ) {
            refreshButtonStatus();
        }
    }

	public void init(ICEConfiguration iceconf) {
		this.iceconf = iceconf;
		enableButtons(true);
        rbCI.setSelected( iceconf.getInterferenceCriterionType() == InterferenceCriterionType.CI);
        rbCIN.setSelected(iceconf.getInterferenceCriterionType() == InterferenceCriterionType.CNI);
        rbNIN.setSelected(iceconf.getInterferenceCriterionType() == InterferenceCriterionType.INI);
        rbIN.setSelected(iceconf.getInterferenceCriterionType() == InterferenceCriterionType.IN);

		// Set values to empty if no interference calculations have taken place
		String ciLevel = iceconf.getCiLevel() == Double.MIN_VALUE ? "" : Double.toString(iceconf.getCiLevel());
		String cinLevel = iceconf.getCniLevel() == Double.MIN_VALUE ? "" : Double.toString(iceconf.getCniLevel());
		String ninLevel = iceconf.getIniLevel() == Double.MIN_VALUE ? "" : Double.toString(iceconf.getIniLevel());
		String inLevel = iceconf.getNiLevel() == Double.MIN_VALUE ? "" : Double.toString(iceconf.getNiLevel());

		tfCI.setText(ciLevel);
		tfCIN.setText(cinLevel);
		tfNIN.setText(ninLevel);
		tfIN.setText(inLevel);
		refreshButtonStatus();
        if (iceconf.getHasBeenCalculated()) {
            enableButtons(false);
        }
    }

    private void enableButtons( boolean enable) {
        rbCI.setEnabled(enable);
        rbCIN.setEnabled(enable);
        rbNIN.setEnabled(enable);
        rbIN.setEnabled(enable);
    }

	private void refreshButtonStatus() {
		if (iceconf != null) {
			boolean setEnabled = iceconf.isUnwanted() || iceconf.isBlocking();

            rbCI.setEnabled(setEnabled);
            rbCIN.setEnabled(setEnabled);
            rbIN.setEnabled(setEnabled);
            rbNIN.setEnabled(setEnabled);
		}
	}

	private JRadioButton rbCI;
	private JTextField tfCI;
	private JRadioButton rbCIN;
	private JTextField tfCIN;
	private JRadioButton rbNIN;
	private JTextField tfNIN;
	private JRadioButton rbIN;
	private JTextField tfIN;
}
