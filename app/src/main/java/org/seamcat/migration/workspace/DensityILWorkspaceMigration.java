package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class DensityILWorkspaceMigration extends AbstractScenarioMigration {


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
        Element conf = doc.createElement("InterferenceLinkConfiguration");
        interferenceLink.appendChild( conf );

        Element old = findFirst(interferenceLink, "InterferingTransmitter");

        Element path = findFirst(interferenceLink, "TransmitterToReceiverPath");
        Element rp = doc.createElement("relativePosition");
        conf.appendChild( rp );
        rp.setAttribute("minimumCouplingLoss", path.getAttribute("mcl"));
        rp.setAttribute("deltaX", path.getAttribute("deltaX"));
        rp.setAttribute("deltaY", path.getAttribute("deltaY"));
        rp.setAttribute("useCorrelatedDistance", path.getAttribute("useCorrelatedDistance"));
        Node pa = path.getFirstChild();
        Node pdf = pa.getNextSibling();
        Node pm = pdf.getNextSibling();
        path.removeChild( pa );
        rp.appendChild( pa );
        path.removeChild(pdf);
        rp.appendChild( pdf );
        path.removeChild( pm );

        Element density = doc.createElement("interferersDensity");
        density.setAttribute("densityTx", old.getAttribute("densActiveTx"));
        old.removeAttribute("densActiveTx");
        density.setAttribute("probabilityOfTransmission", old.getAttribute("transProb"));
        old.removeAttribute("transProb");
        density.setAttribute("hourOfDay", old.getAttribute("time"));
        old.removeAttribute("time");

        Element activity = (Element) old.getElementsByTagName("activity").item(0);
        Node removed = activity.getFirstChild();
        Node function = removed.getFirstChild();
        removed.removeChild( function );
        Element newActivity = doc.createElement("activity");
        newActivity.appendChild( function );
        density.appendChild( newActivity );
        conf.appendChild( density );

        Element pl = doc.createElement("pathlossCorrelation");
        pl.setAttribute("usePathLossCorrelation", interferenceLink.getAttribute("pathloss_correlated"));
        interferenceLink.removeAttribute("pathloss_correlated");
        pl.setAttribute("pathLossVariance", interferenceLink.getAttribute("pathloss_variance"));
        interferenceLink.removeAttribute("pathloss_variance");
        pl.setAttribute("correlationFactor", interferenceLink.getAttribute("correlation_factor"));
        interferenceLink.removeAttribute("correlation_factor");
        conf.appendChild( pl );

        conf.appendChild( pm );
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
	   return new FormatVersion(29);
   }


}
