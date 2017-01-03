package org.seamcat.model.types.result;

public class BarChartValue {

    private String name;
    private double value;

    public BarChartValue( String name, double value ) {
        this.name = name;
        this.value = value;
    }


    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }
}
