package org.seamcat.marshalling;

import org.jfree.data.xy.XYDataItem;
import org.seamcat.loadsave.AttributeAccessor;
import org.seamcat.loadsave.ElementProcessor;
import org.seamcat.loadsave.XmlEventStream;
import org.seamcat.presentation.Argument;
import org.seamcat.presentation.ExtendableXYSeries;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import java.util.List;

import static org.seamcat.util.XmlEventFactory.attribute;
import static org.seamcat.util.XmlEventFactory.endElement;
import static org.seamcat.util.XmlEventFactory.startElement;

public class ExtendableXYSeriesMarshaller {
    public static final String ELEMENT_TAG = "xyseries";
    private static final String ELEMENT_NAME_TAG = "name";
    private static final String ELEMENT_TYPE_TAG = "type";
    private static final String ELEMENT_DATA_TAG = "data";
    private static final String ELEMENT_X_TAG = "x";
    private static final String ELEMENT_Y_TAG = "y";
    private static final String ELEMENT_ARGUMENTS_TAG = "arguments";

    public static ExtendableXYSeries fromXmlStream(XmlEventStream eventStream) throws XMLStreamException {
        final ExtendableXYSeries result = new ExtendableXYSeries();
        StartElement element = eventStream.checkAndSkipStartElement(ELEMENT_TAG);
        AttributeAccessor attributes = new AttributeAccessor(element);

        result.setKey(attributes.value(ELEMENT_NAME_TAG));
        result.setType( attributes.value(ELEMENT_TYPE_TAG) );

        eventStream.processElementSequence(ELEMENT_DATA_TAG, new ElementProcessor() {

            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                addXYDataItem(result, eventStream);
            }
        });

        eventStream.checkAndSkipEndElement(ELEMENT_TAG);
        return result;
    }

    private static void addXYDataItem( final ExtendableXYSeries result, XmlEventStream eventStream) throws XMLStreamException {
        StartElement element = eventStream.checkAndSkipStartElement(ELEMENT_DATA_TAG);
        AttributeAccessor attributes = new AttributeAccessor(element);
        final double x = Double.parseDouble(attributes.value(ELEMENT_X_TAG));
        final double y = Double.parseDouble(attributes.value(ELEMENT_Y_TAG));

        result.add(x, y);

        eventStream.processWrappedElementSequence(ELEMENT_ARGUMENTS_TAG, ArgumentMarshaller.ELEMENT_TAG, new ElementProcessor() {

            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                result.add(x, y, ArgumentMarshaller.fromXmlStream(eventStream));
            }
        });
        eventStream.checkAndSkipEndElement(ELEMENT_DATA_TAG);
    }

    public static void toXmlStream(ExtendableXYSeries series, XMLEventWriter eventWriter) throws XMLStreamException {
        eventWriter.add(startElement(ELEMENT_TAG));
        eventWriter.add(attribute(ELEMENT_NAME_TAG, series.getKey().toString()));
        eventWriter.add(attribute(ELEMENT_TYPE_TAG, series.getType()));

        @SuppressWarnings("unchecked")
        List<XYDataItem> list = series.getItems();
        for (XYDataItem xyDataItem : list) {
            saveWrappedXYDataItemToXmlStream( series, eventWriter, ELEMENT_DATA_TAG, xyDataItem);
        }
        eventWriter.add(endElement(ELEMENT_TAG));
    }

    private static void saveWrappedXYDataItemToXmlStream( ExtendableXYSeries series, XMLEventWriter eventWriter, String containingElementName, XYDataItem xyDataItem) throws XMLStreamException {
        eventWriter.add(startElement(containingElementName));
        double x, y;
        x = xyDataItem.getXValue();
        y = xyDataItem.getYValue();
        eventWriter.add(attribute(ELEMENT_X_TAG, Double.toString(x)));
        eventWriter.add(attribute(ELEMENT_Y_TAG, Double.toString(y)));
        List<Argument> arguments = series.getArgsForPoint(x, y);

        // Add arguments
        eventWriter.add(startElement(ELEMENT_ARGUMENTS_TAG));
        if (arguments != null) {
            for (Argument argument : arguments) {
                ArgumentMarshaller.toXmlToStream( argument, eventWriter );
            }
        }
        eventWriter.add(endElement(ELEMENT_ARGUMENTS_TAG));

        eventWriter.add(endElement(containingElementName));

    }
}
