package org.seamcat.model.distributions;

import org.seamcat.model.functions.Bounds;


public class UniformPolarDistanceDistributionImpl extends AbstractDistribution implements UniformPolarDistanceDistribution  {

    private final Bounds bounds;
    private final UniformDistributionImpl u;

	public UniformPolarDistanceDistributionImpl(double maxDistance) {
		super(0,0,0,maxDistance,0,0,0,0,0);
        u = new UniformDistributionImpl(0, 1);
        bounds = new Bounds(0, maxDistance, true);
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

    @Override
	public String toString() {
		return "Uniform Polar Dist. Distri(" + this.getMaxDistance() + ")";
	}

	@Override
	public double trial() {
		return Math.sqrt(u.trial()) * getMaxDistance();
	}
}
