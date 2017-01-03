package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class VictimReceiverWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List blocking = context.selectNodes("//VictimReceiver");
        for (Object o : blocking) {
            Element element = (Element) o;
            Node intermodResponse = element.getFirstChild();
            Node receiver = element.getLastChild();
            element.removeChild( intermodResponse );
            receiver.appendChild( intermodResponse );
            element.getParentNode().replaceChild( receiver, element );
		}

		updateVersion(document);
	}

	@Override
	void migrateResultsDocument(Document document) {
		// nothing to do here
	}

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(15);
   }


}
