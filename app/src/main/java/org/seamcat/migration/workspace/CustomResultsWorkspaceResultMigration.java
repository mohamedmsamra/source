package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class CustomResultsWorkspaceResultMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		updateVersion(document);
	}

	@Override
	void migrateResultsDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List list = context.selectNodes("//CustomResults");
        for (Object aList : list) {
            Element results = (Element) aList;
            results.getParentNode().removeChild(results);
        }
	}

	private void updateVersion(Document document) {
	   JXPathContext context = JXPathContext.newContext(document);
	   context.createPathAndSetValue("Workspace/@workspace_format_version", getOriginalVersion().nextVersion().getNumber());
   }

	@Override
   public FormatVersion getOriginalVersion() {
	   return new FormatVersion(25);
   }


}
