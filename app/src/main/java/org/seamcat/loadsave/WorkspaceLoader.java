package org.seamcat.loadsave;

import org.seamcat.commands.CancelLoadCommand;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.EventHandler;
import org.seamcat.marshalling.WorkspaceMarshaller;
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

public class WorkspaceLoader {

    private boolean loadCancelled;
    private XmlEventStream eventStream;

    public WorkspaceLoader() {
        loadCancelled = false;
        EventBusFactory.getEventBus().subscribe( this );
    }

	public Workspace loadFromFile(File file) throws IOException {
		ZipFile zipFile = new ZipFile(file);

        Workspace workspace = loadWorkspaceScenarioFromZipFile(zipFile);
        loadResultsIntoWorkspace(workspace, zipFile);
        return workspace;
	}

	private Workspace loadWorkspaceScenarioFromZipFile(ZipFile zipFile) {
		ZipEntry scenarioEntry = zipFile.getEntry(Constants.SCENARIO_ZIP_ENTRY_NAME);
		if  (scenarioEntry == null) {
			throw new LoadException("Failed to load workspace. Scenario entry not found in zip file.");
		}
		
		Document document;
		try {
			document = XmlUtils.parse(zipFile.getInputStream(scenarioEntry));
		} catch (IOException e) {
			throw new LoadException(e);
		}
		
		return WorkspaceMarshaller.fromElement(document.getDocumentElement());
	}

	private void loadResultsIntoWorkspace(Workspace workspace, ZipFile zipFile) {
		ZipEntry resultsEntry = zipFile.getEntry("results.xml");
		if  (resultsEntry != null) {
			try {
				InputStream inputStream = zipFile.getInputStream(resultsEntry);
				XMLEventReader eventReader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
				eventStream = new XmlEventStream(eventReader);
				new ResultLoader(eventStream).readResultsFromXmlStream( workspace );
                workspace.setHasBeenCalculated(true);
            } catch (XmlEventStream.Cancelled c ) {
                // cancelled. Swallow exception
            } catch (Exception e) {
				throw new LoadException("Failed to load results into workspace.", e);
			}
		}
		else {
			workspace.setHasBeenCalculated(false);			
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
