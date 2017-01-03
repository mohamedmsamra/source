package org.seamcat.simulation.cellular;

import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMASystem;
import org.seamcat.cdma.CDMAUplinkSystem;
import org.seamcat.dmasystems.*;
import org.seamcat.model.Scenario;
import org.seamcat.model.Workspace;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.CollectedResults;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.simulation.VictimSystemSimulation;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.Interferer;
import org.seamcat.model.simulation.result.PreSimulationResults;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.types.EventProcessing;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.DoubleResultType;
import org.seamcat.model.types.result.ResultTypes;
import org.seamcat.model.types.result.VectorResultType;
import org.seamcat.ofdma.*;
import org.seamcat.simulation.calculator.InterferenceCalculator;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;
import org.seamcat.simulation.result.MutableSimulationElement;

import java.util.*;

import static org.seamcat.model.cellular.CellularSystem.*;
import static org.seamcat.model.simulation.result.SimulationResult.*;

public abstract class CellularVictimSystemSimulation implements VictimSystemSimulation<CellularSystem> {

    protected static Logger LOG = Logger.getLogger(CellularVictimSystemSimulation.class);
    protected Workspace workspace;
    private Scenario scenario;
    private CellularSystem victim;
    protected static ThreadLocal<AbstractDmaSystem> victimSystem = new ThreadLocal<>();

    public CellularVictimSystemSimulation(Workspace workspace) {
        this.scenario = workspace.getScenario();
        this.workspace = workspace;
        victim = (CellularSystem) scenario.getVictimSystem();
        if ( victim.getCDMASettings() != null ) {
            group = SimulationResult.CDMA_RESULTS;
            interferedOutage = "Interfered Outage Percentage, ref cell";
            interferedCapacity = "Interfered Capacity (active users only), ref cell";
            initialVictimOutage = "Initial Outage Percentage, ref cell";
            interferedCapacitySystem = "Interfered Capacity (active users only), system";
            nonInterferedCapacity = "Non Interfered Capacity (active users only), ref cell";
        } else {
            group = SimulationResult.OFDMA_RESULTS;
            interferedOutage = "Interfered Bitrate, ref cell";
            initialVictimOutage = "Non Interfered Bitrate, ref cell";
        }
        createUnitMap();
    }

    @Override
    public List<EventProcessing> getEmbeddedEPPs() {
        return Collections.emptyList();
    }

    public static final String UNW = "External Interference (Reference cell), Unwanted";
    public static final String SEL = "External Interference (Reference cell), Selectivity";
    private final String group;
    private static String nonInterferedCapacity;
    private static String interferedCapacity;
    private static String initialVictimOutage;
    private static String interferedOutage;
    private static String interferedCapacitySystem;
    private static String nonInterferedCapacitySystem = "Non Interfered Capacity, system";
    private static String avgAchievedBitRateSystem = "Avg Non Interfered Bitrate, system";
    private static String avgInterferedBitRateSystem = "Avg Interfered Bitrate, system";
    private static String sinrRefCell = "SINR, Victim ref cell";
    private static String sinrVictimSystem = "SINR, Victim system";
    private static String nonInterferedCapacityCombinedRefCell = "Non Interfered Capacity (active and inactive users), ref cell";
    private static String interferedCapacityCombinedRefCell = "Interfered Capacity (active and inactive users), ref cell";
    private static String initialCapacitySystem = "Non Interfered Capacity (active users only), system";
    private static String nonInterferedCapacityCombinedSystem = "Non Interfered Capacity (active and inactive users), system";
    private static String interferedCapacityCombinedSystem = "Interfered Capacity (active and inactive users), system";
    private static String initialOutageSystem = "Initial Outage Percentage, system";
    private static String interferedOutageSystemPercentage = "Interfered Outage Percentage, system";
    private static String totalDroppedUsers = "Number of Dropped users, System";
    private static String simulatedUsers = "Number of Simulated users, System";
    private static String capacityLossCombinedRefCell = "Capacity Loss (active and inactive users), ref cell";
    private static String capacityLossDroppedSystem = "Capacity Loss (based on dropped users), system";
    private static String capacityLossCombinedSystem = "Capacity Loss (active and inactive users), system";
    private static String capacityLossWorstCell = "Capacity Loss (active and inactive users), worst cell";
    public final static String avgNetworkNoiseRiseInitialNoExt = "Average network noise rise, (initial - no Ext. interference)";
    public final static String avgNetworkNoiseRiseInitial = "Average network noise rise, (initial)";
    public final static String avgNetworkNoiseRise = "Average network rise, (resulting)";
    public final static String numberOfAffectedCells = "Number of Affected Cells";
    public final static String droppedBeforeInterference = "Dropped before interference";
    public final static String highestPCLoopCount = "Highest PC loop count";

