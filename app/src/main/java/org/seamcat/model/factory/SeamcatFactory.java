package org.seamcat.model.factory;

import org.seamcat.model.distributions.DistributionFactoryImpl;
import org.seamcat.plugin.AntennaGainFactoryImpl;
import org.seamcat.plugin.PropagationModelFactoryImpl;

import java.util.Random;

public class SeamcatFactory {

    private static boolean seedFixed = false;
    private static long fixedSeed;
    private static final ThreadLocal<Random> threadLocalRandom = new ThreadLocal<>();

    public static Random getRandom() {
        if ( threadLocalRandom.get() == null ) {
            if ( seedFixed ) {
                fixSeed( fixedSeed );
            } else {
                threadLocalRandom.set( new Random());
            }
        }
        return threadLocalRandom.get();
    }

    public static void fixSeed( long seed ) {
        fixedSeed = seed;
        seedFixed = true;
        Random random = new Random(seed);
        long steps = Math.abs( seed % 37);
        for (int i=0; i<steps; i++) {
            random.nextDouble();
        }
        threadLocalRandom.set(random);
    }


    public static PropagationModelFactoryImpl propagation() {
        return new PropagationModelFactoryImpl();
    }

    public static DistributionFactoryImpl distributions() {
        return new DistributionFactoryImpl();
    }

    public static BuildersImpl builders() {
        return new BuildersImpl();
    }

    public static AntennaGainFactoryImpl antennaGain() {
        return new AntennaGainFactoryImpl();
    }

    public static FunctionFactoryImpl functions() {
        return new FunctionFactoryImpl();
    }

    public static DataExporterImpl dataExporter() {
        return new DataExporterImpl();
    }

}
