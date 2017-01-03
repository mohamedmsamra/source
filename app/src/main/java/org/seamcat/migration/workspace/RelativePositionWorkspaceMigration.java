package org.seamcat.migration.workspace;

import org.apache.commons.jxpath.JXPathContext;
import org.seamcat.migration.FormatVersion;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class RelativePositionWorkspaceMigration extends AbstractScenarioMigration {


	@Override
	void migrateScenarioDocument(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List interferenceLinks = context.selectNodes("//relativePosition");
		for (Object o : interferenceLinks) {
            Element element = (Element) o;
            fixRelativePosition(element, document);
		}

        updateVersion(document);
	}

    public static void fixRelativePosition(Element relativePosition, Document doc) {
        Element relativeLocation = doc.createElement("relativeLocation");


        NodeList childNodes = relativePosition.getChildNodes();
        while ( childNodes.getLength() > 0 ) {
            Node child = childNodes.item(0);
            relativeLocation.appendChild( child );
            //relativePosition.removeChild( child );
        }
        relativePosition.appendChild( relativeLocation );

        moveAtt( "deltaX", relativePosition, relativeLocation );
        moveAtt( "deltaY", relativePosition, relativeLocation );
        moveAtt( "useCorrelatedDistance", relativePosition, relativeLocation );
    }

    private static void moveAtt( String name, Element from, Element to ) {
        String attribute = from.getAttribute(name);
        to.setAttribute( name, attribute );
        from.removeAttribute( name );
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
	   return new FormatVersion(31);
   }


}
