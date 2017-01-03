package org.seamcat.migration.workspace;

import java.io.File;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BlockingAttenuationMigration implements FileMigration {

	@Override
   public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
		Document document = XmlUtils.parse(originalFile);
		migrateDocument(document);
		XmlUtils.write(document, migratedFile);
   }

	private void migrateDocument(Document document) {
		moveBlockingAttenuation(document);
		updateVersion(document);
   }

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	private void moveBlockingAttenuation(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		Element victimReceiver = (Element) context.selectSingleNode("Workspace/VictimSystemLink/VictimReceiver");
		String attribute = victimReceiver.getAttribute("blocking-attenuation-mode");
		victimReceiver.removeAttribute( "blocking-attenuation-mode" );

		Element receiver = (Element) context.selectSingleNode("Workspace/VictimSystemLink/VictimReceiver/receiver");
		receiver.setAttribute( "blocking-attenuation-mode", attribute );
	}
	
	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(2);
   }
}
