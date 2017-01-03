package org.seamcat.simulation.cellular;

import org.seamcat.cdma.CDMADownlinkSystem;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.cdma.CdmaUserTerminal;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.model.Workspace;
import org.seamcat.model.simulation.result.EventResult;

/**
 * CDMA - Code Division Multiple Access DownLink victim simulation
 */
public class CDMADownLinkVictimSystemSimulation extends CellularVictimSystemSimulation {

    public CDMADownLinkVictimSystemSimulation(Workspace workspace) {
        super(workspace);
    }

    @Override
    public AbstractDmaSystem getVictim() {
        AbstractDmaSystem victim = victimSystem.get();
        if ( victim == null ) {
            victim = new CDMADownlinkSystem(workspace.getVictimSystemLink().getDMASystem());
            victim.setResults( workspace.getVictimSystemLink().getDMASystem().getResults());
            victimSystem.set( victim );
        }
        return victim;
    }

    private void calculateExternalInterference() {
        CDMADownlinkSystem system = (CDMADownlinkSystem) getVictim();
        for (int i = 0; i < system.getActiveUsers().size(); i++) {
            CdmaUserTerminal user = system.getActiveUsers().get(i);
            if ( user.getActiveList().size() > 0 ) {
                calculateExternalInterference(user, user.getActiveList().get(0));
            }
            user.calculateTotalInterference_dBm();
            user.calculateGeometry( system.getLinkLevelData().getInitialMinimumGeometry(), system.getLinkLevelData().getInitialMaximumGeometry() );
            user.findLinkLevelDataPoint( system.getLinkLevelData());
            if (user.getLinkLevelData().getEcIor() > CDMALinkLevelData.MAX_EC_IOR) {
                system.dropActiveUser(user);
                user.setDropReason("Ec/Ior requirement to high");
                i--;
            }
        }
    }

    @Override
    public void collect(EventResult eventResult) {
        CDMADownlinkSystem system = (CDMADownlinkSystem) getVictim();
        system.activateInterference();
        calculateExternalInterference();
        system.balanceInterferedSystem();
        super.collect(eventResult);
    }

    @Override
    protected double getSystemFrequency() {
        return getVictim().getFrequency();
    }
}
