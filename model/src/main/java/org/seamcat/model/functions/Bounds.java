package org.seamcat.model.functions;

public class Bounds {

	private boolean bounded;
	private double max;
	private double min;

	public Bounds(double min, double max, boolean bounded) {
		this.max = max;
		this.min = min;
		this.bounded = bounded;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public boolean isBounded() {
		return bounded;
	}

    public boolean contains( double value ) {
        if ( bounded ) {
            return value >= min && value <= max;
        }
        return true;
    }

}
