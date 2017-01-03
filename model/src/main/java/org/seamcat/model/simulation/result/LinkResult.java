package org.seamcat.model.simulation.result;

import java.util.LinkedHashMap;

public interface LinkResult {

    /**
     * Receiver antenna results in the direction of the transmitter
     */
    AntennaResult rxAntenna();

    /**
     * Transmitter antenna results in the direction of the receiver
     */
    AntennaResult txAntenna();


    /**
     * Angle between transmitter and receiver.
     */
    double getTxRxAngle();

    /**
     * Distance between transmitter and receiver.
     */
    double getTxRxDistance();

    /**
     * Path loss between transmitter-receiver path.
     */
    double getTxRxPathLoss();

    /**
     * Effective Path loss between transmitter-receiver path. It takes into account the mcl value
     */
    double getEffectiveTxRxPathLoss();

    double getBlockingAttenuation();

    double getTxPower();

    double getFrequency();

    boolean isTxRxInSameBuilding();

    double getRxNoiseFloor();

    LinkedHashMap<String, Double> getValues();

    Double getValue( String name );
}
