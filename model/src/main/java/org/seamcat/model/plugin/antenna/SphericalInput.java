package org.seamcat.model.plugin.antenna;

import org.seamcat.model.functions.Function;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Spherical;

public interface SphericalInput {
    @Config(order = 1, name = "Spherical")
    @Spherical
    Function spherical();
}
