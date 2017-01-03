package org.seamcat.model.plugin.eventprocessing;

import org.seamcat.model.Scenario;
import org.seamcat.model.plugin.Plugin;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.result.ResultTypes;

public interface EventProcessingPlugin<T> extends Plugin<T> {

    ResultTypes evaluate( Scenario scenario, Iterable<EventResult> results, T input );
}
