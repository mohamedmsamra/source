package org.seamcat;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.seamcat.model.factory.Model;


public class Logging {
	
	private static final String LOG4J_FILE_NAME = "log4j.properties";
	
	public static void initialize() {
		showMessage("Initializing log4j with basic configuration");
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);					
	}
	
	public static void initializeFromHomeDir(String seamcatHomeDir) {
		File log4jConfigurationFile = getLog4jConfigurationFileFromHomeDir(seamcatHomeDir);
		if (log4jConfigurationFile != null) {
			showMessage("Initializing log4j from file: "+log4jConfigurationFile);
			PropertyConfigurator.configureAndWatch(
					new File(Model.getSeamcatHomeDir()).getAbsolutePath() + File.separator + "log4j.properties",
					2000);			
		}
		else {
			//showMessage("Log4j configuration file not found. Setting log level back to INFO");
			Logger.getRootLogger().setLevel(Level.INFO);								
		}
	}

	private static File getLog4jConfigurationFileFromHomeDir(String seamcatHomeDirString) {
		File file = new File(seamcatHomeDirString, LOG4J_FILE_NAME);
		if (file.exists()) {
			return file;
		}
		else {
			return null;			
		}		
   }

	private static void showMessage(String message) {
		System.err.println(message);
   }
}
