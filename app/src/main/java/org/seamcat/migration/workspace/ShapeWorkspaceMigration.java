package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class ShapeWorkspaceMigration extends AbstractScenarioMigration {

    @Override
	void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List relativeLocations = context.selectNodes("//relativeLocation");

        for (Object o : relativeLocations) {
            Element element = (Element) o;
            element.setAttribute("shape", "0");
            element.setAttribute("turnCCW", "0.0");
            element.setAttribute("usePolygon", "false");
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
	   return new FormatVersion(40);
   }


}
