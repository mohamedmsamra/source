package org.seamcat.model.plugin.builder;

/**
 * Used inside a builder to specify which values to return for a particular method
 * @param <V>
 */
public interface Returner<V> {

    /**
     * Value to return for a previously called prototype method
     * @param value actual return value
     */
    void thenReturn( V value );
}
