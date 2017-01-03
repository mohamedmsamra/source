package org.seamcat.model.functions;

public class Point3D extends Point2D {

	private double rZ;

	public Point3D(double rX, double rY, double rZ) {
		super(rX, rY);
		this.rZ = rZ;
	}

	public Point3D(Point2D p2d, double rZ) {
		super(p2d);
		this.rZ = rZ;
	}

	public double getRZ() {
		return rZ;
	}
}
