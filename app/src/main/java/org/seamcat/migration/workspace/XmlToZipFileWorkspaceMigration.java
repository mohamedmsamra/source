package org.seamcat.migration.workspace;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.FileUtils;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.migration.MigrationException;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlEventFactory;


public class XmlToZipFileWorkspaceMigration implements FileMigration {

	@Override
   public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
		try {
			tryMigrate(originalFile, migratedFile);
		}
		catch (Exception e) {
			throw new MigrationException("Failed to migrate XML workspac e to ZIP workspace", e);
		}
	}

	public void tryMigrate(File originalFile, File migratedFile) throws Exception {
		File scenarioFile = IOUtils.createTempFile();
		File resultsFile = IOUtils.createTempFile();
		
		boolean hasResults = splitOriginalIntoScenarioAndResults(originalFile, scenarioFile, resultsFile);		
		createZipFileFromScenarioAndResultsFiles(migratedFile, scenarioFile, resultsFile, hasResults);		
		
		scenarioFile.delete();
		resultsFile.delete();
   }

	private boolean splitOriginalIntoScenarioAndResults(File originalFile, File scenarioFile, File resultsFile) throws Exception {
		XMLEventWriter scenarioEventWriter = null;
		XMLEventWriter resultsEventWriter = null;		
		XMLEventReader originalEventReader = null;

		try {
			scenarioEventWriter = XMLOutputFactory.newFactory().createXMLEventWriter(new BufferedOutputStream(new FileOutputStream(scenarioFile)));
			resultsEventWriter = XMLOutputFactory.newFactory().createXMLEventWriter(new BufferedOutputStream(new FileOutputStream(resultsFile)));		
			originalEventReader = XMLInputFactory.newFactory().createXMLEventReader(new BufferedInputStream(new FileInputStream(originalFile)));			
			
			scenarioEventWriter.add(XmlEventFactory.startDocument());
			resultsEventWriter.add(XmlEventFactory.startDocument());
			resultsEventWriter.add(XmlEventFactory.startElement("workspaceResults"));
			
			boolean hasResults = splitOriginalIntoScenarioAndResults(originalEventReader, scenarioEventWriter, resultsEventWriter);			

			resultsEventWriter.add(XmlEventFactory.endElement("workspaceResults"));
			scenarioEventWriter.add(XmlEventFactory.endDocument());
			resultsEventWriter.add(XmlEventFactory.endDocument());
			
			return hasResults;
		}
		finally {
			IOUtils.closeQuietly(scenarioEventWriter);
			IOUtils.closeQuietly(resultsEventWriter);
			IOUtils.closeQuietly(originalEventReader);
		}
	}

	private boolean splitOriginalIntoScenarioAndResults(XMLEventReader eventReader, XMLEventWriter scenarioEventWriter, XMLEventWriter resultsEventWriter) throws XMLStreamException {
		boolean resultsElementFound = false;
		skipNonElementEvents(eventReader);
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.peek();
			if (isWorkspaceElementStartEvent(event)) {
				scenarioEventWriter.add(makeWorkspaceStartElementWithBumpedVersion(event.asStartElement()));
				eventReader.nextEvent();
			}
			else if (isWorkspaceElementEndEvent(event)) {
				moveEvent(eventReader, scenarioEventWriter);
			}
			else if (isScenarioTopLevelElement(event)) {
				moveSubTree(eventReader, scenarioEventWriter);
			}
			else if (isResultsTopLevelElement(event)) {
				resultsElementFound = true;
				moveSubTree(eventReader, resultsEventWriter);
			}
			else {
				 throw new MigrationException("Unexpected element event: "+event);
			}
			skipNonElementEvents(eventReader);
		}
		return resultsElementFound; 
	}

	private StartElement makeWorkspaceStartElementWithBumpedVersion(final StartElement originalWorkspaceElement) {
		final XMLEventFactory eventFactory = XMLEventFactory.newFactory();
		
		Iterator replacingAttributeIterator = new Iterator() {
			Iterator originalAttributeIterator = originalWorkspaceElement.getAttributes();

			@Override
			public boolean hasNext() {
				return originalAttributeIterator.hasNext();
			}

			@Override
			public Object next() {
				Attribute attribute = (Attribute) originalAttributeIterator.next();
				if (attribute.getName().getLocalPart().equals("workspace_format_version")) {
					return eventFactory.createAttribute(new QName("workspace_format_version"), Integer.toString(getOriginalVersion().nextVersion().getNumber()));
				}
				else {
					return attribute;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}			
		};
		
		return eventFactory.createStartElement(originalWorkspaceElement.getName(), replacingAttributeIterator, originalWorkspaceElement.getNamespaces());
	}

	private void skipNonElementEvents(XMLEventReader eventReader) throws XMLStreamException {
		XMLEvent event = eventReader.peek();
		while (event != null  && !event.isStartElement() && !event.isEndElement()) {
			eventReader.nextEvent();
			event = eventReader.peek();
		}
	}

	private boolean isWorkspaceElementStartEvent(XMLEvent event) {
		return event.isStartElement() && event.asStartElement().getName().getLocalPart().equals("Workspace");
	}

	private boolean isWorkspaceElementEndEvent(XMLEvent event) {
		return event.isEndElement() && event.asEndElement().getName().getLocalPart().equals("Workspace");
	}

	private  final static Set<String> SCENARIO_TOP_LEVEL_ELEMENT_NAMES = new HashSet<String>(Arrays.asList("InterferenceLink", "VictimSystemLink", "Control", "postprocessing-plugins"));
	private boolean isScenarioTopLevelElement(XMLEvent event) {
		if (event.isStartElement()) {
			String elementName = event.asStartElement().getName().getLocalPart();
			return SCENARIO_TOP_LEVEL_ELEMENT_NAMES.contains(elementName);
		}
		else {
			return false;
		}		
	}

	private  final static Set<String> RESULTS_TOP_LEVEL_ELEMENT_NAMES = new HashSet<String>(Arrays.asList("iceConfigurations", "CDMAResults", "Signals", "Correlations", "CoverageRadiuses"));
	private boolean isResultsTopLevelElement(XMLEvent event) {
		if (event.isStartElement()) {
			String elementName = event.asStartElement().getName().getLocalPart();
			return RESULTS_TOP_LEVEL_ELEMENT_NAMES.contains(elementName);
		}
		else {
			return false;
		}		
	}

	private void moveEvent(XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
		writer.add(reader.nextEvent());		
	}

	private void moveSubTree(XMLEventReader reader, XMLEventWriter writer) throws XMLStreamException {
		StartElement startElement = reader.nextEvent().asStartElement();		
		Stack<String> elementNames = new Stack<String>();
		elementNames.push(startElement.getName().getLocalPart());		
		writer.add(startElement);
		while (!elementNames.empty()) {
			if (reader.hasNext()) {
				XMLEvent currentEvent = reader.nextEvent();
				writer.add(currentEvent);
				if (currentEvent.isStartElement()) {
					elementNames.push(currentEvent.asStartElement().getName().getLocalPart());
				}
				if (currentEvent.isEndElement()) {
					String poppedElementName = elementNames.pop();
					if (!poppedElementName.equals(currentEvent.asEndElement().getName().getLocalPart())) {
						throw new XMLStreamException("Unexpected end element event: " + currentEvent);						
					}
				}
			}
			else {
				throw new XMLStreamException("Unexpected end of XML stream while moving subtree for element: " + startElement);
			}
		}
	}

	private void createZipFileFromScenarioAndResultsFiles(File migratedFile, File scenarioFile, File resultsFile, boolean hasResults) throws Exception {
		ZipOutputStream zipOutputStream = null;
		try {
			zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(migratedFile)));
			
			ZipEntry scenarioEntry = new ZipEntry("scenario.xml");
			zipOutputStream.putNextEntry(scenarioEntry);
			FileUtils.copyFile(scenarioFile, zipOutputStream);

			if (hasResults) {
				ZipEntry resultsEntry = new ZipEntry("results.xml");
				zipOutputStream.putNextEntry(resultsEntry);
				FileUtils.copyFile(resultsFile, zipOutputStream);						
			}
		}
		finally {
			IOUtils.closeQuietly(zipOutputStream);
		}		
	}

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(5);
   }
}
