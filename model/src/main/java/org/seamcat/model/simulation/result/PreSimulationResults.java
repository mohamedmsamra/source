package org.seamcat.model.simulation.result;

import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.result.ResultTypes;

public interface PreSimulationResults {

    double getThermalNoise();

    EmissionMask getNormalizedEmissionsMask();

    EmissionMask getNormalizedEmissionsFloor();

    EmissionMask getNormalizedEIRPInBlockMask();

    AntennaGain getAntennaGainForSector( int sectorId );

    ResultTypes getPreSimulationResults();

    Function getBlockingMaskIntegral();
}
