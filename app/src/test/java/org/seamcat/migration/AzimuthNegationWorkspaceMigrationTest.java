package org.seamcat.migration;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.AzimuthNegationWorkspaceMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;


public class AzimuthNegationWorkspaceMigrationTest {

	AzimuthNegationWorkspaceMigration migration;

	@Before
	public void setUp() {
		migration = new AzimuthNegationWorkspaceMigration();
	}
	
	@Test
	public void migrate() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/AzimuthNegation.sws");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate(originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		Document migratedDocument = XmlUtils.parse(migratedFile);
		XPathAssert xpathAssert = new XPathAssert(migratedDocument);
		xpathAssert.nodeValueEquals("-3.3", "Workspace/VictimSystemLink/VictimReceiver/receiver/transceiver/azimuth/distribution/user-defined-stair/point2d/@x");
		xpathAssert.nodeValueEquals("-1.0", "Workspace/VictimSystemLink/WantedTransmitter/transmitter/transceiver/azimuth/distribution/@min");
		xpathAssert.nodeValueEquals("-2.0", "Workspace/VictimSystemLink/WantedTransmitter/transmitter/transceiver/azimuth/distribution/@max");

		xpathAssert.nodeValueEquals(Integer.toString(5), "Workspace/@workspace_format_version");
	}
}
