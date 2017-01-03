package org.seamcat.events;

import org.seamcat.model.engines.ICEConfiguration;

public class ICESignalTypeChanged {
    private Object origin;
    private ICEConfiguration ice;

    public ICESignalTypeChanged(ICEConfiguration ice, Object origin) {
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
