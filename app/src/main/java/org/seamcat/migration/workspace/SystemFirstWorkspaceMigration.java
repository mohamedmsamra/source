package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

public class SystemFirstWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List isl = context.selectNodes("//InterferingSystemLink");
		int untitledCounter = 1;
        for (Object o : isl) {
            Element element = (Element) o;

            Element general = document.createElement("general");
            Element iLink = (Element)element.getParentNode();
            String name = iLink.getAttribute("id");
            if ( name == null || name.isEmpty() ) {
                name = "UNTITLED_"+untitledCounter;
                untitledCounter++;

            }
            general.setAttribute("name", name);

            Element it = (Element) element.getElementsByTagName("InterferingTransmitter").item(0);
            Node transmitter = it.getElementsByTagName("transmitter").item(0);
            it.removeChild( transmitter );
            element.appendChild( transmitter );

            Element frequency = (Element) it.getElementsByTagName("frequency").item(0);
            it.removeChild( frequency );
            general.appendChild(frequency);

            document.renameNode(element, null, "system");
            element.appendChild( general );
        }

        List vsl = context.selectNodes("//VictimSystemLink");
        for ( Object o : vsl ) {
            Element element = (Element) o;

            String useWantedTransmitter = element.getAttribute("useWantedTransmitter");
            element.removeAttribute("useWantedTransmitter");

            Element general = document.createElement("general");

            Element frequency = (Element) element.getElementsByTagName("vlk_frequency").item(0);
            document.renameNode( frequency, null, "frequency");
            element.removeChild(frequency);
            general.appendChild( frequency );

            Element dRSS = (Element) element.getElementsByTagName("dRSS").item(0);
            element.removeChild( dRSS );
            Boolean aBoolean = Boolean.valueOf(useWantedTransmitter);
            dRSS.setAttribute("enabled", Boolean.toString(!aBoolean));
            general.appendChild( dRSS );

            document.renameNode(element, null,  "system");
            element.appendChild(general);
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
	   return new FormatVersion(33);
   }


}
