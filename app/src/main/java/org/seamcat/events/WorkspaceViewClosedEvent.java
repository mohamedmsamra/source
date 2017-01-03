package org.seamcat.events;

import org.seamcat.presentation.WorkspaceView;

public class WorkspaceViewClosedEvent {

	private WorkspaceView view;
	
	public WorkspaceViewClosedEvent( WorkspaceView view ) {
	this.view = view;
	}
	
	public WorkspaceView getView() {
		return view;
	}
}
