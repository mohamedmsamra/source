package org.seamcat.model.cellular;

import org.seamcat.model.types.Receiver;

public interface CellularReceiver extends Receiver {

    double standardDesensitisation();

    double targetINR();
}
