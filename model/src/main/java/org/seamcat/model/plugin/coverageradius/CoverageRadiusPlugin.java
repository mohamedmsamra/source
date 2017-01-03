package org.seamcat.model.plugin.coverageradius;

import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.plugin.Plugin;

public interface CoverageRadiusPlugin<T> extends Plugin<T> {

    double evaluate(GenericSystem system, T input );
}
