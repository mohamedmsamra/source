package org.seamcat.simulation.cellular;

import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMADownlinkSystem;
import org.seamcat.cdma.CDMAUplinkSystem;
import org.seamcat.dmasystems.*;
import org.seamcat.model.Scenario;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.correlation.CorrelationModeCalculator;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.InterferenceLinkSimulation;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.ofdma.DownlinkOfdmaSystem;
import org.seamcat.ofdma.OfdmaUplink;
import org.seamcat.ofdma.UplinkOfdmaSystem;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;

import java.util.ArrayList;
import java.util.List;

public class CellularInterfererInterferenceLinkSimulation implements InterferenceLinkSimulation<CellularSystem> {

    private static Logger LOG = Logger.getLogger(CellularInterfererInterferenceLinkSimulation.class);

    public CellularInterfererInterferenceLinkSimulation(InterferenceLink<CellularSystem> link) {
        org.seamcat.model.core.InterferenceLink interferenceLink = (org.seamcat.model.core.InterferenceLink) link;
        AbstractDmaSystem copy = copy(interferenceLink.getInterferingLink().getDMASystem());
        copy.setResults( interferenceLink.getInterferingLink().getDMASystem().getResults());
        system = copy;
    }

    private AbstractDmaSystem copy( AbstractDmaSystem system ) {
        if ( system.isCdma() ) {
            if ( system.isUplink() ){
                return new CDMAUplinkSystem( system );
            } else {
                return new CDMADownlinkSystem( system );
            }
        } else {
            if ( system.isUplink() ){
                return new UplinkOfdmaSystem(system);
            } else {
                return new DownlinkOfdmaSystem(system);
            }
        }
    }

    private AbstractDmaSystem system;

    public AbstractDmaSystem getSystem() {
        return system;
    }

