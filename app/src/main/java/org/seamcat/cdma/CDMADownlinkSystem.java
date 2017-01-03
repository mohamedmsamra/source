package org.seamcat.cdma;

import org.apache.log4j.Logger;
import org.seamcat.cdma.exceptions.ScalingException;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.dmasystems.UserShouldBeIgnoredException;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.CapacityEndingTest;
import org.seamcat.events.CapacityEndingTrial;
import org.seamcat.events.CapacityStartingTest;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.result.BarChartResultType;
import org.seamcat.scenario.CDMADownLinkImpl;

public class CDMADownlinkSystem extends CDMASystem {

    private static final Logger LOG = Logger.getLogger(CDMADownlinkSystem.class);

    public CDMADownlinkSystem() {
        super();
        init();
    }

    public CDMADownlinkSystem(AbstractDmaSystem<?> sys) {
        super(sys);
        init();
        if ( sys instanceof CDMADownlinkSystem) {
            CDMADownLinkImpl other = sys.getSystemSettings().getCDMASettings().getDownLinkSettings();
            getSystemSettings().getCDMASettings().getDownLinkSettings().setSuccessThreshold(other.getSuccessThreshold());
            getSystemSettings().getCDMASettings().getDownLinkSettings().setPilotChannelFraction(other.getPilotChannelFraction());
            getSystemSettings().getCDMASettings().getDownLinkSettings().setOverheadChannelFraction(other.getOverheadChannelFraction());
            getSystemSettings().getCDMASettings().getDownLinkSettings().setMaximumBroadcastChannel(other.getMaximumBroadcastChannel());
            getSystemSettings().getCDMASettings().getDownLinkSettings().setMaximumTrafficChannelFraction(other.getMaximumTrafficChannelFraction());

        }
    }

    private void init() {
        getSystemSettings().setUpLink(false);
        getSystemSettings().getCDMASettings().setDownLinkSettings(new CDMADownLinkImpl());
    }

    public double getMaxTrafficChannelPowerIndBm() {
        return Mathematics.fromWatt2dBm(getMaxTrafficChannelPowerInWatt());

    }

    public double getMaxTrafficChannelPowerInWatt() {
        double maxBroadcastPower = getSystemSettings().getCDMASettings().getDownLinkSettings().getMaximumBroadcastChannel();
        double maxPowerChannelFraction = getSystemSettings().getCDMASettings().getDownLinkSettings().getMaximumTrafficChannelFraction();

        return Mathematics.fromdBm2Watt(maxBroadcastPower) * maxPowerChannelFraction;
    }


    @Override
    public void balanceInterferedSystem() {
        internalPowerBalance();
    }

    @Override
    public void balancePower() {
        calculateProcessingGain();
        int cellsPerSite = cellsPerSite();
        for (int j = 0, bStop = cells.length; j < bStop; j++) {
            for (int k = 0; k < cellsPerSite; k++) {
                ((CdmaBaseStation)cells[j][k]).initializeTransmitPowerLevels();
            }
        }
        try {
            internalPowerBalance();
        } catch (Exception ex) {
            LOG.error("Error scaling power", ex);
        }
    }

    /**
     * Method to compute the optimum number of users in a non-interfered downlink network
     * <p></p>
     * This is part of the pre-simulation stage of the CDMA simulation
     * <p></p>
     * The returned value is passed on to start the simulation
     *
     * @param capFinding
     * @param search
     * @param context
     * @return optimum number of mobile station
     * @throws InterruptedException
     */
    @Override
    protected NonInterferedCapacitySearch findNonInterferedCapacityInternal(BarChartResultType capFinding, NonInterferedCapacitySearch search, Object context)
    throws InterruptedException
    {
        int usersPerCell = search.getCapacity();
        int deltaN = search.getDeltaUsers();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding non interfered capacity. [N = " + usersPerCell + ", deltaN = " + deltaN + "]");
        }
        int numberOfTrials = getSystemSettings().getCDMASettings().getNumberOfTrials();
        EventBusFactory.getEventBus().publish( new CapacityStartingTest(context, usersPerCell, numberOfTrials));
        int trialThreshold = (int) Math.ceil(usersPerCell * getNumberOfBaseStations() * getSystemSettings().getCDMASettings().getToleranceOfInitialOutage() / 100);

