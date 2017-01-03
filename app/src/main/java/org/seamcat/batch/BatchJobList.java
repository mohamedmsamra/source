package org.seamcat.batch;

import org.seamcat.marshalling.WorkspaceMarshaller;
import org.seamcat.migration.batch.BatchFormatVersionConstants;
import org.seamcat.migration.batch.BatchVersionExtractor;
import org.seamcat.model.Workspace;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.objectutils.WorkspaceCloneHelper;
import org.seamcat.presentation.batch.BatchViewState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class BatchJobList {

	private final List<Workspace> batchItems = new ArrayList<>();
    private String absoluteLocation;
    private boolean incrementalSave = true;
    private Description description;

    public Description getDescription() {
        return description;
    }

    public void setDescription( Description description ) {
        this.description = description;
    }

    public BatchJobList() {
		description = new DescriptionImpl("Batch", "");
	}
	public BatchJobList( String name, String description ) {
	    this.description = new DescriptionImpl(name, description);
    }

	public BatchJobList(Element element) {
		description = new DescriptionImpl(element.getAttribute("batch_reference"),element.getAttribute("batch_description") );
        if ( element.hasAttribute("incrementalSave")) {
            incrementalSave = Boolean.parseBoolean( element.getAttribute("incrementalSave"));
        }
		NodeList workspaces = element.getElementsByTagName("Workspace");
		for (int i = 0, size = workspaces.getLength(); i < size; i++) {
			Workspace bj = WorkspaceMarshaller.fromElement((Element) workspaces.item(i));
			addBatchJob(bj);
		}
	}

    public BatchViewState getViewState() {
        BatchViewState state = new BatchViewState(description.name(), description.description(), incrementalSave);
        state.setWorkspaces(new ArrayList<Workspace>());
        for (Workspace batchItem : batchItems) {
            state.getWorkspaces().add(WorkspaceCloneHelper.clone(batchItem));
        }
        return state;
    }

	public void addBatchJob(Workspace bj) {
		batchItems.add(bj);
	}

    public void remove( Workspace workspace ) {
        batchItems.remove(workspace);
    }

	public List<Workspace> getBatchJobs() {
		return batchItems;
	}

    public boolean isEmpty() {
        return batchItems.isEmpty();
    }

    public int size() {
        return batchItems.size();
    }

	public Element toElement(Document doc) {
        Element element = doc.createElement("BatchJobList");
		element.setAttribute("batch_reference", description.name());
        element.setAttribute("batch_description", description.description());
        element.setAttribute("incrementalSave", Boolean.toString(getIncrementalSave()));
        element.setAttribute(BatchVersionExtractor.BATCH_FORMAT_VERSION, Integer.toString( BatchFormatVersionConstants.CURRENT_VERSION.getNumber()));
        Element workspaces = doc.createElement("workspaces");
        for (Workspace bj : batchItems) {
			workspaces.appendChild( WorkspaceMarshaller.toElement(bj, doc));
		}
        element.appendChild( workspaces );
		return element;

	}

    public String getAbsoluteLocation() {
        return absoluteLocation;
    }

    public void setAbsoluteLocation(String absoluteLocation) {
        this.absoluteLocation = absoluteLocation;
    }

    public boolean hasLocation() {
        return absoluteLocation != null;
    }

    public boolean hasBeenCalculated() {
        if ( !batchItems.isEmpty() ) {
            return batchItems.get(0).isHasBeenCalculated();
        }
        return false;
    }

    public boolean getIncrementalSave() {
        return incrementalSave;
    }

    public void setIncrementalSave( boolean incrementalSave ) {
        this.incrementalSave = incrementalSave;
    }
}
