package org.seamcat;

import org.apache.log4j.Logger;
import org.seamcat.batch.BatchJobList;
import org.seamcat.commands.DisplayWelcomeCommand;
import org.seamcat.commands.OpenWorkspaceFileCommand;
import org.seamcat.events.WorkspacesPaneEmptyEvent;
import org.seamcat.events.WorkspacesPaneNonEmptyEvent;
import org.seamcat.exception.SeamcatUncaughtExceptionHandler;
import org.seamcat.migration.BackwardMigrationNotSupportedException;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.factory.Model;
import org.seamcat.model.XmlValidationHandler;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.plugin.SandboxInitializer;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.SeamcatAwtExceptionHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;
import java.util.prefs.Preferences;
import java.net.*;

import static org.seamcat.eventbus.EventBusFactory.getEventBus;


/**
 * This is the class that contains the main method, which is the application starting point. <br>
 * The class can not be instantiated. The static main method is the method called by the java launching mechanism when <br>
 *     the application is started.
 * 
 */
public final class Seamcat {

	public final static String SEAMCAT_HOME = "SEAMCAT_HOME";
	public final static long ctm = System.currentTimeMillis();
	private static final Logger LOG = Logger.getRootLogger();
	public static final String LOOK_AND_FEEL_KEY = "SEAMCAT_LF";
	public static final String SHOW_WELCOME = "SEAMCAT_WELCOME";
	private static SplashScreen splash = new SplashScreen(new JFrame("Launching SEAMCAT"));
	public static double f, power_Value, y;
	 
	public static SplashScreen getSplashScreen() {
		return splash;
	}

	/**
	 * This is the method called initially when the application is started. First the appropriate "look and feel" for <br>
     * the platform is chosen, if possible, then an instance of the domain {@link Model Model} is created. <br>
     * After this the {@link org.seamcat.presentation.MainWindow MainWindow} is created and shown.
	 * 
	 * @param args
	 *           the command line arguments
	 */
	public static void main(final String[] args) {

	//	Seamcat sobj = new Seamcat();
    	int x=0;

    	try {
			        
             while (x<2) {
            	Random rnd = new Random();
        	   	y = rnd.nextInt(50);
        	   	System.out.println("before Window Initialization"+ y);
                initializeApplication(args);
                x++;
             }

		}
    	
		catch (BackwardMigrationNotSupportedException e) {
			LOG.warn("Backward migration attempted");
			DialogHelper.backwardMigrationNotSupported();
			System.exit(1);
		}
		catch (Exception e) {
			LOG.error("Exception during initialization", e);
			DialogHelper.generalSeamcatInitializationError(e);
			System.exit(1);
		}
		
	}

	private static void initializeApplication(final String[] args) {
        setupExceptionHandler();
		Logging.initialize();
		Logging.initializeFromHomeDir(Model.getSeamcatHomeDir());
		SandboxInitializer.initializeSandbox();

		String lf = "";
		Preferences pref = Preferences.userNodeForPackage(Seamcat.class);
		try {
			lf = pref.get(LOOK_AND_FEEL_KEY, "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			UIManager.setLookAndFeel(lf);
		} catch (Exception ex) {
			System.out.println("Unable to Load Look and Feel: " + lf);
			ex.printStackTrace();
		}
       
		Model.getInstance().setShowWelcome(Boolean.parseBoolean(pref.get(SHOW_WELCOME, "false")));
		

		MainWindow mainWindow = MainWindow.getInstance();
	
		getEventBus().subscribe( mainWindow );

		LOG.info("Starting SEAMCAT");
		LOG.debug("Initializing MainWindow");
		System.out.println("After Window Initialization " + y);
		
		JLabel label = new JLabel("Test");
		label.setSize(500, 500);
		label.setText(" "+ y);
		mainWindow.getContentPane().add(label);
		
		mainWindow.init();
		mainWindow.setSize(1100, 700);
		mainWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
		mainWindow.setVisible(true);
		mainWindow.getComponentAt(1, 2);
		
		
        try {
        	
			Thread.sleep(3000);
			//mainWindow.dispose();
			mainWindow.reset();
			
		//	mainWindow.revalidate();
			//mainWindow.ad
			//mainWindow.repaint();
		
			//mainWindow.repaint();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (String s : args) {
			try {
				File f = new File(s);
				if (f.exists()) {
					if (s.toLowerCase().endsWith(SeamcatConstants.WORKSPACE_FILEEXTENSION)) {
						LOG.info("Opening workspace from: " + s);
						getEventBus().publish( new OpenWorkspaceFileCommand( f ));
					} else if (s.toLowerCase().endsWith(SeamcatConstants.LIBRARY_FILEEXTENSION)) {
						LOG.info("Importing Library from: " + s);
                        //Model.getInstance().getLibrary().importLibrary( f );
					} else if (s.toLowerCase().endsWith(SeamcatConstants.BATCH_FILEEXTENSION)) {
						LOG.info("Importing Batch Job from: " + s);
						DocumentBuilder db = Model.getSeamcatDocumentBuilderFactory().newDocumentBuilder();
						db.setErrorHandler(new XmlValidationHandler(false));
						Document doc = db.parse(f);
						BatchJobList bj = new BatchJobList((Element) doc.getElementsByTagName("BatchJobList").item(0));
						// TODO open batchjob
						//Model.getInstance().getLibrary().getBatchjoblists().add(bj);
						JOptionPane.showMessageDialog(mainWindow, "Succesfully imported Batch Job");
					}
					getEventBus().publish(new WorkspacesPaneNonEmptyEvent());
				} else {
					getEventBus().publish(new WorkspacesPaneEmptyEvent());
				}
			} catch (Exception e) {
				LOG.warn("Unable to open file: " + s, e);
			}
		}

		if ( args.length == 0 ) {
			getEventBus().publish(new WorkspacesPaneEmptyEvent());
		}

		LOG.addAppender(Model.getInstance().getLogFileAppender());

		splash.setVisible(false);
		splash.dispose();

		if ( Model.getInstance().showWelcomeScreen() ) {
			getEventBus().publish(new DisplayWelcomeCommand());
		}		
	}

	private static void setupExceptionHandler() {
	    // This way of handling AWT exceptions does not work for JDK7
		System.setProperty("sun.awt.exception.handler", SeamcatAwtExceptionHandler.class.getName());
		
		// If the above does not work, this should:
		Thread.setDefaultUncaughtExceptionHandler(new SeamcatUncaughtExceptionHandler());
	}
	
	
	  public static int OMNET_GetFrequency(){
          
          
         // frequency_Value= basic_packet.substring(index_f + 19, index_f + 28);
         // int frequency_Value = 50000;
          //return frequency_Value;
		  Random rnd = new Random();
		  int n = rnd.nextInt(50);
		  return n;
         }
}
