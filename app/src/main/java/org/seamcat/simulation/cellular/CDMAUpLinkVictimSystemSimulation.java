package org.seamcat.simulation.cellular;

import org.seamcat.cdma.CDMAUplinkSystem;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.model.Workspace;
import org.seamcat.model.simulation.result.EventResult;

/**
 * CDMA - Code Division Multiple Access UpLink victim simulation
 */
public class CDMAUpLinkVictimSystemSimulation extends CellularVictimSystemSimulation {

    public CDMAUpLinkVictimSystemSimulation(Workspace workspace) {
        super(workspace);
    }

    @Override
    public AbstractDmaSystem getVictim() {
        AbstractDmaSystem victim = victimSystem.get();
        if ( victim == null ) {
            victim = new CDMAUplinkSystem(workspace.getVictimSystemLink().getDMASystem());
            victim.setResults( workspace.getVictimSystemLink().getDMASystem().getResults());
            victimSystem.set( victim );
        }
        return victim;
    }

    @Override
    public void collect(EventResult eventResult) {
        CDMAUplinkSystem system = (CDMAUplinkSystem) getVictim();
        system.activateInterference();
        calculateExternalInterference();
        system.balanceInterferedSystem();
        super.collect(eventResult);
    }


    private void calculateExternalInterference() {
        CDMAUplinkSystem system = (CDMAUplinkSystem) getVictim();
        for (AbstractDmaBaseStation baseStation : system.getAllBaseStations()) {
            if ( baseStation.getOldTypeActiveConnections().size() > 0 ) {
                calculateExternalInterference(baseStation, baseStation.getOldTypeActiveConnections().get(0));
            }
        }
    }

    @Override
    protected double getSystemFrequency() {
        return getVictim().getFrequency();
    }
}
