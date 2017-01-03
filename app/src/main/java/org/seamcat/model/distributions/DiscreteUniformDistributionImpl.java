package org.seamcat.model.distributions;

import org.seamcat.model.functions.Bounds;

public class DiscreteUniformDistributionImpl extends AbstractDistribution implements DiscreteUniformDistribution {

	private final UniformDistributionImpl u = new UniformDistributionImpl(0, 1);
	private final Bounds bounds;

	public DiscreteUniformDistributionImpl(double min, double max, double step, double stepShift) {
		super(0,max,0,0,0,min,0,step, stepShift);
		bounds = new Bounds(min, max, true);
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public double trial() {
		double rP = u.trial();
		double rMax = getMax();
		double rMin = getMin();
		double rStep = getStep();

		/** @todo: Check this - possible loss of precession! */
		int n = (int) ((rMax - rMin) / rStep);

		if ( getStepShift() == 0) {
            n += 1;
		}

		double rPi = 1d / n;

		int i;
		for (i = 0; i < n && rPi < rP; rPi += 1.0 / n, i++) ;
		return getStepShift() + rMin + i * rStep;
	}

	@Override
	public String toString() {
		return String.format("DiscreteUniformDistribution(%f, %f, %f)", getMin(), getMax(), getStep());
	}
}
