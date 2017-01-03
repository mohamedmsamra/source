package org.seamcat.presentation.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import org.apache.log4j.Logger;

public class LinkConfigurationTopPanelLayout implements LayoutManager {
	
	private static final Logger logger = Logger.getLogger(LinkConfigurationTopPanelLayout.class);

	@Override
   public void addLayoutComponent(String name, Component comp) {
   }

	@Override
   public void removeLayoutComponent(Component comp) {
   }

	@Override
   public Dimension preferredLayoutSize(Container parent) {
      Dimension size = LayoutHelper.addInsets( 
      		new Dimension(LayoutHelper.sumPreferredWidths(parent.getComponents()),
      						  LayoutHelper.maxPreferredHeight(parent.getComponents())
      						  ),
      		parent.getInsets()
      		);
        if (logger.isDebugEnabled()) {
            logger.debug("preferred size: " + size);
        }
		return size;
   }

	@Override
   public Dimension minimumLayoutSize(Container parent) {
		Dimension size = LayoutHelper.addInsets(
				new Dimension(LayoutHelper.sumMinimumWidths(parent.getComponents()),
					   		  LayoutHelper.maxMinimumHeight(parent.getComponents())
					   		  ),
			   parent.getInsets()
				);
        if (logger.isDebugEnabled()) {
            logger.debug("minimum size: " + size);
        }
		return size;
   }

	@Override
   public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
      int additionalSpace = LayoutHelper.availableWidthWithin(parent) - LayoutHelper.sumPreferredWidths(parent.getComponents()); 
      int left = 0;
      for (int i = 0; i<parent.getComponents().length; i++) {
      	Component c = parent.getComponents()[i];
      	int currentComponentWidth = c.getPreferredSize().width;
      	if (additionalSpace > 0) {
	      	if (i == 0) {
	      		currentComponentWidth += additionalSpace;	      		
	      	}	      		
      	}
      	else {
      		currentComponentWidth += additionalSpace / parent.getComponents().length;
      	}
			c.setBounds(insets.left + left, insets.top, currentComponentWidth, LayoutHelper.availableHeightWithin(parent));
			left += currentComponentWidth;
      }
   }	
}

