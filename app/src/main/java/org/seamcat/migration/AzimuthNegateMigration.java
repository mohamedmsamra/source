package org.seamcat.migration;

import java.util.List;

import org.apache.commons.jxpath.JXPathContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

abstract public class AzimuthNegateMigration implements FileMigration {

	protected void fixAzimuth(Document document) {
		JXPathContext context = JXPathContext.newContext(document);
		List azimuths = context.selectNodes("//azimuth/distribution");

		for (Object o : azimuths) {
			migrateAzimuth((Element) o);
		}
	}

	private void migrateAzimuth(Element azimuth ) {
		negateAttribute( azimuth, "constant" );
		negateAttribute( azimuth, "max" );
		negateAttribute( azimuth, "max-angle" );
		negateAttribute( azimuth, "max-distance" );
		negateAttribute( azimuth, "mean" );
		negateAttribute( azimuth, "min" );
		negateAttribute( azimuth, "step" );

		int type = Integer.parseInt(azimuth.getAttribute("type"));
		if ( type == 1 || type == 7 ) {
			NodeList point2d = azimuth.getElementsByTagName("point2d");
			for ( int i=0; i<point2d.getLength(); i++ ) {
				Element point = (Element) point2d.item(i);
				negateAttribute( point, "x" );
			}
		}
	}

	private void negateAttribute( Element element, String name ) {
		element.setAttribute( name, negate( element.getAttribute(name) ) );
	}

	private String negate( String original ) {
		double v = Double.parseDouble(original);
		return Double.toString( -v );
	}

}
