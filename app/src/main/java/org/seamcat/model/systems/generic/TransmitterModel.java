package org.seamcat.model.systems.generic;

import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.generic.Defaults;
import org.seamcat.model.generic.EmissionCharacteristics;
import org.seamcat.model.generic.TXAntennaPointingUI;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.LibraryItem;

public interface TransmitterModel extends LibraryItem {

    @UIPosition(row = 1, col = 1, name = "Transmitter identification", height = 200, width = 400)
    Description description();

    @UIPosition(row = 2, col = 1, name = "Antenna pointing")
    TXAntennaPointingUI antennaPointing();

    @UIPosition(row = 1, col = 2, name = "Antenna Patterns Identification", width = 300)
    AntennaGain antennaGain();
    AntennaGain antennaGain = Defaults.defaultAntennaGain();

    @UIPosition(row = 1, col = 3, name = "Emission characteristics")
    EmissionCharacteristics emissionCharacteristics();
    EmissionMask emi = Defaults.defaultEmissionMask();
    
}
