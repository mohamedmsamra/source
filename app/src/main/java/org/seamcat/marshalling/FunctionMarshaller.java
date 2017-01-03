package org.seamcat.marshalling;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.loadsave.AttributeAccessor;
import org.seamcat.loadsave.ElementProcessor;
import org.seamcat.loadsave.XmlEventStream;
import org.seamcat.model.functions.*;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.List;

public class FunctionMarshaller {

    public static Element toElement( Document document, Function function ) {
        if ( function.isConstant() ) {
            Element element = document.createElement("ConstantFunction");
            element.setAttribute("value", Double.toString(function.getConstant()));
            return element;
        } else if ( function instanceof DiscreteFunction) {
            Element element = document.createElement("discretefunction");
            for (Point2D p : ((DiscreteFunction) function).points()) {
                element.appendChild(PointMarshaller.toElement2D(document, p));
            }
            return element;
        }

        throw new RuntimeException("Could not serialize function. "+function);
    }

    public static void toElement( Element element, Document document, MaskFunction function ) {
        EmissionMaskImpl mask = (EmissionMaskImpl) function;
        element.setAttribute( "reference", mask.description().name() );
        element.setAttribute( "description", mask.description().description() );
        List<Point2D> points = mask.points();
        for (Point2D point : points) {
            element.appendChild(PointMarshaller.toElement3D(document, new Point3D(point, function.getMask(point))));
        }
    }

    public static DiscreteFunction fromFunctionElement( Element element ) {
        NodeList nl = element.getElementsByTagName("ConstantFunction");
        if (nl.getLength() > 0) {
            double constant = Double.parseDouble(((Element) nl.item(0)).getAttribute("value"));
            return new DiscreteFunction( constant );
        } else if ((nl = element.getElementsByTagName("discretefunction"))
                .getLength() > 0) {

            Element function = (Element) nl.item(0);

            NodeList nodes = function.getElementsByTagName("point2d");
            int size = nodes.getLength();
            List<Point2D> _points = new ArrayList<Point2D>(size);
            if (size > 0) {
                for (int x = 0; x < size; x++) {
                    _points.add( PointMarshaller.fromElement2D((Element) nodes.item(x)));
                }
                return new DiscreteFunction(_points);
            }
            return new DiscreteFunction();
        }

        return null;
    }

    public static DiscreteFunction load( XmlEventStream eventStream ) throws XMLStreamException {

        try {
            StartElement constant = eventStream.checkAndSkipStartElement("ConstantFunction");
            DiscreteFunction function = new DiscreteFunction(Double.parseDouble(new AttributeAccessor(constant).value("value")));
            eventStream.checkAndSkipEndElement("ConstantFunction");
            return function;
        } catch (XMLStreamException e ) {
            // not a constant
        }
        eventStream.checkAndSkipStartElement("discretefunction");

        final List<Point2D> points = new ArrayList<Point2D>();
        eventStream.processElementSequence("point2d", new ElementProcessor() {
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                points.add(PointMarshaller.fromStream2D(eventStream));
            }
        });
        eventStream.checkAndSkipEndElement("discretefunction");
        return new DiscreteFunction(points);
    }

    public static EmissionMaskImpl fromElement( Element element) {
        EmissionMaskImpl function = new EmissionMaskImpl();
        String reference = element.getAttribute("reference");
        if ( reference.isEmpty() ) {
            reference = "Spectrum Emission Mask";
        }
        Description desc = new DescriptionImpl(reference, element.getAttribute("description"));
        function.setDescription( desc );
        NodeList nl = element.getElementsByTagName("point3d");
        for (int x = 0, size = nl.getLength(); x < size; x++) {
            Point3D point3D = PointMarshaller.fromElement3D((Element) nl.item(x));
            function.addPoint(point3D, point3D.getRZ());
        }
        return function;
    }


    public static DiscreteFunction copy( Function function ) {
        if ( function == null ) return null;

        if (function.isConstant()) {
            return new DiscreteFunction(function.getConstant());
        } else if (function instanceof DiscreteFunction) {
            DiscreteFunction newFunc = new DiscreteFunction();
            for (Point2D point : ((DiscreteFunction) function).points()) {
                newFunc.addPoint(new Point2D(point));
            }
            return newFunc;
        }

        return null;
    }

    public static <T extends EmissionMask> T copy(EmissionMask orig) {
        if ( orig == null ) return null;
        if (orig instanceof EmissionMaskImpl) {
            EmissionMaskImpl result = new EmissionMaskImpl();
            List<Point2D> points = ((EmissionMaskImpl) orig).points();
            for (Point2D point : points) {
                result.addPoint(point, ((EmissionMaskImpl) orig).getMask(point));
            }
            return (T) result;
        }

        return null;
    }
}
