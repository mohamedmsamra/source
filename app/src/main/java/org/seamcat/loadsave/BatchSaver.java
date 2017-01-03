package org.seamcat.loadsave;

import org.apache.commons.io.IOUtils;
import org.seamcat.batch.BatchJobList;
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

public class BatchSaver {

    private BatchJobList batch;

    public BatchSaver(BatchJobList batch) {
        this.batch = batch;
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
        saveResultsToZip(zipOutputStream);
		zipOutputStream.close();
	}

	private void saveResultsToZip(ZipOutputStream zipOutputStream) throws IOException, XMLStreamException, FactoryConfigurationError {
        for (Workspace item : batch.getBatchJobs()) {
            if ( item.isHasBeenCalculated() ) {
                ZipEntry resultsXmlZipEntry = new ZipEntry( item.getName() + "_results.xml" );
                zipOutputStream.putNextEntry(resultsXmlZipEntry);
                XMLEventWriter resultsXmlEventWriter = XMLOutputFactory.newFactory().createXMLEventWriter(zipOutputStream);
                resultsXmlEventWriter.add(XmlEventFactory.startDocument());
                WorkspaceMarshaller.saveResultsToXmlStream( item, resultsXmlEventWriter );
                resultsXmlEventWriter.add(XmlEventFactory.endDocument());
                resultsXmlEventWriter.close();
            }
        }
    }

	private void saveScenarioToZip(ZipOutputStream zipOutputStream) throws IOException {
		ZipEntry scenarioXmlZipEntry = new ZipEntry(Constants.BATCH_SCENARIO_ENTRY_NAME);
		zipOutputStream.putNextEntry(scenarioXmlZipEntry);
		Document scenarioDocument = batchScenarioDocument();
		XmlUtils.write(scenarioDocument, zipOutputStream);
	}

	private Document batchScenarioDocument() {
		Document document = XmlUtils.createDocument();
        Element element = batch.toElement(document);
        document.appendChild(element);
		return document;
	}	
}
