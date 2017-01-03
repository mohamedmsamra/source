package org.seamcat.migration.settings;

import java.io.File;
import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FileMigration;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.MigrationIssue;
import org.seamcat.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReceiverBlockingMaskSettingsMigration implements FileMigration {

	@Override
   public void migrate(File originalFile, File migratedFile, List<MigrationIssue> migrationIssues) {
		Document document = XmlUtils.parse(originalFile);
		migrate(document);
		XmlUtils.write(document, migratedFile);
   }

	private void migrate(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List blocking = context.selectNodes("//blocking_response");

		for (Object o : blocking) {
			rename((Element) o, document, "RECEIVER_BLOCKING_MASK", "migrated");
		}
		updateVersion(document);
   }

	private void rename( Element orig, Document document, String reference, String description ) {
		Element elm = document.createElement("receiver-blocking-mask");
		elm.setAttribute( "reference", reference );
		elm.setAttribute( "description", description );

		for ( int i=0; i<orig.getChildNodes().getLength(); i++ ) {
			elm.appendChild(orig.getChildNodes().item(i));
		}

		Node parentNode = orig.getParentNode();
		parentNode.removeChild( orig );
		parentNode.appendChild( elm );
	}

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("seamcat/@settings_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
		return new FormatVersion(2);
   }
}
