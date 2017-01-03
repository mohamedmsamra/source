package org.seamcat.presentation;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.SelectionMadeEvent;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.presentation.components.DiscreteFunction2Panel;
import org.seamcat.presentation.components.NavigateButtonPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

public class DialogFunction2OptionDefine extends EscapeDialog {

    private static final ResourceBundle stringlist = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private final String xAxis;
    private final String yAxis;
    private SelectionPanel selectionPanel = new SelectionPanel(this, stringlist.getString("FUNCTION_CONSTANT"),stringlist.getString("FUNCTION_USERDEFINED"));
    private JPanel detailPanel = new JPanel(new BorderLayout());
    private ConstantPanel constantFunctionPanel = new ConstantPanel();
    private DiscreteFunction2Panel userDefinitionPanel = new DiscreteFunction2Panel();

	public DialogFunction2OptionDefine(Frame parent, boolean modal, String xAxis, String yAxis) {
		super(parent, modal);
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        init();
        userDefinitionPanel.setVictimCharacteristics(-1, -1, false, 0);
        userDefinitionPanel.setFunctionable(new EmissionMaskImpl(), xAxis, yAxis);
		this.setLocationRelativeTo(parent);
        EventBusFactory.getEventBus().subscribe(this);
	}

    public void destroy() {
        EventBusFactory.getEventBus().unsubscribe( this );
    }

    @UIEventHandler
    public void handle( SelectionMadeEvent event ) {
        if ( event.getContext() == this ) {
            updateEditPanel(event.getName());
        }
    }

    private void updateEditPanel( String selected ) {
        detailPanel.removeAll();
        if ( selected.equals(stringlist.getString("FUNCTION_CONSTANT"))) {
            detailPanel.add( constantFunctionPanel, BorderLayout.CENTER );
        } else {
            detailPanel.add( userDefinitionPanel, BorderLayout.CENTER );
        }
        detailPanel.revalidate();
        detailPanel.repaint();

    }

	public EmissionMask getFunction() {
        return userDefinitionPanel.getFunctionable();
	}

	private void init() {
		setSize( new Dimension(700, 450));
        getContentPane().add(selectionPanel, BorderLayout.WEST );
        getContentPane().add(detailPanel, BorderLayout.CENTER);
		getContentPane().add(new NavigateButtonPanel(this), BorderLayout.SOUTH);
		registerHelp();
	}

	private void registerHelp() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
         public void actionPerformed(ActionEvent e) {
                SeamcatHelpResolver.showHelp(DialogFunction2OptionDefine.this);
         }
			
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
   }

	public boolean show(EmissionMask function) {
		return show(function, "Function definition", "X", "Y");
	}

    private void setFunction( EmissionMask function, double victimBandwidth, double frequencyOffset, boolean showACLR, double interfererBandwidth, String xUnit, String yUnit ) {
        if ( function.isConstant()) {
            constantFunctionPanel.setConstant(function.getConstant());
            detailPanel.add( constantFunctionPanel, BorderLayout.CENTER );
            selectionPanel.setSelection(stringlist.getString("FUNCTION_CONSTANT"));

        } else {
            detailPanel.add( userDefinitionPanel, BorderLayout.CENTER );
            userDefinitionPanel.setVictimCharacteristics(victimBandwidth, frequencyOffset, showACLR, interfererBandwidth);
            userDefinitionPanel.setFunctionable((EmissionMaskImpl) function, xUnit, yUnit);
            selectionPanel.setSelection(stringlist.getString("FUNCTION_USERDEFINED"));
        }
        updateEditPanel(selectionPanel.getSelection());
    }

	public boolean show(EmissionMask function, String windowtitle, double victimBandwidth, double frequencyOffset, boolean showACLR, double interfererBandwidth, String xUnit, String yUnit) {
		setFunction(function, victimBandwidth, frequencyOffset, showACLR, interfererBandwidth, xUnit, yUnit);
        setTitle(windowtitle);
        setAccept( false );
        setVisible(true);
        userDefinitionPanel.stopEditing();
		return isAccept();
	}

	public boolean show(EmissionMask function, String string, String xUnit, String yUnit) {
		return show(function, string, -1, -1, false, 0, xUnit, yUnit);
	}
}
