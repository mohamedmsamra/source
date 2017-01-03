package org.seamcat.marshalling;

import org.seamcat.loadsave.AttributeAccessor;
import org.seamcat.loadsave.ElementProcessor;
import org.seamcat.loadsave.XmlEventStream;
import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.types.result.*;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.util.XmlEventFactory.*;

public class WorkspaceResultMarshaller {

    public static void saveToXmlStream(XMLEventWriter eventWriter, List<SimulationResultGroup> results, String group, String name ) throws XMLStreamException {
        eventWriter.add(startElement(group));
        for (SimulationResultGroup result : results) {
            eventWriter.add(startElement(name));
            saveResultTypes(eventWriter, result);
            eventWriter.add(endElement(name));
        }
        eventWriter.add(endElement(group));
    }

    private static void saveResultTypes(XMLEventWriter eventWriter, SimulationResultGroup result) throws XMLStreamException {
        eventWriter.add(attribute("id", result.getId()));
        eventWriter.add(attribute("name", result.getName()));
        ResultTypes resultTypes = result.getResultTypes();
        if ( resultTypes != null ) {
            List<SingleValueTypes<?>> single = resultTypes.getSingleValueTypes();
            if ( single != null ) {
                eventWriter.add(startElement("SingleValues"));
                for (SingleValueTypes<?> valueTypes : single) {
                    if (valueTypes instanceof DoubleResultType) {
                        writeSingleValue(eventWriter, valueTypes, Double.toString((Double) valueTypes.getValue()), "double");
                    } else if ( valueTypes instanceof IntegerResultType ) {
                        writeSingleValue(eventWriter, valueTypes, Integer.toString((Integer) valueTypes.getValue()), "int");
                    } else if ( valueTypes instanceof StringResultType ) {
                        writeSingleValue(eventWriter, valueTypes, (String) valueTypes.getValue(), "string");
                    } else if ( valueTypes instanceof LongResultType) {
                        writeSingleValue(eventWriter, valueTypes, Long.toString( (Long) valueTypes.getValue()), "longInt");
                    }
                }
                eventWriter.add(endElement("SingleValues"));
            }
            List<VectorGroupResultType> vectorGroups = resultTypes.getVectorGroupResultTypes();
            if ( vectorGroups != null ) {
                eventWriter.add(startElement("VectorGroups"));
                for (VectorGroupResultType vectorGroup : vectorGroups) {
                    eventWriter.add(startElement("VectorGroup"));
                    eventWriter.add(attribute("name", vectorGroup.getName()));
                    eventWriter.add(attribute("unit", vectorGroup.getUnit()));
                    for (NamedVectorResult namedVector : vectorGroup.getVectorGroup()) {
                        eventWriter.add(startElement("NamedVector"));
                        eventWriter.add(attribute("name", namedVector.getName()));
                        writeVector(eventWriter, namedVector.getVector());
                        eventWriter.add(endElement("NamedVector"));
                    }
                    eventWriter.add(endElement("VectorGroup"));
                }
                eventWriter.add(endElement("VectorGroups"));
            }
            List<VectorResultType> vectors = resultTypes.getVectorResultTypes();
            if ( vectors != null ) {
                eventWriter.add(startElement("VectorValues"));
                for (VectorResultType vector : vectors) {
                    eventWriter.add(startElement("Vector"));
                    eventWriter.add(attribute("name", vector.getName()));
                    eventWriter.add(attribute("unit", vector.getUnit()));
                    writeVector(eventWriter, vector.getValue());
                    eventWriter.add(endElement("Vector"));
                }
                eventWriter.add(endElement("VectorValues"));
            }
            List<ScatterDiagramResultType> scatters = resultTypes.getScatterDiagramResultTypes();
            if ( scatters != null ) {
                eventWriter.add(startElement("ScatterPlots"));
                for (ScatterDiagramResultType scatter : scatters) {
                    eventWriter.add(startElement("Scatter"));
                    eventWriter.add(attribute("title", scatter.getTitle()));
                    eventWriter.add(attribute("xLabel", scatter.getxLabel()));
                    eventWriter.add(attribute("yLabel", scatter.getyLabel()));
                    writePoints(eventWriter, scatter.getScatterPoints());
                    eventWriter.add(endElement("Scatter"));
                }
                eventWriter.add(endElement("ScatterPlots"));
            }

            List<BarChartResultType> barCharts = resultTypes.getBarChartResultTypes();
            if ( barCharts != null ) {
                eventWriter.add(startElement("BarCharts"));
                for (BarChartResultType barChart : barCharts) {
                    eventWriter.add(startElement("BarChart"));
                    eventWriter.add(attribute("title", barChart.getTitle()));
                    eventWriter.add(attribute("xLabel", barChart.getxLabel()));
                    eventWriter.add(attribute("yLabel", barChart.getyLabel()));
                    writeValues(eventWriter, barChart.getChartPoints());
                    eventWriter.add(endElement("BarChart"));
                }
                eventWriter.add(endElement("BarCharts"));
            }
        }
    }

