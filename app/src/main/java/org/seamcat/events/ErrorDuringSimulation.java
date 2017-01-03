package org.seamcat.events;

public class ErrorDuringSimulation extends ContextEvent {

    private Exception simulationException;
    private String message;

    public ErrorDuringSimulation(Exception simulationException, String message, Object context) {
        super(context);
        this.simulationException = simulationException;
        this.message = message;
    }

    public Exception getSimulationException() {
        return simulationException;
    }

    public String getMessage() {
        return message;
    }
}
