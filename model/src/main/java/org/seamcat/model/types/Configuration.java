package org.seamcat.model.types;

import org.seamcat.model.plugin.Plugin;

public interface Configuration<T> {

    T getConfiguration();

    Class<? extends Plugin> getPluginClass();

}
