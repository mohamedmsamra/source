package org.seamcat.model.systems.ofdma;

import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.ofdma.CellularBastStation;
import org.seamcat.model.systems.ofdma.OFDMAMobile;
import org.seamcat.presentation.systems.CellularPosition;

public interface OFDMAPositioningTab {

    @UIPosition(row = 1, col = 1, name = "", width = 1000)
    CellularPosition position();

    @UIPosition(row = 1, col = 2, name = "Mobile", height = 200)
    OFDMAMobile mobile();

    @UIPosition(row = 2, col = 2, name = "Base Station")
    CellularBastStation baseStation();
}
