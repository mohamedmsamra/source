package org.seamcat.cdma;

import org.apache.log4j.Logger;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.CapacityStartingCapacityFinding;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.result.BarChartResultType;
import org.seamcat.model.types.result.BarChartValue;
import org.seamcat.model.types.result.IntegerResultType;
import org.seamcat.scenario.CDMASettingsImpl;
import org.seamcat.simulation.result.PreSimulationResultsImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the abstract base class for CDMA simulations.
 */
public abstract class CDMASystem extends AbstractDmaSystem<CdmaUserTerminal> {

    private static Logger LOG = Logger.getLogger(CDMASystem.class);

    public static final String CAPACITY_FINDING = "Capacity finding";
    public static final String NON_INTERFERED_CAPACITY = "Non interfered capacity";

    // for non interfered capacity
    protected boolean finalFineTuning = false;
    protected boolean fineTuning = false;
    protected double maxTargetNoiseRise;
    protected double minTargetNoiseRise;
    protected double meanNoiseRiseOverTrials;
    protected double succesCriteria = 0.8;
    protected List<CdmaUserTerminal> inactiveUsers = new ArrayList<>();
    protected List<CdmaUserTerminal> droppedUsers = new ArrayList<>();


    public CDMASystem() {
        init();
    }

    private void init() {
        CDMASettingsImpl cdmaSettings = new CDMASettingsImpl();
        cdmaSettings.setLld(new CDMALinkLevelData());
        getSystemSettings().setCDMASettings(cdmaSettings);
    }

    protected CDMASystem(AbstractDmaSystem<?> sys) {
        super(sys);
        init();
        if (sys instanceof CDMASystem) {
            CDMASystem cdma = (CDMASystem) sys;
            getSystemSettings().getCDMASettings().setCallDropThreshold(cdma.getSystemSettings().getCDMASettings().getCallDropThreshold());
            getSystemSettings().getCDMASettings().setToleranceOfInitialOutage(cdma.getSystemSettings().getCDMASettings().getToleranceOfInitialOutage());
            getSystemSettings().getCDMASettings().setTargetNoiseRisePrecision(cdma.getSystemSettings().getCDMASettings().getTargetNoiseRisePrecision());

            getSystemSettings().getCDMASettings().setDeltaUsersPerCell(cdma.getSystemSettings().getCDMASettings().getDeltaUsersPerCell());
            setFrequency(cdma.getFrequency());
            setLinkLevelData(new CDMALinkLevelData(cdma.getLinkLevelData()));
            setLocation(cdma.getLocationX(), cdma.getLocationY());

            getSystemSettings().getCDMASettings().setNumberOfTrials(cdma.getSystemSettings().getCDMASettings().getNumberOfTrials());
            getSystemSettings().getCDMASettings().setSimulateNonInterferedCapacity(cdma.getSystemSettings().getCDMASettings().isSimulateNonInterferedCapacity());
            setSuccesCriteria(cdma.getSuccesCriteria());

            getSystemSettings().getCDMASettings().setVoiceActivityFactor(cdma.getSystemSettings().getCDMASettings().getVoiceActivityFactor());
            getSystemSettings().getCDMASettings().setVoiceBitRate(cdma.getSystemSettings().getCDMASettings().getVoiceBitRate());
            setVictimSystem(cdma.isVictimSystem());
        }
    }

    public abstract void balanceInterferedSystem();

    public abstract void balancePower();

    public void resetSystem() {
        super.resetSystem();
        droppedUsers.clear();
        inactiveUsers.clear();
    }

    /**
     * Counts the number of dropped users (size() of droppedUsers List). <br>
     * Note: Result is not cached - multiple calls to this method will result in multiple calls to size()
     *
     * @return int The number of dropped users.
     */
    public int countDroppedUsers() {
        return droppedUsers.size();
    }

    /**
     * counts the number of inactive users. It should be zero as CDMA module activity factor is set to 100%
     * @return the number of inactive users
     */
    public int countInactivateUsers() {
        return inactiveUsers.size();
    }

    /**
     * count the total number of users. It take into account the number of active, dropped and ianctive users
     * @return the total number of users
     */
    public int countTotalNumberOfUsers() {
        return countActiveUsers() + countDroppedUsers() + countInactivateUsers();
    }

    public List<CdmaUserTerminal> getInactiveUsers() {
        return inactiveUsers;
    }

    public List<CdmaUserTerminal> getDroppedUsers() {
        return droppedUsers;
    }


