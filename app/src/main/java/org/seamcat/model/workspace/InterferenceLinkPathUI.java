package org.seamcat.model.workspace;

import org.seamcat.model.generic.PathLossCorrelationUI;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.types.PropagationModel;

public interface InterferenceLinkPathUI {

    @UIPosition(name = "Relative positioning of Interfering Link", row = 1, col = 1, width = 450)
    RelativeLocationInterferenceUI relativeLocation();

    @UIPosition(name = "Path loss correlation", row = 1, col = 2, width = 400, height = 200)
    PathLossCorrelationUI pathLossCorrelation();

    @UIPosition(name = "Propagation Model", row = 1, col = 3)
    PropagationModel propagationModel();

}
