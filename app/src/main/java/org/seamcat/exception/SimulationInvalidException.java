package org.seamcat.exception;

/**
 * This exception is used to indicate that a simulation
 * is not valid and will therefore not produce any result.
 *
 */
public class SimulationInvalidException extends RuntimeException {

    private final String description;
    private RuntimeException origin;

    public SimulationInvalidException( String description, RuntimeException origin ) {
        this.description = description;
        this.origin = origin;
    }

    public String getDescription() {
        return description;
    }

    public RuntimeException getOrigin() {
        return origin;
    }
}
