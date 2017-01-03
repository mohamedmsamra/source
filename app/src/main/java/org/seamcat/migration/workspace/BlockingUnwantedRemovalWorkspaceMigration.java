package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.scenario.MutableLocalEnvironment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class BlockingUnwantedRemovalWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List links = context.selectNodes("//BlockingInterference");
        for (Object o : links) {
            Node node = (Node) o;
            node.getParentNode().removeChild( node );
        }
        links = context.selectNodes("//UnwantedInterference");
        for (Object o : links) {
            Node node = (Node) o;
            node.getParentNode().removeChild( node );
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
	   return new FormatVersion(17);
   }


}
