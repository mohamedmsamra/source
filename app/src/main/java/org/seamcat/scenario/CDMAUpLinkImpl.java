package org.seamcat.scenario;

import org.seamcat.model.cellular.cdma.CDMAUpLink;

public class CDMAUpLinkImpl implements CDMAUpLink {

    private double targetNetworkNoiseRise;
    private boolean cellNoiseRise;
    private double targetCellNoiseRise;
    private double MSMaximumTransmitPower;
    private double MSPowerControlRange;
    private double MSConvergencePrecision;

    public CDMAUpLinkImpl() {
        targetNetworkNoiseRise = 5.5;
        cellNoiseRise = false;
        targetCellNoiseRise = 0.1;
        MSMaximumTransmitPower = 25;
        MSPowerControlRange = 75;
        MSConvergencePrecision = 0.001;
    }

    @Override
    public double getTargetNetworkNoiseRise() {
        return targetNetworkNoiseRise;
    }

    public void setTargetNetworkNoiseRise(double targetNetworkNoiseRise ) {
        this.targetNetworkNoiseRise = targetNetworkNoiseRise;
    }

    @Override
    public boolean isCellNoiseRise() {
        return cellNoiseRise;
    }

    public void setCellNoiseRise(boolean cellNoiseRise ) {
        this.cellNoiseRise = cellNoiseRise;
    }

    @Override
    public double getTargetCellNoiseRise() {
        return targetCellNoiseRise;
    }

    public void setTargetCellNoiseRise(double targetCellNoiseRise ) {
        this.targetCellNoiseRise = targetCellNoiseRise;
    }

    @Override
    public double getMSMaximumTransmitPower() {
        return MSMaximumTransmitPower;
    }

    public void setMSMaximumTransmitPower(double MSMaximumTransmitPower ) {
        this.MSMaximumTransmitPower = MSMaximumTransmitPower;
    }

    @Override
    public double getMSPowerControlRange() {
        return MSPowerControlRange;
    }

    public void setMSPowerControlRange( double MSPowerControlRange ) {
        this.MSPowerControlRange = MSPowerControlRange;
    }

    @Override
    public double getMSConvergencePrecision() {
        return MSConvergencePrecision;
    }

    public void setMSConvergencePrecision(double MSConvergencePrecision ) {
        this.MSConvergencePrecision = MSConvergencePrecision;
    }
}
