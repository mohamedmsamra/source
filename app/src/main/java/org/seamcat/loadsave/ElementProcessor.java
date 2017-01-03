package org.seamcat.loadsave;

import javax.xml.stream.XMLStreamException;

public interface ElementProcessor {
	
	/** Process and consume one element up to and including its matching end element.
	 */
	public void process(XmlEventStream eventStream) throws XMLStreamException;
}
