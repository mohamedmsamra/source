package org.seamcat.model.simulation.result;

import java.util.List;

public interface InterferenceLinkResults {

    List<? extends InterferenceLinkResult> getInterferenceLinkResults();

    List<? extends LinkResult> getVictimSystemLinks();

    List<? extends LinkResult> getInterferingSystemLinks();
}
