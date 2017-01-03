package org.seamcat.simulation.result;

import org.seamcat.model.simulation.result.InterferenceLinkResults;
import org.seamcat.model.simulation.result.LinkResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MutableInterferenceLinkResults implements InterferenceLinkResults {

    private List<MutableInterferenceLinkResult> interferenceResults;
    private List<MutableLinkResult> victimSystemlinks;
    private List<MutableLinkResult> interferingSystemLinks;

    public MutableInterferenceLinkResults( List<MutableLinkResult> victimSystemlinks ) {
        this.victimSystemlinks = victimSystemlinks;
        interferingSystemLinks = new ArrayList<>();
        interferenceResults = new ArrayList<>();
    }

    public MutableLinkResult getLastInterfererSubLink() {
        return interferenceResults.get( interferenceResults.size() -1 ).getInterferingSystemLink();
    }

    public MutableInterferenceLinkResult getLastInterfererResult() {
        return interferenceResults.get(interferenceResults.size() - 1);
    }

    public void addInterferenceLinkResult( MutableInterferenceLinkResult result ) {
        interferenceResults.add( result );
        interferingSystemLinks.add( result.getInterferingSystemLink() );
    }


    @Override
    public List<MutableInterferenceLinkResult> getInterferenceLinkResults() {
        return Collections.unmodifiableList( interferenceResults );
    }

    @Override
    public List<? extends LinkResult> getVictimSystemLinks() {
        return Collections.unmodifiableList( victimSystemlinks );
    }

    @Override
    public List<? extends LinkResult> getInterferingSystemLinks() {
        return Collections.unmodifiableList(interferingSystemLinks);
    }
}
