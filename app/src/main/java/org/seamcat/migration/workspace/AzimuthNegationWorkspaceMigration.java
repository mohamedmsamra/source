package org.seamcat.migration.workspace;

import java.io.File;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.AzimuthNegateMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;

public class AzimuthNegationWorkspaceMigration extends AzimuthNegateMigration {

	@Override
   public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
		Document document = XmlUtils.parse(originalFile);
		migrateDocument(document);
		XmlUtils.write(document, migratedFile);
   }

	private void migrateDocument(Document document) {
		fixAzimuth(document);
		updateVersion(document);
   }

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(4);
   }
}
