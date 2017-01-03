package org.seamcat.util;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;


/** Static helper methods for generating XMLEvents for use with StAX API.
 */
public class XmlEventFactory {

	public static XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
	
	public static StartDocument startDocument() {
		return xmlEventFactory.createStartDocument();
	}
	
	public static EndDocument endDocument() {
		return xmlEventFactory.createEndDocument();
	}
	
	public static StartElement startElement(String tagName) {
		return xmlEventFactory.createStartElement(new QName(tagName), null, null);
	}
	
	public static EndElement endElement(String tagName) {
		return xmlEventFactory.createEndElement(new QName(tagName), null);
	}
	
	public static Attribute attribute(String name, String value) {
		return xmlEventFactory.createAttribute(name, value);		
	}
	
	public static Characters characters(String text) {
		return xmlEventFactory.createCharacters(text);
	}
	
}
