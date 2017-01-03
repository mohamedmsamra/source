package org.seamcat.model.systems.generic;

import org.seamcat.model.generic.InterferersDensityUI;
import org.seamcat.model.generic.RelativeLocationUI;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.types.CoverageRadius;
import org.seamcat.model.types.PropagationModel;

public interface TransmitterReceiverPathModel {

    @UIPosition(row = 1, col = 1, name = "Relative location", width = 410)
    RelativeLocationUI relativeLocation();

    @UIPosition(row = 2, col = 1, name = "Interferers Density")
    InterferersDensityUI density();

    @UIPosition(row = 1, col = 2, name = "Coverage Radius", height = 300, width = 410)
    CoverageRadius coverageRadius();

    @UIPosition(row = 2, col = 2, name = "Local Environments")
    LocalEnvironments localEnvironments();

    @UIPosition(row = 1, col = 3, name = "Propagation Model")
    PropagationModel propagationModel();
}
