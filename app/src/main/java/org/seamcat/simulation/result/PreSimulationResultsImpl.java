package org.seamcat.simulation.result;

import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.simulation.result.PreSimulationResults;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.plugin.AntennaGainConfiguration;

import java.util.HashMap;
import java.util.Map;

public class PreSimulationResultsImpl implements PreSimulationResults {

    private EmissionMask normalizedEmissionsMask;
    private EmissionMask normalizedEmissionsFloor;
    private EmissionMask normalizedEIRPInBlockMask;
    private AntennaGainConfiguration[] antennaGains;
    private double thermalNoise;
    private ResultTypes resultTypes = new ResultTypes();
    private Function blockingMaskIntegral;


    @Override
    public EmissionMask getNormalizedEmissionsMask() {
        return normalizedEmissionsMask;
    }

    @Override
    public EmissionMask getNormalizedEmissionsFloor() {
        return normalizedEmissionsFloor;
    }

    @Override
    public EmissionMask getNormalizedEIRPInBlockMask() {
        return normalizedEIRPInBlockMask;
    }

    public void setNormalizedEmissionsMask(EmissionMask normalizedEmissionsMask) {
        this.normalizedEmissionsMask = normalizedEmissionsMask;
    }

    public void setNormalizedEmissionsFloor(EmissionMask normalizedEmissionsFloor) {
        this.normalizedEmissionsFloor = normalizedEmissionsFloor;
    }

    public void setNormalizedEIRPInBlockMask(EmissionMask normalizedEIRPInBlockMask) {
        this.normalizedEIRPInBlockMask = normalizedEIRPInBlockMask;
    }

    @Override
    public AntennaGainConfiguration getAntennaGainForSector(int sectorId) {
        if ( antennaGains.length == 1 ) return antennaGains[0];
        return antennaGains[sectorId-1];
    }

    public void setAntennaGainForSectors(AntennaGainConfiguration[] antennas ) {
        this.antennaGains = antennas;
    }

    @Override
    public double getThermalNoise() {
        return thermalNoise;
    }

    public void setThermalNoise( double thermalNoise ) {
        this.thermalNoise = thermalNoise;
    }

    @Override
    public ResultTypes getPreSimulationResults() {
        return resultTypes;
    }

    public void setPreSimulationResults( ResultTypes results ) {
        this.resultTypes = results;
    }

    public void setBlockingMaskIntegral(Function blockingMaskIntegral) {
        this.blockingMaskIntegral = blockingMaskIntegral;
    }

    @Override
    public Function getBlockingMaskIntegral() {
        return blockingMaskIntegral;
    }

}
