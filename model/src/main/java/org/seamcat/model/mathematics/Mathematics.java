package org.seamcat.model.mathematics;

import org.seamcat.model.functions.Point2D;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;

/**
 * This class contains the needed functions not included in the J2SE
 * standard Math-class
 */
public class Mathematics {

    public static final int KM_TO_METERS = 1000;

    public static final double BPS_TO_KBPS = 1000;
    public static final double MHZ_TO_HZ = 1000000;

    public static final double DEGRAD = PI / 180.0;
	public static final double RADEG = 180.0 / PI;

	public static final double DEFAULT_DELOG = -1000;
	public static final double DEFAULT_LOG_START_RANGE = 1E-4;

    /**
     * Calculates the distance between two points
     * @param from
     * @param to
     * @return distance
     */
    public static double distance( Point2D from, Point2D to ) {
        return Math.sqrt((from.getX() - to.getX()) * (from.getX() - to.getX()) + (from.getY() - to.getY()) * (from.getY() - to.getY()));
    }

    /**
     * Calculates the distance from (0,0) to the given point
     * @param to
     * @return distance
     */
    public static double distance( Point2D to ) {
        return Math.sqrt((to.getX() * to.getX()) + (to.getY() * to.getY()));
    }


    /**
     * @deprecated Use dB2Linear instead
     */
    @Deprecated
    public static double delogaritmize(double value) {
        return pow(10, value / 10);
    }

    /**
     * @deprecated Use linear2dB instead
     */
    @Deprecated
    public static double fromLinearTodB(double value) {
        if (value == 0) {
            return DEFAULT_DELOG;
        }
        return 10 * log10(value);
    }

    public static double dB2Linear(double value) {
		return pow(10, value / 10);
	}

    public static double linear2dB(double value) {
        if (value == 0) {
            return DEFAULT_DELOG;
        }
        return 10 * log10(value);
    }

	public static double fromdBm2Watt(double dbm) {
		return pow(10, (dbm - 30) / 10);
	}

	public static double fromWatt2dBm(double watt) {
		if (watt == 0) {
			return DEFAULT_DELOG;
		}
		return (10 * log10(watt)) + 30;
	}

	/**
	 * Get the smallest number that can be used a range start for the logarithmic
	 * graphs
	 * 
	 * @param minimumDistribution
	 *           the smallest distribution
	 * @param minRange
	 *           the range minimum
	 */
	public static double getMinimumLogDomainValue(double minimumDistribution,
			double minRange) {
		double returnValue;
		if (minRange == 0) {
			returnValue = (minimumDistribution > DEFAULT_LOG_START_RANGE) ? DEFAULT_LOG_START_RANGE
					: minimumDistribution;
		} else {
			returnValue = minRange;
		}
		return returnValue;
	}

	public static double powerSummation(double... powers) {
		double total = 0;
		for (double power : powers) {
			if (power != 0.0) {
				total += pow(10, (power - 30) / 10);
			}
		}
		if (total == 0) {
			return DEFAULT_DELOG;
		}
		return (10 * log10(total)) + 30;
	}

    public static double powerSubtract( double value, double sub ) {
        return (10 * log10( pow(10, (value - 30)/10) - pow(10, (sub - 30)/10) )) + 30;
    }

	/**
	 * Rounds a double to 3 digits
	 * 
	 * @param d
	 */
	public static double round(double d) {
		if (d == 0) {
			return 0.0d;
		}
		return rint(d * 1000) / 1000;

	}

	/**
	 * Returns the arc cosine of an angle, in the range of 0.0 through pi.
	 * 
	 * @param angle
	 *           given in degrees
	 * @return The arc cosine of an angle given in degrees, in the range of 0.0
	 *         through pi.
	 */
	public static double acosD(double angle) {
		return acos(angle) * RADEG;
	}

	/**
	 * Returns the arc sine of an angle, in the range of -pi/2 through pi/2.
	 * 
	 * @param x
	 *           angle given in degrees
	 * @return The arc sine of an angle given in degrees, in the range of -pi/2
	 *         through pi/2.
	 */
	public static double asinD(double x) {
		return asin(x) * RADEG;
	}

