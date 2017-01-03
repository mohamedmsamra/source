package org.seamcat.model.types.result;

public class NamedVectorResult {

    private String name;
    private VectorResult vector;

    public NamedVectorResult( String name, VectorResult vector ) {
        this.name = name;
        this.vector = vector;
    }

    public NamedVectorResult( String name, double[] values) {
        this.name = name;
        this.vector = new VectorResult(values);
    }

    public String getName() {
        return name;
    }

    public VectorResult getVector() {
        return vector;
    }

    @Override
    public String toString() {
        return name;
    }
}
