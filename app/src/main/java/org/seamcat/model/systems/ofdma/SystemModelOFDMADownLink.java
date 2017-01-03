package org.seamcat.model.systems.ofdma;

import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.model.types.Description;

public interface SystemModelOFDMADownLink extends SystemModel {

    @UIPosition(row = 1, col = 1, name = "System")
    Description description();

    @UIPosition(row = 1, col = 2, name = "General")
    General general();

    @UITab(order = 1, value = "General settings")
    OFDMADownLinkGeneralTab generalSettings();

    @UITab(order = 2, value = "Positioning")
    OFDMAPositioningTab positioning();
}