	/**
	 * Converts rectangular coordinates (x, y) to polar (r, theta).
	 * @return The arc tangent of an angle in degrees specified by rectangular
	 *         coordinates.
	 */
	public static double atan2D(double x, double y) {
		return atan2(y, x) * RADEG;
	}

	/**
	 * Finds the angle between two points
	 */
	public static double angle(double x1, double y1, double x2, double y2) {
		return atan2D(x2 - x1, y2 - y1);
	}

	/**
	 * Returns the arc tangent of an angle, in the range of -pi/2 through pi/2.
	 * 
	 * @param angle
	 *           given in degrees
	 * @return The arc tangent of an angle given degrees, in the range of -pi/2
	 *         through pi/2.
	 */
	public static double atanD(double angle) {
		return RADEG * atan(angle);
	}

	/**
	 * Returns the trigonometric cosine of an angle
	 * 
	 * @param angle
	 *           in degrees
	 * @return The trigonometric cosine of an angle in degrees
	 */
	public static double cosD(double angle) {
		return cos(angle * DEGRAD);
	}

	/**
	 * Hyberbolic Cosine function
	 * 
	 * @param x
	 * @return The hyberbolic cosine value of given argument
	 */
	public static double cosh(double x) {
		return (exp(x) + exp(-x)) / 2;
	}

	/**
	 * Return average value of an array of <code>double</code> values.
	 * 
	 * @param p
	 *           Array of double values
	 * @return The average value of array range from 0 - p.length
	 */
	public static double getAverage(double[] p) {
		return getAverage(p, p.length);
	}

	public static double getAverage(double[] p, int length) {
		return getAverage(p, length, false);
	}

	/**
	 * Return average value of an array of <code>double</code> values, for
	 * <code>length</code> length of array.
	 * 
	 * @param p
	 *           Array of double values
	 * @param length
	 *           The number of array entries to average over
	 * @return The average value of array range from 0 - <code>length</code>
	 */
	public static double getAverage(double[] p, int length, boolean ignoreZeros) {
		double sum = 0;
		int len = 0;
		for (int i = 0; (i < length) && (i < p.length); i++) {
			double n = p[i];
			if (!ignoreZeros || (n != 0d)) {
				sum += n;
				len++;
			}
		}
		return len > 0 ? sum / len : 0;
	}

	public static double getMedian(double[] p, int length, boolean ignoreZeros) {
		if (ignoreZeros) {
			p = stripZeros(p);
		}
        if ( length < 0 ) return 0;
		length = Math.min(length, p.length);
		double median;
		if (length == 0) {
			median = 0;
		} else if (length == 1) {
			median = p[0];
		} else {
			//TODO out of memory for large simulations
            double[] sorted = new double[length];
			System.arraycopy(p, 0, sorted, 0, sorted.length);
			Arrays.sort(sorted);
			// If even
			if ((length % 2) == 0) {
				int index = (length / 2);
				median = fromWatt2dBm((fromdBm2Watt(sorted[index]) + fromdBm2Watt(sorted[index - 1])) / 2);
			}
			// Else odd
			else {
				median = sorted[(length / 2)];
			}
		}
		return median;
	}

	public static double[] stripZeros(double[] array) {
		double[] target = new double[array.length];
		int len = 0;
		for (int x = 0; x < target.length; x++) {
			double n = array[x];
			if (n != 0) {
				target[len++] = n;
			}
		}

		double[] strippedArray;
		if (len == target.length) {
			strippedArray = target;
		} else {
			strippedArray = new double[len];
			System.arraycopy(target, 0, strippedArray, 0, strippedArray.length);
		}
		return strippedArray;
	}

	/**
	 * Return average value of an array of <code>double</code> values, for
	 * <code>length</code> length of array.
	 * 
	 * @param p
	 *           Array of double values
	 * @param length
	 *           The number of array entries to average over
	 * @return The average value of array range from 0 - <code>length</code>
	 */
	public static double getAverage(double[] p, int length, double minValue,
			double maxValue) {
		return getAverage(p, length, minValue, maxValue, false);
	}

