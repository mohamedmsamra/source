package org.seamcat.model.systems.cdma;

import org.seamcat.model.generic.Defaults;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.generic.LocalEnvironments;
import org.seamcat.model.systems.ofdma.ReceiverSettings;
import org.seamcat.model.systems.ofdma.TransmitterSettings;
import org.seamcat.model.types.PropagationModel;

public interface CDMADownLinkGeneralTab {

    @UIPosition(row = 1, col = 1, name = "CDMA General Settings", width = 390, height = 330)
    CDMAGeneralSettings generalSettings();

    @UIPosition(row = 2, col = 1, name = "Local Environments")
    LocalEnvironments localEnvironments();

    @UIPosition(row = 1, col = 2, name = "Receiver settings", width = 370, height = 100)
    ReceiverSettings receiverSettings();

    @UIPosition(row = 2, col = 2, name = "Transmitter settings", height = 130)
    TransmitterSettings transmitterSettings();

    @UIPosition(row = 3, col = 2, name = "CDMA Downlink", height = 200)
    CDMADownLink cdmaDownLink();

    @UIPosition(row = 4, col = 2, name = "CDMA Capacity")
    CDMADownLinkCapacity cdmaCapacity();

    @UIPosition(row = 1, col = 3, name = "Propagation Model")
    PropagationModel propagationModel();
}
