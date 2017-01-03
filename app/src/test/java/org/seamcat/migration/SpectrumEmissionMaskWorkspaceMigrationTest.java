package org.seamcat.migration;

import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.SpectrumEmissionMaskWorkspaceMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class SpectrumEmissionMaskWorkspaceMigrationTest {

	SpectrumEmissionMaskWorkspaceMigration migration;

	@Before
	public void setUp() {
		migration = new SpectrumEmissionMaskWorkspaceMigration();
	}
	
	@Test
	public void migrate() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/spectrumEmissionWs.sws");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate(originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		Document migratedDocument = XmlUtils.parse(migratedFile);		
		List<?> unwantedEmissions = JXPathContext.newContext(migratedDocument).selectNodes("//spectrum-emission-mask");
		assertEquals(2, unwantedEmissions.size());
		
		for (Object unWantedEmission: unwantedEmissions) {
			XPathAssert xpathAssert = new XPathAssert(unWantedEmission);
			xpathAssert.hasOneNode("discretefunction2");
			xpathAssert.hasNoNode("unwantedemission");
		}
				
		XPathAssert xpathAssert = new XPathAssert(migratedDocument);
		xpathAssert.nodeValueEquals(Integer.toString(4), "Workspace/@workspace_format_version");
	}
}