	/**
	 * Return average value of an array of <code>double</code> values, for
	 * <code>length</code> length of array.
	 * 
	 * @param p
	 *           Array of double values
	 * @param length
	 *           The number of array entries to average over
	 * @return The average value of array range from 0 - <code>length</code>
	 */
	public static double getAverage(double[] p, int length, double minValue,
			double maxValue, boolean ignoreZeros) {
		double sum = 0;
		int count = 0;
		for (int i = 0; (i < length) && (i < p.length); i++) {
			double a = p[i];
			if ((a > minValue) && (a < maxValue) && (!ignoreZeros || (a != 0d))) {
				sum += a;
				count++;
			}
		}
		return count > 0 ? sum / count : 0;
	}

	/**
	 * Return Standard Deviation of array
	 * 
	 * @param p
	 *           Array of double values
	 * @return getStdDev(p,ave,p.length)
	 */
	public static double getStdDev(double[] p) {
		return getStdDev(p, Mathematics.getAverage(p), p.length);
	}

	/**
	 * Return Standard Deviation of array with given average.
	 * 
	 * @param p
	 *           Array of double values
	 * @param ave
	 *           Average value of p
	 * @return getStdDev(p,ave,p.length)
	 */
	public static double getStdDev(double[] p, double ave) {
		return Mathematics.getStdDev(p, ave, p.length);
	}

	/**
	 * Return Standard Deviation of array with given average and length
	 * 
	 * @param p
	 *           Array of double values
	 * @param ave
	 *           Average value of p
	 * @param length
	 *           The number of array which has been averaged over
	 * @return Standard deviation of array values ranging from 0 - length
	 */
	public static double getStdDev(double[] p, double ave, int length,
			double minValue, double maxValue) {
		return getStdDev(p, ave, length, minValue, maxValue, false);
	}

	/**
	 * Return Standard Deviation of array with given average and length
	 * 
	 * @param p
	 *           Array of double values
	 * @param ave
	 *           Average value of p
	 * @param length
	 *           The number of array which has been averaged over
	 * @return Standard deviation of array values ranging from 0 - length
	 */
	public static double getStdDev(double[] p, double ave, int length,
			double minValue, double maxValue, boolean ignoreZeros) {
		double stddev;
		if (p.length == 1) {
			stddev = 0;
		} else {
			double sum = 0;
			int count = 0;
			for (int i = 0; (i < length) && (i < p.length); i++) {
				double a = p[i];
				if ((a > minValue) && (a < maxValue) && (!ignoreZeros || (a != 0d))) {
					double dev = ave - a;
					sum += dev * dev;
					count++;
				}
			}
			stddev = count > 0 ? sqrt(sum / (count - 1)) : 0;
		}
		return stddev;
	}

	/**
	 * Return Standard Deviation of array with given average and length
	 * 
	 * @param p
	 *           Array of double values
	 * @param ave
	 *           Average value of p
	 * @param length
	 *           The number of array which has been averaged over
	 * @return Standard deviation of array values ranging from 0 - length
	 */
	public static double getStdDev(double[] p, double ave, int length,
			boolean ignoreZeros) {
		double stddev;
		if (length == 1) {
			stddev = 0;
		} else {
			double sum = 0;
			int len = 0;
			for (int i = 0; (i < length) && (i < p.length); i++) {
				double a = p[i];
				if (!ignoreZeros || (a != 0d)) {
					double dev = ave - a;
					sum += dev * dev;
					len++;
				}
			}
			stddev = len > 0 ? sqrt(sum / (len - 1)) : 0;
		}
		return stddev;
	}

	/**
	 * Return Standard Deviation of array with given average and length
	 * 
	 * @param p
	 *           Array of double values
	 * @param ave
	 *           Average value of p
	 * @param length
	 *           The number of array which has been averaged over
	 * @return Standard deviation of array values ranging from 0 - length
	 */
	public static double getStdDev(double[] p, double ave, int length) {
		return getStdDev(p, ave, length, false);
	}

	/**
	 * Return maximum value of double array
	 * 
	 * @param p
	 *           Array of double values
	 * @return highest entry value.
	 */
	public static double max(double[] p) {
		double max = Double.NEGATIVE_INFINITY;
		for (double element : p) {
			if (element > max) {
				max = element;
			}
		}
		return max;
	}

