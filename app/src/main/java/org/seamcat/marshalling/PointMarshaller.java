package org.seamcat.marshalling;

import org.seamcat.loadsave.AttributeAccessor;
import org.seamcat.loadsave.XmlEventStream;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.functions.Point3D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import static org.seamcat.util.XmlEventFactory.*;

public class PointMarshaller {

    public static Point3D fromElement3D( Element element ) {
        double x = Double.parseDouble(element.getAttribute("x"));
        double y = Double.parseDouble(element.getAttribute("y"));
        double z = Double.parseDouble(element.getAttribute("z"));
        return new Point3D(x, y, z );
    }

    public static Element toElement3D( Document document, Point3D point ) {
        Element element = document.createElement("point3d");
        element.setAttribute("x", String.valueOf(point.getX()));
        element.setAttribute("y", String.valueOf(point.getY()));
        element.setAttribute("z", String.valueOf(point.getRZ()));
        return element;
    }


    public static Point2D fromElement2D(Element element) {
        double x = Double.parseDouble(element.getAttribute("x"));
        double y = Double.parseDouble(element.getAttribute("y"));
        return new Point2D(x, y);
    }

    public static Point2D fromStream2D(XmlEventStream eventStream) throws XMLStreamException {
        StartElement element = eventStream.checkAndSkipStartElement("point2d");
        AttributeAccessor attributes = new AttributeAccessor(element);
        double x = Double.parseDouble(attributes.value("x"));
        double y = Double.parseDouble(attributes.value("y"));
        eventStream.checkAndSkipEndElement("point2d");
        return new Point2D(x,y);
    }

    public static Point3D fromStream3D(XmlEventStream eventStream) throws XMLStreamException {
        StartElement element = eventStream.checkAndSkipStartElement("point3d");
        AttributeAccessor attributes = new AttributeAccessor(element);
        double x = Double.parseDouble(attributes.value("x"));
        double y = Double.parseDouble(attributes.value("y"));
        double z = Double.parseDouble(attributes.value("z"));
        eventStream.checkAndSkipEndElement("point3d");
        return new Point3D(x,y,z);
    }


    public static Element toElement2D(Document document, Point2D point) {
        Element element = document.createElement("point2d");
        element.setAttribute("x", String.valueOf(point.getX()));
        element.setAttribute("y", String.valueOf(point.getY()));
        return element;
    }

    public static void saveToXmlStream(XMLEventWriter eventWriter, Point2D point) throws XMLStreamException {
        eventWriter.add(startElement("point2d"));
        eventWriter.add(attribute("x", String.valueOf(point.getX())));
        eventWriter.add(attribute("y", String.valueOf(point.getY())));
        eventWriter.add(endElement("point2d"));
    }

    public static void saveToXmlStream(XMLEventWriter eventWriter, Point3D point) throws XMLStreamException {
        eventWriter.add(startElement("point3d"));
        eventWriter.add(attribute("x", String.valueOf(point.getX())));
        eventWriter.add(attribute("y", String.valueOf(point.getY())));
        eventWriter.add(attribute("z", String.valueOf(point.getRZ())));
        eventWriter.add(endElement("point3d"));
    }

}
