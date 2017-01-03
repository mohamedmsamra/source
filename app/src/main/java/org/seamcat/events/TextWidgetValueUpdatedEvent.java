package org.seamcat.events;

public class TextWidgetValueUpdatedEvent extends ContextEvent {
	private String value;

	public TextWidgetValueUpdatedEvent( String value, Object context ) {
		super( context );
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