	/**
	 * Return minimum value of double array
	 * 
	 * @param p
	 *           Array of double values
	 * @return lowest entry value
	 */
	public static double min(double[] p) {
		double min = Double.MAX_VALUE;
		for (double element : p) {
			if (element < min) {
				min = element;
			}
		}
		return min;
	}

	/**
	 * Return maximum value of double array
	 * 
	 * @param p
	 *           Array of double values
	 * @return highest entry value.
	 */
	public static double max(double[] p, double minValue, double maxValue) {
		double max = Double.NEGATIVE_INFINITY;
		for (double a : p) {
			if ((a > max) && (a > minValue) && (a < maxValue)) {
				max = a;
			}
		}
		return max;
	}

	/**
	 * Return minimum value of double array
	 * 
	 * @param p
	 *           Array of double values
	 * @return lowest entry value
	 */
	public static double min(double[] p, double minValue, double maxValue) {
		double min = Double.MAX_VALUE;
		for (double a : p) {
			if ((a < min) && (a > minValue) && (a < maxValue)) {
				min = a;
			}
		}
		return min;
	}

	/**
	 * Returns the trigonometric sine of an angle
	 * 
	 * @param angle
	 *           in degrees
	 * @return The trigonometric sine of an angle in degrees
	 */
	public static double sinD(double angle) {
		return sin(angle * DEGRAD);
	}

	/**
	 * Hyberbolic Sine function
	 * 
	 * @param x
	 * @return The hyberbolic sine value of given argument
	 */
	public static double sinh(double x) {
		return (exp(x) - exp(-x)) / 2;
	}

	/**
	 * Returns the trigonometric tangent of an angle
	 * 
	 * @param angle
	 *           in degrees
	 * @return The trigonometric tangent of an angle in degrees
	 */
	public static double tanD(double angle) {
		return tan(angle * DEGRAD);
	}

	/**
	 * Hyberbolic trigonometric tangent function
	 * 
	 * @param x
	 * @return The hyberbolic trigonometric tangent value of given argument
	 */
	public static double tanh(double x) {
		return (exp(x) - exp(-x)) / (exp(x) + exp(-x));
	}

	/**
	 * Pearson Correlation
	 */
	public static double calculateCorrelation(double[] a, double[] b) {
		double result = 0;
		double sum_sq_x = 0;
		double sum_sq_y = 0;
		double sum_coproduct = 0;
		double mean_x = a[0];
		double mean_y = b[0];
		for (int i = 2; i < (a.length + 1); i += 1) {
			double sweep = Double.valueOf(i - 1) / i;
			double delta_x = a[i - 1] - mean_x;
			double delta_y = b[i - 1] - mean_y;
			sum_sq_x += delta_x * delta_x * sweep;
			sum_sq_y += delta_y * delta_y * sweep;
			sum_coproduct += delta_x * delta_y * sweep;
			mean_x += delta_x / i;
			mean_y += delta_y / i;
		}
		double pop_sd_x = sqrt(sum_sq_x / a.length);
		double pop_sd_y = sqrt(sum_sq_y / a.length);
		double cov_x_y = sum_coproduct / a.length;
		result = cov_x_y / (pop_sd_x * pop_sd_y);

		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] sortBest(T[] elements) {
		List<T> best = null;
		if ((elements != null) && (elements.length > 0)) {
			best = new ArrayList<T>();
			Arrays.sort(elements);
			T bestElement = elements[elements.length - 1];
			best.add(bestElement);
			for (int x = elements.length - 2; x >= 0; x--) {
				if (((Comparable<T>) bestElement).compareTo(elements[x]) == 0) {
					best.add(elements[x]);
				} else {
					break;
				}
			}
		}

		return ((best != null) && (best.size() > 0) ? best.toArray((T[]) Array
				.newInstance(best.get(0).getClass(), best.size())) : null);
	}