    @Override
    public void simulate(Scenario scenario, EventResult result, InterferenceLink<CellularSystem> link, Point2D victimSystemPosition) {
        AbstractDmaSystem dmasystem = system;
        if (LOG.isDebugEnabled()) {
            CellularSystem is = link.getInterferingSystem();
            if (is.getCDMASettings() != null) {
                LOG.debug("The Interferer is a CDMA system");
            } else if (is.getOFDMASettings() != null) {
                LOG.debug("The Interferer is a OFDMA system");
            }
        }

        // this link is only used for calculating the position of the interfering system
        MutableLinkResult tmplink = new MutableLinkResult();
        CorrelationModeCalculator.itVrLoc(tmplink, victimSystemPosition, link);

        dmasystem.initialize((MutableEventResult) result);
        dmasystem.resetSystem();
        dmasystem.setLocation( tmplink.txAntenna().getPosition() );
        dmasystem.generateSystemCells();

        if (dmasystem.isOfdma()) {
            dmasystem.getSystemSettings().getLayout().setMeasureInterferenceFromEntireCluster(true);
        }

        if (dmasystem.isOfdma() && dmasystem.isDownlink()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Skipping simulation of OFDMA Downlink Interferer (" + link.getInterferingSystem().getName() + ")");
            }
            DownlinkOfdmaSystem ofdma = (DownlinkOfdmaSystem) dmasystem;
            for (AbstractDmaBaseStation base : ofdma.getAllBaseStations()) {
                base.setSubCarriersInUse(ofdma.getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation());
            }

        } else {
            long start = System.currentTimeMillis();
            dmasystem.simulate();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Balanced CDMA System in " + (System.currentTimeMillis() - start) + " millis");
            }
        }
        getInterferenceLinkVictimSimulation(scenario, link, result );

        CellularVictimSystemSimulation.cellularInternals((MutableEventResult) result, dmasystem, "interferer");
    }

    private void getInterferenceLinkVictimSimulation(Scenario scenario, InterferenceLink link, EventResult result) {
        MutableEventResult currentResult = (MutableEventResult) result;
        AbstractDmaSystem<?> dmasystem = system;
        boolean entireCluster = dmasystem.getSystemSettings().getLayout().measureInterferenceFromEntireCluster();

        if (dmasystem.isUplink()) {
            // OFDMA uplink
            if (dmasystem.isOfdma()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Interfering OFDMA system is uplink - IT's are MS's connected to reference cell ID: " + dmasystem.getReferenceCell().getCellid() + " Sector ID: " + dmasystem.getReferenceCell().getSectorId());
                }
                UplinkOfdmaSystem system = (UplinkOfdmaSystem) dmasystem;

                List<? extends AbstractDmaLink> users = system.getActiveConnections();
                for (AbstractDmaLink user : users) {
                    final OfdmaUplink m = (OfdmaUplink) user;
                    ActiveInterferer in = new ActiveInterferer(scenario, link, m.getUserTerminal().getPosition(),
                            m.getUserTerminal().getCurrentTransmitPowerIndBm(),
                            m.calculateFrequency(),
                            m.getUserTerminal().getAntennaHeight(),
                            m.getUserTerminal().getAntennaTilt(),
                            m.getUserAntGain(),
                            0,
                            0) {
                        @Override
                        public void applyInterferenceLinkCalculations(MutableInterferenceLinkResult link) {
                            CellularCalculations.setRxTxAngleElevation(link, link.getInterferenceLink().getVictimSystem().getReceiver(),
                                    m.getUserTerminal().getPosition(), m.getUserTerminal().getAntennaHeight());
                            CellularCalculations.setTxRxAngleElevation(link, false, link.getVictimSystemLink().rxAntenna().getHeight() );
                        }
                    };

                    currentResult.addInterferingElement(in);
                }
            }
            // CDMA uplink
            else {
                // IT is Ms's connected to reference cell or all Mobiles in
                // cluster
                double tempUw = 0, tempBl = 0;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Interfering CDMA system is uplink - IT's are MS's connected to reference cell ID: " + dmasystem.getReferenceCell().getCellid() + " Sector ID: " + dmasystem.getReferenceCell().getSectorId());
                }
                List<AbstractDmaMobile> list = new ArrayList<AbstractDmaMobile>();

                if (entireCluster) {
                    list.addAll(dmasystem.getActiveUsers());
                } else {
                    for (AbstractDmaLink dmaLink : dmasystem.getReferenceCell().getOldTypeActiveConnections()) {
                        list.add(dmaLink.getUserTerminal());
                    }
                }

                for (final AbstractDmaMobile user : list) {
                    double txPower = Mathematics.fromWatt2dBm(user.getCurrentTransmitPower());
                    double txGain = user.calculateAntennaGainTo(0, 0);
                    final double txHeight = user.getAntennaHeight();
                    double txTilt = user.getAntennaTilt();

                    ActiveInterferer interferer = new ActiveInterferer(scenario, link, user.getPosition(), txPower,
                            dmasystem.getFrequency(), txHeight, txTilt, txGain,0, 0 ){
                        @Override
                        public void applyInterferenceLinkCalculations(MutableInterferenceLinkResult link) {
                            CellularCalculations.setRxTxAngleElevation( link, link.getInterferenceLink().getVictimSystem().getReceiver(),
                                    user.getPosition(), txHeight);
                            CellularCalculations.setTxRxAngleElevation( link, false, link.getVictimSystemLink().rxAntenna().getHeight() );

                        }
                    };

                    currentResult.addInterferingElement( interferer );
                }
            }
        } else { // Downlink
            // IT is Reference cell BS or all BS in cluster
            if (LOG.isDebugEnabled()) {
                LOG.debug("Interfering OFDMA system is downlink - IT is reference cell BS:" + dmasystem.getReferenceCell().getCellid() + " Sector ID: " + dmasystem.getReferenceCell().getSectorId());
            }
            List<AbstractDmaBaseStation> itCells = new ArrayList<AbstractDmaBaseStation>();

            if (entireCluster) {
                itCells.addAll(dmasystem.getAllBaseStations());
            } else {
                itCells.add(dmasystem.getReferenceCell());
            }
            // DMA downlink
            for (final AbstractDmaBaseStation cell : itCells) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Interferer Cell ID: " + cell.getCellid() + ", Sector ID: " + cell.getSectorId());
                }

                double txPower = cell.getCurrentTransmitPower_dBm();
                final double txHeight = cell.getAntennaHeight();
                double txTilt = cell.getAntennaTilt();
                ActiveInterferer interferer = new ActiveInterferer(scenario, link, cell.getPosition(), txPower, dmasystem.getFrequency(),
                        txHeight, txTilt, cell.getAntennaGain(), link.getInterferingSystem().getName()
                        + " - BS #" + cell.getCellid() + " - Sector ID: " + cell.getSectorId(),0, 0) {
                    @Override
                    public void applyInterferenceLinkCalculations(MutableInterferenceLinkResult link) {
                        link.setTxAntennaGain(cell.getAntennaGain());
                        CellularCalculations.setRxTxAngleElevation(link, link.getInterferenceLink().getVictimSystem().getReceiver(),
                                cell.getPosition(), txHeight);
                        CellularCalculations.setTxRxAngleElevation( link, true, link.getVictimSystemLink().rxAntenna().getHeight() );
                    }
                };

                currentResult.addInterferingElement( interferer );
            }
        }
    }
}
