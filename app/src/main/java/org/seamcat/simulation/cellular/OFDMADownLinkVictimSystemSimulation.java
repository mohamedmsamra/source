package org.seamcat.simulation.cellular;

import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.dmasystems.ActiveInterferer;
import org.seamcat.model.Workspace;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.Interferer;
import org.seamcat.ofdma.DownlinkOfdmaMobile;
import org.seamcat.ofdma.DownlinkOfdmaSystem;
import org.seamcat.ofdma.OfdmaExternalInterferer;
import org.seamcat.simulation.calculator.InterferenceCalculator;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;
import org.seamcat.simulation.result.MutableSimulationElement;

/**
 * OFDMA - Orthogonal Frequency Division Multiple Access DownLink victim simulation
 */
public class OFDMADownLinkVictimSystemSimulation extends CellularVictimSystemSimulation {

    public OFDMADownLinkVictimSystemSimulation(Workspace workspace) {
        super(workspace);
    }

    @Override
    public AbstractDmaSystem getVictim() {
        AbstractDmaSystem victim = victimSystem.get();
        if ( victim == null ) {
            victim = new DownlinkOfdmaSystem(workspace.getVictimSystemLink().getDMASystem());
            victim.setResults( workspace.getVictimSystemLink().getDMASystem().getResults());
            victimSystem.set( victim );
        }
        return victim;
    }

    @Override
    public void collect(EventResult eventResult) {
        DownlinkOfdmaSystem system = (DownlinkOfdmaSystem) getVictim();
        system.activateInterference();
        calculateExternalInterference();
        system.simulateLinkSpecifics();
        super.collect(eventResult);
    }

    private void calculateExternalInterference() {
        DownlinkOfdmaSystem system = (DownlinkOfdmaSystem) getVictim();
        for (DownlinkOfdmaMobile mobile : system.getAllActiveUsers()) {
            calculateExternalInterference(mobile);
        }
    }

    protected void calculateExternalInterference(DownlinkOfdmaMobile mobile) {
        // Calculate and assign external interference
        double resourceBlockBandwidthCorrection = getBandwidthCorrection(mobile);

        MutableEventResult eventResult = getVictim().getEventResult();
        double extIntUnw = 0, extIntBlo = 0;
        for (ActiveInterferer externalInterferer : eventResult.getInterferingElements()) {
            double angle = Mathematics.calculateKartesianAngle(mobile.getPosition(), externalInterferer.getPoint());
            double elevation = Mathematics.calculateElevation(mobile.getPosition(), mobile.getAntennaHeight(),
                    externalInterferer.getPoint(), externalInterferer.getAntennaHeight());

            MutableLinkResult victimLink = mobile.getServingLink().asLinkResult();

            MutableInterferenceLinkResult iLink = new MutableInterferenceLinkResult(externalInterferer.getInterferenceLink(), victimLink, externalInterferer.getLinkResult());
            iLink.rxAntenna().setGain(mobile.calculateAntennaGainTo(angle,elevation));
            iLink.getVictimSystemLink().setFrequency(mobile.getFrequency());
            iLink.setRxBandwidth(mobile.getBandwidth());
            iLink.setTxRxDistance(Mathematics.distance(iLink.getInterferingSystemLink().txAntenna().getPosition(), mobile.getPosition()));
            externalInterferer.applyInterferenceLinkCalculations( iLink );
            externalInterferer.calculateLosses(mobile.getPosition(), mobile.getAntennaHeight(), iLink);

            InterferenceCalculator.unwantedInterference(externalInterferer.getScenario(), iLink);
            extIntUnw += Mathematics.dB2Linear(iLink.getRiRSSUnwantedValue());

            iLink.getVictimSystemLink().setFrequency(getSystemFrequency());
            InterferenceCalculator.blockingInterference(externalInterferer.getScenario(), iLink);
            extIntBlo += Mathematics.dB2Linear(iLink.getRiRSSBlockingValue() + resourceBlockBandwidthCorrection);

            handleInterferer(mobile, externalInterferer, iLink);
        }
        mobile.setExternalInterferenceUnwanted(Mathematics.linear2dB(extIntUnw));
        mobile.setExternalInterferenceBlocking(Mathematics.linear2dB(extIntBlo));
    }

    @Override
    protected double getSystemFrequency() {
        return getVictim().getFrequency();
    }

    @Override
    protected double getBandwidthCorrection(MutableSimulationElement element) {
        DownlinkOfdmaMobile mobile = (DownlinkOfdmaMobile) element;
        return Mathematics.linear2dB(((double) mobile.getRequestedSubCarriers()) / getVictim().getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation());
    }

    @Override
    protected void handleInterferer(MutableSimulationElement element, Interferer interferer, MutableInterferenceLinkResult iLink) {
        DownlinkOfdmaMobile mobile = (DownlinkOfdmaMobile) element;
        OfdmaExternalInterferer ext = new OfdmaExternalInterferer(interferer, iLink.getRiRSSBlockingValue() + getBandwidthCorrection(element),
                iLink.getRiRSSUnwantedValue());
        mobile.addExternalInterferer(ext);
    }
}
