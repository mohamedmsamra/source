package org.seamcat.migration;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.WorkspaceFormatVersionConstants;
import org.seamcat.migration.workspace.ZeroPathPositioningsMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;


public class ZeroPathPositioningsMigrationTest {

	ZeroPathPositioningsMigration migration;
	
	@Before
	public void setUp() {
		migration = new ZeroPathPositioningsMigration();
	}
	
	@Test
	public void migrate() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/pre323Workspace.sws");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate(originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		Document migratedDocument = XmlUtils.parse(migratedFile);
		XPathAssert xpathAssert = new XPathAssert(migratedDocument);
		xpathAssert.nodeValueEquals("0.0", "Workspace/InterferenceLink[0]/TransmitterToReceiverPath/@deltaX");
		xpathAssert.nodeValueEquals("0.0", "Workspace/InterferenceLink[0]/TransmitterToReceiverPath/@deltaY");
		xpathAssert.nodeValueEquals("0.0", "Workspace/InterferenceLink[0]/InterferingSystemLink/systemLink/TransmitterToReceiverPath/@deltaX");
		xpathAssert.nodeValueEquals("0.0", "Workspace/InterferenceLink[0]/InterferingSystemLink/systemLink/TransmitterToReceiverPath/@deltaY");
		xpathAssert.nodeValueEquals("0.0", "Workspace/InterferenceLink[1]/TransmitterToReceiverPath/@deltaX");
		xpathAssert.nodeValueEquals("0.0", "Workspace/InterferenceLink[1]/TransmitterToReceiverPath/@deltaY");
		xpathAssert.nodeValueEquals("0.0", "Workspace/InterferenceLink[1]/InterferingSystemLink/systemLink/TransmitterToReceiverPath/@deltaX");
		xpathAssert.nodeValueEquals("0.0", "Workspace/InterferenceLink[1]/InterferingSystemLink/systemLink/TransmitterToReceiverPath/@deltaY");
		xpathAssert.nodeValueEquals("0.0", "Workspace/VictimSystemLink/systemLink/TransmitterToReceiverPath/@deltaX");
		xpathAssert.nodeValueEquals("0.0", "Workspace/VictimSystemLink/systemLink/TransmitterToReceiverPath/@deltaY");
		xpathAssert.nodeValueEquals(Integer.toString(WorkspaceFormatVersionConstants.POST_3_2_3.getNumber()), "Workspace/@workspace_format_version");
	}
}
