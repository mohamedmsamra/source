package org.seamcat.model;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.seamcat.presentation.MainWindow;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlValidationHandler extends DefaultHandler {

	private static final Logger LOG = Logger.getLogger(XmlValidationHandler.class);

	public SAXParseException saxParseException = null;
	private boolean silentMode = true;

	public boolean validationError = false;

	public XmlValidationHandler(boolean silent) {
		silentMode = silent;
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		validationError = true;
		saxParseException = exception;
		LOG.error("Error parsing XML: " + exception.getMessage());
		if (!silentMode) {
			JOptionPane.showMessageDialog(MainWindow.getInstance(), exception
			      .getMessage(), "Error parsing XML", JOptionPane.WARNING_MESSAGE);
		}
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		validationError = true;
		saxParseException = exception;
//		if (!silentMode) {
			JOptionPane.showMessageDialog(MainWindow.getInstance(), exception
			      .getMessage(), "Fatal Error while parsing XML",
			      JOptionPane.ERROR_MESSAGE);
//		}
		LOG.error("Fatal Error while parsing XML", exception);
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		if (!silentMode) {
			JOptionPane.showMessageDialog(MainWindow.getInstance(), exception
			      .getMessage(), "XML Parsing Warning",
			      JOptionPane.WARNING_MESSAGE);
		}
	}

}
