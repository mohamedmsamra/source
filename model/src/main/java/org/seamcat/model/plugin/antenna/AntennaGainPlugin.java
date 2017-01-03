package org.seamcat.model.plugin.antenna;

import org.seamcat.model.plugin.Plugin;
import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.LinkResult;

public interface AntennaGainPlugin<T> extends Plugin<T> {

    /**
     * Calculates the precise gain from this antenna towards a specific set of
     * horizontal and vertical angles If no specific antenna patterns are defined
     * for this antenna, the peak gain is returned.
     *
     * @param link holds the general results for the links currently being evaluated
     * @param antenna holds the antenna results, specifically the azimuth and elevation,
     * @param peakGain value of the peak gain of the antenna
     * @param input plugin specific inputs
     * @return The precise gain in the 3 dimensional direction specified by given
     *         angles
     */
    double evaluate(LinkResult link, AntennaResult antenna, double peakGain, T input);
}
