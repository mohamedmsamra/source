package org.seamcat.model.types.result;

import java.util.ArrayList;
import java.util.List;

public class ResultTypes {

    private List<SingleValueTypes<?>> singleValueTypes;
    private List<VectorGroupResultType> vectorGroupResultTypes;
    private List<VectorResultType> vectorResultTypes;
    private List<ScatterDiagramResultType> scatterDiagramResultTypes;
    private List<BarChartResultType> barChartResultTypes;

    public ResultTypes() {
        singleValueTypes = new ArrayList<SingleValueTypes<?>>();
        vectorResultTypes = new ArrayList<VectorResultType>();
        vectorGroupResultTypes = new ArrayList<VectorGroupResultType>();
        scatterDiagramResultTypes = new ArrayList<ScatterDiagramResultType>();
        barChartResultTypes = new ArrayList<BarChartResultType>();
    }

    public List<SingleValueTypes<?>> getSingleValueTypes() {
        return singleValueTypes;
    }

    public List<VectorResultType> getVectorResultTypes() {
        return vectorResultTypes;
    }

    public VectorResultType findVector( String name ) {
        for (VectorResultType vector : vectorResultTypes) {
            if ( vector.getName().equals( name ))  return vector;
        }

        return null;
    }

    public double findDoubleValue( String name ) {
        for (SingleValueTypes<?> single : singleValueTypes) {
            if ( single.getName().equals(name) && single instanceof DoubleResultType) {
                return ((DoubleResultType) single).getValue();
            }
        }

        return -1;
    }

    public boolean hasSingleValue( String name ) {
        for (SingleValueTypes<?> type : singleValueTypes) {
            if ( type.getName().equals(name)) return true;
        }
        return false;
    }

    public int findIntValue( String name ) {
        for (SingleValueTypes<?> single : singleValueTypes) {
            if ( single.getName().equals(name) && single instanceof IntegerResultType) {
                return ((IntegerResultType) single).getValue();
            }
        }

        return -1;
    }

    public BarChartResultType findBarChart(String title) {
        for (BarChartResultType type : barChartResultTypes) {
            if ( type.getTitle().equals( title )) {
                return type;
            }
        }

        return null;
    }

    public List<VectorGroupResultType> getVectorGroupResultTypes() {
        return vectorGroupResultTypes;
    }

    public List<ScatterDiagramResultType> getScatterDiagramResultTypes() {
        return scatterDiagramResultTypes;
    }

    public List<BarChartResultType> getBarChartResultTypes() {
        return barChartResultTypes;
    }

    public boolean isEmpty() {
        return singleValueTypes.isEmpty() && vectorResultTypes.isEmpty()
                && vectorGroupResultTypes.isEmpty() && barChartResultTypes.isEmpty()
                && scatterDiagramResultTypes.isEmpty();
    }

}
