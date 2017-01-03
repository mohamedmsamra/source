package org.seamcat.presentation.systems.cdma;

import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaMobile;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.Interferer;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.ofdma.OfdmaVictim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CDMAPlotModel {

    public boolean victimSystem;
    public ResultTypes preResults;
    public EventResult eventResult;
    public CellularSystem cellularSystem;
    public List<AbstractDmaMobile> activeUsers;
    public List<AbstractDmaMobile> inactiveUsers;
    public List<AbstractDmaMobile> droppedUsers;
    public AbstractDmaBaseStation[][] baseStations;
    public List<Interferer> externalInterferers;
    public AbstractDmaBaseStation referenceCell;
    public Point2D location;
    public double thermalNoise;
    public String name;

    // ofmda only
    public List<OfdmaVictim> ofdmaVictims;

    // ofdma.getProcessingGain()
    public double processingGain;

    // cdma only
    public int numberOfLLDFound;

    //round(Mathematics.fromWatt2dBm(((CDMADownlinkSystem)cdma).getMaxTrafficChannelPowerInWatt()))
    public double maxTrafficChannelPower;
    public double frequency;
    public double intercellDistance;


    public CellularSystem getCellularSystem() { return cellularSystem; }

    public List<AbstractDmaMobile> getActiveUsers() {
        return activeUsers;
    }

    public AbstractDmaBaseStation[][] getBaseStations() {
        return baseStations;
    }

    public List<Interferer> getExternalInterferers() {
        return externalInterferers;
    }

    public AbstractDmaBaseStation getReferenceCell() {
        return referenceCell;
    }

    public Point2D getLocation() {
        return location;
    }

    public boolean isVictimSystem() {
        return victimSystem;
    }

    public List<AbstractDmaBaseStation> getAllBaseStations() {
        List<AbstractDmaBaseStation> list = new ArrayList<AbstractDmaBaseStation>();
        if (baseStations != null) {
            for (AbstractDmaBaseStation[] bases : baseStations) {
                list.addAll(Arrays.asList(bases));
            }
        }
        return list;
    }

    public List<AbstractDmaMobile> getDroppedUsers() {
        return droppedUsers;
    }

    public List<OfdmaVictim> getOfdmaVictims() {
        return ofdmaVictims;
    }

    public List<AbstractDmaMobile> getInactiveUsers() {
        return inactiveUsers;
    }

    public ResultTypes getPreResults() {
        return preResults;
    }

    public int getNumberOfBaseStations() {
        return (cellularSystem.getLayout().getSectorSetup() == CellularLayout.SectorSetup.SingleSector ? 1 : 3) *
                getNumberOfCellSitesInPowerControlCluster();
    }

    private int getNumberOfCellSitesInPowerControlCluster() {
        switch ( cellularSystem.getLayout().getTierSetup() ) {
            case SingleCell: return 1;
            case OneTier: return 7;
            default: return 19;
        }
    }

    public EventResult getEventResult() {
        return eventResult;
    }

    public double getProcessingGain() {
        return processingGain;
    }

    public int getNumberOfLLDFound() {
        return numberOfLLDFound;
    }

    public double getMaxTrafficChannelPower() {
        return maxTrafficChannelPower;
    }

    public double getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        if ( name != null ) return name;
        return super.toString();
    }
}
