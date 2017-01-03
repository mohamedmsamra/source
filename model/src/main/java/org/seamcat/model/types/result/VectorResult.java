package org.seamcat.model.types.result;

import java.util.List;

public class VectorResult {

    private double[] values;

    public VectorResult( double[] values) {
        this.values = values;
    }

    public VectorResult( List<Double> values ) {
        this.values = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            this.values[i] = values.get(i);
        }
    }

    public double[] asArray() {
        return values;
    }

    public double get(int i) {
        return values[i];
    }

    public int size() {
        if ( values == null ) return 0;
        return values.length;
    }
}
