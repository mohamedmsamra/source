package org.seamcat.model.engines;

import org.seamcat.model.simulation.InterferenceLinkSimulation;
import org.seamcat.model.simulation.VictimSystemSimulation;
import org.seamcat.model.simulation.result.EventResult;

import java.util.List;

public interface SimulationListener {

    void preSimulate(int totalEvents);

    void eventComplete( EventResult eventResult, VictimSystemSimulation victimSimulation, List<InterferenceLinkSimulation> interferenceSimulations);

    void postSimulate();
}
