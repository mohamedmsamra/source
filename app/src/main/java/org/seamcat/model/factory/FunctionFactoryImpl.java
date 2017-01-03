package org.seamcat.model.factory;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.FunctionFactory;
import org.seamcat.model.functions.Point2D;

import java.util.List;

public class FunctionFactoryImpl implements FunctionFactory  {

    @Override
    public Function constantFunction(double value) {
        return new DiscreteFunction(value);
    }

    @Override
    public Function discreteFunction(List<Point2D> points) {
        return new DiscreteFunction(points);
    }

    @Override
    public EmissionMask emissionMask(List<Point2D> points, List<Double> mask) {
        return new EmissionMaskImpl(points, mask);
    }
}