    private static void writeSingleValue(XMLEventWriter eventWriter, SingleValueTypes single, String value, String type) throws XMLStreamException {
        eventWriter.add(startElement("Single"));
        eventWriter.add(attribute("name", single.getName()));
        eventWriter.add(attribute("value", value));
        eventWriter.add(attribute("unit", single.getUnit()));
        eventWriter.add(attribute("type", type));
        eventWriter.add(endElement("Single"));
    }

    private static void writeVector(XMLEventWriter eventWriter, VectorResult vector) throws XMLStreamException {
        eventWriter.add(startElement("values"));
        for (double d : vector.asArray()) {
            eventWriter.add(startElement("value"));
            eventWriter.add(attribute("v", Double.toString(d)));
            eventWriter.add(endElement("value"));
        }
        eventWriter.add(endElement("values"));
    }

    private static void writePoints(XMLEventWriter eventWriter, List<Point2D> points ) throws XMLStreamException {
        eventWriter.add(startElement("points"));
        for (Point2D point : points) {
            eventWriter.add(startElement("point"));
            eventWriter.add(attribute("x", Double.toString(point.getX())));
            eventWriter.add(attribute("y", Double.toString(point.getY())));
            eventWriter.add(endElement("point"));
        }
        eventWriter.add(endElement("points"));
    }

    private static void writeValues(XMLEventWriter eventWriter, List<BarChartValue> values ) throws XMLStreamException {
        eventWriter.add(startElement("values"));
        for (BarChartValue value : values) {
            eventWriter.add(startElement("value"));
            eventWriter.add(attribute("name", value.getName()));
            eventWriter.add(attribute("value", Double.toString(value.getValue())));
            eventWriter.add(endElement("value"));
        }
        eventWriter.add(endElement("values"));
    }

