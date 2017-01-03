package org.seamcat.scenario;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.model.cellular.cdma.CDMASettings;

public class CDMASettingsImpl implements CDMASettings {

    private double voiceBitRate;
    private double callDropThreshold;
    private double voiceActivityFactor;
    private boolean simulateNonInterferedCapacity;
    private int numberOfTrials;
    private double toleranceOfInitialOutage;
    private double targetNoiseRisePrecision;
    private int deltaUsersPerCell;
    private CDMALinkLevelData lld;
    private CDMAUpLinkImpl upLink;
    private CDMADownLinkImpl downLink;

    public CDMASettingsImpl() {
        voiceBitRate = 9.6;
        callDropThreshold = 3;
        voiceActivityFactor = 1.0;
        simulateNonInterferedCapacity = true;
        numberOfTrials = 20;
        toleranceOfInitialOutage = 0.05;
        targetNoiseRisePrecision = 0.05;
        deltaUsersPerCell = 20;
    }

    @Override
    public double getCallDropThreshold() {
        return callDropThreshold;
    }

    public void setCallDropThreshold( double callDropThreshold ) {
        this.callDropThreshold = callDropThreshold;
    }

    @Override
    public double getVoiceBitRate() {
        return voiceBitRate;
    }

    public void setVoiceBitRate( double voiceBitRate ) {
        this.voiceBitRate = voiceBitRate;
    }

    @Override
    public double getVoiceActivityFactor() {
        return voiceActivityFactor;
    }

    public void setVoiceActivityFactor( double voiceActivityFactor ) {
        this.voiceActivityFactor = voiceActivityFactor;
    }

    @Override
    public boolean isSimulateNonInterferedCapacity() {
        return simulateNonInterferedCapacity;
    }

    public void setSimulateNonInterferedCapacity( boolean simulateNonInterferedCapacity ) {
        this.simulateNonInterferedCapacity = simulateNonInterferedCapacity;
    }

    @Override
    public int getDeltaUsersPerCell() {
        return deltaUsersPerCell;
    }

    public void setDeltaUsersPerCell(int deltaUsersPerCell ) {
        this.deltaUsersPerCell = deltaUsersPerCell;
    }

    @Override
    public int getNumberOfTrials() {
        return numberOfTrials;
    }

    public void setNumberOfTrials( int numberOfTrials ) {
        this.numberOfTrials = numberOfTrials;
    }

    @Override
    public double getToleranceOfInitialOutage() {
        return toleranceOfInitialOutage;
    }

    public void setToleranceOfInitialOutage( double toleranceOfInitialOutage ) {
        this.toleranceOfInitialOutage = toleranceOfInitialOutage;
    }

    @Override
    public double getTargetNoiseRisePrecision() {
        return targetNoiseRisePrecision;
    }

    public void setTargetNoiseRisePrecision( double targetNoiseRisePrecision ) {
        this.targetNoiseRisePrecision = targetNoiseRisePrecision;
    }

    @Override
    public CDMAUpLinkImpl getUpLinkSettings() {
        return upLink;
    }

    public void setUpLinkSettings(CDMAUpLinkImpl upLink ) {
        this.upLink = upLink;
    }

    @Override
    public CDMADownLinkImpl getDownLinkSettings() {
        return downLink;
    }

    public void setDownLinkSettings( CDMADownLinkImpl downLink ) {
        this.downLink = downLink;
    }

    public CDMALinkLevelData getLld() {
        return lld;
    }

    public void setLld(CDMALinkLevelData lld) {
        this.lld = lld;
    }
}
