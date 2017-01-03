package org.seamcat.migration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.XmlToZipFileWorkspaceMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;


public class XmlToZipFileWorkspaceMigrationTest {

	XmlToZipFileWorkspaceMigration migration;

	@Before
	public void setUp() {
		migration = new XmlToZipFileWorkspaceMigration();
	}
	
	@Test
	public void migrateClassicWorkspaceWithoutResults() throws Exception {
		File originalFile = IOUtils.copyResourceToTempFile("migration/xml-v5-no-results.sws");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate(originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		ZipFile zipFile = new ZipFile(migratedFile);
		ZipEntry scenarioEntry = zipFile.getEntry("scenario.xml");
		assertNotNull(scenarioEntry);
		ZipEntry resultsEntry = zipFile.getEntry("results.xml");
		assertNull(resultsEntry);
		
		Document scenarioDocument = XmlUtils.parse(zipFile.getInputStream(scenarioEntry));
		verifyScenarioContents(scenarioDocument);
	}

	@Test
	public void migrateClassicWorkspaceWithResults() throws Exception {
		File originalFile = IOUtils.copyResourceToTempFile("migration/xml-v5-with-results.sws");
		File migratedFile = IOUtils.createTempFile();
		migration.migrate(originalFile, migratedFile, new ArrayList<MigrationIssue>());
		
		ZipFile zipFile = new ZipFile(migratedFile);
		ZipEntry scenarioEntry = zipFile.getEntry("scenario.xml");
		assertNotNull(scenarioEntry);
		ZipEntry resultsEntry = zipFile.getEntry("results.xml");
		assertNotNull(resultsEntry);
		
		Document scenarioDocument = XmlUtils.parse(zipFile.getInputStream(scenarioEntry));
		verifyScenarioContents(scenarioDocument);

		Document resultsDocument = XmlUtils.parse(zipFile.getInputStream(resultsEntry));
		verifyResultsContents(resultsDocument);
}

	private void verifyScenarioContents(Document scenarioDocument) {
		XPathAssert scenarioAsserts = new XPathAssert(scenarioDocument);
		scenarioAsserts.nodeValueEquals("6", "/Workspace/@workspace_format_version");
		scenarioAsserts.hasOneNode("Workspace/InterferenceLink");
		scenarioAsserts.hasNoNode("Workspace/iceConfigurations");
		scenarioAsserts.hasNoNode("Workspace/CDMAResults");
		scenarioAsserts.hasNoNode("Workspace/Signals");
		scenarioAsserts.hasNoNode("Workspace/Correlations");
		scenarioAsserts.hasNoNode("Workspace/CoverageRadiuses");
		scenarioAsserts.hasOneNode("Workspace/VictimSystemLink");
		scenarioAsserts.hasOneNode("Workspace/Control");
		scenarioAsserts.hasOneNode("Workspace/postprocessing-plugins");
	}

	private void verifyResultsContents(Document resultsDocument) {
		XPathAssert resultsAsserts = new XPathAssert(resultsDocument);
		resultsAsserts.hasNoNode("workspaceResults/InterferenceLink");
		resultsAsserts.hasOneNode("workspaceResults/iceConfigurations");
		resultsAsserts.hasNoNode("workspaceResults/CDMAResults");
		resultsAsserts.hasOneNode("workspaceResults/Signals");
		resultsAsserts.hasOneNode("workspaceResults/Correlations");
		resultsAsserts.hasOneNode("workspaceResults/CoverageRadiuses");
		resultsAsserts.hasNoNode("workspaceResults/VictimSystemLink");
		resultsAsserts.hasNoNode("workspaceResults/Control");
		resultsAsserts.hasNoNode("workspaceResults/postprocessing-plugins");
	}
}
