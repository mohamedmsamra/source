package org.seamcat.model.systems.cdma;

import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.model.systems.ofdma.General;
import org.seamcat.model.types.Description;

public interface SystemModelCDMAUpLink extends SystemModel {

    @UIPosition(row = 1, col = 1, name = "System")
    Description description();

    @UIPosition(row = 1, col = 2, name = "General")
    General general();

    @UITab(order = 1, value = "General settings")
    CDMAUpLinkGeneralTab generalSettings();

    @UITab(order = 2, value = "Positioning")
    CDMAPositioningTab positioning();

}
