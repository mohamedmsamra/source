package org.seamcat.ofdma;

import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.model.mathematics.Mathematics;

public class OfdmaUplink extends AbstractDmaLink {

    protected OfdmaSystem system;
    private double[][] externalInterferenceVector_Watt;

    public OfdmaUplink(OfdmaBaseStation _cell, OfdmaMobile _user, OfdmaSystem system) {
        super(true, _cell, _user );
        this.system = system;
        setFrequency(system.getFrequency());
    }

    /**
     * compute the current received power at the BS since it is an uplink direction
     */
    @Override
    public double calculateCurrentReceivePower_dBm() {
        double chPower = getUserTerminal().getCurrentTransmitPowerIndBm();
        totalReceivedPower = chPower - getEffectivePathloss();
        return totalReceivedPower;
    }

    private double externalInterference_dBm = -1000;
    public double getExternalInterference_dBm() {
        return externalInterference_dBm;
    }

    /**
     * compute the external interference by combining the unwanted and the blocking interference effect
     *
     * @return the external interference in dBm (UL)
     */
    public double calculateExternalInterference_dBm() {
        double unw = 0;
        double bloc = 0;

        for (int i = 0;i < externalInterferenceVector_Watt.length;i++) {
            unw   += externalInterferenceVector_Watt[i][0];
            bloc  += externalInterferenceVector_Watt[i][1];
        }
        double sum = unw + bloc;

        externalInterference_dBm = Mathematics.fromWatt2dBm(sum);
        return externalInterference_dBm;
    }

    /**
     * compute the summation of the unwanted effect (in Watt) over all the links and return a dBm value
     *
     * @return SUM unwanted component in dBm
     */
    public double calculateExternalInterferenceUnwanted_dBm() {
        double unw = 0;

        for (int i = 0;i < externalInterferenceVector_Watt.length;i++) {
            unw   += externalInterferenceVector_Watt[i][0];
        }

        return Mathematics.fromWatt2dBm(unw);
    }

    /**
     * compute the summation of the blocking effect (in Watt) over all the links and return a dBm value
     *
     * @return SUM blocking component in dBm
     */
    public double calculateExternalInterferenceBlocking_dBm() {
        double bloc = 0;

        for (int i = 0;i < externalInterferenceVector_Watt.length;i++) {
            bloc  += externalInterferenceVector_Watt[i][1];
        }

        return Mathematics.fromWatt2dBm(bloc);
    }

    public void activateLink() {
        getUserTerminal().setServingLink(this);
        system.getActiveUsers().add(getUserTerminal());
        getUserTerminal().initializeInActiveConnections(this);
    }

    @Override
    public String toString() {
        return "OfdmaUplink between BS " + getBaseStation().getCellid() + " and Mobile " + getUserTerminal().getUserId();
    }

    public int getLinkIndex() {
        return getBaseStation().getLinkIndexOfActiveUser(this);
    }

    @Override
    public OfdmaBaseStation getBaseStation() {
        return (OfdmaBaseStation) super.getBaseStation();
    }

    @Override
    public OfdmaMobile getUserTerminal() {
        return (OfdmaMobile) super.getUserTerminal();
    }

    public double calculateFrequency() {
        return system.calculateFrequency(getLinkIndex());
    }

    public void initializeInterferenceVector(int size) {
        externalInterferenceVector_Watt = new double[size][2];
    }

    public void setExternalUnwanted(int index, double externalUnwanted) {
        externalInterferenceVector_Watt[index][0] = externalUnwanted;
    }

    public void setExternalBlocking(int index, double externalBlocking) {
        externalInterferenceVector_Watt[index][1] = externalBlocking;
    }
}
