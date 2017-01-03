package org.seamcat.presentation.library;

import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.types.Description;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.DiscreteFunction2Panel;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.item.TextItem;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.awt.*;

public class SpectrumEmissionMaskPanel extends JPanel {

	private DiscreteFunction2Panel panel;
	private GenericPanelEditor<Description> idPanel;

	public SpectrumEmissionMaskPanel( EmissionMaskImpl model, ChangeNotifier notifier ) {
		setLayout( new BorderLayout());
		panel = new DiscreteFunction2Panel();
        panel.setVictimCharacteristics(-1, -1, false, 0);
        panel.setFunctionable( model, null, null );

		idPanel = new GenericPanelEditor<Description>(MainWindow.getInstance(), Description.class, model.description());
        java.util.List<AbstractItem> allItems = idPanel.getAllItems();
        for (AbstractItem item : allItems) {
            if ( item instanceof TextItem && item.getLabel().equals("Name") ) {
                ((TextItem) item).addChangeNotifier( notifier );
            }
        }
		idPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		add(idPanel, BorderLayout.NORTH);
		add(panel, BorderLayout.CENTER);
	}

    public EmissionMaskImpl getModel() {
        EmissionMaskImpl model = panel.getFunctionable();
        model.setDescription( idPanel.getModel() );
        return model;
    }
}