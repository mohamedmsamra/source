package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class InterferenceLinkWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List interferenceLinks = context.selectNodes("//InterferenceLink");
		for (Object o : interferenceLinks) {
            Element element = (Element) o;
            fixDensity(element, document);
		}

        updateVersion(document);
	}

    public static void fixDensity(Element interferenceLink, Document doc) {
        Element link = findFirst(interferenceLink, "link");
        interferenceLink.setAttribute("id", link.getAttribute("reference"));

        Element rel = findFirst(interferenceLink, "relativePosition");
        rel.setAttribute("colocated", interferenceLink.getAttribute("colocated"));
        interferenceLink.removeAttribute("colocated");
        rel.setAttribute("correlationMode", interferenceLink.getAttribute("correlationMode"));
        interferenceLink.removeAttribute("correlationMode");
        rel.setAttribute("protectionDistance", interferenceLink.getAttribute("protectionDistance"));
        interferenceLink.removeAttribute("protectionDistance");
        rel.setAttribute("wrCenterOfItDistribution", interferenceLink.getAttribute("wrCenterOfItDistribution"));
        interferenceLink.removeAttribute("wrCenterOfItDistribution");
        if ( interferenceLink.hasAttribute("colocation_link")) {
            rel.setAttribute("colocation_link", interferenceLink.getAttribute("colocation_link"));
            interferenceLink.removeAttribute("colocation_link");
        }
        String dx = "0.0";
        if ( interferenceLink.hasAttribute("colocation_delta_x")) {
            dx = interferenceLink.getAttribute("colocation_delta_x");
            interferenceLink.removeAttribute("colocation_delta_x");
        }
        String dy = "0.0";
        if ( interferenceLink.hasAttribute("colocation_delta_y")) {
            dy = interferenceLink.getAttribute("colocation_delta_y");
            interferenceLink.removeAttribute("colocation_delta_y");
        }
        rel.setAttribute("colocation_delta_x", dx);
        rel.setAttribute("colocation_delta_y", dy);

        Element it = findFirst(interferenceLink, "InterferingTransmitter");
        rel.setAttribute("numberOfActiveTransmitters", it.getAttribute("nbActiveTx"));
        it.removeAttribute("nbActiveTx");
        rel.setAttribute("simulationRadius", it.getAttribute("rsimu"));
        it.removeAttribute("rsimu");

    }

    private static Element findFirst(Element parent, String name ) {
        NodeList powerDists = parent.getElementsByTagName(name);
        if ( powerDists.getLength() > 0 ) {
            return (Element) powerDists.item(0);
        }

        return null;
    }

    private static void rename( Element element, String attribute, String newName ) {
        String value = element.getAttribute(attribute);
        element.removeAttribute(attribute);
        element.setAttribute(newName, value);
    }

    private static Element rename( Element orig, Document document, String newName ) {
        Element elm = document.createElement(newName);

        for ( int i=0; i<orig.getChildNodes().getLength(); i++ ) {
            elm.appendChild(orig.getChildNodes().item(i));
        }

        Node parentNode = orig.getParentNode();
        parentNode.removeChild( orig );
        parentNode.appendChild( elm );
        return elm;
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
	   return new FormatVersion(30);
   }


}
