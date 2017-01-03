package org.seamcat.loadsave;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Stack;

/** Provides a higher level abstraction for working with input of Xml events
 *
 */
public class XmlEventStream {
	
	XMLEventReader eventReader;
    private boolean cancelled = false;
	
	public XmlEventStream(XMLEventReader eventReader) {
		this.eventReader = eventReader;
	}

	/** Skip to the first start or end element event, check that it is a start element event
	 * with the given name, and skip it. Returns the event.
	 * 
	 * @throws XMLStreamException if there is no more start or end elements, if the next start or 
	 * end element is not a start element or if the element name  does not match.
	 */
	public StartElement checkAndSkipStartElement(String elementName) throws XMLStreamException {
		skipNonElementEvents();
		XMLEvent event = eventReader.peek();
		if (event != null && event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(elementName)) {
			eventReader.nextEvent();
			return event.asStartElement();
		}
		else {
			throw new XMLStreamException("Expected start element: " + elementName + ", but found " + event);
		}
	}


    private boolean hasStartElement(String elementName) throws XMLStreamException {
        skipNonElementEvents();
        XMLEvent event = eventReader.peek();
        if (event != null && event.isStartElement() && event.asStartElement().getName().getLocalPart().equals(elementName)) {
            return true;
        }
        return false;
    }

	
	/** Skip to the first start or end element event, check that it is an end element event
	 * with the given name, and skip it. Returns the event.
	 * 
	 * @throws XMLStreamException if there is no more start or end elements, if the next start or 
	 * end element is not an end element or if the element name  does not match.
	 */
	public EndElement checkAndSkipEndElement(String elementName) throws XMLStreamException {
		skipNonElementEvents();
		XMLEvent event = eventReader.peek();
		if (event != null && event.isEndElement() && event.asEndElement().getName().getLocalPart().equals(elementName)) {
			eventReader.nextEvent();
			return event.asEndElement();
		}
		else {
			throw new XMLStreamException("Expected end element: " + elementName + ", but found " + event);
		}
	}
	
	/** For as long as the next element is a start element with the given name, calls the
	 * processor to process that element. The processor must consume events from the stream 
	 * up until and including the matching end element event.
	 * @throws XMLStreamException 
	 */
	public void processElementSequence(String childElementName, ElementProcessor processor) throws XMLStreamException {
		skipNonElementEvents();
		XMLEvent event = eventReader.peek();
		while (event != null 
				 && event.isStartElement() 
				 && event.asStartElement().getName().getLocalPart().equals(childElementName)) {
			cancelCheck();
            processor.process(this);
			skipNonElementEvents();
			event = eventReader.peek();
		}
	}

	private void skipNonElementEvents() throws XMLStreamException {
		while (eventReader.hasNext() && !eventReader.peek().isStartElement() && !eventReader.peek().isEndElement()) {
			eventReader.nextEvent();
		}
	}
	
	/** Works as processElementSequence except it also processes a containing start/end element pair. 
	 * For example, it will process events corresponding to <bar><foo></foo><foo></foo></bar>
	 * calling the processor for each <foo></foo> pair.
	 * @throws XMLStreamException 
	 */
	public void processWrappedElementSequence(String containerElementName, String childElementName, ElementProcessor processor) throws XMLStreamException {
		checkAndSkipStartElement(containerElementName);
		processElementSequence(childElementName, processor);
		checkAndSkipEndElement(containerElementName);
	}

	/** Processes an optional element, invoking the processor if  the element is found.
	 */
	public void processOptionalElement(String elementName, ElementProcessor processor) throws XMLStreamException {
		skipNonElementEvents();
		XMLEvent event = eventReader.peek();
		if (event != null 
			 && event.isStartElement() 
			 && event.asStartElement().getName().getLocalPart().equals(elementName)) {
			processor.process(this);
		}
	}

	/** Skips the element by skipping the start element event, all events corresponding to child nodes
	 * and the end element event.
	 * @throws XMLStreamException 
	 */
	public void skipElementAndSubTree(String elementName) throws XMLStreamException {
		StartElement startElement = checkAndSkipStartElement(elementName);
		Stack<String> elementNames = new Stack<String>();
		elementNames.push(startElement.getName().getLocalPart());
		while (!elementNames.empty()) {
			skipNonElementEvents();
			if (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent(); 
				if (event.isStartElement()) {
					elementNames.push(event.asStartElement().getName().getLocalPart());
				}
				else {
					String poppedName = elementNames.pop();
					if (!poppedName.equals(event.asEndElement().getName().getLocalPart())) {
						throw new XMLStreamException("Unexpected end element event: " + event.asEndElement().getName().getLocalPart());						
					}
				}
			}
			else {
				throw new XMLStreamException("Unexpected end of XML stream while skipping subtree for element: " + elementName);
			}
		}
	}

    public void cancel() {
        cancelled = true;
    }
    private void cancelCheck() {
        if ( cancelled ) {
            throw new Cancelled();
        }
    }

    public class Cancelled extends RuntimeException {

    }
}
