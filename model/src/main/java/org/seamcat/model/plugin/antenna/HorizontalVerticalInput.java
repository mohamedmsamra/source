package org.seamcat.model.plugin.antenna;

import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.Horizontal;
import org.seamcat.model.plugin.OptionalFunction;
import org.seamcat.model.plugin.Vertical;

public interface HorizontalVerticalInput {

    @Config(order = 1, name = "Horizontal")
    @Horizontal
    OptionalFunction horizontal();

    @Config(order = 2, name = "Vertical")
    @Vertical
    OptionalFunction vertical();
}
