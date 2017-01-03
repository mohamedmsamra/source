package org.seamcat.model.plugin.propagation;

import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.PropagationModel;

public interface PropTestInput {
    @Config(order = 1, name = "A value", unit = "dB")
    double value();

    @Config(order = 2, name = "Propagation")
    PropagationModel pm();

    @Config(order = 3, name = "Antenna")
    AntennaGain gain();

    @Config(order = 4, name = "Emission")
    EmissionMask emission();

    @Config(order = 5, name = "Blocking mask")
    BlockingMask blockingMask();
}
