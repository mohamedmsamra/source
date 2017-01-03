package org.seamcat.model.distributions;

import org.seamcat.model.functions.Bounds;
import org.seamcat.model.mathematics.Mathematics;

public class UniformPolarAngleDistributionImpl extends AbstractDistribution implements UniformPolarAngleDistribution {

    private final Bounds bounds;

    public UniformPolarAngleDistributionImpl(double maxAngle) {
		super(0, 0, maxAngle, 0, 0, 0, 0, 0,0);
        bounds = new Bounds(-maxAngle, maxAngle, true);
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public double trial() {
		double rU = new UniformDistributionImpl(-1, 1).trial();
		double rA;

		// rA = 180 / Math.PI * Math.asin(rU * Math.sin(this.getMaxAngle() *
		// Math.PI / 180));
		rA = 180
		      / Math.PI
		      * Mathematics.asinD(rU
                * Mathematics.sinD(getMaxAngle() * Math.PI / 180));
		return rA;
	}

    @Override
    public String toString() {
        return "Uniform Polar Angle distribution(" + getMaxAngle() + ")";
    }
}
