package org.seamcat.presentation.report;

public class ReportValue {

    private String name;
    private Object value;
    private String unit;

    public ReportValue(String name, Object value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }
}
