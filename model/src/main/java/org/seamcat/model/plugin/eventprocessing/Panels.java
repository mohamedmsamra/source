package org.seamcat.model.plugin.eventprocessing;

public interface Panels {

    <T> ModelPanel<T> get( String name );
}
