package org.seamcat.simulation.generic;

import org.apache.log4j.Logger;
import org.seamcat.dmasystems.ActiveInterferer;
import org.seamcat.model.Scenario;
import org.seamcat.model.correlation.CorrelationModeCalculator;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.simulation.InterferenceLinkSimulation;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.scenario.WorkspaceScenario;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;

public class GenericInterfererInterferenceLinkSimulation implements InterferenceLinkSimulation<GenericSystem> {

    private static Logger LOG = Logger.getLogger(GenericInterfererInterferenceLinkSimulation.class);

    @Override
    public void simulate(Scenario scenario, EventResult result, final InterferenceLink<GenericSystem> link, Point2D victimSystemPosition ) {
        LOG.debug("-------- Calculation of iRSS Values --------");
        WorkspaceScenario wsScenario = (WorkspaceScenario) scenario;
        MutableEventResult current = (MutableEventResult) result;

        if (LOG.isDebugEnabled()) {
            LOG.debug("The Interferer is a generic system ");
            LOG.debug("Starting loop on " + subLinks(link) + " active interferers");
        }

        for (int z = 0; z < subLinks(link); z++) {
            MutableLinkResult iLink = new MutableLinkResult();
            iLink.setValue(GenericSystem.COVERAGE_RADIUS, wsScenario.getCoverageRadius(link));
            if (LOG.isDebugEnabled()) {
                LOG.debug("************* Processing interferer #" + z + "*****************");
            }

            GenericSystemPlugin.handleInterferenceLink(victimSystemPosition, current, link, iLink);

            ActiveInterferer interferer = new ActiveInterferer(scenario, link, iLink) {
                @Override
                public void applyInterferenceLinkCalculations(MutableInterferenceLinkResult link11) {
                    CorrelationModeCalculator.itVrPathAntAziElev(link11, link.getVictimSystem(), link.getInterferingSystem());
                }
            };
            current.addInterferingElement(interferer);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding external interferer to victim: " + interferer);
                LOG.debug("Processed interferer #" + z + "\n*******************************************");
            }
        }
    }

    private int subLinks( InterferenceLink link ){
        if (link.getInterferingLinkRelativePosition().useCoLocatedWith() ) {
            return link.getInterferingLinkRelativePosition().getCoLocatedWith().getInterferingLinkRelativePosition().getNumberOfActiveTransmitters();
        } else {
            return link.getInterferingLinkRelativePosition().getNumberOfActiveTransmitters();
        }
    }

}
