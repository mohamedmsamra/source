package org.seamcat.simulation.result;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.distributions.GaussianDistributionImpl;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.LocalEnvironment;

import java.util.Comparator;
import java.util.LinkedHashMap;

public class MutableLinkResult implements LinkResult {



    private double trialFrequency;

    private double txRxAngle;
    private double txRxDistance;
    private double blockingAttenuation;
    private double txPower;
    private boolean txRxInSameBuilding;
    private double rxNoiseFloor;
    private MutableAntennaResult tx, rx;
    private LinkedHashMap<String, Double> values;
    private LinkedHashMap<String, Boolean> boolValues;

    public MutableLinkResult() {
        tx = new MutableAntennaResult();
        rx = new MutableAntennaResult();
        values = new LinkedHashMap<>();
        boolValues = new LinkedHashMap<>();
    }

    @Override
    public MutableAntennaResult rxAntenna() {
        return rx;
    }

    @Override
    public MutableAntennaResult txAntenna() {
        return tx;
    }

    @Override
    public double getTxRxAngle() {
        return txRxAngle;
    }

    public void setTxRxAngle(double txRxAngle) {
        this.txRxAngle = txRxAngle;
    }

    @Override
    public double getTxRxDistance() {
        return txRxDistance;
    }

    public void setTxRxDistance(double txRxDistance) {
        this.txRxDistance = txRxDistance;
    }

    @Override
    public double getTxRxPathLoss() {
        Double aDouble = values.get(RadioSystem.TX_RX_PATHLOSS);
        if ( aDouble == null ) return 0;
        return aDouble;
    }

    public void setTxRxPathLoss(double txRxPathLoss) {
        values.put(RadioSystem.TX_RX_PATHLOSS, txRxPathLoss);
    }

    @Override
    public double getEffectiveTxRxPathLoss() {
        Double aDouble = values.get(RadioSystem.EFFECTIVE_PATHLOSS);
        if ( aDouble == null ) return 0;
        return aDouble;
    }

    public void setEffectiveTxRxPathLoss(double effectiveTxRxPathLoss) {
        values.put(RadioSystem.EFFECTIVE_PATHLOSS, effectiveTxRxPathLoss);
    }

    @Override
    public double getBlockingAttenuation() {
        return blockingAttenuation;
    }

    public void setBlockingAttenuation(double blockingAttenuation) {
        this.blockingAttenuation = blockingAttenuation;
    }

    @Override
    public double getTxPower() {
        return txPower;
    }

    public void setTxPower(double txPower) {
        this.txPower = txPower;
    }

    @Override
    public double getFrequency() {
        return trialFrequency;
    }

    public void setFrequency( double frequency ) {
        this.trialFrequency = frequency;
    }

    @Override
    public boolean isTxRxInSameBuilding() {
        return txRxInSameBuilding;
    }

    public void trialTxRxInSameBuilding() {
        if (tx.getLocalEnvironment().getEnvironment() == LocalEnvironment.Environment.Outdoor && rx.getLocalEnvironment().getEnvironment() == LocalEnvironment.Environment.Outdoor) {
            txRxInSameBuilding = false;
        } else if (tx.getLocalEnvironment().getEnvironment() == LocalEnvironment.Environment.Indoor && rx.getLocalEnvironment().getEnvironment() == LocalEnvironment.Environment.Indoor){
            txRxInSameBuilding = txRxDistance < 0.020 || txRxDistance < 0.050 && new GaussianDistributionImpl(0, 1).trial() < (0.05 - txRxDistance) / 0.030;
        } else {
            txRxInSameBuilding = false;
        }
    }

    @Override
    public double getRxNoiseFloor() {
        return rxNoiseFloor;
    }

    public void setRxNoiseFloor(double rxNoiseFloor ) {
        this.rxNoiseFloor = rxNoiseFloor;
    }

    @Override
    public LinkedHashMap<String, Double> getValues() {
        return values;
    }

    @Override
    public Double getValue( String name ) {
        return values.get(name);
    }

    public void setValue(String name, double value) {
        values.put( name, value );
    }

    public void setValue(String name, boolean value) {
        boolValues.put(name, value);
    }

    public Boolean is( String name ) {
        return boolValues.get( name );
    }
}
