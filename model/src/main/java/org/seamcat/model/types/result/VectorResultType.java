package org.seamcat.model.types.result;

import java.util.List;

public class VectorResultType {

    private String name;
    private VectorResult vector;
    private String unit;
    private String label;

    public VectorResultType(String name, String unit, String label, List<Double> values ) {
        this.name = name;
        this.unit = unit;
        this.label = label;
        this.vector = new VectorResult(values);
    }

    public VectorResultType(String name, String unit, String label, double[] values ) {
        this.name = name;
        this.unit = unit;
        this.label = label;
        this.vector = new VectorResult(values);
    }


    public VectorResultType(String name, String unit, List<Double> values ) {
        this(name, unit, "Events", values);
    }

    public VectorResultType(String name, String unit, double[] values) {
        this(name, unit, "Events", values);
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public VectorResult getValue() {
        return vector;
    }


    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "Array["+ vector.size()+"]";
    }
}
