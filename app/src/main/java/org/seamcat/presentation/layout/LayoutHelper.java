package org.seamcat.presentation.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;


public class LayoutHelper {

	public static int sumPreferredWidths(Component[] components) {
		int result = 0;
		for (Component c: components) {
			result += c.getPreferredSize().width;
		}
	   return result;
   }

	public static int maxPreferredHeight(Component[] components) {
		int result = 0;
		for (Component c: components) {
			if (c.getPreferredSize().height > result) {
				result = c.getPreferredSize().height;				
			}
		}
	   return result;
   }

	public static int sumMinimumWidths(Component[] components) {
		int result = 0;
		for (Component c: components) {
			result += c.getMinimumSize().width;
		}
	   return result;
   }

	public static int maxMinimumHeight(Component[] components) {
		int result = 0;
		for (Component c: components) {
			if (c.getMinimumSize().height > result) {
				result = c.getMinimumSize().height;				
			}
		}
	   return result;
   }

	public static int availableWidthWithin(Container parent) {
	   return parent.getWidth() -  parent.getInsets().left - parent.getInsets().right;
   }

	public static int availableHeightWithin(Container parent) {
	   return parent.getHeight() -  parent.getInsets().top - parent.getInsets().bottom;
   }

	public static Dimension addInsets(Dimension dimension, Insets insets) {
	   return new Dimension(dimension.width + insets.left + insets.right,
	   							dimension.height + insets.top + insets.bottom);
   }
}
