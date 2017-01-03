package org.seamcat.testutil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.seamcat.migration.workspace.WorkspaceMigrator;
import org.seamcat.model.MigrationIssue;

/**
 * Run this to force a migration of the test workspaces
 */
public class TestMigrator {

	public static void main(String[] args) throws IOException {
		File testWorkspaceDir = new File("test/resources/");
		for (String workspace : testWorkspaceDir.list()) {
			if ( workspace.endsWith(".sws")) {
				String name = "test/resources/" + workspace;
				File migrated = new WorkspaceMigrator().migrate( new File(name), new ArrayList<MigrationIssue>());
				try {
					FileUtils.copyFile( migrated, new File(name));
				} catch (IOException e ) {
					if ( !e.getMessage().endsWith("are the same") ) {
						throw e;
					}
				}
				System.out.print( ".");
			}
		}
	}

}
