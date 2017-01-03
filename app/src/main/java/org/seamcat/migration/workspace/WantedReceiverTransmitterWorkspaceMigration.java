package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class WantedReceiverTransmitterWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List blocking = context.selectNodes("//WantedReceiver");
		for (Object o : blocking) {
			replaceChild((Element) o);
		}
        blocking = context.selectNodes("//WantedTransmitter");
        for (Object o : blocking) {
            replaceChild((Element) o);
        }

		updateVersion(document);
	}

	@Override
	void migrateResultsDocument(Document document) {
		// nothing to do here
	}

    private void replaceChild( Element element ) {
        Node firstChild = element.getFirstChild();
        element.getParentNode().replaceChild( firstChild, element );
    }

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(14);
   }


}
