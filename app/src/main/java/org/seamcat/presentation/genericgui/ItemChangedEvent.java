package org.seamcat.presentation.genericgui;

import org.seamcat.presentation.genericgui.item.Item;


public class ItemChangedEvent {
	private Item item;

	public ItemChangedEvent( Item item ) {
		this.item = item;
	}

	public Item getItem() {
   	return item;
   }

   public void setItem(Item item) {
   	this.item = item;
   }
}
