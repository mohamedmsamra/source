package org.seamcat.model.types.result;

public class IntegerResultType implements SingleValueTypes<Integer> {

    private String name;
    private int value;
    private String unit;

    public IntegerResultType(String name, String unit, int value ) {
        this.name = name;
        this.value = value;
        this.unit = unit;
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
    public Integer getValue() {
        return value;
    }

    @Override
    public String getType() {
        return "Integer";
    }

    @Override
    public String toString() {
        return name + ": " + value + " " + unit;
    }
}
