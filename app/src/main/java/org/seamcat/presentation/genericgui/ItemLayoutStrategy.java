package org.seamcat.presentation.genericgui;

import java.awt.Container;
import java.awt.LayoutManager;
import java.util.List;

import org.seamcat.presentation.genericgui.item.Item;


public interface ItemLayoutStrategy {
	public void layoutItemsInContainer(Container container, List<Item> items);
	public LayoutManager getLayoutManager();
}
