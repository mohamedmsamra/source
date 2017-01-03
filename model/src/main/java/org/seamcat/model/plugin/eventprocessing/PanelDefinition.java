package org.seamcat.model.plugin.eventprocessing;

public class PanelDefinition<T> {

    private String name;
    private Class<T> clazz;

    public PanelDefinition(String name, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    public Class<T> getModelClass() {
        return clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PanelDefinition nameClass = (PanelDefinition) o;

        if (!clazz.equals(nameClass.clazz)) return false;
        if (!name.equals(nameClass.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + clazz.hashCode();
        return result;
    }
}