    /**
     * adds a pre-determmined number of users. The capacity (i.e. number of users per cell) is multiplied by the number <br>
     *     of cells (i.e. number of BS x number of sector) the capacity is evaluated when there is no interferer
     */
    protected void generateAndPositionMobiles() {
        // Add predetermined number of users
        int capacity = getResults().getPreSimulationResults().findIntValue(CDMASystem.NON_INTERFERED_CAPACITY);
        for (int i = 0, stop = capacity * getNumberOfBaseStations(); i < stop; i++) {
            CdmaUserTerminal user = generateInitializedMobile();

            if (user == null) {
                continue;
            }
            if (user.connect()) {
                user.setAllowedToConnect(true);
                activeUsers.add(user);
            } else {
                dropActiveUser(user);
                user.setDropReason("Unable to connect during first initialisation of UE");
            }
        }// for loop - all users added
    }

    /**
     * Drops the given AbstractDmaMobile. Users dropCall method will be called and user will be moved from active list <br>
     *     to droppedUsers List
     * @param user AbstractDmaMobile
     */
    public void dropActiveUser(CdmaUserTerminal user) {
        user.dropCall();
        activeUsers.remove(user);
        getDroppedUsers().add(user);
    }

    /**
 * Sets whether or not user is active, according to current setting of voice
 * activity factor
 * <p></p>
 *   Note that the activity factor is not used in SEAMCAT as it is set to 100% active.
 *
 */
    public boolean trialVoiceActivity() {
        //return true;
        return random.trial() <= getSystemSettings().getCDMASettings().getVoiceActivityFactor();
    }

    /**
     * Calculates the Processing Gain G for the current system. G is given as:
     * <br>
     * <code>G = (BW / R) * 10^3 </code><br>
     * <code>G = (Receiver Bandwith[MHz] / Service Bit Rate[kbps]) * 10^3</code>
     *
     * @return processing gain G
     */
    public double calculateProcessingGain() {
        processingGain = getSystemSettings().getBandwidth() / getSystemSettings().getCDMASettings().getVoiceBitRate() * 1000;
        processingGain = Mathematics.linear2dB(processingGain);
        return processingGain;
    }

    @Override
    protected void configureBaseStation(AbstractDmaBaseStation base) {
        if ( this instanceof CDMADownlinkSystem ) {
            base.setMaximumChannelPowerFraction(getSystemSettings().getCDMASettings().getDownLinkSettings().getMaximumTrafficChannelFraction());
            base.setMaximumTransmitPower_dBm(getSystemSettings().getCDMASettings().getDownLinkSettings().getMaximumBroadcastChannel());
            base.setPilotFraction(getSystemSettings().getCDMASettings().getDownLinkSettings().getPilotChannelFraction());
            base.setOverheadFraction(getSystemSettings().getCDMASettings().getDownLinkSettings().getOverheadChannelFraction());
        } else {
            base.setMaximumChannelPowerFraction(0.15);
            base.setMaximumTransmitPower_dBm( 40 );
            base.setPilotFraction(0.15);
            base.setOverheadFraction(0.05);
        }
        ((CdmaBaseStation)base).initializeTransmitPowerLevels();
    }

    protected abstract NonInterferedCapacitySearch findNonInterferedCapacityInternal(BarChartResultType capFinding,
                                 NonInterferedCapacitySearch search, Object context) throws InterruptedException;

