package org.seamcat.model.types;

import org.seamcat.model.plugin.Config;

public interface Description {

    @Config(order = 1, name = "Name")
    String name();

    @Config(order = 2, name = "Description")
    String description();
}
