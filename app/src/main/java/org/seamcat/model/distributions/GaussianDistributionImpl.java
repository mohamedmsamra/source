package org.seamcat.model.distributions;

import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.functions.Bounds;

import java.util.Random;

public class GaussianDistributionImpl extends AbstractDistribution implements GaussianDistribution {

	private static final Bounds BOUNDS = new Bounds(0, 0, false);

	public GaussianDistributionImpl(double mean, double stdDev) {
		super(0,0,0,0,mean, 0,stdDev,0,0);
	}

	@Override
	public Bounds getBounds() {
		return BOUNDS;
	}

	@Override
	public double trial() {
		double rU, rV, rS;

        Random random = RandomAccessor.getRandom();
        do {
            rU = 2.0 * random.nextDouble() - 1.0;
			rV = 2.0 * random.nextDouble() - 1.0;
			rS = rU * rU + rV * rV;
		} while (rS >= 1.0 || rS == 0.0);

        return getMean() + getStdDev() * rU
              * Math.sqrt(-2.0 * Math.log(rS) / rS);
	}

    @Override
    public String toString() {
        return "Gaussian Distribution(" + getMean() + ", " + getStdDev() + ")";
    }
}
