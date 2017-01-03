package org.seamcat.loadsave;

import org.seamcat.marshalling.ScenarioOutlineModelMarshaller;
import org.seamcat.marshalling.WorkspaceResultMarshaller;
import org.seamcat.model.Workspace;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.scenario.WorkspaceScenario;

import javax.xml.stream.XMLStreamException;
import java.util.List;

public class ResultLoader {

    private XmlEventStream eventStream;

    public ResultLoader(XmlEventStream eventStream) {
        this.eventStream = eventStream;
    }

    public void readResultsFromXmlStream( final Workspace workspace ) throws XMLStreamException {
        eventStream.checkAndSkipStartElement("workspaceResults");
        WorkspaceScenario scenario = new WorkspaceScenario(workspace);

        eventStream.processWrappedElementSequence("iceConfigurations", "ICEConfiguration", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                workspace.addIceConfiguration(new ICEConfiguration(eventStream));
            }
        });

        eventStream.processOptionalElement("SimulationOutlinePlot", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                workspace.setScenarioOutlineModel( ScenarioOutlineModelMarshaller.fromXmlStream(eventStream) );
            }
        });

        List<SimulationResultGroup> preResults = WorkspaceResultMarshaller.loadFromXmlStream(eventStream, scenario, "PreResults", "PreResult");
        List<SimulationResultGroup> seamcatResults = WorkspaceResultMarshaller.loadFromXmlStream(eventStream, scenario, "SEAMCATResults", "SEAMCATResult");
        List<SimulationResultGroup> eppResults = WorkspaceResultMarshaller.loadFromXmlStream(eventStream, scenario, "EventProcessingResults", "EventProcessingResult");

        workspace.setSimulationResult(new SimulationResult(preResults, seamcatResults, eppResults));
        scenario.setPreSimulationResults( workspace );
        workspace.setScenario(scenario);

        eventStream.processOptionalElement("SimulationOutlinePlot", new ElementProcessor() {
            @Override
            public void process(XmlEventStream eventStream) throws XMLStreamException {
                workspace.setScenarioOutlineModel( ScenarioOutlineModelMarshaller.fromXmlStream(eventStream) );
            }
        });
        eventStream.checkAndSkipEndElement("workspaceResults");
    }
}
