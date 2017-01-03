package org.seamcat.presentation.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/** Layout manager for laying out sub panels vertically, such that the panels 
 * get their preferred heightand all panels gets the width of the container, 
 * and all panels are flushed towards the top.
 */

public class VerticalSubPanelLayoutManager implements LayoutManager {
	private int componentSpacing = 0;
	
	
   public VerticalSubPanelLayoutManager() {
   }

   public VerticalSubPanelLayoutManager(int componentSpacing) {
   	this.componentSpacing  = componentSpacing; 
   }

	@Override
   public void addLayoutComponent(String name, Component comp) {
   }

	@Override
   public void removeLayoutComponent(Component comp) {
   }

	@Override
   public Dimension preferredLayoutSize(Container parent) {
		int largestComponentPreferredWidth = 0;
		int accumulatedComponentPreferredHeight = 0;

		for (Component c: parent.getComponents()) {
			Dimension ps = c.getPreferredSize();
			accumulatedComponentPreferredHeight += ps.height;
			if (ps.width > largestComponentPreferredWidth) {
				largestComponentPreferredWidth = ps.width;
			}
		}
		
		Insets insets = parent.getInsets();
      return new Dimension(largestComponentPreferredWidth + insets.left + insets.right,
      							accumulatedComponentPreferredHeight + insets.top + insets.bottom + totalComponentSpacing(parent.getComponents()));
   }

	private int totalComponentSpacing(Component[] components) {
		if (components.length > 0) {
			return (components.length - 1)*componentSpacing;
		}
		else {
			return 0;
		}
   }

	@Override
   public Dimension minimumLayoutSize(Container parent) {
		int largestComponentMinimumWidth = 0;
		int accumulatedComponentMinimumHeight = 0;

		for (Component c: parent.getComponents()) {
			Dimension ms = c.getMinimumSize();
			accumulatedComponentMinimumHeight += ms.height;
			if (ms.width > largestComponentMinimumWidth) {
				largestComponentMinimumWidth = ms.width;
			}
		}

		Insets insets = parent.getInsets();
      return new Dimension(largestComponentMinimumWidth + insets.left + insets.right,
      							accumulatedComponentMinimumHeight + insets.top + insets.bottom + totalComponentSpacing(parent.getComponents()));
   }

	@Override
   public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int top = 0;
		for (Component c: parent.getComponents()) {
			Dimension ps = c.getPreferredSize();
			c.setBounds(insets.left, top+insets.top, parent.getWidth()-insets.left-insets.right, (int) ps.getHeight());
			top +=  ps.getHeight() + componentSpacing;
		}
   }		
}