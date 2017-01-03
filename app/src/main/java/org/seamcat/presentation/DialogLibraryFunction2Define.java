package org.seamcat.presentation;

import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.types.Description;
import org.seamcat.presentation.components.DiscreteFunction2Panel;
import org.seamcat.presentation.components.NavigateButtonPanel;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class DialogLibraryFunction2Define extends EscapeDialog {

	private JPanel idPanelContainer = new JPanel(new BorderLayout());
	private GenericPanelEditor<Description> idPanel;
	private DiscreteFunction2Panel userDefinitionPanel = new DiscreteFunction2Panel();
	private EmissionMaskImpl model;

	public DialogLibraryFunction2Define(JFrame parent, boolean modal) {
		super(parent, modal);
		init();
		this.setLocationRelativeTo(parent);
	}

	public EmissionMaskImpl getModel() {
		return model;
	}

	private void init() {
		setSize( new Dimension(700, 450));
		getContentPane().add( idPanelContainer, BorderLayout.NORTH );
		getContentPane().add(userDefinitionPanel, BorderLayout.CENTER);
		getContentPane().add(new NavigateButtonPanel(this), BorderLayout.SOUTH);
		registerHelp();
	}

	private void registerHelp() {
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
		getRootPane().registerKeyboardAction(new ActionListener() {

			@Override
         public void actionPerformed(ActionEvent e) {
				SeamcatHelpResolver.showHelp(DialogLibraryFunction2Define.this);
         }
			
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
   }

	public boolean show( EmissionMaskImpl model, String windowtitle, double victimBandwidth, double frequencyOffset, boolean showACLR, double interfererBandwidth, String xUnit, String yUnit) {
		this.model = model;
		idPanel = new GenericPanelEditor<>(MainWindow.getInstance(), Description.class, model.description() );
		idPanelContainer.add( idPanel, BorderLayout.CENTER );
        userDefinitionPanel.setVictimCharacteristics(victimBandwidth, frequencyOffset, showACLR, interfererBandwidth);
		userDefinitionPanel.setFunctionable(model, xUnit, yUnit);
		setTitle(windowtitle);
		setAccept( false );
		setVisible(true);
		userDefinitionPanel.stopEditing();
		return isAccept();
	}

	public boolean show(EmissionMaskImpl model, String string, String xUnit, String yUnit) {
		return show(model, string, -1, -1, false, 0, xUnit, yUnit);
	}

	public void updateModel() {
		model = userDefinitionPanel.getFunctionable();
        model.setDescription( idPanel.getModel());
	}
}
