package org.seamcat.migration.workspace;

import org.seamcat.migration.FileMigrator;



public class WorkspaceMigrator extends FileMigrator {

	public WorkspaceMigrator() {
		setCurrentVersion(WorkspaceFormatVersionConstants.CURRENT_VERSION);
		setVersionExtractor(new WorkspaceVersionExtractor());
		setMigrationRegistry(new WorkspaceMigrationRegistry());
	}	
}
