package org.seamcat.migration;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.AzimuthElevationTransceiverWorkspaceMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;


public class AzimuthElevationTransceiverWorkspaceMigrationTest {

	AzimuthElevationTransceiverWorkspaceMigration migration;

	@Before
	public void setUp() {
		migration = new AzimuthElevationTransceiverWorkspaceMigration();
	}
	
	@Test
	public void migrate() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/azimuthElevationTransceiverWs.sws");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate(originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		Document migratedDocument = XmlUtils.parse(migratedFile);
		XPathAssert xpathAssert = new XPathAssert(migratedDocument);
		xpathAssert.nodeValueEquals("5.0", "Workspace/InterferenceLink[0]/InterferingSystemLink/InterferingTransmitter/transmitter/transceiver/azimuth/distribution/@constant");
		xpathAssert.nodeValueEquals("6.0", "Workspace/InterferenceLink[0]/InterferingSystemLink/InterferingTransmitter/transmitter/transceiver/elevation/distribution/@constant");
		xpathAssert.nodeValueEquals("7.0", "Workspace/InterferenceLink[0]/InterferingSystemLink/WantedReceiver/receiver/transceiver/azimuth/distribution/@constant");
		xpathAssert.nodeValueEquals("8.0", "Workspace/InterferenceLink[0]/InterferingSystemLink/WantedReceiver/receiver/transceiver/elevation/distribution/@constant");

		xpathAssert.nodeValueEquals("1.0", "Workspace/VictimSystemLink/VictimReceiver/receiver/transceiver/azimuth/distribution/@constant");
		xpathAssert.nodeValueEquals("2.0", "Workspace/VictimSystemLink/VictimReceiver/receiver/transceiver/elevation/distribution/@constant");
		xpathAssert.nodeValueEquals("3.0", "Workspace/VictimSystemLink/WantedTransmitter/transmitter/transceiver/azimuth/distribution/@constant");
		xpathAssert.nodeValueEquals("4.0", "Workspace/VictimSystemLink/WantedTransmitter/transmitter/transceiver/elevation/distribution/@constant");

		xpathAssert.nodeValueEquals(Integer.toString(2), "Workspace/@workspace_format_version");
	}
}
