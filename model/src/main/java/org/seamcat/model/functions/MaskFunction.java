package org.seamcat.model.functions;

public interface MaskFunction extends Function {

    double integrate(double rX, double rBvr) throws FunctionException;

    EmissionMask normalize();

    Double getMask( Point2D point );
}