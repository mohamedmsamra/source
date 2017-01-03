package org.seamcat.marshalling;

import org.seamcat.loadsave.AttributeAccessor;
import org.seamcat.loadsave.XmlEventStream;
import org.seamcat.presentation.Argument;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import static org.seamcat.util.XmlEventFactory.attribute;
import static org.seamcat.util.XmlEventFactory.endElement;
import static org.seamcat.util.XmlEventFactory.startElement;

public class ArgumentMarshaller {

    public static final String ELEMENT_TAG = "argument";
    private static final String ELEMENT_ARGUMENT_TITLE_TAG = "title";
    private static final String ELEMENT_ARGUMENT_VALUE_TAG = "value";
    private static final String ELEMENT_ARGUMENT_UNIT_TAG = "unit";

    public static Argument fromXmlStream(XmlEventStream eventStream) throws XMLStreamException {
        StartElement element = eventStream.checkAndSkipStartElement(ELEMENT_TAG);
        AttributeAccessor attributes = new AttributeAccessor(element);
        Argument result = new Argument(attributes.value(ELEMENT_ARGUMENT_TITLE_TAG), attributes.value(ELEMENT_ARGUMENT_VALUE_TAG), attributes.value(ELEMENT_ARGUMENT_UNIT_TAG));
        eventStream.checkAndSkipEndElement(ELEMENT_TAG);
        return result;
    }

    public static void toXmlToStream( Argument argument, XMLEventWriter eventWriter) throws XMLStreamException {
        if ( argument == null ) return;
        eventWriter.add(startElement(ELEMENT_TAG));
        eventWriter.add(attribute(ELEMENT_ARGUMENT_TITLE_TAG, argument.getTitle()));
        eventWriter.add(attribute(ELEMENT_ARGUMENT_VALUE_TAG, argument.getValue()));
        eventWriter.add(attribute(ELEMENT_ARGUMENT_UNIT_TAG, argument.getUnit()));
        eventWriter.add(endElement(ELEMENT_TAG));
    }
}
