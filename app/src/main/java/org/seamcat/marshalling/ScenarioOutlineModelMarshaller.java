package org.seamcat.marshalling;

import org.seamcat.loadsave.ElementProcessor;
import org.seamcat.loadsave.XmlEventStream;
import org.seamcat.model.core.ScenarioOutlineModel;
import org.seamcat.presentation.ExtendableXYSeries;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import java.util.ArrayList;
import java.util.List;

import static org.seamcat.util.XmlEventFactory.endElement;
import static org.seamcat.util.XmlEventFactory.startElement;

public class ScenarioOutlineModelMarshaller {

    private static final String ELEMENT_TAG = "SimulationOutlinePlot";
    private static final String ELEMENT_CHILD_TAG = ExtendableXYSeriesMarshaller.ELEMENT_TAG;

    public static ScenarioOutlineModel fromXmlStream(XmlEventStream eventStream) throws XMLStreamException {
        eventStream.checkAndSkipStartElement(ELEMENT_TAG);
        final List<ExtendableXYSeries> series = new ArrayList<ExtendableXYSeries>();
        eventStream.processElementSequence(ELEMENT_CHILD_TAG, new ElementProcessor() {

            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                series.add(ExtendableXYSeriesMarshaller.fromXmlStream(eventStream));
            }
        });
        eventStream.checkAndSkipEndElement(ELEMENT_TAG);
        return new ScenarioOutlineModel( series );
    }


    public static void toXmlStream(ScenarioOutlineModel model, XMLEventWriter eventWriter) throws XMLStreamException {
        eventWriter.add(startElement(ELEMENT_TAG));
        ExtendableXYSeriesMarshaller.toXmlStream(model.getItSeries(), eventWriter);
        ExtendableXYSeriesMarshaller.toXmlStream(model.getWtSeries(), eventWriter);
        ExtendableXYSeriesMarshaller.toXmlStream(model.getWrSeries(), eventWriter);
        ExtendableXYSeriesMarshaller.toXmlStream(model.getVrSeries(), eventWriter);
        eventWriter.add(endElement(ELEMENT_TAG));
    }
}
