package org.seamcat.model.simulation.result;

import org.seamcat.model.functions.Point2D;

public interface SimulationElement {

    Point2D getPosition();

    double getAntennaHeight();

    double getAntennaTilt();

    double getExternalInterferenceBlocking();

    double getExternalInterferenceUnwanted();

    double getInterSystemInterference();

    double getTotalInterference();

    double getFrequency();

    double getReferenceBandwidth();

    double getExternalInterference();
}