    /**
     * Does a linear interpolation of points first and second to calculate
     * the wanted function value of wantedX
     * @param wantedX
     * @param first
     * @param second
     */
    public static double linearInterpolate( double wantedX, Point2D first, Point2D second ) {
        double rX1 = first.getX();
        double rY1 = first.getY();
        double rX2 = second.getX();
        double rY2 = second.getY();

        if (rX2 == rX1) {
            return rY2;
        } else {
            double rT = (wantedX - rX1) / (rX2 - rX1);
            return rT * rY2 + (1 - rT) * rY1;
        }
    }

	public static boolean equals( double a, double b, double tolerance ) {
		return Math.abs( a - b ) < tolerance;
	}


    public static double Qi(double x) {
        double Q, t, xi;
        double c0,c1,c2,d1,d2,d3;

        try {
            c0 = 2.515517;
            c1 = 0.802853;
            c2 = 0.010328;
            d1 = 1.432788;
            d2 = 0.189269;
            d3 = 0.001308;

            if ( x <= 0.5 ) {
                t = Math.sqrt(-2.0*Math.log(x));
            } else {
                t = Math.sqrt(-2.0*Math.log(1-x));
            }

            xi = (((c2*t+c1)*t)+c0)/(((d3*t+d2)*t+d1)*t+1);

            if (x <= 0.5){
                Q = t-xi;
            } else {
                Q = -(t - xi);
            }
            return Q;
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Calculate the Kartesian angle between points.
     * <ol>
     * <li>Calculate distance between points: linearyDistance = distance(to, from); </li>
     * <li>Project y difference to form triangle: yProjection = Math.abs(from.getY() - to.getY()); </li>
     * <li>calculate the relative angle: angle = Mathematics.asinD(yProjection / linearyDistance);</li>
     * <ol/>
     * <br>
     * and then convert to an angle in the first Quadrant.
     *
     * @param to
     * @param from
     * @return angle between two points in Degree
     */
    public static double calculateKartesianAngle( Point2D to, Point2D from ) {
        double angle;
        // Calculate distance between points:
        double linearyDistance = distance(to, from);
        // Project y difference to form triangle
        double yProjection = Math.abs(from.getY() - to.getY());

        // Calculate relative angle in
        angle = asinD(yProjection / linearyDistance);
        if (Double.isNaN(angle)){
            angle = 0.0;
        }

        // Second Quatrant:
        if (to.getX() < from.getX() && to.getY() >= from.getY()) {
            angle = 180 - angle;
        }
        // Third Quatrant:
        else if (to.getX() < from.getX() && to.getY() <= from.getY()) {
            angle += 180;
        }
        // Fourth Quatrant:
        else if (to.getX() >= from.getX() && to.getY() < from.getY() && angle != 0.0) {// TODO KK added: && angle != 0.0 to prevent an angle of 180 degrees instead of zero

            angle = 360 - angle;
        }

        return angle;
    }

    /**
     * Calculate the Kartesian angle between (0,0) and point.
     * <p>
     * equivalent to: calculateKartesianAngle( Point2D to, 0)
     * <p>
     * See calculateKartesianAngle( Point2D to, Point2D from) for more details.
     * </p>
     *
     * @param to
     * @return angle in degree between (0,0) and a point
     */
    public static double calculateKartesianAngle( Point2D to ) {
        return calculateKartesianAngle(to,new Point2D(0,0));
    }


    /**
     * Calculate the elevation angle between points.
     * <p>
     * the fundamental equation is as follow:
     *  <p>
     *     <code>elevation = atanD((h1 - h2) / (distance * KM_TO_METERS))</code>
     *
     * @param from
     * @param h1
     * @param to
     * @param h2
     * @return angle in Degree
     */
    public static double calculateElevation( Point2D from, double h1,
                                             Point2D to,   double h2) {
        double threshold = 0.00001; // 1 cm

        double distance = distance(from, to);
        if ( Mathematics.equals(distance, 0, threshold) ) {
//            distance = threshold;
            //TODO KK avoid undefined elevation angles is case co-located
            if(h1 > h2) return 90;
            else if(h1 < h2) return -90;
            else if (Mathematics.equals(h1,h2,threshold))return 0;

        }

        return Mathematics.atanD((h1 - h2) / (distance * KM_TO_METERS));
    }

}