    /**
     * Compute the optimum number of users without external interference as part of the pre-setting of the CDMA network
     * <ol>
     *     <li>uplink: users per cell is based on the average noise rise (dB)</li>
     * <li>downlink: users per cell is based on the succesful trial (i.e. trial to connect to the network)</li>
     *</ol>
     * @param results of a type PreSimulationResultsImpl
     * @param context of a type object
     * @throws InterruptedException
     */
    public void findNonInterferedCapacity(PreSimulationResultsImpl results, Object context) throws InterruptedException {
        if ( results.getPreSimulationResults().hasSingleValue(NON_INTERFERED_CAPACITY) ) {
            // this has already been calculated to return;
            return;
        }

        int capacity = getSystemSettings().getUsersPerCell();
        if (getSystemSettings().getCDMASettings().isSimulateNonInterferedCapacity()) {
            fineTuning = false;

            BarChartResultType capFinding;
            int deltaN = getSystemSettings().getCDMASettings().getDeltaUsersPerCell();
            if (isUplink()) {
                capFinding = new BarChartResultType(CAPACITY_FINDING, "Users per cell", "Average Noiserise (dB)");
                double noiseRise = getSystemSettings().getCDMASettings().getUpLinkSettings().getTargetNetworkNoiseRise();
                minTargetNoiseRise = noiseRise - getSystemSettings().getCDMASettings().getTargetNoiseRisePrecision();
                maxTargetNoiseRise = noiseRise;
                EventBusFactory.getEventBus().publish( new CapacityStartingCapacityFinding(context, getSystemSettings().getUsersPerCell(), deltaN,
                        getSystemSettings().getCDMASettings().getTargetNoiseRisePrecision(), getSystemSettings().getCDMASettings().getNumberOfTrials(), true, noiseRise));
            } else {
                capFinding = new BarChartResultType(CAPACITY_FINDING, "Users per cell", "Successful trials");
                EventBusFactory.getEventBus().publish( new CapacityStartingCapacityFinding(context, getSystemSettings().getUsersPerCell(), deltaN,
                        getSystemSettings().getCDMASettings().getToleranceOfInitialOutage(), getSystemSettings().getCDMASettings().getNumberOfTrials(), false, succesCriteria));
            }
            results.getPreSimulationResults().getBarChartResultTypes().add( capFinding );
            NonInterferedCapacitySearch search = new NonInterferedCapacitySearch(getSystemSettings().getUsersPerCell(), deltaN);
            search = findNonInterferedCapacityInternal( capFinding, search, context);
            while ( !search.isConverged() ) {
                search = findNonInterferedCapacityInternal( capFinding,  search, context);
            }

            capacity = search.getCapacity();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initial Capacity found: " + capacity + " users per cell");
            }
        }
        results.getPreSimulationResults().getSingleValueTypes().add(new IntegerResultType(NON_INTERFERED_CAPACITY, "", capacity));
    }

    protected void addPoint( BarChartResultType capFinding, int usersPerCell, double value ) {
        int num = capFinding.getChartPoints().size()+1;
        capFinding.getChartPoints().add( new BarChartValue(num+"#Users: "+ usersPerCell, value));
    }


    @Override
    protected CdmaBaseStation generateBaseStation(
            Point2D position, int cellid, double antennaHeight, double antennaTilt, int sectorid, boolean triSector) {
        if (triSector) {
            return new CDMATriSectorCell(position, this, cellid, antennaHeight, antennaTilt, sectorid);
        } else {
            return new CDMAOmniDirectionalCell(position, this, cellid, antennaHeight, antennaTilt);
        }
    }

    @Override
    protected CdmaBaseStation[][] generateBaseStationArray() {
        return new CdmaBaseStation[getNumberOfCellSitesInPowerControlCluster()][cellsPerSite()];
    }

    @Override
    public CdmaUserTerminal generateUserTerminal() {
        CdmaUserTerminal user = new CdmaUserTerminal(
                new Point2D(0, 0), this, useridcount++, getSystemSettings().getLink().getMobileStation().getAntennaGain().trial(),
                getSystemSettings().getLink().getMobileStation().getAntennaHeight().trial());
        user.setThermalNoise(getResults().getThermalNoise());
        user.setUpLinkMode(isUplink());
        if ( this instanceof CDMAUplinkSystem ) {
            user.setMaxTxPower(getSystemSettings().getCDMASettings().getUpLinkSettings().getMSMaximumTransmitPower());
            user.setMinTxPower(getSystemSettings().getCDMASettings().getUpLinkSettings().getMSMaximumTransmitPower() -
                    getSystemSettings().getCDMASettings().getUpLinkSettings().getMSPowerControlRange());
        }
        return user;
    }

    public final CDMALinkLevelData getLinkLevelData() {
        return getSystemSettings().getCDMASettings().getLld();
    }

    public int getNumberOfNoLinkLevelDataUsers() {
        return noLinkLevelFoundUsers.size();
    }

    public double getSuccesCriteria() {
        return succesCriteria;
    }

    protected abstract int internalPowerBalance();


    public final void setLinkLevelData(final CDMALinkLevelData linkLevelData) {
        getSystemSettings().getCDMASettings().setLld(linkLevelData);
    }

    public void setSuccesCriteria(double succesCriteria) {
        this.succesCriteria = succesCriteria;
    }

    /**
     * center core of the CDMA simulation
     *
     * <ol>
     *     <li>position system (i.e. set up the network layout)</li>
     * <li>generate and position mobiles in the network</li>
     * <li>balance the power of of the mobile in the network</li>
     * </ol>
     */
    @Override
    public void simulate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Positioning the Networks");
        }
        positionSystem();
        if (LOG.isDebugEnabled()) {
            LOG.debug(" Generating And Positioning mobiles: "+toString());
            LOG.debug(" System Description: CDMA System");
            LOG.debug(" System Minimum coupling loss: "+getSystemSettings().getMinimumCouplingLoss()+" dBm");
            LOG.debug(" Calculate the thermal noise()");
        }
        generateAndPositionMobiles();
        balancePower();
    }
}
