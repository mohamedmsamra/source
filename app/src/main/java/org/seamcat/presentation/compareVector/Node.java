package org.seamcat.presentation.compareVector;

import javax.swing.tree.DefaultMutableTreeNode;

public class Node extends DefaultMutableTreeNode{
	private boolean enabled = true;

	public Node(Object object) {
		super(object);
	}

	public void setEnabled(boolean enabled) {
        this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
}
