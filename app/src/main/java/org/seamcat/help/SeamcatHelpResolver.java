package org.seamcat.help;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.seamcat.presentation.MainWindow;

public class SeamcatHelpResolver {

	private static final Logger LOG = Logger.getLogger(SeamcatHelpResolver.class);

	private static Properties helpUriMapping;

	public static URI resolveHelpURI(Object o, String context) throws Exception {
		if (helpUriMapping == null) {
			reloadHelpUriMappings();
		}
		String key = o.getClass().getName();
		return getUri(context, key);
	}

	private static URI getUri(String context, String key) throws URISyntaxException {
		URI uri;
		if (helpUriMapping.containsKey(key)) {
			String url = helpUriMapping.getProperty(key);
			if (context != null) {
				url += "#" + context;
			}

			uri = new URI(url);
			return uri;
		} else {
			LOG.warn("No Help URI found for: " + key);
			// Add to properties to only get error once
			//helpUriMapping.put(key, "http://seamcat.iprojects.dk/wiki/Manual");
			//return resolveHelpURI(o, context);
			uri = new URI("http://tractool.seamcat.org/wiki/Manual");
			return uri;
		}
	}

	public static void reloadHelpUriMappings() throws IOException {
		helpUriMapping = new Properties();
		helpUriMapping.load(SeamcatHelpResolver.class.getResourceAsStream("/help_urls.properties"));
	}

	public static void showHelp(Object o, String context) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			URI uri = null;
			try {
				uri = SeamcatHelpResolver.resolveHelpURI(o, context);
				desktop.browse(uri);
			} catch (Exception use) {
				LOG.error("Unable to show help page in browser", use);

				JOptionPane.showMessageDialog(MainWindow.getInstance(),
				      "<html>SEAMCAT was unable to launch a browser and direct you to the help page:<br />"
				            + (uri != null ? uri.toString() : "No URL was found"), "Unable to launch browser",
				      JOptionPane.WARNING_MESSAGE);

			}
		}
	}
	
	public static void showHelp( String name ) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			URI uri = null;
			try {
				if (helpUriMapping == null) {
					reloadHelpUriMappings();
				}
				uri = getUri( null, name);
				desktop.browse(uri);
			} catch (Exception use) {
				LOG.error("Unable to show help page in browser", use);

				JOptionPane.showMessageDialog(MainWindow.getInstance(),
						  "<html>SEAMCAT was unable to launch a browser and direct you to the help page:<br />"
									 + (uri != null ? uri.toString() : "No URL was found"), "Unable to launch browser",
						  JOptionPane.WARNING_MESSAGE);

			}
		}
	}

	public static void showHelp(Object o) {
		showHelp(o, null);
	}

}
