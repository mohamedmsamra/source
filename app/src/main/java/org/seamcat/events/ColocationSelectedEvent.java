package org.seamcat.events;

public class ColocationSelectedEvent extends ContextEvent{

	private Boolean selected;

	public ColocationSelectedEvent(Object context, Boolean selected ) {
		super(context);
		this.selected = selected;
	}

	public Boolean getSelected() {
		return selected;
	}
}
