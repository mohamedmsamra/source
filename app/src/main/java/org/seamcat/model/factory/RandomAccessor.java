package org.seamcat.model.factory;

import java.util.Random;

public class RandomAccessor {

    public static Random getRandom() {
        return SeamcatFactory.getRandom();
    }

    public static void fixSeed( long seed ) {
        SeamcatFactory.fixSeed( seed );
    }
}
