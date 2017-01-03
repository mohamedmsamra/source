package org.seamcat.model.simulation.result;

import org.seamcat.model.functions.Point2D;

public interface AntennaResult {

    double getGain();

    /**
     * Azimuth of the antenna in degrees in the range
     * [0-360]
     */
    double getAzimuth();

    /**
     * Elevation of the antenna in degrees in the
     * range [-90;90]
     */
    double getElevation();

    /**
     * Used for compensating the lack of angle information at the victim receiver or interfering transmitter
     * when the antenna are pointing at each other.
     *
     * It is equivalent to the Elevation of the receiver antenna in the transmitter direction or
     * to the Elevation of the transmitter antenna in the receiver direction.
     */
    double getElevationCompensation();

    double getTilt();

    double getHeight();

    Point2D getPosition();

    LocalEnvironmentResult getLocalEnvironment();
}
