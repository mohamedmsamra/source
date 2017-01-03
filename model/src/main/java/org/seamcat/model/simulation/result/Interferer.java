package org.seamcat.model.simulation.result;

import org.seamcat.model.Scenario;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.types.AntennaGain;

public interface Interferer {

	AntennaGain getAntennaGain();

	double getFrequency();

	boolean isUsingFixedGain();

	String getName();

	boolean isPathlossCorrelated();

	double getCorrelationFactor();

	double getPathlossVariance();

	double getMinimumCouplingLoss();

    LinkResult getLinkResult();

    Scenario getScenario();

    double getFixedGain();

    double getHorizontalAngle(Point2D p);

    double getElevation(double height, Point2D p);

    double getAntennaHeight();

    Point2D getPoint();

	void calculateLosses( Point2D victimPos, double victimHeight, InterferenceLinkResult link );
}
