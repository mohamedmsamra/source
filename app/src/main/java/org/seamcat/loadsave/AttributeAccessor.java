package org.seamcat.loadsave;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

public class AttributeAccessor {

	StartElement element;
	
	public AttributeAccessor(StartElement element) {
		this.element = element;
	}
	
	/** Returns the value of the attribute or null if the element does not have the attribute.
	 */
	public String value(String attributeName) {
		Attribute attribute = element.getAttributeByName(new QName(attributeName));
		return attribute != null ? attribute.getValue() : null;
	}

	/** Returns the value of the attribute or the defaultValue if the element does not have 
	 * the attribute.
	 */
	public String valueOrDefault(String attributeName, String defaultValue) {
		String value = value(attributeName);
		return value!=null ? value : defaultValue;
	}
}
