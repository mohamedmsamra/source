package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class ResultVectorsWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		updateVersion(document);
	}

	@Override
	void migrateResultsDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		removeNode( context, "//DRSSDistrib" );
		removeNode( context, "//iRSSDistribListBlocking");
		removeNode( context, "//iRSSDistribListIntermodulation");
		removeNode( context, "//iRSSDistribListUnwanted");
	}

	private void removeNode( JXPathContext context, String searchPath ) {
		List blocking = context.selectNodes( searchPath );
		for (Object o : blocking) {
			if ( o instanceof Node ) {
				((Node) o).getParentNode().removeChild((Node) o );
			}
		}
	}

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(7);
   }


}
