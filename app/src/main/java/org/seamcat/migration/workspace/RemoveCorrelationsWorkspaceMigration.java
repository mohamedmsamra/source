package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class RemoveCorrelationsWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		updateVersion(document);
	}

	@Override
	void migrateResultsDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List blocking = context.selectNodes("//Correlations");

        for (Object o : blocking) {
            Element correlations = (Element) o;
            correlations.getParentNode().removeChild( correlations );
        }
	}

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(8);
   }


}
