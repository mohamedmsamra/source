package org.seamcat.model.systems.generic;

import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.model.systems.ofdma.General;
import org.seamcat.model.types.Description;

public interface SystemModelGeneric extends SystemModel {

    @UIPosition(row = 1, col = 1, name = "System")
    Description description();

    @UIPosition(row = 1, col = 2, name = "General")
    General general();

    @UITab(order = 1, value = "Receiver")
    ReceiverModel receiver();
    
    //@UITab(order = 1, value = "Receiver")
    //T_ReceiverModel t_receiver();
    

    @UITab(order = 2, value = "Transmitter")
    TransmitterModel transmitter();

    @UITab(order = 3, value = "Transmitter to Receiver Path")
    TransmitterReceiverPathModel path();
}
