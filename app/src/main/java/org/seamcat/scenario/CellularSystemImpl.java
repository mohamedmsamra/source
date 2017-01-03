package org.seamcat.scenario;

import org.seamcat.model.cellular.CellularReceiverImpl;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.cellular.CellularTransmitterImpl;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.types.PropagationModel;

public class CellularSystemImpl implements CellularSystem {

    private String name;
    private Distribution frequency;
    private CellularReceiverImpl receiver;
    private CellularTransmitterImpl transmitter;
    private CellularLinkImpl cellularLink;
    private CDMASettingsImpl cdmaSettings;
    private OFDMASettingsImpl OFDMASettings;
    private double receiverNoiseFigure;
    private double minimumCouplingLoss;
    private double handoverMargin;
    private double systemBandwidth;
    private int usersPerCell;
    private boolean upLink;
    private CellularLayoutImpl layout;

    public CellularSystemImpl(String name, Distribution frequency, CellularReceiverImpl receiver, CellularTransmitterImpl transmitter, PropagationModel propagationModel, BaseStationImpl bs, MobileStationImpl ms) {
        this.name = name;
        this.frequency = frequency;
        this.receiver = receiver;
        this.transmitter = transmitter;
        this.cellularLink = new CellularLinkImpl(propagationModel, ms, bs);
        receiverNoiseFigure = 4.0;
        minimumCouplingLoss = 70;
        handoverMargin = 4;
        systemBandwidth = 1.25;
        usersPerCell = 20;
        layout = new CellularLayoutImpl();
    }

    @Override
    public CellularLinkImpl getLink() {
        return cellularLink;
    }

    @Override
    public CellularLayoutImpl getLayout() {
        return layout;
    }

    @Override
    public Distribution getFrequency() {
        return frequency;
    }

    @Override
    public String getDescription() {
        if ( isUpLink() ) {
            if ( getOFDMASettings() != null ) {
                return "Victim OFDMA is uplink - VR is reference cell BS";
            } else {
                return "Victim CDMA is uplink - VR is reference cell BS";
            }
        } else {
            return "Cellular Victim is downlink - VR's are MS's connected to reference cell";
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CellularReceiverImpl getReceiver() {
        return receiver;
    }

    @Override
    public CellularTransmitterImpl getTransmitter() {
        return transmitter;
    }

    @Override
    public boolean isUpLink() {
        return upLink;
    }

    public void setUpLink( boolean upLink ) {
        this.upLink = upLink;
    }

    @Override
    public CDMASettingsImpl getCDMASettings() {
        return cdmaSettings;
    }

    @Override
    public OFDMASettingsImpl getOFDMASettings() {
        return OFDMASettings;
    }

    @Override
    public double getReceiverNoiseFigure() {
        return receiverNoiseFigure;
    }

    public void setReceiverNoiseFigure(double receiverNoiseFigure ) {
        this.receiverNoiseFigure = receiverNoiseFigure;
    }

    @Override
    public double getHandoverMargin() {
        return handoverMargin;
    }

    public void setHandoverMargin( double handoverMargin ) {
        this.handoverMargin = handoverMargin;
    }

    @Override
    public double getBandwidth() {
        return systemBandwidth;
    }

    public void setBandwidth(double bandwidth ) {
        this.systemBandwidth = bandwidth;
    }

    @Override
    public double getMinimumCouplingLoss() {
        return minimumCouplingLoss;
    }

    public void setMinimumCouplingLoss( double minimumCouplingLoss ) {
        this.minimumCouplingLoss = minimumCouplingLoss;
    }

    public void setCDMASettings(CDMASettingsImpl cdmaSettings) {
        this.cdmaSettings = cdmaSettings;
    }

    public void setOFDMASettings(OFDMASettingsImpl ofdmaSettings) {
        this.OFDMASettings = ofdmaSettings;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFrequency(Distribution frequency) {
        this.frequency = frequency;
    }

    public void setReceiver(CellularReceiverImpl receiver) {
        this.receiver = receiver;
    }

    public void setTransmitter(CellularTransmitterImpl transmitter) {
        this.transmitter = transmitter;
    }

    @Override
    public int getUsersPerCell() {
        return usersPerCell;
    }

    public void setUsersPerCell( int usersPerCell ) {
        this.usersPerCell = usersPerCell;
    }


    @Override
    public String toString() {
        return getName();
    }
}
