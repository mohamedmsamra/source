package org.seamcat.events;

import org.seamcat.exception.SimulationInvalidException;

import java.awt.*;

public class SimulationErrorEvent {

    private final Component component;
    private final SimulationInvalidException e;

    public SimulationErrorEvent(Component component, SimulationInvalidException e) {

        this.component = component;
        this.e = e;
    }

    public Component getComponent() {
        return component;
    }

    public SimulationInvalidException getE() {
        return e;
    }
}
