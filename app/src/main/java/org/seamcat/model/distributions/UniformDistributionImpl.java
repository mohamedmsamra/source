package org.seamcat.model.distributions;

import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.functions.Bounds;

public class UniformDistributionImpl extends AbstractDistribution implements UniformDistribution {

    private final Bounds bounds;
    private final double range;

    public UniformDistributionImpl(double min, double max) {
        super(33, max, 360, 1, 0, min, 0, 0.2,0);
        range = max - min;
        bounds = new Bounds(min, max, true);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

	@Override
	public double trial() {
		return RandomAccessor.getRandom().nextDouble() * range + getMin();
	}

    @Override
    public String toString() {
        return "UniformDistribution(" + getMin() + ", " + getMax() + ")";
    }
}
