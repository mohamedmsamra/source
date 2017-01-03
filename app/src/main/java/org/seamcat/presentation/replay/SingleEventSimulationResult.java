package org.seamcat.presentation.replay;

import org.seamcat.model.Scenario;
import org.seamcat.model.Workspace;
import org.seamcat.model.simulation.result.EventResult;

import java.io.File;

public class SingleEventSimulationResult {

    private File logFile;
    private EventResult eventResult;
    private Scenario scenario;
    private Workspace simulatedWorkspace;

    public SingleEventSimulationResult( File logFile, EventResult eventResult, Scenario scenario, Workspace simulatedWorkspace ) {
        this.logFile = logFile;
        this.eventResult = eventResult;
        this.scenario = scenario;
        this.simulatedWorkspace = simulatedWorkspace;
    }

    public File getLogFile() {
        return logFile;
    }

    public EventResult getEventResult() {
        return eventResult;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public Workspace getSimulatedWorkspace() {
        return simulatedWorkspace;
    }
}

