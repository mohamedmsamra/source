package org.seamcat.events;

import org.seamcat.model.engines.ICEConfiguration;

public class ICECriterionChanged {
    private Object origin;
    private ICEConfiguration ice;

    public ICECriterionChanged(ICEConfiguration ice, Object origin ) {
        this.ice = ice;
        this.origin = origin;
    }

    public ICEConfiguration getIce() {
        return ice;
    }

    public Object getOrigin() {
        return origin;
    }
}
