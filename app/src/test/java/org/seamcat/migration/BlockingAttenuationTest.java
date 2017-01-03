package org.seamcat.migration;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.BlockingAttenuationMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;


public class BlockingAttenuationTest {

	BlockingAttenuationMigration migration;

	@Before
	public void setUp() {
		migration = new BlockingAttenuationMigration();
	}
	
	@Test
	public void migrate() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/blockingAttenuation.sws");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate(originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		Document migratedDocument = XmlUtils.parse(migratedFile);
		XPathAssert xpathAssert = new XPathAssert(migratedDocument);
		xpathAssert.nodeValueEquals("1", "Workspace/VictimSystemLink/VictimReceiver/receiver/@blocking-attenuation-mode");
		xpathAssert.nodeValueEquals(Integer.toString(3), "Workspace/@workspace_format_version");
	}
}