    public static List<SimulationResultGroup> loadFromXmlStream(XmlEventStream eventStream, final Scenario scenario, final String group, final String name) throws XMLStreamException {
        final List<SimulationResultGroup> results = new ArrayList<SimulationResultGroup>();

        eventStream.processOptionalElement(group, new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                eventStream.checkAndSkipStartElement(group);
                eventStream.processElementSequence(name, new ElementProcessor() {
                    @Override
                    public void process(XmlEventStream eventStream) throws XMLStreamException {
                        StartElement element = eventStream.checkAndSkipStartElement(name);
                        String id = new AttributeAccessor(element).value("id");
                        loadResult(id, eventStream, element, scenario, results);
                        eventStream.checkAndSkipEndElement(name);
                    }
                });
                eventStream.checkAndSkipEndElement(group);
            }
        });
        return results;
    }

    private static void loadResult(String id, XmlEventStream eventStream, StartElement element, Scenario scenario, List<SimulationResultGroup> results) throws XMLStreamException {
        AttributeAccessor attributes = new AttributeAccessor(element);
        String name = attributes.value("name");
        final ResultTypes result = new ResultTypes();

        eventStream.processWrappedElementSequence("SingleValues", "Single", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                StartElement element = eventStream.checkAndSkipStartElement("Single");
                AttributeAccessor attributes = new AttributeAccessor(element);
                String type = attributes.value("type");
                if ( type.equals("double")) {
                    result.getSingleValueTypes().add(new DoubleResultType(attributes.value("name"), attributes.value("unit"), new Double(attributes.value("value"))));
                } else if ( type.equals("int")) {
                    result.getSingleValueTypes().add(new IntegerResultType(attributes.value("name"), attributes.value("unit"), new Integer(attributes.value("value"))));
                } else if ( type.equals("string")) {
                    result.getSingleValueTypes().add(new StringResultType(attributes.value("name"), attributes.value("value")));
                } else if ( type.equals("longInt")) {
                    result.getSingleValueTypes().add(new LongResultType(attributes.value("name"), attributes.value("unit"), new Long(attributes.value("value"))));
                }
                eventStream.checkAndSkipEndElement("Single");
            }
        });

        eventStream.processWrappedElementSequence("VectorGroups", "VectorGroup", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                StartElement element = eventStream.checkAndSkipStartElement("VectorGroup");
                AttributeAccessor accessor = new AttributeAccessor(element);
                final VectorGroupResultType vg = new VectorGroupResultType(accessor.value("name"), accessor.value("unit"));
                eventStream.processElementSequence("NamedVector", new ElementProcessor() {
                    @Override
                    public void process(XmlEventStream eventStream) throws XMLStreamException {
                        StartElement element = eventStream.checkAndSkipStartElement("NamedVector");
                        String name = new AttributeAccessor(element).value("name");
                        vg.getVectorGroup().add(new NamedVectorResult(name, new VectorResult(readVector(eventStream))));
                        eventStream.checkAndSkipEndElement("NamedVector");
                    }
                });
                result.getVectorGroupResultTypes().add( vg );
                eventStream.checkAndSkipEndElement("VectorGroup");
            }
        });

        eventStream.processWrappedElementSequence("VectorValues", "Vector", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                StartElement element = eventStream.checkAndSkipStartElement("Vector");
                AttributeAccessor accessor = new AttributeAccessor(element);
                result.getVectorResultTypes().add(new VectorResultType(accessor.value("name"), accessor.value("unit"), readVector(eventStream)));
                eventStream.checkAndSkipEndElement("Vector");
            }
        });
        eventStream.processWrappedElementSequence("ScatterPlots", "Scatter", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                StartElement element = eventStream.checkAndSkipStartElement("Scatter");
                AttributeAccessor attributes = new AttributeAccessor(element);
                ScatterDiagramResultType scatter = new ScatterDiagramResultType(attributes.value("title"), attributes.value("xLabel"), attributes.value("yLabel"));
                readPointList(eventStream, scatter.getScatterPoints());
                result.getScatterDiagramResultTypes().add( scatter );
                eventStream.checkAndSkipEndElement("Scatter");
            }
        });
        eventStream.processWrappedElementSequence("BarCharts", "BarChart", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                StartElement element = eventStream.checkAndSkipStartElement("BarChart");
                AttributeAccessor attributes = new AttributeAccessor(element);
                BarChartResultType barChart = new BarChartResultType(attributes.value("title"), attributes.value("xLabel"), attributes.value("yLabel"));
                readValues(eventStream, barChart.getChartPoints());
                result.getBarChartResultTypes().add(barChart);
                eventStream.checkAndSkipEndElement("BarChart");
            }
        });

        results.add(new SimulationResultGroup(id, name, result, scenario));
    }

    private static List<Double> readVector(XmlEventStream eventStream) throws XMLStreamException {
        final List<Double> vector = new ArrayList<>();
        eventStream.processWrappedElementSequence("values", "value", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                StartElement element = eventStream.checkAndSkipStartElement("value");
                vector.add(new Double(new AttributeAccessor(element).value("v")));
                eventStream.checkAndSkipEndElement("value");
            }
        });
        return vector;
    }

    private static void readPointList(XmlEventStream eventStream, final List<Point2D> target) throws XMLStreamException {
        eventStream.processWrappedElementSequence("points", "point", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                StartElement element = eventStream.checkAndSkipStartElement("point");
                AttributeAccessor accessor = new AttributeAccessor(element);
                target.add(new Point2D(Double.parseDouble(accessor.value("x")), Double.parseDouble(accessor.value("y"))));
                eventStream.checkAndSkipEndElement("point");
            }
        });
    }

    private static void readValues(XmlEventStream eventStream, final List<BarChartValue> target) throws XMLStreamException {
        eventStream.processWrappedElementSequence("values", "value", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                StartElement element = eventStream.checkAndSkipStartElement("value");
                AttributeAccessor accessor = new AttributeAccessor(element);
                target.add(new BarChartValue(accessor.value("name"), Double.parseDouble(accessor.value("value"))));
                eventStream.checkAndSkipEndElement("value");
            }
        });
    }

}
