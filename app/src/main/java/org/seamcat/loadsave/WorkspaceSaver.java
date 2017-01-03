package org.seamcat.loadsave;

import org.apache.commons.io.IOUtils;
import org.seamcat.marshalling.WorkspaceMarshaller;
import org.seamcat.model.Workspace;
import org.seamcat.util.XmlEventFactory;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WorkspaceSaver {

	private Workspace workspace;
    private Workspace results;

    public WorkspaceSaver(Workspace workspace, Workspace results ) {
		this.workspace = workspace;
        this.results = results;
	}

	public void saveToFile(File file) {
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			saveToStream(outputStream);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		IOUtils.closeQuietly(outputStream);

	}

	public void saveToStream(OutputStream outputStream) {
		try {
			trySaveToStream(outputStream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void trySaveToStream(OutputStream outputStream) throws Exception {
		ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
		saveScenarioToZip(zipOutputStream);
		if (results != null) {
			saveResultsToZip(zipOutputStream);
		}

		zipOutputStream.close();
	}

	private void saveResultsToZip(ZipOutputStream zipOutputStream) throws IOException, XMLStreamException, FactoryConfigurationError {
		ZipEntry resultsXmlZipEntry = new ZipEntry(Constants.RESULTS_ZIP_ENTRY_NAME);
		zipOutputStream.putNextEntry(resultsXmlZipEntry);
		XMLEventWriter resultsXmlEventWriter = XMLOutputFactory.newFactory().createXMLEventWriter(zipOutputStream);
		resultsXmlEventWriter.add(XmlEventFactory.startDocument());
        WorkspaceMarshaller.saveResultsToXmlStream( results, resultsXmlEventWriter );
		resultsXmlEventWriter.add(XmlEventFactory.endDocument());
		resultsXmlEventWriter.close();
	}

	private void saveScenarioToZip(ZipOutputStream zipOutputStream) throws IOException {
		ZipEntry scenarioXmlZipEntry = new ZipEntry(Constants.SCENARIO_ZIP_ENTRY_NAME);
		zipOutputStream.putNextEntry(scenarioXmlZipEntry);
		Document scenarioDocument = workspaceToScenarioDocument(workspace);
		XmlUtils.write(scenarioDocument, zipOutputStream);
	}

	private Document workspaceToScenarioDocument(Workspace workspace) {
		Document document = XmlUtils.createDocument();
		Element workspaceElement = WorkspaceMarshaller.toElement(workspace, document );
		document.appendChild(workspaceElement);
		return document;
	}	
}
