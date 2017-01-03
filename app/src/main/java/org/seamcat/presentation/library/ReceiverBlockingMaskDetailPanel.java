package org.seamcat.presentation.library;

import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.types.Description;
import org.seamcat.presentation.components.DiscreteFunctionPanel;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.item.TextItem;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.awt.*;

public class ReceiverBlockingMaskDetailPanel extends JPanel {


	private DiscreteFunctionPanel panel;
	private GenericPanelEditor<Description> idPanel;

	public ReceiverBlockingMaskDetailPanel(JFrame owner, BlockingMaskImpl model, ChangeNotifier notifier ) {
		setLayout( new BorderLayout() );
		panel = new DiscreteFunctionPanel();
        panel.setModel( model );
		idPanel = new GenericPanelEditor<Description>(owner, Description.class, model.description());
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

    public BlockingMaskImpl getModel() {
        DiscreteFunction model = panel.getModel();
        BlockingMaskImpl mask;
        if ( model.isConstant() ) {
            mask = new BlockingMaskImpl(model.getConstant());
        } else {
            mask = new BlockingMaskImpl(model.points());
        }
        mask.setDescription( idPanel.getModel() );
        return mask;
    }
}