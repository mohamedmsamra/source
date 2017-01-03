package org.seamcat.model.systems.cdma;

import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.ofdma.CellularBastStation;
import org.seamcat.presentation.systems.CellularPosition;

public interface CDMAPositioningTab {

    @UIPosition(row = 1, col = 1, name = "", width = 1000)
    CellularPosition position();

    @UIPosition(row = 1, col = 2, name = "Mobile station", height = 200)
    CDMAMobile mobile();

    @UIPosition(row = 2, col = 2, name = "Base station")
    CellularBastStation baseStation();
}
