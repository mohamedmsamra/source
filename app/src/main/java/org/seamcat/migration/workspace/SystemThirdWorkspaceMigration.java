package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class SystemThirdWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List sys = context.selectNodes("//system");
		for (Object o : sys) {
            Element element = (Element) o;

            Element systemLink = (Element) element.getElementsByTagName("systemLink").item(0);
            Node path = systemLink.getElementsByTagName("TransmitterToReceiverPath").item(0);
            systemLink.removeChild( path );
            element.appendChild( path );

            Element link = (Element) systemLink.getElementsByTagName("link").item(0);
            String reference = link.getAttribute("reference");
            Element general = (Element) element.getElementsByTagName("general").item(0);
            if ( !general.hasAttribute("name")) {
                general.setAttribute("name", reference);
            }
            general.setAttribute("description", "");

            element.removeChild( systemLink );
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
	   return new FormatVersion(35);
   }


}
