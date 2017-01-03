package org.seamcat.model.simulation;

import org.seamcat.model.Scenario;
import org.seamcat.model.types.result.ResultTypes;

public class SimulationResultGroup {

    private String id;
    private String name;
    private Exception exception;
    private ResultTypes resultTypes;
    private Scenario scenario;

    public SimulationResultGroup(String name, ResultTypes resultTypes, Scenario scenario) {
        this("seamcatResult", name, resultTypes, scenario);
    }

    public SimulationResultGroup(String id, String name, ResultTypes resultTypes, Scenario scenario) {
        this.id = id;
        this.name = name;
        this.resultTypes = resultTypes;
        this.scenario = scenario;
    }

    public SimulationResultGroup(String name, Exception exception) {
        this.name = name;
        this.exception = exception;
    }

    public ResultTypes getResultTypes() {
        return resultTypes;
    }

    public void setResultTypes( ResultTypes resultTypes ) {
        this.resultTypes = resultTypes;
    }

    public String getName() {
        return name;
    }

    public boolean failed() {
        return exception != null;
    }

    public Exception getException(){
        return exception;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public String getId() {
        return id;
    }
}
