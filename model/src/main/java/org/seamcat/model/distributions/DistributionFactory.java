package org.seamcat.model.distributions;

import org.seamcat.model.functions.Function;

public interface DistributionFactory {

    ConstantDistribution getConstantDistribution(double constant);

    DiscreteUniformDistribution getDiscreteUniformDistribution(double min, double max, double step, double stepShift);

    GaussianDistribution getGaussianDistribution(double mean, double stdDev);

    RayleighDistribution getRayleighDistribution(double min, double stdDev);

    UniformDistribution getUniformDistribution(double min, double max);

    UniformPolarAngleDistribution getUniformPolarAngleDistribution(double maxAngle);

    UniformPolarDistanceDistribution getUniformPolarDistanceDistribution(double maxDistance);

    UserDefinedDistribution getUserDefined(Function cdf);

    StairDistribution getUserDefinedStair(Function cdf);
}
