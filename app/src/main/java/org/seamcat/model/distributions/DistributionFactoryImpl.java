package org.seamcat.model.distributions;

import org.seamcat.model.functions.Function;

public class DistributionFactoryImpl implements DistributionFactory  {

    @Override
    public ConstantDistributionImpl getConstantDistribution(double constant) {
        return new ConstantDistributionImpl(constant);
    }

    @Override
    public DiscreteUniformDistributionImpl getDiscreteUniformDistribution(double min, double max, double step, double stepShift) {
        return new DiscreteUniformDistributionImpl(min, max, step, stepShift);
    }

    @Override
    public GaussianDistributionImpl getGaussianDistribution(double mean, double stdDev) {
        return new GaussianDistributionImpl(mean, stdDev);
    }

    @Override
    public RayleighDistributionImpl getRayleighDistribution(double min, double stdDev) {
        return new RayleighDistributionImpl(min, stdDev);
    }

    @Override
    public UniformDistributionImpl getUniformDistribution(double min, double max) {
        return new UniformDistributionImpl(min, max);
    }

    @Override
    public UniformPolarAngleDistributionImpl getUniformPolarAngleDistribution(double maxAngle) {
        return new UniformPolarAngleDistributionImpl(maxAngle);
    }

    @Override
    public UniformPolarDistanceDistributionImpl getUniformPolarDistanceDistribution(double maxDistance) {
        return new UniformPolarDistanceDistributionImpl(maxDistance);
    }

    @Override
    public UserDefinedDistributionImpl getUserDefined(Function cdf) {
        return new UserDefinedDistributionImpl(cdf);
    }

    @Override
    public StairDistributionImpl getUserDefinedStair(Function stairFunction) {
        return new StairDistributionImpl(stairFunction);
    }
}
