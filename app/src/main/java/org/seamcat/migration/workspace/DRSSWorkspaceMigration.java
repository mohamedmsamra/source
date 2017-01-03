package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class DRSSWorkspaceMigration extends AbstractScenarioMigration {

    @Override
	void migrateScenarioDocument(Document document) {
        JXPathContext context = JXPathContext.newContext(document);
        Element ws = (Element) context.selectNodes("//Workspace").get(0);
        String vId = ws.getAttribute("victim");

        List systems = context.selectNodes("//system");

        for (Object sys : systems) {
            Element system = (Element) sys;
            String id = system.getAttribute("id");
            NodeList dRSS = system.getElementsByTagName("dRSS");
            if ( dRSS.getLength() > 0 ) {
                Node dRSSNode = dRSS.item(0);
                if ( vId.equals( id )) {
                    dRSSNode.getParentNode().removeChild( dRSSNode );
                    ws.appendChild( dRSSNode );
                } else {
                    dRSSNode.getParentNode().removeChild( dRSSNode );
                }
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
	   return new FormatVersion(44);
   }


}
