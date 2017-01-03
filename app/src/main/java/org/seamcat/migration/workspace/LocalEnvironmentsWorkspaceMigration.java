package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class LocalEnvironmentsWorkspaceMigration extends AbstractScenarioMigration {

    @Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
        List systems = context.selectNodes("//system");

        for (Object o : systems) {
            Element system = (Element) o;
            Node local = system.getElementsByTagName("localEnvironments").item(0);
            NodeList childNodes = local.getChildNodes();
            if ( childNodes.getLength() == 2 ) {
                Node receiver = childNodes.item(0);
                Node transmitter = childNodes.item(1);

                local.removeChild( receiver );
                Element receiverEnvironments = document.createElement("receiverEnvironments");
                receiverEnvironments.appendChild( receiver );

                local.removeChild(transmitter);
                Element transmitterEnvironments = document.createElement("transmitterEnvironments");
                transmitterEnvironments.appendChild( transmitter );

                local.appendChild( receiverEnvironments );
                local.appendChild( transmitterEnvironments );
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
	   return new FormatVersion(39);
   }


}
