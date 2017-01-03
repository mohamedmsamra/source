package org.seamcat.simulation.coverageradius;

import org.apache.log4j.Logger;
import org.seamcat.model.Scenario;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Validator;
import org.seamcat.model.plugin.coverageradius.CoverageRadiusPlugin;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.List;

public class TrafficLimitedNetworkCoverageRadius implements CoverageRadiusPlugin<TrafficLimitedNetworkCoverageRadius.Input> {

    private static final Logger LOG = Logger.getLogger(TrafficLimitedNetworkCoverageRadius.class);

    @Override
    public void consistencyCheck(Scenario scenario, List<Object> path, Input input, Validator<Input> validator) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double evaluate(GenericSystem genericSystem, Input input) {
        double rRmax = Math.sqrt(input.numberOfChannels() * input.numberOfUsers() / (Math.PI * input.density() * input.frequencyCluster()));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Transceiver name: " + genericSystem.toString());
            LOG.debug("Density = " + input.density());
            LOG.debug("NumberOfChannels = " + input.numberOfChannels());
            LOG.debug("Number of users per channel = " + input.numberOfUsers());
            LOG.debug("Frequency cluster = " + input.frequencyCluster());
            LOG.debug("Coverage Radius (Traffic Limited) = " + rRmax);
        }
        return rRmax;
    }

    public interface Input {
        @Config(order = 1, name = "Density", unit = "1/km²")
        double density();

        @Config(order = 2, name = "Number of channels")
        int numberOfChannels();

        @Config(order = 3, name = "Number of users per channel")
        int numberOfUsers();

        @Config(order = 4, name = "Frequency cluster")
        int frequencyCluster();
    }

    @Override
    public Description description() {
        return new DescriptionImpl("Traffic Limited",
                "<html>The coverage radius is derived from the traffic parameters <br>" +
                        "of the transmitter, i.e. the maximum number of active transmitter <br>" +
                        "per km2 (density), the number of frequency channels that a radio <br>" +
                        "system provides, the number of MS per frequency channel provided by <br>" +
                        "a radio system and the size of a group of frequency channels <br>" +
                        "(frequency cluster).</html>");
    }
}
