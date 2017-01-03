package org.seamcat.model.distributions;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;

import java.util.List;

public class UserDefinedDistributionImpl extends UserDistribution implements UserDefinedDistribution {

	public UserDefinedDistributionImpl(Function cdf) {
		super(cdf);
	}

    @Override
    public double trial() {
        Function cdf = getCdf();
        if ( cdf instanceof DiscreteFunction ) {
            return inverse( ((DiscreteFunction) cdf).points(), RandomAccessor.getRandom().nextDouble());
        }
        return 0;
    }

    private double inverse(List<Point2D> points, double rY) {
        double rX;
        if (points.size() == 0) {
            return 0;
        } else {
            Point2D _p, p = null;
            int i = 0;
            int size = points.size();
            do {
                _p = p;
                p = points.get(i);
            } while (++i < size && rY > p.getY());

            if (rY == p.getY()) {
                rX = p.getX();
            } else {
                if (_p.getY() == p.getY()) {
                    rX = 0;
                } else {
                    rX = _p.getX() + (rY - _p.getY()) / (p.getY() - _p.getY()) * (p.getX() - _p.getX());
                }
            }
        }
        return rX;
    }

	@Override
	public String toString() {
		return "User defined distribution";
	}
}
