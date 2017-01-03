package org.seamcat.model.functions;

import java.util.List;

public interface FunctionFactory {

    Function constantFunction( double value );

    Function discreteFunction( List<Point2D> points );

    EmissionMask emissionMask( List<Point2D> points, List<Double> mask );

    // maskFunction( List<Point2D> points, List<Double> mask );
}
