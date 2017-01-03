package org.seamcat.model.systems.ofdma;

import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.generic.Defaults;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.plugin.OptionalMaskFunction;

public interface TransmitterSettings {

    @Config(order = 1, name = "Emissions mask", unit = "dBc/Ref.BW")
    EmissionMask emissionMask();
    EmissionMask emissionMask = Defaults.defaultEmissionMask();

    @Config(order = 2, name = "Emission mask as BEM")
    boolean emissionMaskAsBEM();

    @Config(order = 3, name = "Emissions floor", unit = "dBm/Ref.BW")
    OptionalMaskFunction emissionFloor();
    OptionalMaskFunction emissionFloor = Defaults.defaultEmissionFloor();

}
