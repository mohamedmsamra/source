package org.seamcat.model.distributions;

public abstract class AbstractDistribution implements Distribution {

    private double constant = 33;
    private double max = 1;
    private double maxAngle = 360;
    private double maxDistance = 1;
    private double mean = 0;
    private double min = 0;
    private double stdDev = 0;
    private double step = 0.2;
    private double stepShift = 0.0;

    public AbstractDistribution(double constant, double max, double maxAngle, double maxDistance, double mean, double min, double stdDev, double step, double stepShift) {
        this.constant = constant;
        this.max = max;
        this.maxAngle = maxAngle;
        this.maxDistance = maxDistance;
        this.mean = mean;
        this.min = min;
        this.stdDev = stdDev;
        this.step = step;
        this.stepShift = stepShift;
    }

    public double getMax() {
        return max;
    }

    public double getMaxAngle() {
        return maxAngle;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public double getMean() {
        return mean;
    }

    public double getMin() {
        return min;
    }

    public double getStdDev() {
        return stdDev;
    }

    public double getStep() {
        return step;
    }

    public double getConstant() {
        return constant;
    }

    public double getStepShift() {
        return stepShift;
    }
}
