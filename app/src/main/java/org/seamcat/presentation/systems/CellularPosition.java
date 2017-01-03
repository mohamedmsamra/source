package org.seamcat.presentation.systems;

import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.plugin.Config;

public interface CellularPosition {

    @Config(order = 1, name = "Number of tiers")
    int tiers();
    int tiers = 2;

    @Config(order = 2, name = "Sector Type")
    CellularLayout.SectorSetup sectorType();
    CellularLayout.SectorSetup sectorType = CellularLayout.SectorSetup.TriSector3GPP2;

    @Config(order = 3, name = "Cell radius")
    double cellRadius();
    double cellRadius = 5.0;

    @Config(order = 4, name = "Layout")
    CellularLayout.SystemLayout layout();

    @Config(order = 5, name = "Measure interference from entire cluster")
    boolean measureFromEntireCluster();

    @Config(order = 6, name = "Generate wrap-around")
    boolean generateWrapAround();
    boolean generateWrapAround = true;

    @Config(order = 7, name = "Reference cell id")
    int referenceCellId();

    @Config(order = 8, name = "Reference sector")
    int referenceSector();
}


