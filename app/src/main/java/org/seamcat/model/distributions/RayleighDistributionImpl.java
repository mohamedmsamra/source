package org.seamcat.model.distributions;

import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.functions.Bounds;

import java.util.Random;

public class RayleighDistributionImpl extends AbstractDistribution implements RayleighDistribution {

    private final Bounds bounds;

    public RayleighDistributionImpl(double min, double stddev) {
		super(0, 0, 0, 0, 0, min, stddev, 0, 0);
        bounds = new Bounds(0,0,false);
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public double trial() {
		double rS;
        Random random = RandomAccessor.getRandom();

        do {
            double rU = 2.0 * random.nextDouble() - 1.0;
			double rV = 2.0 * random.nextDouble() - 1.0;
			rS = rU * rU + rV * rV;
		} while (rS >= 1.0 || rS == 0.0);

		double rR0 = Math.sqrt(-2.0 * Math.log(rS));

		double rSigma = getStdDev() / Math.sqrt(2 - Math.PI / 2);
		double rR = getMin() + rSigma * rR0;
		return rR;
	}

    @Override
    public String toString() {
        return "RayleighDistribution(" + getMin() + ", " + getStdDev() + ")";
    }
}
