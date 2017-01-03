package org.seamcat.model.types;

import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.LinkResult;

public interface AntennaGain<T> extends Configuration<T>, LibraryItem {

    double evaluate(LinkResult linkResult, AntennaResult directionResult );

    double peakGain();
}

