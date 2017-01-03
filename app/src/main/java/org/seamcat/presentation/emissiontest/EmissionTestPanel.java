package org.seamcat.presentation.emissiontest;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.plugin.Config;

public interface EmissionTestPanel {

    @Config(order = 1, name = "Frequency Difference", unit = "MHz")
    Distribution frequencyDiff();
    Distribution frequencyDiff = Factory.distributionFactory().getConstantDistribution(-0.025);

    @Config(order = 2, name = "VR Bandwidth", unit = "kHz")
    double bandwidth();
    double bandwidth = 25.0;

    @Config(order = 3, name = "Define Unwanted Function")
    EmissionMask mask();

    @Config(order = 4, name = "Number of samples")
    int samples();
    int samples = 1000;
}
