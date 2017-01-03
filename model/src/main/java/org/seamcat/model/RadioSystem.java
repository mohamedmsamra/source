package org.seamcat.model;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.types.Link;
import org.seamcat.model.types.Receiver;
import org.seamcat.model.types.Transmitter;

public interface RadioSystem extends Named {

    String TX_RX_PATHLOSS = "path loss";
    String EFFECTIVE_PATHLOSS = "effective path loss";
    String UNWANTED_EMISSION_INTEGRATION = "Unwanted emission integration";

    Distribution getFrequency();

    Receiver getReceiver();

    Transmitter getTransmitter();

    // TransmitterReceiverPath getPath();
    Link getLink();
}
