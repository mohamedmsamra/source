package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class SystemDistributionWorkspaceMigration extends AbstractScenarioMigration {

    @Override
	void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        Element ws = (Element) context.selectNodes("//Workspace").get(0);
        String vId = ws.getAttribute("victim");

        List systems = context.selectNodes("//system");

        List links = context.selectNodes("//link");

        Element frequencies = document.createElement("frequencies");
        ws.appendChild( frequencies );

        // append victim frequency
        findAndAppendFreq( systems, vId, frequencies, document);

        for ( int i=0; i<links.size(); i++) {
            Element linkElm = (Element) links.get(i);
            String sysId = linkElm.getAttribute("interferingSystemId");
            findAndAppendFreq(systems, sysId, frequencies, document);
        }

        updateVersion(document);
	}

    private void findAndAppendFreq(List systems, String id, Element frequencies, Document document) {
        for (int i = 0; i < systems.size(); i++) {
            Element item = (Element) systems.get(i);
            if ( item.getAttribute("id").equals( id) ) {
                Element vFreq = (Element) item.getElementsByTagName("frequency").item(0);
                frequencies.appendChild( vFreq.cloneNode(true) );
            }
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
	   return new FormatVersion(43);
   }


}
