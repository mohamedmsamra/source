package org.seamcat.model.types.result;

public class DoubleResultType implements SingleValueTypes<Double> {

    private String name;
    private double value;
    private String unit;

    public DoubleResultType(String name, String unit, double value ) {
        this.name = name;
        this.unit = unit;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public String getType() {
        return "Double";
    }

    @Override
    public String toString() {
        return name + ": " + value + " " + unit;
    }
}