        try {
            calculateProcessingGain();
            int[] dropped = new int[numberOfTrials];
            int smartStopIndex = 0;
            for (int i = 0; i < dropped.length; i++) {
                // Detect if it is still posible to reach target:
                int currentSuccess = 0;
                for (int j = 0; j < i; j++) {
                    if (dropped[j] <= trialThreshold) {
                        currentSuccess++;
                    }
                }// Current Succes for loop

                if (currentSuccess > numberOfTrials * succesCriteria) {
                    // We have already reached more than target -> no need to
                    // continue
                    smartStopIndex = i;
                    for (; i < dropped.length; i++) {
                        dropped[i] = -1;
                    }
                    continue;

                } else if (currentSuccess + dropped.length - i < numberOfTrials
                        * succesCriteria) {
                    // Target unreachable even if rest of trials are successes ->
                    // no need to continue
                    smartStopIndex = i;
                    for (; i < dropped.length; i++) {
                        dropped[i] = trialThreshold + 1;
                    }
                    continue;
                }

                resetSystem();
                int cellsPerSite = cellsPerSite();
                for (int j = 0, bStop = cells.length; j < bStop; j++) {
                    for (int k = 0; k < cellsPerSite; k++) {
                        ((CdmaBaseStation)cells[j][k]).initializeTransmitPowerLevels();
                    }
                }
                CdmaUserTerminal user;
                for (int j = 0, stop = usersPerCell * getNumberOfBaseStations(); j < stop; j++) {
                    user = generateInitializedMobile();
                    if (user != null) {
                        if (user.connectToBaseStationsDownlink()) {
                            user.setAllowedToConnect(true);
                            activeUsers.add(user);

                            // Only cell(s) in users activelist have had their transmit
                            // power changed
                            for (AbstractDmaLink l : user.getActiveList()) {
                                CDMADownlink link = (CDMADownlink) l;
                                CdmaBaseStation cell = link.getBaseStation();
                                if (cell.getCurrentTransmitPower_dBm() > cell.getMaximumTransmitPower()) {
                                    cell.scaleChannelPower();
                                }
                                cell.calculateCurrentChannelPower_dBm();
                            }

                        } else {
                            dropActiveUser(user);
                            user.setDropReason("Unable to connect");
                        }
                    } else {
                        // j--;
                    }
                } // Inner for loop
                for (int j = 0, bStop = cells.length; j < bStop; j++) {
                    for (int k = 0; k < cells[j].length; k++) {
                        CdmaBaseStation cell = (CdmaBaseStation) cells[j][k];
                        cell.calculateCurrentChannelPower_dBm();
                    }
                }
                internalPowerBalance();

                dropped[i] = getDroppedUsers().size();

                EventBusFactory.getEventBus().publish( new CapacityEndingTrial(context, dropped[i],
                        (dropped[i] <= trialThreshold), i));
            }// Outer for loop
            int succes = 0;
            int stop = dropped.length;
            if (smartStopIndex > 0) {
                stop = smartStopIndex;
            }
            for (int i = 0; i < stop; i++) {
                if (dropped[i] <= trialThreshold) {
                    succes++;
                }
            }// Succes for loop
            addPoint(capFinding, usersPerCell, succes);
            EventBusFactory.getEventBus().publish( new CapacityEndingTest( context, usersPerCell, succes));
            if (LOG.isDebugEnabled()) {
                LOG.debug("Number of succesfull trials was: " + succes + " out of " + numberOfTrials);
            }
            if (succes > numberOfTrials * succesCriteria) {
                if (fineTuning) {
                    if (deltaN == 1 && finalFineTuning) {
                        return new NonInterferedCapacitySearch(usersPerCell);
                    } else {
                        deltaN = (int) Math.ceil(deltaN / 2.0);
                    }
                }// fine tuning loop
                usersPerCell += deltaN;
                return new NonInterferedCapacitySearch(usersPerCell, deltaN);
            } else if (succes < numberOfTrials * succesCriteria) {
                fineTuning = true;
                if (deltaN == 1) {
                    finalFineTuning = true;
                } else {
                    deltaN = (int) Math.ceil(deltaN / 2.0);
                }
                usersPerCell -= deltaN;
                return new NonInterferedCapacitySearch(usersPerCell, deltaN);
            } else {
                return new NonInterferedCapacitySearch(usersPerCell);
            }

        } catch (Exception ex) {
            if ( ex.getCause() instanceof InterruptedException ) {
                throw (InterruptedException) ex.getCause();
            }
            LOG.error("An Error occured", ex);
            ex.printStackTrace();
            return new NonInterferedCapacitySearch(-1);
        }
    }

    /**
     * Method that adjust the transmit power of the base station based on a Ec/Ior requirement
     * <p></p>
     * Depending on a success threshold set as input parameter, some user (i.e. mobile station) may be dropped
     */
    @Override
    protected int internalPowerBalance() {
        try {
            boolean powerConverged = false;
            double maxPower = Mathematics.fromWatt2dBm( ((CdmaBaseStation)cells[0][0]).getMaximumChannelPower_Watt());
            while (!powerConverged) {
                powerConverged = true;

                for (CdmaUserTerminal user : activeUsers) {
                    user.calculateReceivedPower();
                    user.calculateGeometry(getSystemSettings().getCDMASettings().getLld().getInitialMinimumGeometry(), getSystemSettings().getCDMASettings().getLld().getInitialMaximumGeometry());
                    user.findLinkLevelDataPoint(getLinkLevelData());
                }

                for (AbstractDmaBaseStation[] cell1 : cells) {
                    for (AbstractDmaBaseStation cell : cell1) {
                        if (cell.countServedUsers() > 0) {
                            double curPower = ((CdmaBaseStation)cell).calculateCurrentChannelPower_dBm();

                            if (curPower > maxPower) {
                                if (curPower - maxPower > 0.0001) {
                                    ((CdmaBaseStation)cell).scaleChannelPower();
                                    powerConverged = false;
                                }
                            }
                        }
                    }
                }

                if (powerConverged) {
                    for (int i=0; i< activeUsers.size(); i++) {
                        CdmaUserTerminal user = activeUsers.get(i);
                        user.calculateAchievedEcIor();
                        int freePassCount = user.getLinkQualityExceptions();
                        if (!user.meetsEcIorRequirement(getSystemSettings().getCDMASettings().getDownLinkSettings().getSuccessThreshold(), true)) {
                            if (user.getPowerScaledUpCount() > 2) {
                                // Power was scaled up for this user on last loop ->
                                // drop user
                                dropActiveUser(user);
                                user.setDropReason("Power scaled up too many times");
                                powerConverged = false;
                                continue;
                            }
                            user.calculateReceivedTrafficChannelPowerInWatt();
                            user.increasePowerScaledUpCount();
                            powerConverged = false;
                        }
                        user.setLinkQualityExceptions(freePassCount);
                    }
                }

            }// while !powerConverged

            for (int i = 0; i < activeUsers.size(); i++) {
                CdmaUserTerminal user = activeUsers.get(i);
                user.calculateReceivedPower();
                user.calculateGeometry( getSystemSettings().getCDMASettings().getLld().getInitialMinimumGeometry(), getSystemSettings().getCDMASettings().getLld().getInitialMaximumGeometry() );
                user.findLinkLevelDataPoint(getLinkLevelData());
                user.calculateAchievedEcIor();
                if (!user.meetsEcIorRequirement(getSystemSettings().getCDMASettings().getDownLinkSettings().getSuccessThreshold(), true)) {
                    dropActiveUser(user);
                    user.setDropReason("due to success threshold");
                    i--;
                }

            }

        } catch (ScalingException ex) {
            LOG.error("Error scaling power", ex);
        }
        return 0;
    }

    /**
     * This method performs system specific initialization
     * <ol>
     * Calculates (and caches) power reception summation values for users current position in system
     * <li> Based on power reception values users geometry is calculated (and cached) </li>
     * <li> Locate Link Level Data based on Geometry and Received Power</li>
     * <li> Based on link level data derive users initial received traffic channel power</li>
     * </ol>
     * @param user
     * @throws UserShouldBeIgnoredException
     */
    @Override
    protected void performSystemSpecificInitialization(CdmaUserTerminal user) throws UserShouldBeIgnoredException {
        boolean voice = trialVoiceActivity();
        if (!voice) {
            for (AbstractDmaLink link : user.getActiveList()) {
                CDMADownlink l = (CDMADownlink) link;
                l.getBaseStation().addVoiceInActiveUser(l);
            }
            inactiveUsers.add(user);
            throw new UserShouldBeIgnoredException();
        }
        try {
            // Calculates (and caches) power reception summation values for users current position in system
            user.calculateInitialReceivedPower(getSystemSettings().getCDMASettings().getDownLinkSettings().getMaximumBroadcastChannel());
            // Based on power reception values users geometry is calculated (and cached)
            user.calculateGeometry( getSystemSettings().getCDMASettings().getLld().getInitialMinimumGeometry(), getSystemSettings().getCDMASettings().getLld().getInitialMaximumGeometry() );
            // Locate Link Level Data based on Geometry and Received Power
            user.findLinkLevelDataPoint(getLinkLevelData());
            // Based on link level data derive users initial received traffic channel power
            user.calculateReceivedTrafficChannelPowerInWatt();
        } catch (Exception e) {
            if (!user.linkLevelDataPointFound()) {
                noLinkLevelFoundUsers.add(user);
                useridcount--;
                throw new UserShouldBeIgnoredException();
            }
            throw new IllegalStateException("Unable to calculate PTraf", e);
        }

    }
}
