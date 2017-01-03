package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class SystemSecondWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List il = context.selectNodes("//InterferenceLink");
		for (Object o : il) {
            Element element = (Element) o;

            String isCDMA;
            if ( element.hasAttribute("isCDMA") ) {
                isCDMA = element.getAttribute("isCDMA");
                element.removeAttribute("isCDMA");
            } else {
                isCDMA = "false";
            }

            Element system = (Element) element.getElementsByTagName("system").item(0);
            system.setAttribute("isCDMA", isCDMA);

            NodeList cdmaSystem = element.getElementsByTagName("CdmaSystem");
            if ( cdmaSystem.getLength() > 0 ) {
                Node cdmaElement = cdmaSystem.item(0);
                element.removeChild( cdmaElement );
                system.appendChild( cdmaElement );
            }
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
	   return new FormatVersion(34);
   }


}
