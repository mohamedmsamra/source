package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class SensingLinkWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List interferenceLinks = context.selectNodes("//InterferenceLink");
		for (Object o : interferenceLinks) {
            Element element = (Element) o;
            fixSensingLink(element, document);
		}

        updateVersion(document);
	}

    public static void fixSensingLink(Element interferenceLink, Document doc) {

        Element sensingLink = (Element) interferenceLink.getElementsByTagName("WantedTransmitterToInterferingTransmitterPath").item(0);
        sensingLink = rename(sensingLink, doc, "SensingLink");

        Element eirp = (Element) interferenceLink.getElementsByTagName("EIRPInBlockMask").item(0);
        eirp = rename( eirp, doc, "eirpMax");

        eirp.getParentNode().removeChild( eirp );
        sensingLink.appendChild( eirp );

        Element sensingDevice = (Element) interferenceLink.getElementsByTagName("SensingDevice").item(0);
        sensingLink.setAttribute("receptionBandwidth", sensingDevice.getAttribute("bandwidth"));
        sensingLink.setAttribute("probabilityOfFailure", sensingDevice.getAttribute("sensingFailureProb"));
        Node detectionThreshold = sensingDevice.getElementsByTagName("detectionThreshold").item(0);
        sensingDevice.removeChild(detectionThreshold);
        sensingLink.appendChild(detectionThreshold);
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
	   return new FormatVersion(28);
   }


}
