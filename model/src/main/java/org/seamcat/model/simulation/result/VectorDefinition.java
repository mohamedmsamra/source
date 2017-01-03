package org.seamcat.model.simulation.result;

public class VectorDefinition {

    private final String group, name, unit;

    public VectorDefinition( String group, String name, String unit ) {
        this.group = group;
        this.name = name;
        this.unit = unit;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }
}
