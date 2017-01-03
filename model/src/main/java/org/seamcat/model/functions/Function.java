package org.seamcat.model.functions;

import java.util.List;

public interface Function {

    double evaluate(double rX) throws FunctionException;

    double evaluateMax();

    double evaluateMin();

    boolean isConstant();

    double getConstant();

    List<Point2D> getPoints();

    Bounds getBounds();

    Function offset( double offset );
}
