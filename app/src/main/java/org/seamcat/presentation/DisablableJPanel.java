package org.seamcat.presentation;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class DisablableJPanel extends JPanel{

	
	
	public DisablableJPanel(LayoutManager layoutManager) {
		super(layoutManager);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setComponentState(this, enabled);
	}
	
	// Recursively Enable/Disable controls
	private void setComponentState(Container container, boolean state) {
		Component[] components = container.getComponents();
		if (components.length > 0) {

			for (Component mComponent : components) {
				mComponent.setEnabled(state);
				if (mComponent instanceof Container) {
					setComponentState((Container) mComponent, state);
				}
			}
		}
	}
}
