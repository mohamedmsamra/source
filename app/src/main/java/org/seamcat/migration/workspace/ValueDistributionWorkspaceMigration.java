package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class ValueDistributionWorkspaceMigration extends AbstractScenarioMigration {

    @Override
	void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        List relative = context.selectNodes("//relativeLocation");

        for (Object o : relative) {
            Element link = (Element) o;

            appendDist(document, link, "deltaX");
            appendDist(document, link, "deltaY");
            appendDist(document, link, "coLocationX");
            appendDist(document, link, "coLocationY");
            appendDist(document, link, "minimumCouplingLoss");
            appendDist(document, link, "protectionDistance");
        }


        updateVersion(document);
	}

    private void appendDist(Document document, Element element, String name ) {
        if ( element.hasAttribute(name)) {
            String constant = element.getAttribute(name);
            element.removeAttribute(name);
            Element namedEle = document.createElement(name);
            Element distribution = document.createElement("distribution");
            distribution.setAttribute("constant", constant);
            distribution.setAttribute("type", "0");

            namedEle.appendChild( distribution );
            element.appendChild( namedEle );
        }
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
	   return new FormatVersion(42);
   }


}
