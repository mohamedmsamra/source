package org.seamcat.loadsave;

public class LoadException extends RuntimeException {

	public LoadException() {
		super();
	}

	public LoadException(String message, Throwable cause) {
		super(message, cause);
	}

	public LoadException(String message) {
		super(message);
	}

	public LoadException(Throwable cause) {
		super(cause);
	}

}
