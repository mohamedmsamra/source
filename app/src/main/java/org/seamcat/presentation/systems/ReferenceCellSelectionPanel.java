package org.seamcat.presentation.systems;

import org.seamcat.presentation.Refreshable;

import javax.swing.*;
import java.awt.*;

public class ReferenceCellSelectionPanel extends JPanel implements Refreshable {

	private ReferenceCellSelector selector;
    private CellularPositionHolder model;

    public ReferenceCellSelectionPanel() {
		setLayout(new BorderLayout());
		selector = new ReferenceCellSelector();
		add(selector, BorderLayout.CENTER);
	}

    public void setModel(CellularPositionHolder model ) {
        this.model = model;
        selector.setModel(model);
	}

    public CellularPositionHolder getModel() {
        return model;
    }
	
	public void refreshFromModel() {
		CellularPositionHolder model = getModel();
		if ( model == null ) return;
        selector.generateCells();
        if ( model.getCellularPosition().tiers() != 2 ) {
			selector.setPlotWrapAround( false );
		} else {
			selector.setPlotWrapAround( model.getCellularPosition().generateWrapAround() );
		}
		
		selector.repaint();
	}
}
