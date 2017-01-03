package org.seamcat.simulation;

import org.seamcat.model.distributions.UniformDistributionImpl;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.scenario.MutableLocalEnvironment;
import org.seamcat.simulation.result.MutableLocalEnvironmentResult;

import java.util.List;

public class LocalEnvironmentSelector {

    public static MutableLocalEnvironmentResult pickLocalEnvironment(List<? extends LocalEnvironment> environments ) {
        if ( environments.size() == 1 ) {
            return convert(environments.get(0));
        } else {
            double trial = new UniformDistributionImpl(0, 1).trial();
            double cum = 0;
            for (LocalEnvironment environment : environments) {
                cum += environment.getProbability();
                if ( trial < cum ) return convert( environment );
            }
        }
        // default response
        return convert( new MutableLocalEnvironment());
    }

    private static MutableLocalEnvironmentResult convert( LocalEnvironment environment ) {
        MutableLocalEnvironmentResult result = new MutableLocalEnvironmentResult();
        result.setEnvironment( environment.getEnvironment() );
        result.setWallLoss( environment.getWallLoss() );
        result.setWallLossStdDev( environment.getWallLossStdDev() );
        return result;
    }
}
