package org.seamcat.model.types.result;

public class StringResultType implements SingleValueTypes<String> {

    private String name;
    private String value;

    public StringResultType(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUnit() {
        return "N/A";
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getType() {
        return "String";
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }

}
