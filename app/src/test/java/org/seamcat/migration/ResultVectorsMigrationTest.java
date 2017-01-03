package org.seamcat.migration;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.migration.workspace.ResultVectorsWorkspaceMigration;
import org.seamcat.migration.workspace.XmlToZipFileWorkspaceMigration;
import org.seamcat.model.MigrationIssue;
import org.seamcat.testutil.XPathAssert;
import org.seamcat.util.IOUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.util.ArrayList;


public class ResultVectorsMigrationTest extends AbstractScenarioHelper {

	ResultVectorsWorkspaceMigration migration;
	XmlToZipFileWorkspaceMigration prerequisiteMigration;
	
	@Before
	public void setUp() {
		migration = new ResultVectorsWorkspaceMigration();
		prerequisiteMigration = new XmlToZipFileWorkspaceMigration();
	}
	
	@Test
	public void migrate() {
		File originalFile = IOUtils.copyResourceToTempFile("migration/ResultVectorsMigration.sws");
		File intermediateFile = IOUtils.createTempFile();
		File migratedFile = IOUtils.createTempFile();
		prerequisiteMigration.migrate( originalFile, intermediateFile, new ArrayList<MigrationIssue>());
		File reopened = new File( intermediateFile.getAbsolutePath() );
		migration.migrate( reopened, migratedFile, new ArrayList<MigrationIssue>());
		
		findScenarioAndResults(migratedFile);
	}

	@Override
	void migrateScenarioDocument(Document document) {
		XPathAssert xpathAssert = new XPathAssert(document);
		xpathAssert.nodeValueEquals( "8", "Workspace/@workspace_format_version");
	}

	@Override
	void migrateResultsDocument(Document document) {
		XPathAssert xpathAssert = new XPathAssert(document);
		xpathAssert.hasNoNode("workspaceResults/Signals/DRSSDistrib");
		xpathAssert.hasNoNode("workspaceResults/Signals/iRSSDistribListBlocking");
		xpathAssert.hasNoNode("workspaceResults/Signals/iRSSDistribListIntermodulation");
		xpathAssert.hasNoNode("workspaceResults/Signals/iRSSDistribListUnwanted");
	}
}
