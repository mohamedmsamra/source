package org.seamcat.presentation.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.List;


/** LayoutManager which knows how to lay out components in a border panel, ie. 
 * a panel with one central component, and a number of widgets placed horizontally
 * in the title position above the main component.
 */
public class BorderPanelLayout implements LayoutManager2 {
	
	public final static String MAIN = "Main";
	public final static String TITLE_WIDGET = "TitleWidget";

	/** Vertical space between main and title widgets */
	private final static int TITLE_MAIN_VERTICAL_SPACE = 1;
	/** Horizontal space before first title widget */
	private final static int TITLE_INDENT_SPACE = 14;
	/** Horizontal space between title widgets */
	private final static int TITLE_WIDGETS_SPACE = 4;
	/** Vertical space above title widgets */
	private final static int TITLE_TOP_SPACE = 2;
	/** Padding along edges of main component */
	private final static int MAIN_PADDING = 2;
	
	private Component mainComponent;
	private List<Component> titleWidgets = new ArrayList<Component>();
	
	public BorderPanelLayout() {
   }

	@Override
   public void addLayoutComponent(String name, Component comp) {
		if (MAIN.equals(name)) {
			mainComponent = comp;
		} 
		else if (TITLE_WIDGET.equals(name)) {
			titleWidgets.add(comp);
		}
		else {
			throw new IllegalArgumentException("Cannot add to layout, unknown constraint: "+name);
		}
   }

	@Override
   public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints instanceof String) {
			addLayoutComponent((String) constraints, comp);
		}
		else {
			throw new IllegalArgumentException("Cannot add to layout, constraint must be a string");			
		}
   }

	@Override
   public void removeLayoutComponent(Component comp) {
		if (comp == mainComponent) {
			mainComponent = null;
		}
		else {
			titleWidgets.remove(comp);
		}
   }

	@Override
   public Dimension preferredLayoutSize(Container target) {		
		Dimension mainSize = mainComponent.getPreferredSize();
		int titleWidgetsHeight = getTitleWidgetsMaxHeight();
		int titleWidgetsWidth = getTitleWidgetsTotalWidth();

		Dimension result = new Dimension(
				Math.max(titleWidgetsWidth, mainSize.width + 2*MAIN_PADDING), 
				titleWidgetsHeight + mainSize.height + TITLE_MAIN_VERTICAL_SPACE + TITLE_TOP_SPACE + MAIN_PADDING
				);

		addInsetsToSize(target, result);
		return result;
   }

	private int getTitleWidgetsMaxHeight() {
		int result = 0;
		for (Component c: titleWidgets) {
			if (c.getPreferredSize().height > result) {
				result = c.getPreferredSize().height;
			}
		}
		return result;
   }

	private int getTitleWidgetsTotalWidth() {
		int result = TITLE_INDENT_SPACE;
		for (Component c: titleWidgets) {
			result += c.getPreferredSize().width;
		}
		result += TITLE_WIDGETS_SPACE*(titleWidgets.size()-1);
		return result;
   }

	private void addInsetsToSize(Container target, Dimension size) {
	   Insets insets = target.getInsets();
	   size.width += insets.left + insets.right;
	   size.height += insets.top + insets.bottom;
   }

	@Override
   public Dimension minimumLayoutSize(Container target) {
		Dimension mainSize = mainComponent.getMinimumSize();
		int titleWidgetsHeight = getTitleWidgetsMaxHeight();
		int titleWidgetsWidth = getTitleWidgetsTotalWidth();

		Dimension result = new Dimension(
				Math.max(titleWidgetsWidth, mainSize.width + 2*MAIN_PADDING), 
				titleWidgetsHeight + mainSize.height + TITLE_MAIN_VERTICAL_SPACE + TITLE_TOP_SPACE + MAIN_PADDING
				);

		addInsetsToSize(target, result);
		return result;
   }

	@Override
   public Dimension maximumLayoutSize(Container target) {
      return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
   }

	@Override
   public void layoutContainer(Container target) {
		int titleWidgetsHeight = getTitleWidgetsMaxHeight();
		Insets insets = target.getInsets();
		mainComponent.setBounds(
				insets.left + MAIN_PADDING, 
				insets.top + titleWidgetsHeight + TITLE_MAIN_VERTICAL_SPACE + TITLE_TOP_SPACE, 
				target.getWidth() - insets.right - insets.left - 2*MAIN_PADDING, 
				target.getHeight() - insets.bottom - insets.top - titleWidgetsHeight - TITLE_MAIN_VERTICAL_SPACE - TITLE_TOP_SPACE - MAIN_PADDING);
		
		int titleWidgetLeft = insets.left + TITLE_INDENT_SPACE; 
		for (Component titleWidget: titleWidgets) {
			Dimension widgetSize = titleWidget.getPreferredSize();
			int widgetCenterAlignAdjustment = (titleWidgetsHeight - widgetSize.height)/2; 
			titleWidget.setBounds(
					titleWidgetLeft,
					insets.top + widgetCenterAlignAdjustment + TITLE_TOP_SPACE,
					widgetSize.width,
					widgetSize.height
					);
			titleWidgetLeft += widgetSize.width + TITLE_WIDGETS_SPACE; 
		}		
   }

	@Override
   public float getLayoutAlignmentX(Container target) {
	   return 0;
   }

	@Override
   public float getLayoutAlignmentY(Container target) {
	   return 0;
   }

	@Override
   public void invalidateLayout(Container target) {
   }
}
