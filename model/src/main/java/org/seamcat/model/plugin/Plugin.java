package org.seamcat.model.plugin;

import org.seamcat.model.Scenario;
import org.seamcat.model.types.LibraryItem;

import java.util.List;

public interface Plugin<T> extends LibraryItem {

    void consistencyCheck( Scenario scenario, List<Object> path, T input, Validator<T> validator);

}
