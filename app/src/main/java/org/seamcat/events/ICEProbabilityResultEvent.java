package org.seamcat.events;

import org.apache.log4j.Logger;
import org.seamcat.model.engines.ICEConfiguration;

public class ICEProbabilityResultEvent {

    private static final Logger LOG = Logger.getLogger(ICEProbabilityResultEvent.class);
    private ICEConfiguration ice;

    public ICEProbabilityResultEvent( ICEConfiguration ice) {
        this.ice = ice;
        if (LOG.isDebugEnabled()) {
            LOG.debug("ProbabilityResult: " + ice.getPropabilityResult());
        }
    }

    public ICEConfiguration getIce() {
        return ice;
    }
}
