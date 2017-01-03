package org.seamcat.events;


public class ProgressEvent {
	
	private String message; 

	public ProgressEvent(String message) {
		this.message = message;
   }
	
	public String getMessage() {
		return message;
	}
}
