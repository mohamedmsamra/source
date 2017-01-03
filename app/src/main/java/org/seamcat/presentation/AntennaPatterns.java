package org.seamcat.presentation;


import org.seamcat.model.functions.Bounds;

public enum AntennaPatterns {
	HORIZONTAL,
	VERTICAL,
	SPHERICAL;

    private Bounds bounds;

    static {
        HORIZONTAL.bounds = new Bounds(0, 360, true);
        VERTICAL.bounds = new Bounds(-90, 90, true);
        SPHERICAL.bounds = new Bounds(0, 180, true);
    }

    public Bounds getBounds() {
        return bounds;
    }
}
