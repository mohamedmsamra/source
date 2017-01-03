package org.seamcat.presentation;

import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.types.Description;
import org.seamcat.presentation.components.DiscreteFunctionPanel;
import org.seamcat.presentation.components.NavigateButtonPanel;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

@SuppressWarnings("serial")
public class DialogLibraryFunctionDefine extends EscapeDialog {

	private JPanel idPanelContainer = new JPanel(new BorderLayout());
    private GenericPanelEditor<Description> idPanel;
	private DiscreteFunctionPanel userDefinitionPanel = new DiscreteFunctionPanel();

	public DialogLibraryFunctionDefine(JFrame parent, boolean modal) {
		super(parent, modal);
		init();
		this.setLocationRelativeTo(parent);
	}

	private BlockingMaskImpl model;

	public boolean show( BlockingMaskImpl model, String windowtitle, String xUnit, String yUnit ) {
		this.model = model;
        idPanel = new GenericPanelEditor<Description>(MainWindow.getInstance(), Description.class, model.description());
		idPanelContainer.add( idPanel, BorderLayout.CENTER );
		userDefinitionPanel.setModel( model );
		userDefinitionPanel.setAxisNames(xUnit, yUnit);
		setTitle(windowtitle);
		setAccept( false );
		setVisible(true);
		return isAccept();
	}

    public BlockingMaskImpl getModel() {
        return model;
    }

	public void updateModel() {
        DiscreteFunction func = userDefinitionPanel.getModel();
        if ( func.isConstant() ) {
            model = new BlockingMaskImpl(func.getConstant());
        } else {
            model = new BlockingMaskImpl(func.points());
        }
        model.setDescription( idPanel.getModel());
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
				SeamcatHelpResolver.showHelp(DialogLibraryFunctionDefine.this);
			}

		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}
}