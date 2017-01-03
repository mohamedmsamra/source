package org.seamcat.loadsave;

import org.seamcat.batch.BatchJobList;
import org.seamcat.commands.CancelLoadCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.EventHandler;
import org.seamcat.model.Workspace;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BatchLoader {

    private boolean loadCancelled;
    private XmlEventStream eventStream;

    public BatchLoader() {
        loadCancelled = false;
        EventBusFactory.getEventBus().subscribe( this );
    }

	public BatchJobList loadFromFile(File file) throws IOException {
		ZipFile zipFile = new ZipFile(file);
		
		BatchJobList batch = loadBatchScenarioFromZipFile(zipFile);
        batch.setAbsoluteLocation(file.getAbsolutePath());
        loadResultsIntoBatch(batch, zipFile);
        return batch;
	}

	private BatchJobList loadBatchScenarioFromZipFile(ZipFile zipFile) {
		ZipEntry scenarioEntry = zipFile.getEntry(Constants.BATCH_SCENARIO_ENTRY_NAME);
		if  (scenarioEntry == null) {
			throw new LoadException("Failed to load workspace. Scenario entry not found in zip file.");
		}
		
		Document document;
		try {
			document = XmlUtils.parse(zipFile.getInputStream(scenarioEntry));
		} catch (IOException e) {
			throw new LoadException(e);
		}
        return new BatchJobList(document.getDocumentElement());
	}

	private void loadResultsIntoBatch(BatchJobList batch, ZipFile zipFile) {
        for (Workspace item : batch.getBatchJobs()) {
            if ( item.isHasBeenCalculated() ) {
                ZipEntry resultsEntry = zipFile.getEntry( item.getName() + "_results.xml" );
                if  (resultsEntry != null) {
                    try {
                        InputStream inputStream = zipFile.getInputStream(resultsEntry);
                        XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
                        eventStream = new XmlEventStream(eventReader);
                        new ResultLoader(eventStream).readResultsFromXmlStream( item );
                    } catch (XmlEventStream.Cancelled c ) {
                        // cancelled. Swallow exception
                    } catch (Exception e) {
                        throw new LoadException("Failed to load results into workspace.", e);
                    }
                } else {
                    item.setHasBeenCalculated(false);
                }
            }
        }

	}

    public boolean isCancelled() {
        return loadCancelled;
    }

    @EventHandler
    public void cancel( CancelLoadCommand command ) {
        loadCancelled = true;
        eventStream.cancel();
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        EventBusFactory.getEventBus().unsubscribe( this );
    }
}
