package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class InterferenceLinkNameWorkspaceMigration extends AbstractScenarioMigration {

    @Override
	void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List links = context.selectNodes("//link");

        int i=1;
        for (Object o : links) {
            Element link = (Element) o;

            String id = link.getAttribute("id");
            link.removeAttribute("id");
            link.setAttribute("interferingSystemId", id);
            link.setAttribute("name", "Link "+i );
            link.setAttribute("id", ""+i);
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
	   return new FormatVersion(41);
   }


}
