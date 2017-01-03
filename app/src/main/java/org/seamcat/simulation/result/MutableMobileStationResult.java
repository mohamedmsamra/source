package org.seamcat.simulation.result;

import org.seamcat.model.mathematics.Mathematics;

public abstract class MutableMobileStationResult extends MutableSimulationElement {

    public void setAllowedToConnect(boolean allowedToConnect) {
        this.allowedToConnect = allowedToConnect;
    }

    public void setAntennaGain(double antennaGain) {
        this.antennaGain = antennaGain;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setCurrentTransmitPower(double currentTransmitPower) {
        this.currentTransmitPower = currentTransmitPower;
    }

    public void setCurrentTransmitPowerIndBm(double currentTransmitPowerIndBm) {
        this.currentTransmitPowerIndBm = currentTransmitPowerIndBm;
    }

    public void setDropped(boolean dropped) {
        this.dropped = dropped;
    }

    public void setDropReason(String dropReason) {
        this.dropReason = dropReason;
    }

    public void setInSoftHandover(boolean inSoftHandover) {
        isInSoftHandover = inSoftHandover;
    }

    public void setMaxTxPower(double maxTxPower) {
        this.maxTxPower = maxTxPower;
    }

    public void setMinTxPower(double minTxPower) {
        this.minTxPower = minTxPower;
    }

    public void setMobilitySpeed(double mobilitySpeed) {
        this.mobilitySpeed = mobilitySpeed;
    }

    public void setThermalNoise(double thermalNoise) {
        this.thermalNoise = thermalNoise;
    }

    public void setUpLinkMode(boolean upLinkMode) {
        this.upLinkMode = upLinkMode;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAchievedCI(double achievedCI) {
        this.achievedCI = achievedCI;
    }

    public void setAchievedEcIor(double achievedEcIor) {
        this.achievedEcIor = achievedEcIor;
    }

    public void setDroppedAsHighest(boolean droppedAsHighest) {
        this.droppedAsHighest = droppedAsHighest;
    }

    public void setGeometry(double geometry) {
        this.geometry = geometry;
    }

    public void setLinkLevelDataPointFound(boolean linkLevelDataPointFound) {
        this.linkLevelDataPointFound = linkLevelDataPointFound;
    }

    public void setLinkQualityExceptions(int linkQualityExceptions) {
        this.linkQualityExceptions = linkQualityExceptions;
    }

    public void setMultiPathChannel(int multiPathChannel) {
        this.multiPathChannel = multiPathChannel;
    }

    public void setOldAchievedCI(double oldAchievedCI) {
        this.oldAchievedCI = oldAchievedCI;
    }

    public void setPowerScaledUpCount(int powerScaledUpCount) {
        this.powerScaledUpCount = powerScaledUpCount;
    }

    public void setReceivedTrafficChannelPowerWatt(double receivedTrafficChannelPowerWatt) {
        this.receivedTrafficChannelPowerWatt = receivedTrafficChannelPowerWatt;
    }

    public void setRequiredEcIor(double requiredEcIor) {
        this.requiredEcIor = requiredEcIor;
    }

    public void setTotalPowerReceivedFromBaseStationsInActiveSet(double totalPowerReceivedFromBaseStationsInActiveSet) {
        this.totalPowerReceivedFromBaseStationsInActiveSet = totalPowerReceivedFromBaseStationsInActiveSet;
    }

    public void setTotalPowerReceivedFromBaseStationsNotInActiveSet(double totalPowerReceivedFromBaseStationsNotInActiveSet) {
        this.totalPowerReceivedFromBaseStationsNotInActiveSet = totalPowerReceivedFromBaseStationsNotInActiveSet;
    }

    public void setBitRateAchieved(double bitRateAchieved) {
        this.bitRateAchieved = bitRateAchieved;
    }

    public void setRequestedSubCarriers(int requestedSubCarriers) {
        this.requestedSubCarriers = requestedSubCarriers;
    }

    public void setSINRAchieved(double SINRAchieved) {
        this.SINRAchieved = SINRAchieved;
    }

    public void setDisconnectAttempts(int disconnectAttempts) {
        this.disconnectAttempts = disconnectAttempts;
    }

    public void setReceivedPowerWatt(double receivedPowerWatt) {
        this.receivedPowerWatt = receivedPowerWatt;
    }

    public void setSubCarrierRatio(double subCarrierRatio) {
        this.subCarrierRatio = subCarrierRatio;
    }

    private boolean allowedToConnect;
    private double antennaGain;
    private boolean connected;
    private double currentTransmitPower;
    private double currentTransmitPowerIndBm;
    private boolean dropped;
    private String dropReason;
    private boolean isInSoftHandover;
    private double maxTxPower;
    private double minTxPower;
    private double mobilitySpeed;
    private double thermalNoise;
    private boolean upLinkMode = false;
    private int userId;

    private double achievedCI;
    private double achievedEcIor;
    private boolean droppedAsHighest;
    private double geometry;
    private boolean linkLevelDataPointFound;
    private int linkQualityExceptions = 0;
    private int multiPathChannel;
    private double oldAchievedCI;
    private int powerScaledUpCount = 0;
    private double receivedTrafficChannelPowerWatt;
    private double requiredEcIor;
    private double totalPowerReceivedFromBaseStationsInActiveSet;
    private double totalPowerReceivedFromBaseStationsNotInActiveSet;

    private double bitRateAchieved;
    private int requestedSubCarriers;
    private double SINRAchieved;

    private int disconnectAttempts;
    private double receivedPowerWatt;
    private double subCarrierRatio;

    public boolean isAllowedToConnect() {
        return allowedToConnect;
    }

    public double getAntennaGain() {
        return antennaGain;
    }

    public boolean isConnected() {
        return connected;
    }

    public double getCurrentTransmitPower() {
        return currentTransmitPower;
    }

    public double getCurrentTransmitPowerIndBm() {
        return currentTransmitPowerIndBm;
    }

    public boolean isDropped() {
        return dropped;
    }

    public String getDropReason() {
        return dropReason;
    }

    public boolean isInSoftHandover() {
        return isInSoftHandover;
    }

    public double getMaxTxPower() {
        return maxTxPower;
    }

    public double getMinTxPower() {
        return minTxPower;
    }

    public double getMobilitySpeed() {
        return mobilitySpeed;
    }

    public double getThermalNoise() {
        return thermalNoise;
    }

    public boolean isUpLinkMode() {
        return upLinkMode;
    }

    public int getUserId() {
        return userId;
    }

    public double getAchievedCI() {
        return achievedCI;
    }

    public double getAchievedEcIor() {
        return achievedEcIor;
    }

    public boolean isDroppedAsHighest() {
        return droppedAsHighest;
    }

    public double getGeometry() {
        return geometry;
    }

    public boolean isLinkLevelDataPointFound() {
        return linkLevelDataPointFound;
    }

    public int getLinkQualityExceptions() {
        return linkQualityExceptions;
    }

    public int getMultiPathChannel() {
        return multiPathChannel;
    }

    public double getOldAchievedCI() {
        return oldAchievedCI;
    }

    public int getPowerScaledUpCount() {
        return powerScaledUpCount;
    }

    public double getReceivedTrafficChannelPowerWatt() {
        return receivedTrafficChannelPowerWatt;
    }

    public double getRequiredEcIor() {
        return requiredEcIor;
    }

    public double getTotalPowerReceivedFromBaseStationsInActiveSet() {
        return totalPowerReceivedFromBaseStationsInActiveSet;
    }

    public double getTotalPowerReceivedFromBaseStationsNotInActiveSet() {
        return totalPowerReceivedFromBaseStationsNotInActiveSet;
    }

    public double getBitRateAchieved() {
        return bitRateAchieved;
    }

    public int getRequestedSubCarriers() {
        return requestedSubCarriers;
    }

    public double getSINRAchieved() {
        return SINRAchieved;
    }

    public int getDisconnectAttempts() {
        return disconnectAttempts;
    }

    public double getReceivedPowerWatt() {
        return receivedPowerWatt;
    }

    public double getReceivedPower() {
        return Mathematics.fromWatt2dBm( receivedPowerWatt );
    }

    public double getSubCarrierRatio() {
        return subCarrierRatio;
    }

    @Override
    public double calculateAntennaGainTo(double horizontalAngle, double verticalAngle) {
        return getAntennaGain();
    }
}
