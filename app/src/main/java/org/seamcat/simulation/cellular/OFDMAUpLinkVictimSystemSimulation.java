package org.seamcat.simulation.cellular;

import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.dmasystems.ActiveInterferer;
import org.seamcat.model.Workspace;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.GaussianDistributionImpl;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.ofdma.*;
import org.seamcat.simulation.calculator.InterferenceCalculator;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableSimulationElement;

import java.util.List;

/**
 * OFDMA - Orthogonal Frequency Division Multiple Access UpLink victim simulation
 */
public class OFDMAUpLinkVictimSystemSimulation extends CellularVictimSystemSimulation {

    public OFDMAUpLinkVictimSystemSimulation(Workspace workspace) {
        super(workspace);
    }

    @Override
    public AbstractDmaSystem getVictim() {
        AbstractDmaSystem victim = victimSystem.get();
        if ( victim == null ) {
            victim = new UplinkOfdmaSystem(workspace.getVictimSystemLink().getDMASystem());
            victim.setResults( workspace.getVictimSystemLink().getDMASystem().getResults());
            victimSystem.set( victim );
        }
        return victim;
    }

    @Override
    public void collect(EventResult eventResult) {
        victimSystem.get().activateInterference();
        calculateExternalInterference();
        ((UplinkOfdmaSystem)victimSystem.get()).simulateLinkSpecifics();
        super.collect(eventResult);
    }

    private void calculateExternalInterference() {
        UplinkOfdmaSystem system = (UplinkOfdmaSystem) victimSystem.get();
        AbstractDistribution pathlossVariance = null;
        double x = 0, y = 0, a_ext = 0, b_ext = 0, correlationExternalInterferer = 0;

        List<ActiveInterferer> interferers = getExternalInterferers(getVictim().getEventResult());

        for (AbstractDmaLink l : system.getActiveConnections()) {
            OfdmaUplink link = (OfdmaUplink) l;
            link.initializeInterferenceVector(interferers.size());
        }

        int interfererIndex = 0;
        for (ActiveInterferer interferer : interferers ) {
            //logInterferer(interferer);

            if (interferer.isPathlossCorrelated()) {
                pathlossVariance = new GaussianDistributionImpl(0, interferer.getPathlossVariance());
                x = pathlossVariance.trial();
                correlationExternalInterferer = interferer.getCorrelationFactor();

                a_ext = Math.sqrt(Math.abs(correlationExternalInterferer));
                b_ext = Math.sqrt(1 - Math.abs(correlationExternalInterferer));
            }

            int pos = 0;
            for (AbstractDmaBaseStation[] bases : system.getBaseStationArray()) {
                int i = 0;

                double pathloss = 0;

                if (interferer.isPathlossCorrelated()) {
                    y = pathlossVariance.trial();
                }

                for (AbstractDmaBaseStation base : bases) {
                    UplinkOfdmaBaseStation<UplinkOfdmaMobile> basestation = (UplinkOfdmaBaseStation<UplinkOfdmaMobile>) base;

                    double horizontalAngle = Mathematics.calculateKartesianAngle(interferer.getPoint(), basestation.getPosition());
                    double elevationAngle = Mathematics.calculateElevation(interferer.getPoint(), interferer.getAntennaHeight(),
                            basestation.getPosition(), basestation.getAntennaHeight());

                    double antennaGain =  basestation.calculateAntennaGainTo(horizontalAngle, elevationAngle);

                    if (interferer.isPathlossCorrelated()) {
                        //assume 50% correlation between all sites
                        if (i == 0) {
                            pathloss = interferer.getLinkResult().getTxRxPathLoss();

                            //Modify calculated pathloss with user defined correlation factor
                            pathloss += a_ext*x + b_ext*y;
                        }

                        interferer.getLinkResult().setTxRxPathLoss(pathloss);
                    }

                    for (AbstractDmaLink l : basestation.getOldTypeActiveConnections()) {
                        OfdmaUplink link = (OfdmaUplink) l;
                        double resourceBlockBandwidthCorrection = getBandwidthCorrection(link.getUserTerminal());
                        MutableInterferenceLinkResult iLink = new MutableInterferenceLinkResult(interferer.getInterferenceLink(), l.asLinkResult(), interferer.getLinkResult());
                        iLink.setRxBandwidth(link.getUserTerminal().getBandwidth());

                        iLink.rxAntenna().setGain(antennaGain);
                        iLink.setTxRxDistance(Mathematics.distance(interferer.getLinkResult().txAntenna().getPosition(), basestation.getPosition()));
                        interferer.applyInterferenceLinkCalculations( iLink );
                        interferer.calculateLosses(basestation.getPosition(), basestation.getAntennaHeight(), iLink);

                        LOG.debug("BS Victim - Reference Bandwidth: " + link.getUserTerminal().getBandwidth() + " MHz");
                        iLink.getVictimSystemLink().setFrequency(link.calculateFrequency());//For the computation of the unwanted, the SINR is taken for each link

                        InterferenceCalculator.unwantedInterference( interferer.getScenario(), iLink);
                        double unw = Mathematics.fromdBm2Watt(iLink.getRiRSSUnwantedValue());
                        iLink.getVictimSystemLink().setFrequency(getSystemFrequency());//For the computation of the blocking, the system frequency is used.
                        InterferenceCalculator.blockingInterference(interferer.getScenario(), iLink);
                        double iRSSBlockingValue = iLink.getRiRSSBlockingValue() + resourceBlockBandwidthCorrection;
                        double bloc= Mathematics.fromdBm2Watt(iRSSBlockingValue);

                        link.setExternalUnwanted(interfererIndex, unw);
                        link.setExternalBlocking(interfererIndex, bloc);

                        //Store interferer
                        OfdmaExternalInterferer ext = new OfdmaExternalInterferer(interferer, iRSSBlockingValue, iLink.getRiRSSUnwantedValue());
                        basestation.addExternalInterferer(ext);

                        basestation.setExternalInterferenceUnwanted(iRSSBlockingValue);
                        basestation.setExternalInterferenceBlocking(iRSSBlockingValue);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug(" BS victim - externalInterferenceUnwanted: " + iLink.getRiRSSUnwantedValue()+" dBm");
                            LOG.debug(" BS victim - externalInterferenceBlocking: " + iRSSBlockingValue+" dBm");
                        }
                    }

                    i++;
                }
                pos++;
            }
            interfererIndex++;
        }
    }

    @Override
    protected double getBandwidthCorrection(MutableSimulationElement element) {
        UplinkOfdmaMobile mobile = (UplinkOfdmaMobile) element;
        return Mathematics.linear2dB(((double) mobile.getRequestedSubCarriers()) / getVictim().getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation());
    }

    @Override
    protected double getSystemFrequency() {
        return victimSystem.get().getFrequency();
    }
}
