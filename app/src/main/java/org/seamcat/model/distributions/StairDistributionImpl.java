package org.seamcat.model.distributions;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import java.util.Collections;
import java.util.List;

public class StairDistributionImpl extends UserDistribution implements StairDistribution {

	public StairDistributionImpl(Function function) {
		super(function);
        // ensure function points are sorted by the Y point
        DiscreteFunction cdf = (DiscreteFunction) getCdf();
        Collections.sort(cdf.points(), Point2D.POINTY_COMPARATOR);
    }

	@Override
	public double trial() {
        Function cdf = getCdf();
        if ( cdf instanceof DiscreteFunction) {
            return findLowestXWithHigherValue(((DiscreteFunction) cdf).points(), RandomAccessor.getRandom().nextDouble());
        }
        return 0;
	}

    private double findLowestXWithHigherValue(List<Point2D> points, double value) {
        int i = 0;
        for (; i < points.size() && points.get(i).getY() <= value; i++) {
        }

        if (i >= points.size()) {
            i = points.size() - 1;
        }
        return points.get(i).getX();
    }

	public boolean validate() {
        DiscreteFunction cdf = (DiscreteFunction) getCdf();
        final List<Point2D> points = cdf.points();
        for (Point2D point : points) {
            if (point.getY() < 0 || point.getY() > 1) return false;
        }

        return Mathematics.equals(points.get(points.size() - 1).getY(), 1, 0.0001 );
    }

    @Override
    public String toString() {
        return "User defined stair distribution";
    }
}
