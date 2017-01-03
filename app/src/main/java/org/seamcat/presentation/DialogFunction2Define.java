package org.seamcat.presentation;

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

public class DialogFunction2Define extends EscapeDialog {

	private DiscreteFunction2Panel userDefinitionPanel = new DiscreteFunction2Panel();

	public DialogFunction2Define(Frame parent, boolean modal) {
		super(parent, modal);
		init();
		this.setLocationRelativeTo(parent);
	}

	public DialogFunction2Define(JDialog parent, boolean modal) {
		super(parent, modal);
		init();
		this.setLocationRelativeTo(parent);
	}

	public EmissionMask getFunction() {
		return this.userDefinitionPanel.getFunctionable();
	}

	private void init() {
		setSize( new Dimension(700, 450));
        getContentPane().add(userDefinitionPanel, BorderLayout.CENTER);
		getContentPane().add(new NavigateButtonPanel(this), BorderLayout.SOUTH);
		registerHelp();
	}

	private void registerHelp() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
         public void actionPerformed(ActionEvent e) {
                SeamcatHelpResolver.showHelp(DialogFunction2Define.this);
         }
			
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
   }

	public boolean show(EmissionMask function) {
		return show(function, "Function definition", "X", "Y");
	}

	public boolean show(EmissionMask function, String windowtitle, double victimBandwidth, double frequencyOffset, boolean showACLR, double interfererBandwidth, String xUnit, String yUnit) {
		userDefinitionPanel.setVictimCharacteristics(victimBandwidth, frequencyOffset, showACLR, interfererBandwidth);
		userDefinitionPanel.setFunctionable((EmissionMaskImpl) function, xUnit, yUnit);
		setTitle(windowtitle);
		setAccept( false );
		setVisible(true);
		userDefinitionPanel.stopEditing();
		return isAccept();
	}

	public boolean show(EmissionMask unwantedEmissionFloor, String string, String xUnit, String yUnit) {
		return show(unwantedEmissionFloor, string, -1, -1, false, 0, xUnit, yUnit);
	}
}
