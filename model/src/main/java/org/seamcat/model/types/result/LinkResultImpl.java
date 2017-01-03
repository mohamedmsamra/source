package org.seamcat.model.types.result;

import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.LinkResult;

import java.util.LinkedHashMap;

/**
 * If plugins need to get a mutable version of a LinkResult
 * this class it to be used (e.g. when instances of built in
 * Propagation Models are to be evaluated)
 *
 * Notice that this is not used by SEAMCAT internally so any
 * LinkResult retrieved from SEAMCAT cannot be cast to this
 * type (all values from SEAMCAT are immutable by plugins)
 */
public class LinkResultImpl implements LinkResult {

    private double rxNoiseFloor, txRxAngle, txRxDistance, txRxPathLoss,txPower;
    private double effectiveTxRxPathLoss, blockingAttenuation, frequency;
    private boolean sameBuilding;
    private AntennaResult rx, tx;
    private LinkedHashMap<String, Double> values = new LinkedHashMap<>();

    @Override
    public AntennaResult rxAntenna() {
        return rx;
    }

    public void setRxAntenna( AntennaResult rx ) {
        this.rx = rx;
    }

    @Override
    public AntennaResult txAntenna() {
        return tx;
    }

    public void setTxAntenna( AntennaResult tx ) {
        this.tx = tx;
    }

    @Override
    public double getTxRxAngle() {
        return txRxAngle;
    }

    public void setTxRxAngle( double txRxAngle) {
        this.txRxAngle = txRxAngle;
    }

    @Override
    public double getTxRxDistance() {
        return txRxDistance;
    }

    public void setTxRxDistance( double txRxDistance) {
        this.txRxDistance = txRxDistance;
    }

    @Override
    public double getTxRxPathLoss() {
        return txRxPathLoss;
    }

    public void setTxRxPathLoss( double txRxPathLoss) {
        this.txRxPathLoss = txRxPathLoss;
    }

    @Override
    public double getEffectiveTxRxPathLoss() {
        return effectiveTxRxPathLoss;
    }

    public void setEffectiveTxRxPathLoss( double effectiveTxRxPathLoss ) {
        this.effectiveTxRxPathLoss = effectiveTxRxPathLoss;
    }

    @Override
    public double getBlockingAttenuation() {
        return blockingAttenuation;
    }

    public void setBlockingAttenuation( double blockingAttenuation) {
        this.blockingAttenuation = blockingAttenuation;
    }

    @Override
    public double getTxPower() {
        return txPower;
    }

    public void setTxPower( double txPower) {
        this.txPower = txPower;
    }

    @Override
    public double getFrequency() {
        return frequency;
    }

    public void setFrequency( double frequency) {
        this.frequency = frequency;
    }

    @Override
    public boolean isTxRxInSameBuilding() {
        return sameBuilding;
    }

    public void setIsSameBuilding( boolean sameBuilding ) {
        this.sameBuilding = sameBuilding;
    }

    @Override
    public double getRxNoiseFloor() {
        return rxNoiseFloor;
    }

    public void setRxNoiseFloor( double rxNoiseFloor) {
        this.rxNoiseFloor = rxNoiseFloor;
    }

    @Override
    public LinkedHashMap<String, Double> getValues() {
        return values;
    }

    @Override
    public Double getValue(String name) {
        return values.get(name);
    }
}
