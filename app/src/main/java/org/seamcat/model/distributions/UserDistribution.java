package org.seamcat.model.distributions;

import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.Function;

/**
 * This class is aimed for the representation of a user distribution.
 */
public abstract class UserDistribution extends AbstractDistribution {

	private Function cdf;

	public UserDistribution(Function cdf) {
		super(0,0,0,0,0,0,0,0,0);
        this.cdf = cdf;
	}

	public Function getCdf() {
		return cdf;
	}

    @Override
    public Bounds getBounds() {
        return getCdf().getBounds();
    }
}
