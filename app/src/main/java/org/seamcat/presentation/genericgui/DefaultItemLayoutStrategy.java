package org.seamcat.presentation.genericgui;

import org.seamcat.presentation.genericgui.item.BooleanItem;
import org.seamcat.presentation.genericgui.item.Item;
import org.seamcat.util.Assert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class DefaultItemLayoutStrategy implements ItemLayoutStrategy {
	
	private static final int WIDGET_HORIZONTAL_SPACING = 3;

	private enum LayoutColumn {
		LABEL, VALUE_PREVIEW, VALUE, UNIT;
		
		static LayoutColumn LAST_COLUMN = LayoutColumn.values()[LayoutColumn.values().length-1];
	}	

	@Override
	public void layoutItemsInContainer(Container container, List<Item> items) {
		int row = 0;
		for (Item item: items) {
			layoutItem(container, item, row);
			row++;
		}	
		addBottomSpacer(container, row);
	}

	@Override
	public LayoutManager getLayoutManager() {
		return new GridBagLayout();
	}
	
	private void layoutItem(Container container, Item item, int row) {
		if ( item instanceof BooleanItem && item.getWidgets().size() > 1 ) {
            layoutItemWithDefaultLayout(container, item, row);
        } else if (item instanceof BooleanItem || countWidgetsOfKinds(item,  WidgetKind.NONE) > 0) {
            layoutItemWithFullWidthLayout(container, item, row);
        }
		else if (countWidgetsOfKinds(item, WidgetKind.VALUE_PREVIEW) == 0) {
            layoutItemWithWideLabelLayout(container, item, row);
        }
		else {
			layoutItemWithDefaultLayout(container, item, row);
		}
	}	

	private void layoutItemWithFullWidthLayout(Container container, Item<?> item, int row) {
		Assert.isTrue("Full width layout only supported for items with exactly one widget", item.getWidgets().size() == 1);
		Component valueWidget = item.getWidgets().get(0).getWidget();
		Object constraints = makeConstraintsForFullWidthLayout(row);
	   container.add(valueWidget, constraints);
   }

	private Object makeConstraintsForFullWidthLayout(int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = row;
		constraints.gridwidth = LayoutColumn.values().length;
		constraints.anchor = GridBagConstraints.BASELINE_LEADING;
		return constraints;
   }


	private void layoutItemWithDefaultLayout(Container container, Item<?> item, int row) {
	   initializeWidgetsInColumnRange(container, item, row, LayoutColumn.LABEL, LayoutColumn.LABEL, WidgetKind.LABEL);
	   initializeWidgetsInColumnRange(container, item, row, LayoutColumn.VALUE_PREVIEW, LayoutColumn.VALUE_PREVIEW, WidgetKind.VALUE_PREVIEW);
	   initializeWidgetsInColumnRange(container, item, row, LayoutColumn.VALUE, LayoutColumn.VALUE, WidgetKind.VALUE);
	   initializeWidgetsInColumnRange(container, item, row, LayoutColumn.UNIT, LayoutColumn.UNIT, WidgetKind.UNIT);
   }

	private void layoutItemWithWideLabelLayout(Container container, Item item, int row) {
	   initializeWidgetsInColumnRange(container, item, row, LayoutColumn.LABEL, LayoutColumn.VALUE_PREVIEW, WidgetKind.LABEL);
	   initializeWidgetsInColumnRange(container, item, row, LayoutColumn.VALUE, LayoutColumn.VALUE, WidgetKind.VALUE);
	   initializeWidgetsInColumnRange(container, item, row, LayoutColumn.UNIT, LayoutColumn.UNIT, WidgetKind.UNIT);
   }


	/** Initialize widgets of certain kinds in a range of layout column.
	 */
	private void initializeWidgetsInColumnRange(Container container, Item<?> item, int row, LayoutColumn layoutColumnStart, LayoutColumn layoutColumnEnd, WidgetKind... kinds) {
	   List<WidgetAndKind> widgetsAndKindsForColumn = findWidgetsOfKinds(item, kinds);
	   if (widgetsAndKindsForColumn.size() == 1) {
	   	container.add(widgetsAndKindsForColumn.get(0).getWidget(), makeColumnRangeConstraints(layoutColumnStart, layoutColumnEnd, row));
	   }
	   else if (widgetsAndKindsForColumn.size() > 1) {
	   	Box box = Box.createHorizontalBox();
	   	for (WidgetAndKind wak: widgetsAndKindsForColumn) {
	   		if (box.getComponents().length > 0) {
	   			box.add(Box.createHorizontalStrut(5));
	   		}
	   		box.add(wak.getWidget());
	   	}
	   	container.add(box, makeColumnRangeConstraints(layoutColumnStart, layoutColumnEnd, row));
	   }
   }

	private Object makeColumnRangeConstraints(LayoutColumn layoutColumnStart, LayoutColumn layoutColumnEnd, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = layoutColumnStart.ordinal();
		constraints.gridwidth = layoutColumnEnd.ordinal() - layoutColumnStart.ordinal() + 1;
		constraints.gridy = row;
		constraints.anchor = GridBagConstraints.BASELINE_LEADING;
		if (layoutColumnEnd != LayoutColumn.LAST_COLUMN) {
			constraints.insets = new Insets(0, 0, 0, WIDGET_HORIZONTAL_SPACING);
		}
		if (layoutColumnStart == LayoutColumn.VALUE) {
			constraints.weightx = 1.0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
		}
		return constraints;
   }

	// HACK: Add a row to the grid layout to force rows to be flushed 
   // against the top of the panel.
   // TODO: Find a better way of doing this
   private void addBottomSpacer(Container container, int row) {
   	GridBagConstraints constraints = new GridBagConstraints();
   	constraints.gridx = 0;
   	constraints.gridy = row;
   	constraints.weighty=1.0;
   	container.add(Box.createVerticalGlue(), constraints);
   }

	private List<WidgetAndKind> findWidgetsOfKinds(Item<?> item, WidgetKind... kinds) {
   	List<WidgetAndKind> result = new ArrayList<WidgetAndKind>();
   	for (WidgetAndKind wak: item.getWidgets()) {   		
   		if (elementInArray(wak.getKind(), kinds)) {
   			result.add(wak);
   		}
   	}
      return result;
   }

	private static <T> boolean elementInArray(T element, T[] array) {
		for (T t: array) {
			if (t.equals(element)) {
				return true;
			}
		}
	   return false;
   }	
	
	private int countWidgetsOfKinds(Item item, WidgetKind... kinds) {
	   return findWidgetsOfKinds(item, kinds).size();
   }
}
