package org.seamcat.presentation.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/** Layout manager for laying out sub panels horizontally, such that the panels 
 * share the width of the container and all panels gets the height of the container.
 */
public class HorizontalSubPanelLayoutManager implements LayoutManager {
	@Override
   public void addLayoutComponent(String name, Component comp) {
   }

	@Override
   public void removeLayoutComponent(Component comp) {
   }

	@Override
   public Dimension preferredLayoutSize(Container parent) {
		int largestComponentPreferredHeight = 0;
		int accumulatedComponentPreferredWidth = 0;

		for (Component c: parent.getComponents()) {
			Dimension ps = c.getPreferredSize();
			accumulatedComponentPreferredWidth += ps.width;
			if (ps.height> largestComponentPreferredHeight) {
				largestComponentPreferredHeight = ps.height;
			}
		}

		Insets insets = parent.getInsets();
      return new Dimension(accumulatedComponentPreferredWidth + insets.left + insets.right,
      							largestComponentPreferredHeight + insets.top + insets.bottom);
   }

	@Override
   public Dimension minimumLayoutSize(Container parent) {
		int largestComponentMinimumHeight = 0;
		int accumulatedComponentMinimumWidth = 0;

		for (Component c: parent.getComponents()) {
			Dimension ms = c.getMinimumSize();
			accumulatedComponentMinimumWidth += ms.width;
			if (ms.height > largestComponentMinimumHeight) {
				largestComponentMinimumHeight = ms.height;
			}
		}

		Insets insets = parent.getInsets();
      return new Dimension(accumulatedComponentMinimumWidth + insets.left + insets.right,
      							largestComponentMinimumHeight + insets.top + insets.bottom);
   }

	@Override
   public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int additionalHorizontalSpace = parent.getWidth() - preferredLayoutSize(parent).width - insets.left - insets.right;
		
		int left = 0;
		for (int componentIndex = 0; componentIndex < parent.getComponents().length; componentIndex++) {
			Component currentComponent = parent.getComponents()[componentIndex];
			int currentComponentWidth = currentComponent.getPreferredSize().width + additionalHorizontalSpace / parent.getComponents().length;
			currentComponent.setBounds(insets.left + left, insets.top, currentComponentWidth, parent.getHeight() - insets.top - insets.bottom);
			left += currentComponentWidth;
		}
   }		
}
