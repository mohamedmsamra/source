package org.seamcat.presentation.genericgui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.seamcat.presentation.genericgui.item.Item;


public class GenericPanel extends JPanel {
	
	ItemLayoutStrategy layoutStrategy = new DefaultItemLayoutStrategy();
	private List<Item> items = new ArrayList<Item>();
	
	public void addItem(Item<?> item) {
		items.add(item);
	}
	
	public List<Item> getItems() {
		return items;
	}

	public void setGlobalRelevance( boolean relevance ) {
		for (Item item : items) {
			item.setRelevant( relevance );
		}
	}

	public void initializeWidgets() {
		setLayout(layoutStrategy.getLayoutManager());
		layoutStrategy.layoutItemsInContainer(this, items);
	}
}
