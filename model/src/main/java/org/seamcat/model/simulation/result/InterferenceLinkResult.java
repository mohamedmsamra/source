package org.seamcat.model.simulation.result;

import org.seamcat.model.types.InterferenceLink;

public interface InterferenceLinkResult extends LinkResult {

    InterferenceLink getInterferenceLink();

    LinkResult getVictimSystemLink();

    LinkResult getInterferingSystemLink();

    SensingLinkResult  getSensingLinkResult();

    double getRiRSSUnwantedValue();

    double getRiRSSBlockingValue();
}
