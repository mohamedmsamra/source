package org.seamcat.plugin;

public class PluginLocation {
    public static final String BUILTIN = "BUILT-IN";

    private String jarId;
    private String className;

    public PluginLocation( String jarId, String className ) {
        if ( jarId == null || jarId.isEmpty() ) {
            throw new RuntimeException("Invalid plugin location");
        }
        this.jarId = jarId;
        this.className = className;
    }

    public PluginLocation( String className ) {
        jarId = BUILTIN;
        this.className = className;
    }


    public boolean isBuiltIn() {
        return BUILTIN.equals( jarId );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginLocation that = (PluginLocation) o;

        if (!className.equals(that.className)) return false;
        if (!jarId.equals(that.jarId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = jarId.hashCode();
        result = 31 * result + className.hashCode();
        return result;
    }

    public String getJarId() {
        return jarId;
    }

    public String getClassName() {
        return className;
    }
}