    private Map<String, String> unitMap;

    @Override
    public List<SimulationResultGroup> buildResults(CollectedResults collected) {
        List<SimulationResultGroup> groups = new ArrayList<>();
        Map<String, double[]> collectedResults = collected.vectorResults();

        ResultTypes unwanted = new ResultTypes();
        groups.add(new SimulationResultGroup(IRSS_UNWANTED, unwanted, scenario));
        unwanted.getVectorResultTypes().add(new VectorResultType(UNW, dBm, collectedResults.remove(UNW)));

        ResultTypes blocking = new ResultTypes();
        groups.add(new SimulationResultGroup(IRSS_SELECTIVITY, blocking, scenario));
        blocking.getVectorResultTypes().add(new VectorResultType(SEL, dBm, collectedResults.remove(SEL)));

        ResultTypes dmaGroup = new ResultTypes();
        groups.add(new SimulationResultGroup(group, dmaGroup, scenario));

        List<VectorResultType> vectors = dmaGroup.getVectorResultTypes();

        for (Map.Entry<String, double[]> entry : collectedResults.entrySet()) {
            vectors.add( new VectorResultType( entry.getKey(), unitMap.get( entry.getKey() ), entry.getValue()));
        }
        return groups;
    }

    public void createUnitMap() {
        unitMap = new HashMap<>();

        if ( victim.getCDMASettings() != null ) {
            unitMap.put( interferedOutage, "%");
            unitMap.put( initialVictimOutage, "%");
            unitMap.put( nonInterferedCapacity, "Active served users");
            unitMap.put( interferedCapacity, "Active served users");
            unitMap.put( interferedCapacitySystem, "Active served users");
            unitMap.put( nonInterferedCapacityCombinedRefCell, "Active and inactive served users");
            unitMap.put( interferedCapacityCombinedRefCell, "Active and inactive served users");
            unitMap.put( initialCapacitySystem, "Active served users");
            unitMap.put( nonInterferedCapacityCombinedSystem, "Active and inactive served users");
            unitMap.put( interferedCapacityCombinedSystem, "Active and inactive served users");
            unitMap.put( interferedOutageSystemPercentage, "%");
            unitMap.put( totalDroppedUsers, "Dropped users");
            unitMap.put( simulatedUsers, "Simulated users");
            unitMap.put( capacityLossCombinedRefCell, "%");
            unitMap.put( capacityLossDroppedSystem, "%");
            unitMap.put( capacityLossCombinedSystem, "%");
            unitMap.put( capacityLossWorstCell, "%");
            unitMap.put( avgNetworkNoiseRiseInitialNoExt, "dB");
            unitMap.put( avgNetworkNoiseRiseInitial, "dB");
            unitMap.put( avgNetworkNoiseRise, "dB");
            unitMap.put( numberOfAffectedCells, "Number of Affected Cells");
            unitMap.put( nonInterferedCapacitySystem, "Active served users");
            unitMap.put( droppedBeforeInterference, "users" );
            unitMap.put( highestPCLoopCount, "#" );
            unitMap.put( initialOutageSystem, "%");
        } else {
            unitMap.put( interferedOutage, "kbps");
            unitMap.put( sinrRefCell, "dB");
            unitMap.put( sinrVictimSystem, "dB");
            unitMap.put( avgInterferedBitRateSystem, "kbps");
            unitMap.put( avgAchievedBitRateSystem, "kbps");
            unitMap.put( initialVictimOutage, "kbps");
        }
    }

    public abstract AbstractDmaSystem getVictim();

    @Override
    public void simulate(EventResult eventResult) {
        victimSystem.remove();

        MutableEventResult currentResult = (MutableEventResult) eventResult;

        AbstractDmaSystem<? extends AbstractDmaMobile> victim = getVictim();
        victim.initialize( currentResult );
        victim.resetSystem();
        victim.setLocation( new Point2D(0,0));
        victim.generateSystemCells();
        victim.setCellularVictimSimulation(this);

        long start = System.currentTimeMillis();
        victim.simulate();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Simulate a DMA System in " + (System.currentTimeMillis() - start) + " millis");
        }

