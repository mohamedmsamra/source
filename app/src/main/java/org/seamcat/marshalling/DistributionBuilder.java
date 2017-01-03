package org.seamcat.marshalling;

import org.seamcat.model.distributions.*;
import org.seamcat.model.functions.Function;

public class DistributionBuilder {

    private double constant;
    private double max;
    private double maxAngle;
    private double maxDistance;
    private double mean;
    private double min;
    private double stdDev;
    private double step;
    private double stepShift;
    private Function function;

    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setMaxAngle(double maxAngle) {
        this.maxAngle = maxAngle;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public double getStep() {
        return step;
    }


    public void setFunction(Function function) {
        this.function = function;
    }

    public void setStepShift(double stepShift) {
        this.stepShift = stepShift;
    }

    public AbstractDistribution build(int type) {
        switch (type) {
            case DistributionMarshaller.TYPE_CONSTANT: {
                return new ConstantDistributionImpl(constant);
            }
            case DistributionMarshaller.TYPE_UNIFORM: {
                return new UniformDistributionImpl(min, max);
            }
            case DistributionMarshaller.TYPE_GAUSSIAN: {
                return new GaussianDistributionImpl(mean, stdDev);
            }
            case DistributionMarshaller.TYPE_RAYLEIGH: {
                return new RayleighDistributionImpl(min, stdDev);
            }
            case DistributionMarshaller.TYPE_DISCRETE_UNIFORM: {
                return new DiscreteUniformDistributionImpl(min, max, step, stepShift);
            }
            case DistributionMarshaller.TYPE_UNIFORM_POLAR_ANGLE: {
                return new UniformPolarAngleDistributionImpl(maxAngle);
            }
            case DistributionMarshaller.TYPE_UNIFORM_POLAR_DISTANCE: {
                return new UniformPolarDistanceDistributionImpl(maxDistance);
            }
            case DistributionMarshaller.TYPE_USER_DEFINED: {
                return new UserDefinedDistributionImpl(function);
            }
            case DistributionMarshaller.TYPE_USER_DEFINED_STAIR: {
                return new StairDistributionImpl(function);
            }
        }
        throw new RuntimeException("Unknown type of Distribution: "+type );
    }
}
