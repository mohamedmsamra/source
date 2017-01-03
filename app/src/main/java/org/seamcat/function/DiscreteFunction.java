package org.seamcat.function;

import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.FunctionException;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscreteFunction extends MutableLibraryItem implements Function, WithPoints {

	private List<Point2D> points = new ArrayList<Point2D>();
    private boolean isConstant;
    private double constant;

	public DiscreteFunction() {
        setPoints(new ArrayList<Point2D>());
    }

    public DiscreteFunction(List<Point2D> _points) {
        setPoints(_points);
    }

    public DiscreteFunction(double constant ) {
        setConstant(constant);
    }

    public void setPoints(List<Point2D> points){
        isConstant = false;
        this.points = points;
    }

    public void setConstant( double constant) {
        isConstant = true;
        this.constant = constant;
    }


    @Override
    public List<Point2D> points() {
        return points;
    }

	public void addPoint(double x, double y) {
		addPoint(new Point2D(x, y));
	}

	public final void addPoint(Point2D point) {
		points.add(point);
		sortPoints();
	}

	@Override
	public double evaluate(double rX) throws FunctionException {
        if ( isConstant ) return constant;
        int i, size = points.size();

        if (size == 0) {
            return 0.0;
        }

        double rLast = points.get(size - 1).getX();
        double rFirst = points.get(0).getX();
        if (rX > rLast || rX < rFirst) {
            throw new FunctionException("Specified value (" + rX + ") is outside bounds [" + rFirst + " to " + rLast + "]");
        }


        final Double _rX = rX;
        i = BinarySearch.search(points, new BinarySearch.Filter<Point2D>() {
            public boolean evaluate(Point2D point, int index) {
                return _rX > point.getX();
            }
        });

        if (i == 0) {
            return points.get(0).getY();
        } else if (rX == points.get(i).getX()) {
            return points.get(i).getY();
        } else {
            return Mathematics.linearInterpolate(rX, points.get(i - 1), points.get(i));
        }
	}

	@Override
	public double evaluateMax() {
        if ( isConstant ) return constant;

        double rFinalY = points.get(0).getY(), rY;
        for (Point2D p : points) {
            rY = p.getY();
            if (rY > rFinalY) {
                rFinalY = rY;
            }
        }
        return rFinalY;
	}

	@Override
	public double evaluateMin() {
		if ( isConstant ) return constant;
        double rFinalY = points.get(0).getY(), rY;

        for (Point2D p : points) {
            rY = p.getY();
            if (rY < rFinalY) {
                rFinalY = rY;
            }
        }
        return rFinalY;
	}

	@Override
	public boolean isConstant() {
		return isConstant;
	}

	public void sortPoints() {
        Collections.sort(points(), Point2D.POINTX_COMPARATOR);
	}

	@Override
	public String toString() {
        return pretty(this);
	}

    public static String pretty(Function function) {
        if (function.isConstant() ) {
            return "Constant (" + function.getConstant() + ")";
        }
        return "User defined function";
    }

    @Override
    public Bounds getBounds() {
        if ( isConstant ) {
            return new Bounds(Double.MIN_VALUE, Double.MAX_VALUE, false );
        }
        if ( points.size() == 0 ) {
            return new Bounds(0,0, true);
        }
        return new Bounds( points.get(0).getX(), points.get( points.size()-1).getX(), true);
    }

    @Override
    public DiscreteFunction offset(double offset) {
        if ( isConstant ) {
            return new DiscreteFunction(constant + offset);
        }
        List<Point2D> offsetPoints = new ArrayList<Point2D>();
        for (Point2D d : points) {
            offsetPoints.add(new Point2D(d.getX(), d.getY() + offset));
        }
        return new DiscreteFunction(offsetPoints);
    }

    @Override
    public double getConstant() {
        if ( isConstant ) {
            return constant;
        }
        throw new UnsupportedOperationException("Non constant function");
    }

    @Override
    public List<Point2D> getPoints() {
        if ( isConstant ) {
            throw new UnsupportedOperationException("No points on a constant function");
        }

        return Collections.unmodifiableList(points);
    }
}
