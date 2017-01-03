package org.seamcat.ofdma;

import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.model.functions.Point2D;
import org.seamcat.scenario.OFDMADownLinkImpl;

import java.util.ArrayList;
import java.util.List;

public class DownlinkOfdmaSystem extends OfdmaSystem<DownlinkOfdmaMobile>{

    private void init() {
        getSystemSettings().getOFDMASettings().setUpLinkSettings(null);
        getSystemSettings().getOFDMASettings().setDownLinkSettings(new OFDMADownLinkImpl());
        getSystemSettings().setUpLink(false);
    }

    public DownlinkOfdmaSystem(AbstractDmaSystem<?> dma) {
        super(dma);
        init();
        if (dma instanceof DownlinkOfdmaSystem) {
            getSystemSettings().setOFDMASettings(dma.getSystemSettings().getOFDMASettings());
        }

    }

    @Override
    protected void configureBaseStation(AbstractDmaBaseStation base) {
        base.resetBaseStation();
        base.setMaximumTransmitPower_dBm(getMaximumTransmitPowerBasestation());
    }

    @Override
    protected OfdmaBaseStation generateBaseStation(
            Point2D position, int cellid, double antennaHeight, double antennaTilt, int sectorid, boolean triSector) {
        return new OfdmaBaseStation(position, this, cellid, antennaHeight, antennaTilt, sectorid);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected OfdmaBaseStation[][] generateBaseStationArray() {
        return new OfdmaBaseStation[getNumberOfCellSitesInPowerControlCluster()]
                [cellsPerSite()];
    }

    @Override
    public DownlinkOfdmaMobile generateUserTerminal() {
        return new DownlinkOfdmaMobile(new Point2D(0, 0), this,
                useridcount++, getSystemSettings().getLink().getMobileStation().getAntennaGain().trial(),
                getSystemSettings().getLink().getMobileStation().getAntennaHeight().trial());
    }

    protected void initializeBaseStations() {
        for (AbstractDmaBaseStation base : getAllBaseStations()) {
            ((OfdmaBaseStation)base).calculateCurrentTransmitPower_Watt();
        }
    }

    @Override
    public void simulateLinkSpecifics() {
        initializeBaseStations();
    }

    public double getMaximumTransmitPowerBasestation() {
        return getSystemSettings().getOFDMASettings().getDownLinkSettings().getBSMaximumTransmitPower();
    }

    @Override
    public List<OfdmaVictim> getVictims() {
        List<OfdmaVictim> victims = new ArrayList<OfdmaVictim>();
        victims.addAll(getAllActiveUsers());
        return victims;
    }
}