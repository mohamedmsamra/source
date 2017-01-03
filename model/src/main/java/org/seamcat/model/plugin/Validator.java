package org.seamcat.model.plugin;

public interface Validator<T> {

    T error(String message);

}
