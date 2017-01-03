package org.seamcat.function;

import org.apache.log4j.Logger;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.FunctionException;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.result.DescriptionImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmissionMaskImpl extends DiscreteFunction implements EmissionMask {

    private static final Logger LOG = Logger.getLogger(EmissionMaskImpl.class);

    private Map<Point2D, Double> mask = new HashMap<Point2D, Double>();

	public EmissionMaskImpl() {
        this(new ArrayList<Point2D>(), new ArrayList<Double>());
        setDescription(new DescriptionImpl("Spectrum Emission Mask", ""));
    }

	public EmissionMaskImpl(List<Point2D> points, List<Double> mask) {
        super(points);
        setDescription(new DescriptionImpl("Spectrum Emission Mask", ""));
        for (int i = 0; i < points.size(); i++) {
            Point2D point = points.get(i);
            this.mask.put(point, mask.get(i));
        }
	}

    @Override
    public boolean isConstant() {
        return false;
    }

    public void updatePoint(Point2D oldPoint, Point2D newPoint) {
        Double maskValue = mask.get(oldPoint);
        mask.remove( oldPoint );
        points().remove(oldPoint);
        addPoint(newPoint, maskValue);
    }

    public void addPoint( Point2D point, double mask) {
        addPoint(point);
        this.mask.put(point, mask);
    }

	public void addPoint(double x, double y) {
		addPoint(new Point2D(x, y), 0);
	}

	@Override
	public double integrate(double bandwithDifference, double referenceBandwith) throws FunctionException {
		if ( isConstant() ) {
            return 0;
        }

        // Round bandwidth difference to Hz precision
        bandwithDifference = (long) (bandwithDifference * 1000000.0) / 1000000.0;

        try {
            checkIntegration(bandwithDifference, referenceBandwith);
            return calculateIntegration(bandwithDifference, referenceBandwith);
        } catch (FunctionException e ) {
            return 0;
        }
	}

    private double calculateIntegration(double bandwithDifference, double referenceBandwith) {
        int i = 0, j = 0, size = points().size();
        double rXa = 0, rXb = 0;
        double rYaLin = 0, rYbLin = 0;
        double rYa = 0, rYb = 0;
        double rSum = 0.0;

        if (size < 2) {
            // throw new FunctionException();
            return 0;
        }

        final Double value = bandwithDifference - referenceBandwith * 0.5;
        i = BinarySearch.search(points(), new BinarySearch.Filter<Point2D>() {
            public boolean evaluate(Point2D point, int index) {
                return point.getX() <= value;
            }
        });

        rXa = bandwithDifference - referenceBandwith * 0.5;
        rYa = points().get(Math.max((i - 1), 0)).getY()
                + (points().get(i).getY() - points().get(Math.max((i - 1), 0)).getY())
                / (points().get(i).getX() - points().get(Math.max((i - 1), 0)).getX())
                * (rXa - points().get(Math.max((i - 1), 0)).getX());
        rYaLin = Math.pow(10.0, rYa / 10.0);
        boolean wasInsideForLoop = false;
        for (j = i; j < size && bandwithDifference + referenceBandwith * 0.5 >= points().get(j).getX(); j++) {
            rXb = points().get(j).getX();
            rYb = points().get(j).getY();
            rYbLin = Math.pow(10.0, rYb / 10.0);
            if (rYb == rYa) {
                rSum += (rXb - rXa) * rYaLin;
            } else {
                rSum += 10.0 / Math.log(10.0) * (rXb - rXa) / (rYb - rYa)
                        * (rYbLin - rYaLin);
            }
            rYa = rYb;
            rXa = rXb;
            rYaLin = rYbLin;
            wasInsideForLoop = true;
        }
        if (j == size) {
            j--;
        }
        if (bandwithDifference + referenceBandwith * 0.5 < points().get(j).getX() || !wasInsideForLoop) {
            rXb = bandwithDifference + referenceBandwith * 0.5;
            rYb = points().get(j - 1).getY()
                    + (points().get(j).getY() - points().get(j - 1).getY())
                    / (points().get(j).getX() - points().get(j - 1).getX())
                    * (rXb - points().get(j - 1).getX());
            rYbLin = Math.pow(10.0, rYb / 10.0);

            if (rYb == rYa) {
                rSum += (rXb - rXa) * rYaLin;
            } else {
                rSum += 10.0 / Math.log(10.0) * (rXb - rXa) / (rYb - rYa)
                        * (rYbLin - rYaLin);
            }
        }

        if (rSum != 0.0) {
            return Mathematics.linear2dB(rSum);
        } else {
            return rSum;
        }
    }

    private void checkIntegration(double bandwithDifference, double referenceBandwidth) throws FunctionException {
        int size = points().size();

        double v = (bandwithDifference - referenceBandwidth) * 0.5;
        double y = (bandwithDifference + referenceBandwidth) * 0.5;
        if (size == 0 || v < points().get(0).getX() || y > points().get(size - 1).getX()) {

            LOG.error("Emission mask is undefined at the begining  or at the end of the reception bandwidth:");
            LOG.error("("
                    + bandwithDifference
                    + " - "
                    + referenceBandwidth
                    + " * 0.5 < ("
                    + points().get(0).getX()
                    + ")) ||"
                    + " ("
                    + bandwithDifference
                    + " + "
                    + referenceBandwidth
                    + " * 0.5 > ("
                    + points().get(size - 1).getX()
                    + ")) -> "
                    + ((bandwithDifference - referenceBandwidth * 0.5 < points()
                    .get(0).getX())
                    + " || " + (bandwithDifference + referenceBandwidth
                    * 0.5 > points().get(size - 1).getX())));
            throw new FunctionException(
                    "Emission mask is undefined at the begining or at "
                            + "the end of the reception bandwidth");
        }
    }

    @Override
    public EmissionMask normalize() {
        if ( isConstant() ) {
            return this;
        }
        EmissionMaskImpl normalized = new EmissionMaskImpl();
        double rY, rZ;
        for (Point2D point : points()) {
            rZ = mask.get(point);
            rY = point.getY();
            rY -= Mathematics.linear2dB(rZ / 1000);
            normalized.addPoint(new Point2D(point.getX(), rY), 1000);
        }
        return normalized;
    }

    @Override
    public Double getMask(Point2D point) {
        return mask.get(point);
    }

    public void setMask(Point2D point, double maskValue) {
        mask.put( point, maskValue);
    }

    @Override
    public String toString() {
        return description().name();
    }
}