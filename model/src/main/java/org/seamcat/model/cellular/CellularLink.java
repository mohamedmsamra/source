package org.seamcat.model.cellular;

import org.seamcat.model.types.Link;

public interface CellularLink extends Link {

    BaseStation getBaseStation();

    MobileStation getMobileStation();
}
