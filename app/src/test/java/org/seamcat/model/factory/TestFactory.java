package org.seamcat.model.factory;

import org.seamcat.plugin.AntennaGainFactoryImpl;
import org.seamcat.model.distributions.DistributionFactoryImpl;
import org.seamcat.plugin.PropagationModelFactoryImpl;

public class TestFactory {

    public static void initialize() {
        Factory.initialize(new DistributionFactoryImpl(), new PropagationModelFactoryImpl(),
                new BuildersImpl(), new AntennaGainFactoryImpl(), new FunctionFactoryImpl(), new DataExporterImpl());
    }
}
