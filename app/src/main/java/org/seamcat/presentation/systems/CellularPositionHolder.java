package org.seamcat.presentation.systems;

import org.seamcat.model.generic.ProxyHelper;

public class CellularPositionHolder {

    private CellularPosition cellularPosition;

    public CellularPositionHolder() {
        cellularPosition = ProxyHelper.newInstance( CellularPosition.class );
    }

    public CellularPosition getCellularPosition() {
        return cellularPosition;
    }

    public void setCellularPosition(CellularPosition cellularPosition) {
        this.cellularPosition = cellularPosition;
    }
}
