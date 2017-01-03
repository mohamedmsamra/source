package org.seamcat.model.types.result;

import java.util.ArrayList;
import java.util.List;

public class VectorGroupResultType {

    private String name;
    private List<NamedVectorResult> vectorGroup;
    private String unit;

    public VectorGroupResultType(String name, String unit ) {
        this.name = name;
        this.unit = unit;
        vectorGroup = new ArrayList<NamedVectorResult>();
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public int size() {
        return vectorGroup.size();
    }

    public void addVector( NamedVectorResult vector ) {
        vectorGroup.add(vector);
    }

    public List<NamedVectorResult> getVectorGroup() {
        return vectorGroup;
    }

    @Override
    public String toString() {
        return "Vector Group["+vectorGroup.size()+"]";
    }
}
