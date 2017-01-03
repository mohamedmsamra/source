package org.seamcat.model.antenna;

import org.seamcat.model.Scenario;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.VoidInput;
import org.seamcat.model.plugin.antenna.AntennaGainPlugin;
import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class PeakGainAntenna implements AntennaGainPlugin<VoidInput> {

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, VoidInput input, Validator<VoidInput> validator) {
    }

    @Override
    public double evaluate(LinkResult link, AntennaResult antenna, double peakGain, VoidInput input) {
        return peakGain;
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Peak gain antenna",
                "<html>Regardless of the direction this always returns <br>the peak gain value specified</html>");
    }
}
