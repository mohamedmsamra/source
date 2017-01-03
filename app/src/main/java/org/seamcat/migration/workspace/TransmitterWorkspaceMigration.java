package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class TransmitterWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List transmitters = context.selectNodes("//transmitter");
		for (Object o : transmitters) {
            Element element = (Element) o;
            fixTransmitter(element, document);
		}

        updateVersion(document);
	}

    public static void fixTransmitter( Element transmitter, Document doc) {

        rename(transmitter, "interferer_is_cr", "cognitiveRadio");

        Node parentNode = transmitter.getParentNode();
        if ( parentNode!= null && parentNode.getNodeName().equals("InterferingTransmitter")) {
            Element it = (Element) parentNode;
            if ( it.hasAttribute("deviceCR") ) {
                transmitter.setAttribute("cognitiveRadio", it.getAttribute("deviceCR"));
                it.removeAttribute("deviceCR");
            }
        }
        rename(transmitter, "power_control_max_threshold", "receivePower");
        rename(transmitter, "use_power_control", "powerControl");

        Element powerDist = findFirst(transmitter, "power-supplied-distribution");
        if ( powerDist != null ) {
            rename(powerDist, doc, "power");
        }
        Element emissionMask = findFirst( transmitter, "spectrum-emission-mask");
        if ( emissionMask != null ) {
            rename(emissionMask, doc, "emissionMask");
        }
        String useFloor = transmitter.getAttribute("use_unwanted_emission");
        transmitter.removeAttribute("use_unwanted_emission");

        Element floor = findFirst(transmitter, "unwantedemissionfloor");
        if ( floor != null ) {
            Element emissionFloor = rename(floor, doc, "emissionFloor");
            emissionFloor.setAttribute("enabled", useFloor);
        }

        Element powercontrol = findFirst(transmitter, "powercontrol");
        String pcmin = powercontrol.getAttribute("pcmin");
        String pcrange = powercontrol.getAttribute("pcrange");
        String pcstep = powercontrol.getAttribute("pcstep");
        transmitter.setAttribute("stepSize", pcstep);
        transmitter.setAttribute("minThreshold", pcmin);
        transmitter.setAttribute("dynamicRange", pcrange);
        transmitter.removeChild( powercontrol );

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
	   return new FormatVersion(27);
   }


}
