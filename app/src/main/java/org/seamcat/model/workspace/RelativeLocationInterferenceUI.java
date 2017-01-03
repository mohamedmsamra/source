package org.seamcat.model.workspace;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.generic.RelativeLocation;
import org.seamcat.model.plugin.Config;

public interface RelativeLocationInterferenceUI {

    @Config(order = 1, name = "Mode")
    InterferingLinkRelativePosition.CorrelationMode mode();
    InterferingLinkRelativePosition.CorrelationMode mode = InterferingLinkRelativePosition.CorrelationMode.VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR;

    @Config(order = 3, name = "Delta x")
    Distribution deltaX();

    @Config(order = 4, name = "Delta y")
    Distribution deltaY();

    @Config(order = 5, name = "ILR center of ILT distribution")
    boolean setILRatTheCenter();

    @Config(order = 6, name = "Path azimuth")
    Distribution pathAzimuth();
    Distribution pathAzimuth = Factory.distributionFactory().getUniformDistribution(0, 360);

    @Config(order = 7, name = "Path distance factor")
    Distribution pathDistanceFactor();
    Distribution pathDistanceFactor = Factory.distributionFactory().getUniformPolarDistanceDistribution(1.0);

    @Config(order = 8, name = "Simulation radius")
    double simulationRadius();
    double simulationRadius = 1.0;

    @Config(order = 9, name = "Number of active Tx")
    int numberOfActiveTransmitters();
    int numberOfActiveTransmitters = 1;

    @Config(order = 10, name = "Colocated")
    boolean isCoLocated();

    @Config(order = 11, name = "Colocated with")
    String coLocatedWith();

    @Config(order = 12, name = "Colocation x")
    Distribution coLocationX();

    @Config(order = 13, name = "colocation y")
    Distribution coLocationY();

    @Config(order = 14, name = "Minimum coupling loss")
    Distribution minimumCouplingLoss();

    @Config(order = 15, name = "Protection distance")
    Distribution protectionDistance();

    @Config(order = 16, name = "Use a polygon")
    boolean usePolygon();

    @Config(order = 17, name = "Shape of the polygon")
    RelativeLocation.Shape shape();
    RelativeLocation.Shape shape = RelativeLocation.Shape.Hexagon;

    @Config(order = 18, name = "Turn ccw")
    Distribution turnCCW();

}
