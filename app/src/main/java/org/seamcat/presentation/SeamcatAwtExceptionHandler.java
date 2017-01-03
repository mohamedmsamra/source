package org.seamcat.presentation;

import org.apache.log4j.Logger;


public class SeamcatAwtExceptionHandler {
	private static final Logger logger = Logger.getLogger(SeamcatAwtExceptionHandler.class); 
	
	public void handle(Throwable e) {
		try {
			logger.error("Caught exception in AWT event handling", e);
			DialogHelper.generalSeamcatError(e);			
		}
		catch (Throwable t) {
			System.err.println("**** Exception while handling AWT exception ****");
		}
	}
}
