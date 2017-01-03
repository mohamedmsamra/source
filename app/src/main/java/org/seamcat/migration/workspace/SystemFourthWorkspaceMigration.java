package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.List;

public class SystemFourthWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List sys = context.selectNodes("//system");
		for (Object o : sys) {
            Element element = (Element) o;

            Element general = (Element) element.getElementsByTagName("general").item(0);

            NodeList dRSS = general.getElementsByTagName("dRSS");
            if ( dRSS.getLength() == 0 ) {
                Element drssElement = document.createElement("dRSS");
                drssElement.setAttribute("enabled", "false");
                Element distribution = document.createElement("distribution");
                distribution.setAttribute("constant","0.0");
                distribution.setAttribute("max","0.0");
                distribution.setAttribute("max-angle","0.0");
                distribution.setAttribute("max-distance","0.0");
                distribution.setAttribute("mean","0.0");
                distribution.setAttribute("min","0.0");
                distribution.setAttribute("std-dev","0.0");
                distribution.setAttribute("step","0.0");
                distribution.setAttribute("stepShift","0.0");
                distribution.setAttribute("type","0");

                drssElement.appendChild( distribution );

                general.appendChild( drssElement );
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
	   return new FormatVersion(36);
   }


}
