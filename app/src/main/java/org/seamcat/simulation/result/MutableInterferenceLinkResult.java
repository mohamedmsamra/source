package org.seamcat.simulation.result;

import org.seamcat.model.RadioSystem;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.InterferenceLinkResult;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.InterferenceLink;

import static org.seamcat.simulation.LocalEnvironmentSelector.pickLocalEnvironment;

public class MutableInterferenceLinkResult extends MutableLinkResult implements InterferenceLinkResult {

    private InterferenceLink interferenceLink;
    private MutableLinkResult victimLink;
    private MutableLinkResult interfererLink;
    private MutableSensingLinkResult sensingLinkResult;
    private double riRSSUnwantedValue;
    private double riRSSBlockingValue;
    private Double rxBandwidth;
    // this one can vary because of tri-sector settings
    private AntennaGain txAntennaGain;

    public MutableInterferenceLinkResult(InterferenceLink interferenceLink, MutableLinkResult victimSystemLink, MutableLinkResult interferingSystemLink) {
        this.interferenceLink = interferenceLink;
        this.victimLink = victimSystemLink;
        this.interfererLink = interferingSystemLink;

        txAntennaGain = interferenceLink.getInterferingSystem().getTransmitter().getAntennaGain();

        setLocalEnvironments(interfererLink, interferenceLink.getInterferingSystem());

        txAntenna().setLocalEnvironment(interfererLink.txAntenna().getLocalEnvironment());
        rxAntenna().setLocalEnvironment(victimLink.rxAntenna().getLocalEnvironment());

        Point2D it = interferingSystemLink.txAntenna().getPosition();
        Point2D vr = victimSystemLink.rxAntenna().getPosition();
        setTxRxAngle(Mathematics.calculateKartesianAngle(it, vr));
        setTxRxDistance(Mathematics.distance(it, vr));
    }

    private void setLocalEnvironments(MutableLinkResult link, RadioSystem system) {
        link.txAntenna().setLocalEnvironment(pickLocalEnvironment(system.getTransmitter().getLocalEnvironments()));
        link.rxAntenna().setLocalEnvironment(pickLocalEnvironment(system.getReceiver().getLocalEnvironments()));
    }

    @Override
    public double getTxRxDistance() {
        return Mathematics.distance(interfererLink.txAntenna().getPosition(), victimLink.rxAntenna().getPosition());
    }

    @Override
    public MutableAntennaResult txAntenna() {
        MutableAntennaResult tx = super.txAntenna();
        tx.setPosition( interfererLink.txAntenna().getPosition() );
        tx.setHeight( interfererLink.txAntenna().getHeight() );
        tx.setTilt( interfererLink.txAntenna().getTilt() );
        return tx;
    }

    @Override
    public MutableAntennaResult rxAntenna() {
        MutableAntennaResult rx = super.rxAntenna();
        rx.setPosition( victimLink.rxAntenna().getPosition());
        rx.setHeight(victimLink.rxAntenna().getHeight());
        rx.setTilt( victimLink.rxAntenna().getTilt());
        return rx;
    }

    @Override
    public double getRiRSSUnwantedValue() {
        return riRSSUnwantedValue;
    }

    public void setRiRSSUnwantedValue(double riRSSUnwantedValue) {
        this.riRSSUnwantedValue = riRSSUnwantedValue;
    }

    @Override
    public double getRiRSSBlockingValue() {
        return riRSSBlockingValue;
    }

    public void setRiRSSBlockingValue(double riRSSBlockingValue) {
        this.riRSSBlockingValue = riRSSBlockingValue;
    }

    @Override
    public MutableSensingLinkResult getSensingLinkResult() {
        return sensingLinkResult;
    }

    public void setSensingLinkResult( MutableSensingLinkResult sensingLinkResult ) {
        this.sensingLinkResult = sensingLinkResult;
    }

    @Override
    public double getFrequency() {
        return interfererLink.getFrequency();
    }

    @Override
    public double getTxPower() {
        return interfererLink.getTxPower();
    }

    @Override
    public InterferenceLink getInterferenceLink() {
        return interferenceLink;
    }

    @Override
    public MutableLinkResult getVictimSystemLink() {
        return victimLink;
    }

    @Override
    public MutableLinkResult getInterferingSystemLink() {
        return interfererLink;
    }

    public double getRxBandwidth() {
        if ( rxBandwidth == null ) {
            return interferenceLink.getVictimSystem().getReceiver().getBandwidth();
        }
        return rxBandwidth;
    }

    public void setRxBandwidth(double rxBandwidth) {
        this.rxBandwidth = rxBandwidth;
    }

    public AntennaGain getTxAntennaGain() {
        return txAntennaGain;
    }

    public void setTxAntennaGain(AntennaGain txAntennaGain) {
        this.txAntennaGain = txAntennaGain;
    }

}