        if ( victim instanceof CDMASystem ) {
            currentResult.addValue(nonInterferedCapacity, victim.getNumberOfActiveServedMobilesInReferenceCell());
            currentResult.addValue(initialCapacitySystem, (double) victim.getNumberOfActiveServedMobilesInSystem());
            currentResult.addValue(nonInterferedCapacityCombinedRefCell, victim.getNumberOfServedMobilesInReferenceCell());
            currentResult.addValue(nonInterferedCapacityCombinedSystem, victim.getNumberOfServedMobilesInSystem());

            currentResult.addValue(initialOutageSystem, victim.getSystemMeasurement());
        }
        currentResult.addValue(initialVictimOutage, victim.getReferenceCellMeasurement());

        int numBS = getVictim().getNumberOfCellSitesInPowerControlCluster();
        int numSector= getVictim().cellsPerSite();
        getVictim().setInitialVictimCapacityActiveAndInactiveUsersWorstCell(new double[numBS][numSector]);
        for (int j = 0; j < numBS; j++) {
            for (int k = 0; k < numSector; k++) {
                getVictim().getInitialVictimCapacityActiveAndInactiveUsersWorstCell()[j][k] = victim.getNumberOfServedMobilesInCell(j, k);
            }
        }

        if (victim instanceof OfdmaSystem) {
            double _averageAchievedBitrate = ((OfdmaSystem) victim).calculateAverageAchievedBitrate();
            currentResult.addValue(avgAchievedBitRateSystem, _averageAchievedBitrate);
            LOG.debug("Average achieved bit rate (initial): " + _averageAchievedBitrate + " kbps");

            if (LOG.isDebugEnabled()) {
                for (AbstractDmaBaseStation base : victim.getAllBaseStations()) {
                    for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
                        LOG.debug("BS - Cell ID: " + base.getCellid() + " sector ID: " + base.getSectorId() + base.getPosition() + "UE-ID (active connection)[" + link.getUserTerminal().getUserId() + "](" + link.getUserTerminal().getPosition()
                                + ", Achieved bit rate: " + link.getUserTerminal().getBitRateAchieved() + " kbps, Achieved SINR: " + link.getUserTerminal().getSINRAchieved() + " dB");

                    }
                }
                for (AbstractDmaLink link : victim.getReferenceCell().getOldTypeActiveConnections()) {
                    LOG.debug("Referrence BS - Cell ID: " + victim.getReferenceCell().getCellid() + " sector ID: " + victim.getReferenceCell().getSectorId() + victim.getReferenceCell().getPosition()+ "UE-ID ["
                            + link.getUserTerminal().getUserId() + "]" + link.getUserTerminal().getPosition() + ", Achieved bit rate: " + link.getUserTerminal().getBitRateAchieved() + " kbps, Achieved SINR: "
                            + link.getUserTerminal().getSINRAchieved() + " dB");
                }
            }

        }

        cellularInternals(currentResult, victim, "victim");

        if ( victim instanceof CDMASystem ) {
            currentResult.addValue(droppedBeforeInterference, ((CDMASystem) victim).countDroppedUsers());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Initial Victim Capacity = " + victim.getNumberOfActiveServedMobilesInReferenceCell() + " users in reference cell");
            if (!(victim instanceof OfdmaSystem)) {
                LOG.debug("Initial Victim Outage = " + currentResult.getValue(initialVictimOutage) + "% in reference cell");
            } else {
                LOG.debug("Initial Victim Bit rate = " + currentResult.getValue(initialVictimOutage) + " kbps in reference cell");

            }
        }
    }

    public static void cellularInternals(MutableEventResult currentResult, AbstractDmaSystem system, String postFix) {
        List<AbstractDmaBaseStation> allBaseStations = system.getAllBaseStations();
        for (AbstractDmaBaseStation base : allBaseStations) {
            for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
                handle(currentResult, link, postFix+" total");
            }
        }
        for (AbstractDmaLink link : system.getReferenceCell().getOldTypeActiveConnections()) {
            handle(currentResult, link, postFix);
        }
    }

    private static void handle(MutableEventResult currentResult, AbstractDmaLink link, String postFix) {
        postFix = " " +postFix;
        AbstractDmaMobile ue = link.getUserTerminal();
        AbstractDmaBaseStation bs = link.getBaseStation();
        currentResult.addVectorValue(REQUESTED_SUBCARRIERS+postFix, ue.getRequestedSubCarriers());
        currentResult.addVectorValue(TOTAL_INTERFERENCE+postFix, ue.getTotalInterference());
        currentResult.addVectorValue(SUB_CARRIER_RATIO + postFix, ue.getSubCarrierRatio());
        currentResult.addVectorValue(FREQUENCY + postFix, ue.getFrequency());
        currentResult.addVectorValue(THERMAL_NOISE + postFix, ue.getThermalNoise());
        currentResult.addVectorValue(BIT_RATE_ACHIEVED + postFix, ue.getBitRateAchieved());
        currentResult.addVectorValue(RECEIVED_POWER + postFix, ue.getReceivedPower());
        currentResult.addVectorValue(SINR_ACHIEVED + postFix, ue.getSINRAchieved());
        currentResult.addVectorValue(PATH_LOSS + postFix, link.getTxRxPathLoss());
        currentResult.addVectorValue(EFFECTIVE_PATH_LOSS + postFix, link.getEffectivePathloss());

        if ( ue instanceof DownlinkOfdmaMobile ) {
            currentResult.addVectorValue(INTERFERENCE_POWER+postFix, ((DownlinkOfdmaMobile) ue).getInterferencePower());
            currentResult.addVectorValue(CURRENT_TRANSMIT_POWER+postFix, bs.getCurrentTransmitPower_dBm());
            currentResult.addVectorValue(BASE_STATION_BIT_RATE + postFix, bs.getBitRateAchieved());
            currentResult.addVectorValue(INTER_SYSTEM_INTERFERENCE + postFix, ue.getInterSystemInterference());

        } else if (ue instanceof UplinkOfdmaMobile ) {
            currentResult.addVectorValue(CURRENT_TRANSMIT_POWER+postFix, ue.getCurrentTransmitPowerIndBm());
            currentResult.addVectorValue(POWER_CONTROL_PL, ((UplinkOfdmaMobile) ue).getPl());
            currentResult.addVectorValue(POWER_CONTROL_PLILX, ((UplinkOfdmaMobile) ue).getPlIlx());
            currentResult.addVectorValue(INTER_SYSTEM_INTERFERENCE + postFix, bs.getInterSystemInterference());
        }
    }

    @Override
    public Point2D getSystemPosition(EventResult eventResult, InterferenceLink<GenericSystem> link) {
        return getVictim().getReferenceCell().getPosition();
    }

    public void calculateExternalInterference(MutableSimulationElement elem, AbstractDmaLink link) {
        // Calculate and assign external interference
        double bandwidthCorrection = getBandwidthCorrection(elem);
        double extIntUnw = 0, extIntBlo = 0;
        for (ActiveInterferer externalInterferer : getVictim().getEventResult().getInterferingElements()) {
            double angle = Mathematics.calculateKartesianAngle(externalInterferer.getPoint(), elem.getPosition());
            double elev = Mathematics.calculateElevation(externalInterferer.getPoint(), externalInterferer.getAntennaHeight(),
                    elem.getPosition(), elem.getAntennaHeight());

            MutableInterferenceLinkResult iLink = new MutableInterferenceLinkResult(externalInterferer.getInterferenceLink(), link.asLinkResult(), externalInterferer.getLinkResult());
            iLink.setRxBandwidth(elem.getReferenceBandwidth());
            iLink.rxAntenna().setGain(elem.calculateAntennaGainTo(angle,elev));
            iLink.setTxRxDistance(Mathematics.distance(externalInterferer.getLinkResult().txAntenna().getPosition(), elem.getPosition()));
            iLink.getVictimSystemLink().setFrequency(elem.getFrequency());
            iLink.getVictimSystemLink().rxAntenna().setHeight( elem.getAntennaHeight());
            externalInterferer.calculateLosses(elem.getPosition(), elem.getAntennaHeight(), iLink);

            externalInterferer.applyInterferenceLinkCalculations( iLink );

            InterferenceCalculator.unwantedInterference(externalInterferer.getScenario(), iLink);
            //externalInterferer.setVictimFrequency( getSystemFrequency() );
            InterferenceCalculator.blockingInterference(externalInterferer.getScenario(), iLink);

            extIntUnw += Mathematics.dB2Linear(iLink.getRiRSSUnwantedValue());
            extIntBlo += Mathematics.dB2Linear(iLink.getRiRSSBlockingValue() + bandwidthCorrection);

            handleInterferer( elem, externalInterferer, iLink);
        }
        elem.setExternalInterferenceUnwanted(Mathematics.linear2dB(extIntUnw));
        elem.setExternalInterferenceBlocking(Mathematics.linear2dB(extIntBlo));
    }

    public List<ActiveInterferer> getExternalInterferers(MutableEventResult eventResult) {
        return eventResult.getInterferingElements();
    }

    @Override
    public void collect(EventResult eventResult) {
        MutableEventResult currentResult = (MutableEventResult) eventResult;

        AbstractDmaSystem<? extends AbstractDmaMobile> victim = getVictim();

        if (victim instanceof OfdmaSystem) {
            List<OfdmaVictim> victims = ((OfdmaSystem) victim).getVictims();
            for (OfdmaVictim vic : victims) {
                for (OfdmaExternalInterferer ext : vic.getExternalInterferers()) {

                    currentResult.addVectorValue(EXTERNAL_INTER_UNW, ext.getExternalUnwanted());
                    currentResult.addVectorValue(EXTERNAL_INTER_BLOC, ext.getExternalBlocking());
                }
            }

            if ( victim instanceof UplinkOfdmaSystem ) {
                for (AbstractDmaBaseStation base : victim.getAllBaseStations()) {
                    for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
                        OfdmaUplink ol = (OfdmaUplink) link;
                        currentResult.addVectorValue(EXTERNAL_INTERFERENCE, ol.getExternalInterference_dBm());
                    }
                }
            }

            double _averageAchievedBitrate = ((OfdmaSystem) victim).calculateAverageAchievedBitrate();
            currentResult.addValue(avgInterferedBitRateSystem, _averageAchievedBitrate);

            for (AbstractDmaBaseStation base : victim.getAllBaseStations()) {
                for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
                    currentResult.addValue(sinrVictimSystem, link.getUserTerminal().getSINRAchieved());
                }
            }
            for (AbstractDmaLink link : victim.getReferenceCell().getOldTypeActiveConnections()) {
                currentResult.addValue(sinrRefCell, link.getUserTerminal().getSINRAchieved());
            }
        }

        int interferedVictimCapacityActiveUsersSystem = victim.getNumberOfActiveServedMobilesInSystem();
        int interferedVictimCapacityActiveAndInactiveUsersSystem = victim.getNumberOfServedMobilesInSystem();
        int interferedVictimCapacityActiveAndInactiveUsersReferenceCell = victim.getNumberOfServedMobilesInReferenceCell();
        currentResult.addValue(interferedOutage, victim.getReferenceCellMeasurement());
        double interferedOutageSystem = victim.getSystemMeasurement();

        if ( victim instanceof CDMASystem) {
            if ( victim instanceof CDMAUplinkSystem ) {
                for (AbstractDmaBaseStation base : victim.getAllBaseStations()) {
                    currentResult.addVectorValue(EXTERNAL_INTERFERENCE, base.getExternalInterference());
                }
            }

            double systemCapacityLossPercentage = calculateLossAvgPercentageSystemPerEvent(
                    ((CDMASystem)victim).countDroppedUsers(), ((CDMASystem)victim).countTotalNumberOfUsers());
            currentResult.addValue(capacityLossDroppedSystem, systemCapacityLossPercentage);

            double SystemCapacityLossActiveAndInactivePercentage = calculateLossAvgPercentagePerEvent(currentResult.getValue(nonInterferedCapacityCombinedSystem), interferedVictimCapacityActiveAndInactiveUsersSystem);
            currentResult.addValue(capacityLossCombinedSystem, SystemCapacityLossActiveAndInactivePercentage);

            double referenceCellCapacityLossPercentage = calculateLossAvgPercentagePerEvent(currentResult.getValue(nonInterferedCapacityCombinedRefCell), interferedVictimCapacityActiveAndInactiveUsersReferenceCell);
            currentResult.addValue(capacityLossCombinedRefCell, referenceCellCapacityLossPercentage);
        }

        if ( victim.isDownlink() ) {
            for (AbstractDmaLink link : victim.getReferenceCell().getOldTypeActiveConnections()) {
                currentResult.addVectorValue(EXTERNAL_INTERFERENCE, link.getUserTerminal().getExternalInterference());
            }
        }

        int VictimCapacityActiveAndInactiveUsersWorstCell = victim.getNumberOfServedMobilesInWorstCell();
        int numBS = getVictim().getNumberOfCellSitesInPowerControlCluster();
        int numSector= getVictim().cellsPerSite();
        int worstCellID = 0;
        int triSectorWorstCellSelection = 0;
        for (int j = 0; j < numBS; j++) {
            for (int k = 0; k < numSector; k++) {
                if(victim.getWorstCell(j, k)){
                    worstCellID = j;
                    triSectorWorstCellSelection = k;
                    victim.resetWorstCell(j, k);
                }
            }
        }
        double temp = getVictim().getInitialVictimCapacityActiveAndInactiveUsersWorstCell()[worstCellID][triSectorWorstCellSelection];
        if(VictimCapacityActiveAndInactiveUsersWorstCell == -1){// in this case there is no affected cell, hence no loss so the number of Served mobile should be the same as before interference introduced.
            VictimCapacityActiveAndInactiveUsersWorstCell = (int) temp;
        }

        if (victim instanceof CDMASystem) {
            currentResult.addValue(interferedCapacity, victim.getNumberOfActiveServedMobilesInReferenceCell());
            currentResult.addValue(totalDroppedUsers, (double) ((CDMASystem)victim).countDroppedUsers());
            currentResult.addValue(simulatedUsers, (double) ((CDMASystem)victim).countTotalNumberOfUsers());
            currentResult.addValue(interferedCapacitySystem, (double) interferedVictimCapacityActiveUsersSystem);
            currentResult.addValue(interferedCapacityCombinedSystem, (double) interferedVictimCapacityActiveAndInactiveUsersSystem);
            currentResult.addValue(interferedCapacityCombinedRefCell, (double) interferedVictimCapacityActiveAndInactiveUsersReferenceCell);
            currentResult.addValue(interferedOutageSystemPercentage, interferedOutageSystem);

            if ( victim.isUplink() ) {
                double worstCellCapacityLossPercentage = calculateLossAvgPercentagePerEvent(temp, VictimCapacityActiveAndInactiveUsersWorstCell);
                currentResult.addValue(capacityLossWorstCell, worstCellCapacityLossPercentage);
            }
        }

        if (LOG.isDebugEnabled()) {
            if (!(victim instanceof OfdmaSystem)) {
                LOG.debug("Victim CDMA interfered capacity = " + currentResult.getValue(interferedCapacity) + " users in reference cell");
                LOG.debug("Victim CDMA interfered outage = " + currentResult.getValue(interferedOutage) + "% outage in reference cell");
            } else {
                LOG.debug("Victim OFDMA interfered capacity = " + currentResult.getValue(interferedCapacity) + " users in reference cell");
                LOG.debug("Victim OFDMA interfered bit rate = " + currentResult.getValue(interferedOutage) + " bit rate in reference cell");
            }
            LOG.debug( victim.toString());
        }

        double unw = 0, blo = 0;
        if (victim.isUplink()) {
            // VR is Reference BS
            if (victim instanceof OfdmaSystem) {
                int size = victim.getReferenceCell().getOldTypeActiveConnections().size();
                for (AbstractDmaLink link : victim.getReferenceCell().getOldTypeActiveConnections()) {
                    OfdmaUplink uplink = (OfdmaUplink) link;
                    unw += uplink.calculateExternalInterferenceUnwanted_dBm();
                    blo += uplink.calculateExternalInterferenceBlocking_dBm();
                }
                unw = unw / size;
                blo = blo / size;
            } else {
                unw = victim.getReferenceCell().getExternalInterferenceUnwanted();
                blo = victim.getReferenceCell().getExternalInterferenceBlocking();
            }
        } else {
            // VR's are Ms's connected to Reference BS
            // iRSS values are average
            for (AbstractDmaLink link : victim.getReferenceCell().getOldTypeActiveConnections()) {
                unw += link.getUserTerminal().getExternalInterferenceUnwanted();
                blo += link.getUserTerminal().getExternalInterferenceBlocking();
            }
            int size = victim.getReferenceCell().getOldTypeActiveConnections().size();
            unw = (size > 0) ? (unw / size) : 0;
            blo = (size > 0) ? (blo / size) : 0;
        }
        currentResult.addValue(UNW, unw);
        currentResult.addValue(SEL, blo);
        if (LOG.isDebugEnabled()) {
            LOG.debug("External Unwanted = " + unw + " dBm");
            LOG.debug("External Blocking = " + blo + " dBm");
        }

        MutableEventResult er = (MutableEventResult) eventResult;
        for (AbstractDmaLink link : victim.getReferenceCell().getOldTypeActiveConnections()) {
            MutableLinkResult victimSystemLink = link.asLinkResult();
            er.addVictimSystemLink( victimSystemLink );
            for (ActiveInterferer interferer : er.getInterferingElements()) {
                MutableInterferenceLinkResult linkResult = new MutableInterferenceLinkResult(interferer.getInterferenceLink(), victimSystemLink, interferer.getLinkResult());
                er.addInterferenceLinkResult( linkResult );
            }
        }

    }

    protected abstract double getSystemFrequency();

    protected double getBandwidthCorrection(MutableSimulationElement element ) {
        return 0;
    }

    protected void handleInterferer( MutableSimulationElement element, Interferer interferer, MutableInterferenceLinkResult iLink ) {

    }

    @Override
    public void postSimulation(SimulationResult simulationResult) {
        SimulationResultGroup cellularResults = simulationResult.getSeamcatResult(group);
        if ( workspace.getVictimSystemLink().isCDMASystem()) {
            VectorResultType vector = cellularResults.getResultTypes().findVector(simulatedUsers);
            Double v = vector.getValue().get(vector.getValue().size() - 1);
            final PreSimulationResults pre = scenario.getPreSimulationResults(scenario.getVictimSystem());
            int capacity = pre.getPreSimulationResults().findIntValue(CDMASystem.NON_INTERFERED_CAPACITY);
            calculateCDMALosses(v.intValue(), capacity, cellularResults);
        } else {
            calculateOFDMALosses(cellularResults);
        }
    }

    private void calculateCDMALosses(int totalCapacitySystem, int simulatedUsersPerCell, SimulationResultGroup cellularResults) {
        List<VectorResultType> vectors = cellularResults.getResultTypes().getVectorResultTypes();
        double[] initialCapacity = findVector(vectors, nonInterferedCapacityCombinedRefCell).getValue().asArray();
        double[] interferedCapacity = findVector(vectors, interferedCapacityCombinedRefCell).getValue().asArray();

        int[] displayNoInt;
        int[] displayInt;
        displayNoInt = new int[ initialCapacity.length ];
        for (int i=0; i<initialCapacity.length; i++) {
            displayNoInt[i] = (int) initialCapacity[i];
        }
        displayInt = new int[ interferedCapacity.length ];
        for ( int i=0; i<interferedCapacity.length; i++ ) {
            displayInt[i] = (int) interferedCapacity[i];
        }

        cellularResults.getResultTypes().getSingleValueTypes().add(new DoubleResultType("Average capacity loss (ref. cell)", "%", calculateLossAvgPercentage(displayNoInt, displayInt)));

        double[] totalDroppedSystem = findVector(vectors, totalDroppedUsers).getValue().asArray();

        cellularResults.getResultTypes().getSingleValueTypes().add(new DoubleResultType("Average capacity loss (system)", "%", Math.min(100.0, calculateLossAvgPercentage(totalDroppedSystem, totalCapacitySystem ))));
        cellularResults.getResultTypes().getSingleValueTypes().add(new DoubleResultType("Non interfered capacity per cell", "", simulatedUsersPerCell));

        VectorResultType nonInterferedCapacity = findVector(vectors, CellularVictimSystemSimulation.nonInterferedCapacity);
        VectorResultType initialCapacitySystem = findVector(vectors, CellularVictimSystemSimulation.initialCapacitySystem);
        VectorResultType interferedCapacityVector = findVector(vectors, CellularVictimSystemSimulation.interferedCapacity);
        double[] excessOutageRefCell = diffVector( nonInterferedCapacity.getValue().asArray(), interferedCapacityVector.getValue().asArray() );
        cellularResults.getResultTypes().getVectorResultTypes().add(new VectorResultType("Excess outage (ref. cell)", "Active served users", excessOutageRefCell));
        double[] excessOutageSystem = diffVector(initialCapacitySystem.getValue().asArray(), findVector(vectors, interferedCapacitySystem).getValue().asArray());
        cellularResults.getResultTypes().getVectorResultTypes().add( new VectorResultType("Excess outage (system)", "Active served users", excessOutageSystem));
    }

    private double[] diffVector( double[] first, double[] second ) {
        double[] diff = new double[first.length];
        for ( int i=0; i< first.length; i++ ) {
            diff[i] = first[i] - second[i];
        }
        return diff;
    }

    private void calculateOFDMALosses(SimulationResultGroup cellularResults) {
        List<VectorResultType> vectors = cellularResults.getResultTypes().getVectorResultTypes();

        double avgBitRateLossRefCell = averagePercentage(findVector(vectors, initialVictimOutage).getValue().asArray(), findVector(vectors, interferedOutage).getValue().asArray());
        cellularResults.getResultTypes().getSingleValueTypes().add( new DoubleResultType("Average bitrate loss (ref. cell)", "%", avgBitRateLossRefCell));

        double avgBitRateLossSystem = averagePercentage(findVector(vectors, avgAchievedBitRateSystem).getValue().asArray(), findVector(vectors, avgInterferedBitRateSystem).getValue().asArray());
        cellularResults.getResultTypes().getSingleValueTypes().add( new DoubleResultType("Average bitrate loss (system)", "%", avgBitRateLossSystem));
    }

    private double averagePercentage(double[] v1, double[] v2 ) {
        double temp = 0;
        int count = 0;
        for (int i = 0;i < v1.length;i++) {
            double initialValue = v1[i];
            double interferedValue = v2[i];

            if (initialValue != 0) {
                temp +=  (1 - (interferedValue / initialValue));
                count++;
            }
        }
        if (count > 0) {
            temp = 100 * (temp / count);
            return Mathematics.round(temp);
        } else {
            return 0;
        }
    }

    private VectorResultType findVector(List<VectorResultType> vectors, String name ) {
        for (VectorResultType vector : vectors) {
            if ( vector.getName().equals(name)) return vector;
        }
        throw new RuntimeException("No such vector "+ name);
    }

    /**
     * Generic calculation of the average loss percentage over the whole simulated events based on the number of users <br>
     *     simulated. This calculation can be applied for both the all network or for the reference cell.
     * <p>
     *     the equation is as follow
     *  <p>
     *      <code>avg = SUM_(100 - ( interferenceCapacity[x] / noInterferenceCapacity[x] ) * 100)/size;</code>
     *
     * @param noInterferenceCapacity the number of users simulated without external interference
     * @param interferenceCapacity the number of users simulated with external interference
     * @return the average loss in percentage over the whole simulated events
     */
    private static double calculateLossAvgPercentage(int[] noInterferenceCapacity, int[] interferenceCapacity) {
        double avg = 0;
        int size = noInterferenceCapacity.length;
        for (int x = 0; x < size; x++) {
            if ( noInterferenceCapacity[x] != 0 ) {
                avg += 100 - ( (double) interferenceCapacity[x] / noInterferenceCapacity[x] ) * 100;
            }
        }
        return avg / size;
    }


    /**
     * Calculate the average loss percentage over the all simulated events based on the dropped user variable.
     *<p>
     * <code>avgLossSystem = 100x((SUM_totalDroppedSystem)/totalDroppedSystem.length)/totalCapacitySystem</code>
     *
     * @param totalDroppedSystem the total of dropped users in the system (i.e. the cellular network) over the all simulated events
     * @param totalCapacitySystem the total number of users simulated in the system (i.e. the cellular network) over the all simulated events
     * @return the average loss of users for the overall system (i.e. the cellular network) and over the all simulated events
     */
    private static double calculateLossAvgPercentage(double[] totalDroppedSystem, int totalCapacitySystem) {
        double avgLossSystem = 0;

        for (double d : totalDroppedSystem) {
            avgLossSystem += d;
        }
        if ( totalDroppedSystem.length != 0 ) {
            avgLossSystem /= totalDroppedSystem.length;
        }
        if ( totalCapacitySystem != 0 ) {
            avgLossSystem /= (double) totalCapacitySystem;
        }
        avgLossSystem *= 100;

        return avgLossSystem;
    }


    /**
     * Calculate the average loss percentage per each event based on the dropped user variable.
     *<p>
     * the equation is as follow:
     * <p>
     *     <code>avgLossSystem = 100 x totalDroppedSystem/totalCapacitySystem</code>
     *
     * @param totalDroppedSystem the total of dropped users in the system (i.e. the cellular network)
     * @param totalCapacitySystem the total number of users simulated in the system (i.e. the cellular network)
     * @return the average loss of users for the overall system (i.e. the cellular network) for one event
     */
    private static double calculateLossAvgPercentageSystemPerEvent(double totalDroppedSystem, int totalCapacitySystem) {
        double avgLossSystem = 0;

        avgLossSystem = totalDroppedSystem;

        if ( totalCapacitySystem != 0 ) {
            avgLossSystem /= (double) totalCapacitySystem;
        }
        avgLossSystem *= 100;

        return avgLossSystem;
    }

    /**
     * Generic calculation of the average loss percentage per each event based on the number of users simulated. This <br>
     *     calculation can be applied for both the all network or for the reference cell.
     *<p>
     * the equation is as follow
     *  <p>
     *  <code>avg = 100 - ( interferenceCapacity / noInterferenceCapacity ) * 100;</code>
     *
     * @param noInterferenceCapacity the number of users simulated without external interference
     * @param interferenceCapacity the number of users simulated with external interference
     * @return the average loss in percentage per event
     */
    private static double calculateLossAvgPercentagePerEvent(double noInterferenceCapacity, int interferenceCapacity) {
        double avg = 0;
        if ( noInterferenceCapacity != 0 ) {
            avg = 100 - ( (double) interferenceCapacity / noInterferenceCapacity ) * 100;
        }
        return avg ;
    }

}
