package org.seamcat.model.distributions;

import org.seamcat.model.functions.Bounds;

public class ConstantDistributionImpl extends AbstractDistribution implements ConstantDistribution {

    private final Bounds bounds;

    public ConstantDistributionImpl(double constant) {
        super(constant, 0,0,0,0,0,0,0,0);
        bounds = new Bounds(constant, constant, true);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public String toString() {
        return "Constant(" + getConstant() + ")";
    }

    @Override
    public double trial() {
        return getConstant();
    }

}
