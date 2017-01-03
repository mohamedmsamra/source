package org.seamcat.model.workspace;

import org.seamcat.model.systems.UITab;

public interface InterferenceLinkUI {

    @UITab(order = 1, value = "Interfering Link Transmitter to Victim Link Receiver Path")
    InterferenceLinkPathUI path();

}
