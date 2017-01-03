package org.seamcat.model.types.result;

public interface SingleValueTypes<T> {

    String getName();

    String getUnit();

    T getValue();

    String getType();
}
