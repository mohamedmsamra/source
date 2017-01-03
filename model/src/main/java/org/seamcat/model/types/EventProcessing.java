package org.seamcat.model.types;

import org.seamcat.model.Scenario;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.result.ResultTypes;

public interface EventProcessing<T> extends Configuration<T>, LibraryItem{

    ResultTypes evaluate( Scenario scenario, Iterable<EventResult> results);
}
