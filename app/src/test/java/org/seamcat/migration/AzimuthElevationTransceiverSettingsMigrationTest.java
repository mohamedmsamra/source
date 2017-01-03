package org.seamcat.migration;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.settings.AzimuthElevationTransceiverSettingsMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;


public class AzimuthElevationTransceiverSettingsMigrationTest {

	AzimuthElevationTransceiverSettingsMigration migration;

	@Before
	public void setUp() {
		migration = new AzimuthElevationTransceiverSettingsMigration();
	}
	
	@Test
	public void migrate() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/prehistoricSettings.xml");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate(originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		Document migratedDocument = XmlUtils.parse(migratedFile);		
		List<?> tranceivers = JXPathContext.newContext(migratedDocument).selectNodes("//transceiver");
		assertEquals(4, tranceivers.size());
		
		for (Object transceiver: tranceivers) {
			XPathAssert xpathAssert = new XPathAssert(transceiver);
			xpathAssert.hasOneNode("azimuth/distribution/description");
			xpathAssert.hasOneNode("elevation/distribution/description");
			xpathAssert.nodeValueEquals("0.0", "azimuth/distribution/@constant");
			xpathAssert.nodeValueEquals("1.0", "azimuth/distribution/@max");
			xpathAssert.nodeValueEquals("360.0", "azimuth/distribution/@max-angle");
			xpathAssert.nodeValueEquals("1.0", "azimuth/distribution/@max-distance");
			xpathAssert.nodeValueEquals("0.0", "azimuth/distribution/@mean");
			xpathAssert.nodeValueEquals("0.0", "azimuth/distribution/@min");
			xpathAssert.nodeValueEquals("0.0", "azimuth/distribution/@std-dev");
			xpathAssert.nodeValueEquals("0.2", "azimuth/distribution/@step");
			xpathAssert.nodeValueEquals("0", "azimuth/distribution/@type");
			xpathAssert.nodeValueEquals("0.0", "elevation/distribution/@constant");
			xpathAssert.nodeValueEquals("1.0", "elevation/distribution/@max");
			xpathAssert.nodeValueEquals("360.0", "elevation/distribution/@max-angle");
			xpathAssert.nodeValueEquals("1.0", "elevation/distribution/@max-distance");
			xpathAssert.nodeValueEquals("0.0", "elevation/distribution/@mean");
			xpathAssert.nodeValueEquals("0.0", "elevation/distribution/@min");
			xpathAssert.nodeValueEquals("0.0", "elevation/distribution/@std-dev");
			xpathAssert.nodeValueEquals("0.2", "elevation/distribution/@step");
			xpathAssert.nodeValueEquals("0", "elevation/distribution/@type");
		}
				
		XPathAssert xpathAssert = new XPathAssert(migratedDocument);
		xpathAssert.nodeValueEquals(Integer.toString(0), "seamcat/@settings_format_version");
	}
}
