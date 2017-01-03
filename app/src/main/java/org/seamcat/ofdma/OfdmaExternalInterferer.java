package org.seamcat.ofdma;

import org.seamcat.model.simulation.result.Interferer;

public class OfdmaExternalInterferer {

    private Interferer interferer;
    private double externalBlocking;
    private double externalUnwanted;

    public OfdmaExternalInterferer(Interferer interferer, double blo, double unw ) {
        this.interferer = interferer;
        this.externalBlocking = blo;
        this.externalUnwanted = unw;
    }

    public String getExternalInterfererName() {
        return interferer.getName();
    }

    public double getExternalUnwanted() {
        return externalUnwanted;
    }

    public double getExternalBlocking() {
        return externalBlocking;
    }

    public double getPathloss() {
        return interferer.getLinkResult().getEffectiveTxRxPathLoss();
    }

    public double getDistance() {
        return interferer.getLinkResult().getTxRxDistance();
    }

    public double getTx_gain() {
        return interferer.getLinkResult().txAntenna().getGain();
    }

    public double getRx_gain() {
        return interferer.getLinkResult().rxAntenna().getGain();
    }

    public double getTxAntHeight() {
        return interferer.getLinkResult().txAntenna().getHeight();
    }

    public double getTxPower() {
        return interferer.getLinkResult().getTxPower();
    }

    public double getRxAntHeight() {
        return interferer.getLinkResult().rxAntenna().getHeight();
    }

    public Interferer getInterferer() {
        return interferer;
    }

    public double getMinimumCouplingLoss() {
        return interferer.getMinimumCouplingLoss();
    }
}
