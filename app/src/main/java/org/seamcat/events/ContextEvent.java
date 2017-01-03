package org.seamcat.events;

public class ContextEvent {

	private final Object context;

    public ContextEvent( Object context ) {
		this.context = context;
	}


	public Object getContext() {
		return context;
	}
}
