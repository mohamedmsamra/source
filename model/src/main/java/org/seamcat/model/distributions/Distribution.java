package org.seamcat.model.distributions;

import org.seamcat.model.functions.Bounds;

public interface Distribution {

    double trial();

    Bounds getBounds();
}
