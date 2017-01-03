package org.seamcat.model.types;

public interface LocalEnvironment {

    enum Environment {
        Indoor,
        Outdoor
    }

    Environment getEnvironment();

    double getWallLoss();

    double getWallLossStdDev();

    double getProbability();
}
