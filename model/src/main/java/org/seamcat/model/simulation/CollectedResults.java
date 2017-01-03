package org.seamcat.model.simulation;

import java.util.LinkedHashMap;

public class CollectedResults {

    private int size;
    private LinkedHashMap<String, double[]> vectorResults = new LinkedHashMap<>();

    public CollectedResults(int size ) {
        this.size = size;
    }

    public double[] vector( String name ) {
        double[] values = vectorResults.get(name);
        if ( values == null ) {
            values = new double[size];
            vectorResults.put(name, values );
        }
        return values;
    }

    public LinkedHashMap<String, double[]> vectorResults() {
        return vectorResults;
    }
}
