package org.seamcat.model.functions;

import java.util.Comparator;

public class Point2D extends java.awt.geom.Point2D implements
        Comparable<Point2D> {

	static final class PointComparator implements Comparator<Point2D> {

		public static final int X = 0;
		public static final int Y = 1;
		private int compareOn;

		public PointComparator(int compareOn) {
			this.compareOn = compareOn;
		}

		public final int compare(Point2D p1, Point2D p2) {
			int result;
			switch (compareOn) {
				case X: {
					if (p1.getX() == p2.getX()) {
						result = 0;
					} else if (p1.getX() > p2.getX()) {
						result = 1;
					} else {
						result = -1;
					}
					break;
				}
				case Y: {
					if (p1.getY() == p2.getY()) {
						result = 0;
					} else if (p1.getY() > p2.getY()) {
						result = 1;
					} else {
						result = -1;
					}
					break;
				}
				default: {
					throw new IllegalStateException("compareOn value out of range");
				}
			}
			return result;
		}
	}

	public static final Comparator<Point2D> POINTX_COMPARATOR = new PointComparator(
	      PointComparator.X);

	public static final Comparator<Point2D> POINTY_COMPARATOR = new PointComparator(
	      PointComparator.Y);
	
	private double rX;

	private double rY;

    public Point2D() {
        this(0d,0d);
    }

	public Point2D(double rX, double rY) {
		this.rX = rX;
		this.rY = rY;
	}

	public Point2D(Point2D p) {
		this(p.getX(), p.getY());
	}

	public int compareTo(Point2D pt) {
		return POINTX_COMPARATOR.compare(this, pt);
	}

	@Override
	public double getX() {
		return rX;
	}

	@Override
	public double getY() {
		return rY;
	}

	@Override
	public void setLocation(double x, double y) {
        throw new UnsupportedOperationException("Point is immutable");
	}

    public Point2D add( Point2D other ) {
        return new Point2D( rX + other.getX(), rY + other.getY() );
    }

    public Point2D subtract( Point2D other ) {
        return new Point2D( rX - other.getX(), rY - other.getY());
    }

    public Point2D transform( double x, double y ) {
        return new Point2D( rX + x, rY + y);
    }

	public Point2D scale( double factor ) {
        return new Point2D( factor * rX, factor * rY );
    }

    public Point2D transform(boolean add, double x, double y ) {
        return add ? transform(x, y) : transform(-x, -y);
    }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(').append(rX).append(", ").append(rY).append(')');
		return sb.toString();
	}
}