package org.seamcat.model.generic;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Point2D;

public interface RelativeLocation {

    enum Shape {
        Octagon,
        Heptagon,
        Hexagon,
        Pentagon,
        Square,
        Triangle
    }

    boolean useCorrelatedDistance();

    Point2D getDeltaPosition();

   // Point2D setLocation(5,5);
    
    Distribution getPathAzimuth();

    Distribution getPathDistanceFactor();

    boolean usePolygon();

    Shape shape();

    Distribution turnCCW();

}
