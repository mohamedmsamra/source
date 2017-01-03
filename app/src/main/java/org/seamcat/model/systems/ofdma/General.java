package org.seamcat.model.systems.ofdma;

import org.seamcat.Seamcat;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.plugin.Config;

public interface General {

	//int x=900000;
    @Config(order = 1, name = "Default Frequency", unit = "MHz", information = "SystemModelFrequency")
    Distribution frequency();
    Distribution frequency = Factory.distributionFactory().getConstantDistribution(Seamcat.y);
    
   
}
