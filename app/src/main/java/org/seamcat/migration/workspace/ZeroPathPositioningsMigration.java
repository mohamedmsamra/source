package org.seamcat.migration.workspace;

import java.io.File;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;

public class ZeroPathPositioningsMigration implements FileMigration {

	@Override
   public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
		Document document = XmlUtils.parse(originalFile);
		migrateDocument(document);
		XmlUtils.write(document, migratedFile);
   }

	private void migrateDocument(Document document) {
		migrateInterfererIt2VrPaths(document);
		migrateInterfererWt2VrPaths(document);
		migrateVictimWt2VrPaths(document);
		updateVersion(document);
   }

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	private void migrateInterfererIt2VrPaths(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
		List<?> links = context.selectNodes("Workspace/InterferenceLink[@colocated='false' and (@correlationMode='0' or @correlationMode='1' or @correlationMode='2' or @correlationMode='13' or @correlationMode='14' or @correlationMode='15')]");
		for (Object link: links) {
			JXPathContext.newContext(link).setValue("TransmitterToReceiverPath/@deltaX", "0.0");
			JXPathContext.newContext(link).setValue("TransmitterToReceiverPath/@deltaY", "0.0");
		}
   }

	private void migrateInterfererWt2VrPaths(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
		List<?> paths = context.selectNodes("Workspace/InterferenceLink/InterferingSystemLink/systemLink/TransmitterToReceiverPath[@useCorrelatedDistance='false']");
		for (Object path: paths) {
			JXPathContext.newContext(path).setValue("@deltaX", "0.0");
			JXPathContext.newContext(path).setValue("@deltaY", "0.0");
		}
   }

	private void migrateVictimWt2VrPaths(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
		List<?> paths = context.selectNodes("Workspace/VictimSystemLink/systemLink/TransmitterToReceiverPath[@useCorrelatedDistance='false']");
		for (Object path: paths) {
			JXPathContext.newContext(path).setValue("@deltaX", "0.0");
			JXPathContext.newContext(path).setValue("@deltaY", "0.0");
		}
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return WorkspaceFormatVersionConstants.PRE_3_2_3;
   }
}
