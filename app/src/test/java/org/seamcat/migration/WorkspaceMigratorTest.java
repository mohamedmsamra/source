package org.seamcat.migration;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.WorkspaceFormatVersionConstants;
import org.seamcat.migration.workspace.WorkspaceMigrator;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.IOUtils;


public class WorkspaceMigratorTest {
	
	WorkspaceMigrator migrator;
	
	@Before
	public void setUp() {
		migrator = new WorkspaceMigrator();
	}

	@Test 
	public void migrateAlreadyAtCurrentVersion() {
		File originalFile = IOUtils.copyResourceToTempFileWithSameName("migration/post323Workspace.sws");
		File migratedFile = migrator.migrateToVersion(originalFile, new ArrayList<MigrationIssue>(),WorkspaceFormatVersionConstants.POST_3_2_3);
		assertSame(originalFile, migratedFile);
	}
	
	@Test 
	public void migratePre323ToPost323() {
		File originalFile = IOUtils.copyResourceToTempFileWithSameName("migration/pre323Workspace.sws");
		File migratedFile = migrator.migrateToVersion(originalFile, new ArrayList<MigrationIssue>(), WorkspaceFormatVersionConstants.POST_3_2_3);
		assertNotSame(originalFile, migratedFile);
	}
	
	@Test(expected = MigrationException.class)
	public void migrateBackwardsThrowsException() {
		File originalFile = IOUtils.copyResourceToTempFileWithSameName("migration/post323Workspace.sws");
		migrator.migrateToVersion(originalFile, new ArrayList<MigrationIssue>(), WorkspaceFormatVersionConstants.PRE_3_2_3);
	}	
}
