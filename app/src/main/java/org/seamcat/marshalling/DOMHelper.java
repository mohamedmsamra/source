package org.seamcat.marshalling;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMHelper {

    public static Element firstChild( Element parent, String name ) {
        NodeList list = parent.getElementsByTagName(name);
        for ( int i=0; i< list.getLength(); i++) {
            Node node = list.item(i);
            if ( node.getParentNode() == parent ) {
                return (Element) node;
            }
        }

        throw new RuntimeException("No rooted element found: " + name);
    }

}
