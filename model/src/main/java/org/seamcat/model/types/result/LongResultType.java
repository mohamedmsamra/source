package org.seamcat.model.types.result;

public class LongResultType implements SingleValueTypes<Long> {

    private String name;
    private long value;
    private String unit;

    public LongResultType(String name, String unit, long value) {
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
    public Long getValue() {
        return value;
    }

    @Override
    public String getType() {
        return "Long Integer";
    }

    @Override
    public String toString() {
        return name + ": " + value + " " + unit;
    }

}
