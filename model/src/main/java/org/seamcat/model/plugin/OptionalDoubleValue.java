package org.seamcat.model.plugin;

public class OptionalDoubleValue {

    private boolean relevant;
    private double value;

    public OptionalDoubleValue(boolean relevant, double value) {
        this.relevant = relevant;
        this.value = value;
    }


    public boolean isRelevant() {
        return relevant;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value + " enabled " + relevant;
    }
}
