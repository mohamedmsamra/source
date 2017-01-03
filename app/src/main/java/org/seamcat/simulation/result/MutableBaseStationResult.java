package org.seamcat.simulation.result;

import org.seamcat.model.core.GridPositionCalculator;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.result.PreSimulationResults;
import org.seamcat.plugin.AntennaGainConfiguration;

import java.util.List;

public abstract class MutableBaseStationResult extends MutableSimulationElement {

    private final int HEXAGONAL = 6;

    private PreSimulationResults preSimulationResults;
    private Point2D[] geometry = new Point2D[ HEXAGONAL ];
    private int cellId;
    private int cellLocationId;
    private boolean isWorstCell;
    private double maximumTransmitPower;
    private double noiseRise;
    private double noiseRiseLinearFactor;
    private double relativeCellNoiseRise;
    private double initialCellNoiseRise;
    private int sectorId;
    private boolean upLinkMode;

    private double currentChannelTransmitPower;
    private double maximumChannelPowerFraction;
    private double overheadFraction;
    private double overheadTransmitPower;
    private double pilotFraction;
    private double pilotTransmitPower;

    private double bitRateAchieved;
    private int subCarriersInUse;

    public MutableBaseStationResult(PreSimulationResults results) {
        this.preSimulationResults = results;
        isWorstCell = false;
        upLinkMode = false;
    }

    public List<MutableLinkResult> getActiveConnections() {
        return null;
    }

    public void calculateHexagon( double cellRadius ) {
        GridPositionCalculator.calculateHexagon( getPosition(), cellRadius, geometry );
    }

    public boolean isInside(Point2D p, double shiftX, double shiftY){
        return GridPositionCalculator.isInside( p, shiftX, shiftY, geometry);
    }

    public int getCellId() {
        return cellId;
    }

    public int getCellLocationId() {
        return cellLocationId;
    }

    public boolean isWorstCell() {
        return isWorstCell;
    }

    public double getMaximumTransmitPower() {
        return maximumTransmitPower;
    }

    public double getNoiseRise() {
        return noiseRise;
    }

    public double getNoiseRiseLinearFactor() {
        return noiseRiseLinearFactor;
    }

    public double getRelativeCellNoiseRise() {
        return relativeCellNoiseRise;
    }

    public double getInitialCellNoiseRise() {
        return initialCellNoiseRise;
    }

    public int getSectorId() {
        return sectorId;
    }

    public boolean isUpLinkMode() {
        return upLinkMode;
    }

    public double getCurrentChannelTransmitPower() {
        return currentChannelTransmitPower;
    }

    public double getMaximumChannelPowerFraction() {
        return maximumChannelPowerFraction;
    }

    public double getOverheadFraction() {
        return overheadFraction;
    }

    public double getOverheadTransmitPower() {
        return overheadTransmitPower;
    }

    public double getPilotFraction() {
        return pilotFraction;
    }

    public double getPilotTransmitPower() {
        return pilotTransmitPower;
    }

    public double getBitRateAchieved() {
        return bitRateAchieved;
    }

    public int getSubCarriersInUse() {
        return subCarriersInUse;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public void setCellLocationId(int cellLocationId) {
        this.cellLocationId = cellLocationId;
    }

    public void setWorstCell(boolean worstCell) {
        isWorstCell = worstCell;
    }

    public void setMaximumTransmitPower(double maximumTransmitPower) {
        this.maximumTransmitPower = maximumTransmitPower;
    }

    public void setNoiseRise(double noiseRise) {
        this.noiseRise = noiseRise;
    }

    public void setNoiseRiseLinearFactor(double noiseRiseLinearFactor) {
        this.noiseRiseLinearFactor = noiseRiseLinearFactor;
    }

    public void setRelativeCellNoiseRise(double relativeCellNoiseRise) {
        this.relativeCellNoiseRise = relativeCellNoiseRise;
    }

    public void setInitialCellNoiseRise(double initialCellNoiseRise) {
        this.initialCellNoiseRise = initialCellNoiseRise;
    }

    public void setSectorId(int sectorId) {
        this.sectorId = sectorId;
    }

    public void setUpLinkMode(boolean upLinkMode) {
        this.upLinkMode = upLinkMode;
    }

    public void setCurrentChannelTransmitPower(double currentChannelTransmitPower) {
        this.currentChannelTransmitPower = currentChannelTransmitPower;
    }

    public void setMaximumChannelPowerFraction(double maximumChannelPowerFraction) {
        this.maximumChannelPowerFraction = maximumChannelPowerFraction;
    }

    public void setOverheadFraction(double overheadFraction) {
        this.overheadFraction = overheadFraction;
    }

    public void setOverheadTransmitPower(double overheadTransmitPower) {
        this.overheadTransmitPower = overheadTransmitPower;
    }

    public void setPilotFraction(double pilotFraction) {
        this.pilotFraction = pilotFraction;
    }

    public void setPilotTransmitPower(double pilotTransmitPower) {
        this.pilotTransmitPower = pilotTransmitPower;
    }

    public void setBitRateAchieved(double bitRateAchieved) {
        this.bitRateAchieved = bitRateAchieved;
    }

    public void setSubCarriersInUse(int subCarriersInUse) {
        this.subCarriersInUse = subCarriersInUse;
    }

    @Override
    public double calculateAntennaGainTo(double horizontalAngle, double verticalAngle) {
        if (horizontalAngle > 180) {
            horizontalAngle -= 360;
        }
        double elevation = verticalAngle - getAntennaTilt();
        MutableAntennaResult direction = new MutableAntennaResult();
        direction.setAzimuth( horizontalAngle );
        direction.setElevation( elevation );
        return getAntennaGain().evaluate(new MutableLinkResult(), direction);
    }

    public AntennaGainConfiguration getAntennaGain() {
        return (AntennaGainConfiguration) preSimulationResults.getAntennaGainForSector(sectorId);
    }


}
