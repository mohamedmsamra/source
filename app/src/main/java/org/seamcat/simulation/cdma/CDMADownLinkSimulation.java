package org.seamcat.simulation.cdma;

import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.cdma.NonInterferedCapacitySearch;
import org.seamcat.cdma.exceptions.ScalingException;
import org.seamcat.dmasystems.UserShouldBeIgnoredException;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.CapacityEndingTest;
import org.seamcat.events.CapacityEndingTrial;
import org.seamcat.events.CapacityStartingCapacityFinding;
import org.seamcat.events.CapacityStartingTest;
import org.seamcat.model.Scenario;
import org.seamcat.model.cellular.BaseStation;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.core.GridPositionCalculator;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.distributions.UniformPolarAngleDistributionImpl;
import org.seamcat.model.distributions.UniformPolarDistanceDistributionImpl;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.PreSimulationResults;
import org.seamcat.model.systems.ParallelSimulation;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.Unit;
import org.seamcat.model.types.result.BarChartResultType;
import org.seamcat.model.types.result.BarChartValue;
import org.seamcat.model.types.result.DoubleResultType;
import org.seamcat.model.types.result.IntegerResultType;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.scenario.CellularSystemImpl;
import org.seamcat.simulation.cellular.CellularCalculations;
import org.seamcat.simulation.result.PreSimulationResultsImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CDMADownLinkSimulation implements ParallelSimulation {

    private static final Logger LOG = Logger.getLogger(CDMADownLinkSystemPlugin.class);

    public static final String FREQUENCY = "Frequency";
    public static final String CAPACITY_FINDING = "Capacity finding";
    public static final String NON_INTERFERED_CAPACITY = "Non interfered capacity";

    public static final String TRANSMITTED_TRAFFIC_CHANNEL_POWER_DBM = "Transmitted traffic channel power";
    public static final String RECEIVED_TRAFFIC_CHANNEL_POWER_DBM = "Received traffic channel power";
    public static final String TOTAL_RECEIVED_POWER_DBM = "Total received power";
    public static final String POWER_SCALED_DOWN_TO_MAX = "Power scaled down to max";
    private CDMADownLinkBaseStation[][] cells;
    private CDMADownLinkBaseStation referenceCell;
    private Point2D location;
    private int useridcount = 0;

    private Distribution userAngle = new UniformPolarAngleDistributionImpl(360);
    private Distribution userLocation = new UniformPolarDistanceDistributionImpl(1);

    private List<CDMADownLinkMobileStation> activeUsers = new ArrayList<>();
    private List<CDMADownLinkMobileStation> droppedUsers = new ArrayList<>();
    private List<CDMADownLinkMobileStation> noLinkLevelFoundUsers = new ArrayList<>();

    private boolean finalFineTuning = false;
    private boolean fineTuning = false;
    private double processingGain;
    private CellularSystem system;
    private PreSimulationResultsImpl preSimulationResults;
    private double frequency;

    public CellularSystem getSystem() {
        return system;
    }

    public CDMADownLinkSimulation(CellularSystem system, PreSimulationResults preSimulationResults) {
        this.system = system;
        this.preSimulationResults = (PreSimulationResultsImpl) preSimulationResults;
    }

    protected double getFrequency() {
        return frequency;
    }

    @Override
    public void preSimulate() {
        location = new Point2D();
        frequency = system.getFrequency().trial();
        preSimulationResults.getPreSimulationResults().getSingleValueTypes().add(new DoubleResultType(FREQUENCY, Unit.MHz.name(), frequency));
        AntennaGainConfiguration[] sectorAntennas = CellularCalculations.createSectorAntennas((CellularSystemImpl) system);
        preSimulationResults.setAntennaGainForSectors( sectorAntennas );

        generateSystemCells();
        calculateThermalNoise();

        try {
            findNonInterferedCapacity( null );
        } catch (InterruptedException e ) {
            // handle
        }
    }

    @Override
    public void simulateAsVictim(EventResult eventResult) {
        location = new Point2D();
        simulate();

    }

    @Override
    public void simulateAsInterferingSystem(Scenario scenario, EventResult result, InterferenceLink link, Point2D victimSystemPosition) {
        // find correlation position and move system accordingly

        simulate();
    }

    public double getMaxTrafficChannelPowerIndBm() {
        return Mathematics.fromWatt2dBm(getMaxTrafficChannelPowerInWatt());

    }

    public double getMaxTrafficChannelPowerInWatt() {
        double maxBroadcastPower = system.getCDMASettings().getDownLinkSettings().getMaximumBroadcastChannel();
        double maxPowerChannelFraction = system.getCDMASettings().getDownLinkSettings().getMaximumTrafficChannelFraction();

        return Mathematics.fromdBm2Watt(maxBroadcastPower) * maxPowerChannelFraction;
    }

    private void simulate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Positioning the Networks");
        }
        //positionSystem();
        if (LOG.isDebugEnabled()) {
            LOG.debug(" Generating And Positioning mobiles: "+toString());
            LOG.debug(" System Description: CDMA System");
            LOG.debug(" System Minimum coupling loss: "+system.getMinimumCouplingLoss()+" dBm");
            LOG.debug(" Calculate the thermal noise()");
        }
        generateAndPositionMobiles();
        balancePower();
    }

    private void balancePower() {
        calculateProcessingGain();
        int cellsPerSite = cellsPerSite();
        for (CDMADownLinkBaseStation[] cell : cells) {
            for (int k = 0; k < cellsPerSite; k++) {
                cell[k].initializeTransmitPowerLevels(system.getCDMASettings().getDownLinkSettings());
            }
        }
        try {
            internalPowerBalance();
        } catch (Exception ex) {
            LOG.error("Error scaling power", ex);
        }
    }

    public void generateSystemCells() {
        cells = new CDMADownLinkBaseStation[getNumberOfCellSitesInPowerControlCluster()][cellsPerSite()];
        boolean triSectorCells = system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.SingleSector;

        generateCells(cells[0], location, 0, triSectorCells);
        for (int i = 0; i < cells.length; i++) {
            generateCells(cells[i], new Point2D(), i, triSectorCells);
        }
        repositionSystem( location );
        referenceCell = cells[system.getLayout().getIndexOfReferenceCell()][system.getLayout().getReferenceSector()];
    }


    private void generateCells(CDMADownLinkBaseStation[] _cells, Point2D location, int _cellid, boolean triSector) {
        int cellid;
        for (int i = 0; i < _cells.length; i++) {
            if (_cells[i] == null) {
                if (triSector) {
                    if (_cellid == 0) {
                        cellid = i;
                    } else {
                        cellid = (_cellid * cellsPerSite()) + i;
                    }
                } else {
                    cellid = _cellid;
                }

                BaseStation bs = system.getLink().getBaseStation();
                _cells[i] = new CDMADownLinkBaseStation(this, location, cellid, bs.getHeight().trial(), bs.getTilt().trial(),(i + 1), triSector);
                _cells[i].setCellLocationId(_cellid);
                _cells[i].initializeTransmitPowerLevels(system.getCDMASettings().getDownLinkSettings());
            }
        }
    }

    /**
     * Calculate the thermal noise based on the bandwidth and the receiver noise figure
     */
    private void calculateThermalNoise() {
        double noise = CellularCalculations.calculateThermalNoise(system.getBandwidth(), system.getReceiverNoiseFigure());
        preSimulationResults.setThermalNoise(Mathematics.fromdBm2Watt(noise));
        if (LOG.isDebugEnabled()) {
            LOG.debug(" Thermal noise (dBm)" + noise);
        }
    }

    /**
     * Moves entire system to new position specified by center (x,y)
     */
    public void repositionSystem(Point2D p) {
        double d = CellularCalculations.getInterCellDistance(system);

        if (referenceCell == null) {
            referenceCell = cells[0][0];
        }

        if ( system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP ) {
            location = GridPositionCalculator.ppg2(true, referenceCell.getCellLocationId(), p, d);
        } else {
            location = GridPositionCalculator.standard(true, referenceCell.getCellLocationId(), p, d);
        }

        for ( int j=0; j<cells.length; j++) {
            for (int i = 0; i < cells[0].length; i++) {
                if ( system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP ) {
                    setPosition(cells[j][i], GridPositionCalculator.ppg2(false, j, location, d));
                } else {
                    setPosition(cells[j][i], GridPositionCalculator.standard(false, j, location, d));
                }
            }
        }
    }

    private void setPosition(CDMADownLinkBaseStation bs, Point2D p) {
        bs.setPosition(p);
        bs.calculateHexagon(system.getLayout().getCellRadius());
    }


    /**
     * Compute the optimum number of users without external interference as part of the pre-setting of the CDMA network
     * <ol>
     *     <li>uplink: users per cell is based on the average noise rise (dB)</li>
     * <li>downlink: users per cell is based on the succesful trial (i.e. trial to connect to the network)</li>
     *</ol>
     * @param context of a type object
     * @throws InterruptedException
     */
    public void findNonInterferedCapacity(Object context) throws InterruptedException {
        if ( preSimulationResults.getPreSimulationResults().hasSingleValue(NON_INTERFERED_CAPACITY) ) {
            // this has already been calculated to return;
            return;
        }

        int capacity = system.getUsersPerCell();
        if (system.getCDMASettings().isSimulateNonInterferedCapacity()) {
            fineTuning = false;

            BarChartResultType capFinding;
            int deltaN = system.getCDMASettings().getDeltaUsersPerCell();

            capFinding = new BarChartResultType(CAPACITY_FINDING, "Users per cell", "Successful trials");
            EventBusFactory.getEventBus().publish( new CapacityStartingCapacityFinding(context, system.getUsersPerCell(), deltaN,
                    system.getCDMASettings().getToleranceOfInitialOutage(), system.getCDMASettings().getNumberOfTrials(), false, 0.8));

            preSimulationResults.getPreSimulationResults().getBarChartResultTypes().add( capFinding );
            NonInterferedCapacitySearch search = new NonInterferedCapacitySearch(system.getUsersPerCell(), deltaN);
            search = findNonInterferedCapacityInternal( capFinding, search, context);
            while ( !search.isConverged() ) {
                search = findNonInterferedCapacityInternal( capFinding,  search, context);
            }

            capacity = search.getCapacity();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initial Capacity found: " + capacity + " users per cell");
            }
        }
        preSimulationResults.getPreSimulationResults().getSingleValueTypes().add(new IntegerResultType(NON_INTERFERED_CAPACITY, "", capacity));
    }

    /**
     * Method to compute the optimum number of users in a non-interfered downlink network
     * <p></p>
     * This is part of the pre-simulation stage of the CDMA simulation
     * <p></p>
     * The returned value is passed on to start the simulation
     *
     * @return optimum number of mobile station
     * @throws InterruptedException
     */
    private NonInterferedCapacitySearch findNonInterferedCapacityInternal(BarChartResultType capFinding, NonInterferedCapacitySearch search, Object context)
            throws InterruptedException
    {
        double succesCriteria = 0.8;
        int usersPerCell = search.getCapacity();
        int deltaN = search.getDeltaUsers();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Finding non interfered capacity. [N = " + usersPerCell + ", deltaN = " + deltaN + "]");
        }
        int numberOfTrials = system.getCDMASettings().getNumberOfTrials();
        EventBusFactory.getEventBus().publish( new CapacityStartingTest(context, usersPerCell, numberOfTrials));
        int trialThreshold = (int) Math.ceil(usersPerCell * getNumberOfBaseStations() * system.getCDMASettings().getToleranceOfInitialOutage() / 100);

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
                        cells[j][k].initializeTransmitPowerLevels(system.getCDMASettings().getDownLinkSettings());
                    }
                }
                CDMADownLinkMobileStation user;
                for (int j = 0, stop = usersPerCell * getNumberOfBaseStations(); j < stop; j++) {
                    user = generateInitializedMobile();
                    if (user != null) {
                        if (user.connect()) {
                            activeUsers.add(user);

                            // Only cell(s) in users activelist have had their transmit
                            // power changed
                            for (CDMADownLinkLink link : user.getActiveList()) {
                                CDMADownLinkBaseStation cell = link.bs;
                                if (cell.getCurrentTransmitPower_dBm() > system.getCDMASettings().getDownLinkSettings().getMaximumBroadcastChannel()) {
                                    cell.scaleChannelPower();
                                }
                                cell.calculateCurrentChannelPower_dBm();
                            }

                        } else {
                            dropActiveUser(user, "Unable to connect");
                        }
                    } else {
                        // j--;
                    }
                } // Inner for loop
                for (int j = 0, bStop = cells.length; j < bStop; j++) {
                    for (int k = 0; k < cells[j].length; k++) {
                        cells[j][k].calculateCurrentChannelPower_dBm();
                    }
                }
                internalPowerBalance();

                dropped[i] = droppedUsers.size();

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

    public List<CDMADownLinkBaseStation> getAllBaseStations() {
        List<CDMADownLinkBaseStation> list = new ArrayList<>();
        if (cells != null) {
            for (CDMADownLinkBaseStation[] bases : cells) {
                list.addAll(Arrays.asList(bases));
            }
        }
        return list;
    }

    private void resetSystem() {
        for (CDMADownLinkBaseStation station : getAllBaseStations()) {
            station.reset();
        }
        activeUsers.clear();
        droppedUsers.clear();
        noLinkLevelFoundUsers.clear();
        useridcount = 0;
    }

    private void addPoint( BarChartResultType capFinding, int usersPerCell, double value ) {
        int num = capFinding.getChartPoints().size()+1;
        capFinding.getChartPoints().add( new BarChartValue(num+"#Users: "+ usersPerCell, value));
    }

    private int getNumberOfBaseStations() {
        return (system.getLayout().getSectorSetup() == CellularLayout.SectorSetup.SingleSector ? 1 : 3) *
                getNumberOfCellSitesInPowerControlCluster();
    }

    private int getNumberOfCellSitesInPowerControlCluster() {
        switch ( system.getLayout().getTierSetup() ) {
            case SingleCell: return 1;
            case OneTier: return 7;
            default: return 19;
        }
    }

    /**
     * Method that returns the number of tiers
     * <ol>
     *     <ul>single cell: 0</ul>
     *     <ul>one tier: 1</ul>
     *     <ul>default - full set-up: 2</ul>
     * </ol>
     *
     * @return the number of tiers (0, 1, 2)
     */
    public int getNumberOfTiers() {
        switch (system.getLayout().getTierSetup()) {
            case SingleCell: return 0;
            case OneTier: return 1;
            default: return 2;
        }
    }

    /**
     * Calculates the Processing Gain G for the current system. G is given as:
     * <br>
     * <code>G = (BW / R) * 10^3 </code><br>
     * <code>G = (Receiver Bandwith[MHz] / Service Bit Rate[kbps]) * 10^3</code>
     *
     * @return processing gain G
     */
    private double calculateProcessingGain() {
        processingGain = system.getBandwidth() / system.getCDMASettings().getVoiceBitRate() * 1000;
        processingGain = Mathematics.linear2dB(processingGain);
        return processingGain;
    }

    private int cellsPerSite() {
        return system.getLayout().getSectorSetup() == CellularLayout.SectorSetup.SingleSector ? 1 : 3;
    }

    private CDMADownLinkMobileStation generateInitializedMobile() {
        CDMADownLinkMobileStation user = generateUserTerminal();

        positionUser(user);
        initializeUser(user);

        try {
            performSystemSpecificInitialization(user);
        } catch (UserShouldBeIgnoredException ex) {
            return null;
        }
        return user;
    }

    private CDMADownLinkMobileStation generateUserTerminal() {
        CDMADownLinkMobileStation user = new CDMADownLinkMobileStation(this,
                new Point2D(0, 0), useridcount++, system.getLink().getMobileStation().getAntennaGain().trial(),
                system.getLink().getMobileStation().getAntennaHeight().trial());
        user.setThermalNoise(preSimulationResults.getThermalNoise());
        return user;
    }

    /** position the user into the cellular system
     * <ol>
     *    <li>calculate the location</li>
     *    <li>generate the user</li>
     *    <li>Recursive call if user is outside system</li>
     * </ol>
     */
    private void positionUser(CDMADownLinkMobileStation user) {
        // Calculate location
        double userAng = userAngle.trial();
        double userDist = 0.0;
        double interCellDistance = CellularCalculations.getInterCellDistance(system);
        if(system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP){
            userDist = (getNumberOfTiers() + 0.5) * interCellDistance * userLocation.trial();
        }else{
            userDist = (getNumberOfTiers() + 1) * interCellDistance * userLocation.trial();
        }

        // Generate user
        user.setPosition(location.add(new Point2D(userDist * Mathematics.cosD(userAng), userDist * Mathematics.sinD(userAng))));

        double [] shiftX;
        shiftX = new double[3];
        shiftX[0] = getCellRadius();
        shiftX[1] = -getCellRadius() * Mathematics.cosD(60.0);
        shiftX[2] = -getCellRadius() * Mathematics.cosD(60.0);

        double [] shiftY;
        shiftY = new double[3];
        shiftY[0] = 0;
        shiftY[1] = -getCellRadius() * Mathematics.sinD(60.0);
        shiftY[2] = getCellRadius() * Mathematics.sinD(60.0);

        for (CDMADownLinkBaseStation[] cell : cells) {
            Point2D position = user.getPosition();
            if(system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP){
                if (cell[0].isInside(position, 0, 0)) {
                    return;
                }
            }else{

                if ((cell[0].isInside(position, shiftX[0], shiftY[0]))||
                        (cell[1].isInside(position, shiftX[1], shiftY[1]))||
                        (cell[2].isInside(position, shiftX[2], shiftY[2]))){
                    return;
                }
            }
        }
        // Recursive call if user is outside system
        positionUser(user);
    }

    private double getCellRadius() {
        return system.getLayout().getCellRadius();
    }

    /**
     * Initialise users
     * <p></p>
     * <ol>
     *    <li>assign speed and activity</li>
     *    <li>generate links to all cells in the system</li>
     *    <li>Based on determined path loss - define active list</li>
     * </ol>
     *
     * @param user generic mobile type
     */
    private void initializeUser(CDMADownLinkMobileStation user) {
        // Assign speed and activity
        user.setMobilitySpeed(system.getLink().getMobileStation().getMobility().trial());
        // Generate links to all Cells in system
        user.generateSortedBSLinks(cells, getNumberOfBaseStations());
        // Based on determined path loss - define active list
        user.selectActiveList( system.getHandoverMargin());
    }


    private void performSystemSpecificInitialization(CDMADownLinkMobileStation user) throws UserShouldBeIgnoredException {
        /*boolean voice = trialVoiceActivity();
        if (!voice) {
            for (AbstractDmaLink link : user.getActiveList()) {
                CDMADownlink l = (CDMADownlink) link;
                l.getBaseStation().addVoiceInActiveUser(l);
            }
            inactiveUsers.add(user);
            throw new UserShouldBeIgnoredException();
        }*/
        try {
            // Calculates (and caches) power reception summation values for users current position in system
            user.calculateInitialReceivedPower(system.getCDMASettings().getDownLinkSettings().getMaximumBroadcastChannel());
            // Based on power reception values users geometry is calculated (and cached)
            CDMALinkLevelData lld = ((CellularSystemImpl) system).getCDMASettings().getLld();
            user.calculateGeometry( lld.getInitialMinimumGeometry(), lld.getInitialMaximumGeometry() );
            // Locate Link Level Data based on Geometry and Received Power
            user.findLinkLevelDataPoint(lld);
            // Based on link level data derive users initial received traffic channel power
            user.calculateReceivedTrafficChannelPowerInWatt();
        } catch (Exception e) {
            if (!user.isLinkLevelDataPointFound()) {
                noLinkLevelFoundUsers.add(user);
                useridcount--;
                throw new UserShouldBeIgnoredException();
            }
            throw new IllegalStateException("Unable to calculate PTraf", e);
        }

    }

    public void connect(CDMADownLinkLink link) {
        link.bs.connect( link );
    }

    public void inActiveConnect(CDMADownLinkLink link) {
        link.bs.inActiveConnect( link );
    }

    public CDMADownLinkLink createLink(CDMADownLinkBaseStation bs, CDMADownLinkMobileStation ms) {
        CDMADownLinkLink link = new CDMADownLinkLink();
        link.ms = ms;
        link.bs = bs;
        //bsMap.put( link, bs );
        //msMap.put( link, ms );
        return link;
    }

    public boolean mobileStationActive( CDMADownLinkLink link, CDMADownLinkBaseStation station, int depth) {
        List<CDMADownLinkLink> list = link.ms.getActiveList();

        for (int i=0; i<=depth; i++) {
            if ( list.get(i).bs == station ) return true;
        }

        return false;
    }

    /**
     * Users dropCall method will be called and user will be moved from active list <br>
     *     to droppedUsers List
     */
    public void dropActiveUser(CDMADownLinkMobileStation ms, String reason) {
        ms.dropCall();
        ms.setDropReason(reason);
        activeUsers.remove(ms);
        droppedUsers.add(ms);
    }

    /**
     * Method that adjust the transmit power of the base station based on a Ec/Ior requirement
     * <p></p>
     * Depending on a success threshold set as input parameter, some user (i.e. mobile station) may be dropped
     */
    private int internalPowerBalance() {
        try {
            boolean powerConverged = false;
            double maxPower = Mathematics.fromWatt2dBm( cells[0][0].getMaximumChannelPower_Watt());
            CDMALinkLevelData lld = ((CellularSystemImpl) system).getCDMASettings().getLld();
            while (!powerConverged) {
                powerConverged = true;

                for (CDMADownLinkMobileStation user : activeUsers) {
                    user.calculateReceivedPower();
                    user.calculateGeometry(lld.getInitialMinimumGeometry(), lld.getInitialMaximumGeometry());
                    user.findLinkLevelDataPoint(lld);
                }

                for (CDMADownLinkBaseStation[] cell1 : cells) {
                    for (CDMADownLinkBaseStation cell : cell1) {
                        if (cell.countServedUsers() > 0) {
                            double curPower = cell.calculateCurrentChannelPower_dBm();

                            if (curPower > maxPower) {
                                if (curPower - maxPower > 0.0001) {
                                    cell.scaleChannelPower();
                                    powerConverged = false;
                                }
                            }
                        }
                    }
                }

                if (powerConverged) {
                    for (int i = 0; i < activeUsers.size(); i++) {
                        CDMADownLinkMobileStation user = activeUsers.get(i);
                        user.calculateAchievedEcIor();
                        int freePassCount = user.getLinkQualityExceptions();
                        if (!user.meetsEcIorRequirement(system.getCDMASettings().getDownLinkSettings().getSuccessThreshold(), true)) {
                            if (user.getPowerScaledUpCount() > 2) {
                                // Power was scaled up for this user on last loop ->
                                // drop user
                                dropActiveUser(user, "Power scaled up too many times");
                                powerConverged = false;
                                continue;
                            }
                            user.calculateReceivedTrafficChannelPowerInWatt();
                            user.setPowerScaledUpCount(user.getPowerScaledUpCount() + 1);
                            powerConverged = false;
                        }
                        user.setLinkQualityExceptions(freePassCount);
                    }
                }

            }// while !powerConverged

            for (int i = 0; i < activeUsers.size(); i++) {
                CDMADownLinkMobileStation user = activeUsers.get(i);
                user.calculateReceivedPower();
                user.calculateGeometry( lld.getInitialMinimumGeometry(), lld.getInitialMaximumGeometry() );
                user.findLinkLevelDataPoint(lld);
                user.calculateAchievedEcIor();
                if (!user.meetsEcIorRequirement(system.getCDMASettings().getDownLinkSettings().getSuccessThreshold(), true)) {
                    dropActiveUser(user,"due to success threshold");
                    i--;
                }
            }

        } catch (ScalingException ex) {
            LOG.error("Error scaling power", ex);
        }
        return 0;
    }

    /**
     * adds a pre-determmined number of users. The capacity (i.e. number of users per cell) is multiplied by the number <br>
     *     of cells (i.e. number of BS x number of sector) the capacity is evaluated when there is no interferer
     */
    private void generateAndPositionMobiles() {
        // Add predetermined number of users
        int capacity = preSimulationResults.getPreSimulationResults().findIntValue(NON_INTERFERED_CAPACITY);
        for (int i = 0, stop = capacity * getNumberOfBaseStations(); i < stop; i++) {
            CDMADownLinkMobileStation user = generateInitializedMobile();

            if (user == null) {
                continue;
            }
            if (user.connect()) {
                activeUsers.add(user);
            } else {
                dropActiveUser(user, "Unable to connect during first initialisation of UE");
            }
        }// for loop - all users added
    }
}
