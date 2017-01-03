package org.seamcat.model.systems.ofdma;

import org.seamcat.model.generic.PathLossCorrelationUI;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.generic.LocalEnvironments;
import org.seamcat.model.types.PropagationModel;

public interface OFDMAUpLinkGeneralTab {

    @UIPosition(row = 1, col = 1, name = "OFDMA General Settings", width = 450, height = 330)
    OFDMAGeneralSettings generalSettings();

    @UIPosition(row = 2, col = 1, name = "Receiver settings", height = 100)
    ReceiverSettings receiverSettings();

    @UIPosition(row = 3, col = 1, name = "Local Environments", height = 300)
    LocalEnvironments localEnvironments();

    @UIPosition(row = 2, col = 2, name = "Transmitter settings", width = 400, height = 130)
    TransmitterSettings transmitterSettings();

    @UIPosition(row = 3, col = 2, name = "OFDMA Uplink", height = 220)
    OFDMAUpLinkUI ofdmaUpLink();

    @UIPosition(row = 4, col = 2, name = "OFDMA Capacity", height = 100)
    OFDMACapacity ofdmaCapacity();

    @UIPosition(row = 5, col = 2, name = "Pathloss Correlation")
    PathLossCorrelationUI pathLossCorrelation();

    @UIPosition(row = 1, col = 3, name = "Propagation Model")
    PropagationModel propagationModel();

}
