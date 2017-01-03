package org.seamcat.migration;

import static org.junit.Assert.*;

import java.io.File;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.WorkspaceFormatVersionConstants;
import org.seamcat.migration.workspace.WorkspaceVersionExtractor;
import org.seamcat.util.IOUtils;


public class WorkspaceVersionExtractorTest {

	private WorkspaceVersionExtractor extractor;
	
	@Before
	public void setUp() {
		extractor = new WorkspaceVersionExtractor();		
	}

	@Test
	public void extractPost323VersionWihtoutVersionAttribute() {
		File workspaceFile = IOUtils.copyResourceToTempFileWithSameName("migration/post323Workspace.sws");
		FormatVersion extractedVersion = extractor.extractVersion(workspaceFile);
		assertEquals(WorkspaceFormatVersionConstants.POST_3_2_3, extractedVersion);
	}
	
	@Test
	public void extractPre323Version() {
		File workspaceFile = IOUtils.copyResourceToTempFileWithSameName("migration/pre323Workspace.sws");
		FormatVersion extractedVersion = extractor.extractVersion(workspaceFile);
		assertEquals(WorkspaceFormatVersionConstants.PRE_3_2_3, extractedVersion);
	}
	
	@Test
	public void extractVersionWithVersionAttribute() {
		File workspaceFile = IOUtils.copyResourceToTempFileWithSameName("migration/version2Workspace.sws");
		FormatVersion extractedVersion = extractor.extractVersion(workspaceFile);
		assertEquals(new FormatVersion(2), extractedVersion);
	}
	
	@Test
	public void pre323SeamcatVersionString() {
		Assert.assertTrue(  WorkspaceVersionExtractor.isPre323SeamcatVersionString("SEAMCAT 3.2.3 - rev 938") );
		Assert.assertFalse( WorkspaceVersionExtractor.isPre323SeamcatVersionString("SEAMCAT 3.2.3 - rev 939") );
		Assert.assertTrue(  WorkspaceVersionExtractor.isPre323SeamcatVersionString("SEAMCAT 3.1.3") );	
		Assert.assertFalse( WorkspaceVersionExtractor.isPre323SeamcatVersionString("SEAMCAT 3.2.3") );
		Assert.assertTrue(  WorkspaceVersionExtractor.isPre323SeamcatVersionString("3.1.3") );	
		Assert.assertFalse( WorkspaceVersionExtractor.isPre323SeamcatVersionString("3.2.3") );
	}

	@Test
	public void extractVersionFromZippedWorkspace() {
		File workspaceFile = IOUtils.copyResourceToTempFileWithSameName("migration/zip-no-results.sws");
		FormatVersion extractedVersion = extractor.extractVersion(workspaceFile);
		assertEquals(new FormatVersion(6), extractedVersion);
	}	
}
