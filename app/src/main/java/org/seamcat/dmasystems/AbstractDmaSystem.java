package org.seamcat.dmasystems;

import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMASystem;
import org.seamcat.cdma.CDMAUplinkSystem;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.cellular.BaseStation;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.core.GridPositionCalculator;
import org.seamcat.model.distributions.*;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.OptionalFunction;
import org.seamcat.model.plugin.antenna.HorizontalVerticalInput;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.ofdma.OfdmaSystem;
import org.seamcat.ofdma.UplinkOfdmaSystem;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.scenario.BaseStationImpl;
import org.seamcat.scenario.CellularSystemImpl;
import org.seamcat.scenario.MobileStationImpl;
import org.seamcat.simulation.cellular.CellularCalculations;
import org.seamcat.simulation.cellular.CellularVictimSystemSimulation;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.PreSimulationResultsImpl;

import java.util.*;

public abstract class AbstractDmaSystem<GenericMobileType extends AbstractDmaMobile> {

    protected static Logger LOG = Logger.getLogger(AbstractDmaSystem.class);
    protected static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private CellularSystemImpl systemSettings;
    private MutableEventResult eventResult;
    private PreSimulationResultsImpl results;
    private CellularVictimSystemSimulation victimSimulation;

    public CellularSystemImpl getSystemSettings() {
        return systemSettings;
    }

    public PreSimulationResultsImpl getResults() {
        return results;
    }

    public void setResults(PreSimulationResultsImpl results) {
        this.results = results;
    }

    public void initialize(MutableEventResult eventResult ) {
        this.eventResult = eventResult;
    }

    public MutableEventResult getEventResult() {
        return eventResult;
    }

    public void setCellularVictimSimulation( CellularVictimSystemSimulation cellularVictimSimulation ) {
        this.victimSimulation = cellularVictimSimulation;
    }

    public void setCells( AbstractDmaBaseStation[][] cells ) {
        this.cells = cells;
    }

    public AbstractDmaBaseStation[][] cells; // index = 0 -> Center Cell
    public List<AbstractDmaBaseStation> selectedCell = new ArrayList<AbstractDmaBaseStation>();
    public List<GenericMobileType> activeUsers = new ArrayList<GenericMobileType>();
    public List<GenericMobileType> selectedCellActiveUsers = new ArrayList<GenericMobileType>();
    public List<GenericMobileType> noLinkLevelFoundUsers = new ArrayList<GenericMobileType>();

    protected double processingGain;

    protected boolean externalInterferenceActive = false;
    protected double frequency = 0;
    protected double locationX;
    protected double locationY;

    public AbstractDmaBaseStation referenceCell;
    protected int useridcount = 1;
    protected boolean victimSystem = false;

    private static final double SQRT3 = Math.sqrt(3);
    protected final AbstractDistribution random = new UniformDistributionImpl(0,1);
    private AbstractDistribution userAngle = new UniformPolarAngleDistributionImpl(360);
    private AbstractDistribution userLocation = new UniformPolarDistanceDistributionImpl(1);

    public AbstractDmaSystem() {
        BaseStationImpl bs = new BaseStationImpl(new ConstantDistributionImpl(30), new ConstantDistributionImpl(0), createDefaultTriSectorAntenna());
        MobileStationImpl ms = new MobileStationImpl(new ConstantDistributionImpl(1.5), new ConstantDistributionImpl(0),new StairDistributionImpl(
                new DiscreteFunction( Arrays.asList(new Point2D[] { new Point2D(0, 0.25), new Point2D(3, 0.50),
                        new Point2D(30, 0.75), new Point2D(100, 1) }))) );
        systemSettings = new CellularSystemImpl("", null, null, null,
                SeamcatFactory.propagation().getHataSE21(),bs, ms);
    }

    public AbstractDmaSystem(AbstractDmaSystem<?> system) {
        this();
        systemSettings.setName( system.toString() );
        systemSettings.setFrequency( system.getSystemSettings().getFrequency() );
        systemSettings.getLayout().setGenerateWrapAround(system.getSystemSettings().getLayout().generateWrapAround());
        this.victimSystem = system.victimSystem;
        BaseStationImpl bs = systemSettings.getLink().getBaseStation();
        bs.setHeight(system.getSystemSettings().getLink().getBaseStation().getHeight());
        bs.setTilt(system.getSystemSettings().getLink().getBaseStation().getTilt());
        bs.setAntennaGain(system.getSystemSettings().getLink().getBaseStation().getAntennaGain());
        systemSettings.getLayout().setCellRadius( system.getSystemSettings().getLayout().getCellRadius());
        systemSettings.setUsersPerCell( system.getSystemSettings().getUsersPerCell() );
        this.frequency = system.frequency;
        systemSettings.setHandoverMargin(system.getSystemSettings().getHandoverMargin());
        this.locationX = system.locationX;
        this.locationY = system.locationY;
        systemSettings.getLayout().setMeasureInterferenceFromEntireCluster(system.getSystemSettings().getLayout().measureInterferenceFromEntireCluster());
        MobileStationImpl ms = systemSettings.getLink().getMobileStation();
        ms.setAntennaGain(system.getSystemSettings().getLink().getMobileStation().getAntennaGain());
        ms.setAntennaHeight(system.getSystemSettings().getLink().getMobileStation().getAntennaHeight());
        ms.setMobility(system.getSystemSettings().getLink().getMobileStation().getMobility());

        systemSettings.getLink().setPropagationModel(system.getSystemSettings().getLink().getPropagationModel());

        //this.numberOfCellSitesInPowerControlCluster = AbstractDmaSystem.calculateNumberOfCells(getNumberOfTiers());
        systemSettings.getLayout().setSystemLayout(system.getSystemSettings().getLayout().getSystemLayout());
        systemSettings.setMinimumCouplingLoss(system.getSystemSettings().getMinimumCouplingLoss());
        systemSettings.getLayout().setTierSetup(system.getSystemSettings().getLayout().getTierSetup());
        this.processingGain = system.processingGain;
        systemSettings.getLayout().setIndexOfReferenceCell( system.getSystemSettings().getLayout().getIndexOfReferenceCell() );
        systemSettings.getLayout().setReferenceSector( system.getSystemSettings().getLayout().getReferenceSector());
        systemSettings.setReceiverNoiseFigure(system.getSystemSettings().getReceiverNoiseFigure());
        systemSettings.setBandwidth(system.getSystemSettings().getBandwidth());
        systemSettings.getLayout().setSectorSetup(system.getSystemSettings().getLayout().getSectorSetup());
        systemSettings.setTransmitter( system.getSystemSettings().getTransmitter() );
        systemSettings.setReceiver( system.getSystemSettings().getReceiver() );
    }

    @Override
    public String toString() {
        if ( systemSettings == null ) {
            return "Cellular system";
        }
        return systemSettings.getName();
    }

    public void activateInterference() {
        externalInterferenceActive = true;
    }

    public boolean isCdma() {
        return this instanceof CDMASystem;
    }

    public boolean isOfdma() {
        return this instanceof OfdmaSystem;
    }

    public boolean isExternalInterferenceActive() {
        return externalInterferenceActive;
    }

    /**
     * Calculate the thermal noise based on the bandwidth and the receiver noise figure
     */
    public void calculateThermalNoise() {
        double noise = CellularCalculations.calculateThermalNoise(systemSettings.getBandwidth(), systemSettings.getReceiverNoiseFigure());
        getResults().setThermalNoise(Mathematics.fromdBm2Watt(noise));
        if (LOG.isDebugEnabled()) {
            LOG.debug(" Thermal noise (dBm)" + noise);
        }
    }

    protected abstract void configureBaseStation(AbstractDmaBaseStation base);

    /** default tri sector antenna
     *
     * @return default tri sector antenna
     */
    public static AntennaGainConfiguration createDefaultTriSectorAntenna() {

        List<Point2D> points = new ArrayList<Point2D>();
        points.add(new Point2D(0.0, 0.0));
        points.add(new Point2D(10.0, 0.0));
        points.add(new Point2D(20.0, -0.182));
        points.add(new Point2D(30.0, -0.364));
        points.add(new Point2D(40.0, -1.37));
        points.add(new Point2D(50.0, -2.73));
        points.add(new Point2D(60.0, -3.82));
        points.add(new Point2D(70.0, -5.27));
        points.add(new Point2D(80.0, -7.18));
        points.add(new Point2D(90.0, -9.36));
        points.add(new Point2D(100.0, -11.36));
        points.add(new Point2D(110.0, -13.73));
        points.add(new Point2D(120.0, -15.55));
        points.add(new Point2D(130.0, -17.36));
        points.add(new Point2D(140.0, -18.64));
        points.add(new Point2D(150.0, -20.364));
        points.add(new Point2D(160.0, -23.0));
        points.add(new Point2D(170.0, -24.27));
        points.add(new Point2D(180.0, -23.18));
        points.add(new Point2D(190.0, -24.27));
        points.add(new Point2D(200.0, -23.0));
        points.add(new Point2D(210.0, -20.364));
        points.add(new Point2D(220.0, -18.64));
        points.add(new Point2D(230.0, -17.36));
        points.add(new Point2D(240.0, -15.55));
        points.add(new Point2D(250.0, -13.73));
        points.add(new Point2D(260.0, -11.36));
        points.add(new Point2D(270.0, -9.36));
        points.add(new Point2D(280.0, -7.18));
        points.add(new Point2D(290.0, -5.27));
        points.add(new Point2D(300.0, -3.82));
        points.add(new Point2D(310.0, -2.73));
        points.add(new Point2D(320.0, -1.37));
        points.add(new Point2D(330.0, -0.364));
        points.add(new Point2D(340.0, -0.182));
        points.add(new Point2D(350.0, 0.0));
        points.add(new Point2D(360.0, 0.0));
        DiscreteFunction function = new DiscreteFunction(points);

        DiscreteFunction verticalFun = new DiscreteFunction(Arrays.asList(new Point2D[]{new Point2D(-90, 0), new Point2D(90, 0)}));

        HorizontalVerticalInput prototype = Factory.prototype(HorizontalVerticalInput.class);
        Factory.when(prototype.horizontal()).thenReturn(new OptionalFunction(true, function));
        Factory.when(prototype.vertical()).thenReturn(new OptionalFunction(false, verticalFun));

        AntennaGainConfiguration<HorizontalVerticalInput> defaultAG = (AntennaGainConfiguration<HorizontalVerticalInput>) Factory.antennaGainFactory()
                .getHorizontalVerticalAntenna(Factory.build(prototype), 15.0);
        defaultAG.setDescription(new DescriptionImpl("Default Tri-Sector Antenna", "3GPP 3-sector"));

        return defaultAG;
    }

    public int countActiveUsers() {
        return activeUsers.size();
    }

    abstract protected void generateAndPositionMobiles();

    protected abstract AbstractDmaBaseStation generateBaseStation(Point2D position, int cellid, double antennaHeight, double antennaTilt,
                                                              int sectorid, boolean triSector);

    protected abstract AbstractDmaBaseStation[][] generateBaseStationArray();

    private double[][] initialVictimCapacityActiveAndInactiveUsersWorstCell;

    public int cellsPerSite() {
        return systemSettings.getLayout().getSectorSetup() == CellularLayout.SectorSetup.SingleSector ? 1 : 3;
    }

    public void setInitialVictimCapacityActiveAndInactiveUsersWorstCell(double[][] initial) {
        initialVictimCapacityActiveAndInactiveUsersWorstCell = initial;
    }

    public double[][] getInitialVictimCapacityActiveAndInactiveUsersWorstCell() {
        return initialVictimCapacityActiveAndInactiveUsersWorstCell;
    }

    /**
     * Initializes AbstractDmaBaseStation array with new CDMACells all located in ring = ring_id and position = (x,y)
     */
    protected void generateCells(AbstractDmaBaseStation[] _cells, double x, double y, int _cellid, boolean triSector) {
        int cellid = 0;
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

                BaseStation bs = systemSettings.getLink().getBaseStation();
                _cells[i] = generateBaseStation(new Point2D(x,y), cellid, bs.getHeight().trial(), bs.getTilt().trial(),(i + 1), triSector);
                _cells[i].setUpLinkMode( isUplink() );

                _cells[i].setCellLocationId(_cellid);
                configureBaseStation(_cells[i]);
            }
        }
    }

    public int getNumberOfBaseStations() {
        return (systemSettings.getLayout().getSectorSetup() == CellularLayout.SectorSetup.SingleSector ? 1 : 3) *
                getNumberOfCellSitesInPowerControlCluster();
    }

    protected GenericMobileType generateInitializedMobile() {
        GenericMobileType user = generateUserTerminal();

        positionUser(user);
        initializeUser(user);

        try {
            performSystemSpecificInitialization(user);
        } catch (UserShouldBeIgnoredException ex) {
            return null;
        }
        return user;
    }

    /**
     * Populates system with CDMACells using current settings. Generated cells will be stored in cells.
     */
    public void generateSystemCells() {
        cells = generateBaseStationArray();
        boolean triSectorCells = systemSettings.getLayout().getSectorSetup() != CellularLayout.SectorSetup.SingleSector;

        generateCells(cells[0], locationX, locationY, 0, triSectorCells);
        for (int i = 0; i < cells.length; i++) {
            generateCells(cells[i], 0, 0, i, triSectorCells);
        }
        repositionSystem( getLocation() );
        setReferenceCell( cells[systemSettings.getLayout().getIndexOfReferenceCell()][systemSettings.getLayout().getReferenceSector()] );
    }

    /**
     * Generates new User Terminal. User is positioned at system center.
     */
    public abstract GenericMobileType generateUserTerminal();

    public List<GenericMobileType> getActiveUsers() {
        return activeUsers;
    }

    public List<AbstractDmaBaseStation> getAllBaseStations() {
        List<AbstractDmaBaseStation> list = new ArrayList<AbstractDmaBaseStation>();
        if (cells != null) {
            for (AbstractDmaBaseStation[] bases : cells) {
                list.addAll(Arrays.asList(bases));
            }
        }
        return list;
    }

    public List<GenericMobileType> getAllActiveUsers() {
        List<GenericMobileType> list = new ArrayList<GenericMobileType>();
        list.addAll(getActiveUsers());
        return list;
    }

    public AbstractDmaBaseStation[][] getBaseStationArray() {
        return cells;
    }

    public double getCellRadius() {
        return systemSettings.getLayout().getCellRadius();
    }

    public int getCellStructure() {
        return 6;
    }

    public List<ActiveInterferer> getExternalInterferers() {
        if ( victimSimulation == null ) {
            return Collections.EMPTY_LIST;
        }
        return victimSimulation.getExternalInterferers(eventResult);
    }

    public double getFrequency() {
        return frequency;
    }

    /**
     * compute the inter cell distance
     *<p></p>
     * <ol>
     *     <ul>3 sector: interCellDistance = CellRadius() * SQRT3</ul>
     * <ul>omni: interCellDistance = CellRadius() * 3</ul>
     * </ol>
     */
    public final double getInterCellDistance() {
        double interCellDistance = 0.0;

        if (systemSettings.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP ){
            interCellDistance = systemSettings.getLayout().getCellRadius() * SQRT3;
        }else{
            interCellDistance = systemSettings.getLayout().getCellRadius() * 3;
        }
        return interCellDistance;
    }

    public double getLocationX() {
        return locationX;
    }

    public double getLocationY() {
        return locationY;
    }

    /**
     * Method that returns the number of base station (i.e. sites, masts) depending on the input setting
     * <ol>
     *     <ul>single cell: 1</ul>
     *     <ul>one tier: 7</ul>
     *     <ul>default - full set-up: 19</ul>
     * </ol>
     *
     *  @return the number of base station (1, 7, 19)
     */
    public int getNumberOfCellSitesInPowerControlCluster() {
        switch ( systemSettings.getLayout().getTierSetup() ) {
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
        switch (systemSettings.getLayout().getTierSetup()) {
            case SingleCell: return 0;
            case OneTier: return 1;
            default: return 2;
        }
    }

    public double getProcessingGain() {
        return processingGain;
    }

    public AbstractDmaBaseStation getReferenceCell() {
        if (referenceCell == null) {
            setReferenceCell( cells[0][systemSettings.getLayout().getReferenceSector()] );
        }
        return referenceCell;
    }

    @SuppressWarnings("unchecked")
    public void setReferenceCell( AbstractDmaBaseStation referenceCell ) {
        this.referenceCell = referenceCell;
    }

    public double getReferenceCellMeasurement() {
        return referenceCell.getOutagePercentage();
    }

    /**
     * calculate the outage for the system
     *<p></p>
     * <code>outage = droppedInSystem / (connectedInSystem + droppedInSystem) * 100</code>
     *
     * @return the outage for the system
     */
    public double getSystemMeasurement() {
        int connectedInSystem = 0;
        int droppedInSystem = 0;
        double outage = 0.0;

        for (AbstractDmaBaseStation[] cell : cells) {
            for (AbstractDmaBaseStation aCell : cell) {
                if (aCell != null) {
                    connectedInSystem += aCell.countActiveUsers();
                    droppedInSystem += aCell.countDroppedUsers();
                }
            }
        }

        if ((connectedInSystem + droppedInSystem) == 0.0 || droppedInSystem == 0.0){
            outage = droppedInSystem;
        }else{
            outage = droppedInSystem / (connectedInSystem + droppedInSystem) * 100;
        }

        return outage;
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
    protected void initializeUser(GenericMobileType user) {
        // Assign speed and activity
        if ( getSystemSettings().getLink().getMobileStation().getMobility() != null ) {
            user.setMobilitySpeed(getSystemSettings().getLink().getMobileStation().getMobility().trial());
        }
        // Generate links to all Cells in system

        user.generateLinksToBaseStations();

        user.sortLinks();
        // Based on determined path loss - define active list
        user.selectActiveList( getSystemSettings().getHandoverMargin());
    }

    public boolean isUplink() {
        return this instanceof CDMAUplinkSystem || this instanceof UplinkOfdmaSystem;
    }

    public boolean isVictimSystem() {
        return victimSystem;
    }

    /**
     * presimulation task method
     * <ol>
     *    <li>create sector antenna</li>
     *    <li>generate the cells of the system</li>
     *    <li>calculate the thermal noise</li>
     * </ol>
     */
    public void performPreSimulationTasks(double frequency) {
        setFrequency( frequency );
        AntennaGainConfiguration[] antennas = CellularCalculations.createSectorAntennas(systemSettings);
        getResults().setAntennaGainForSectors(antennas);
        generateSystemCells();
        calculateThermalNoise();
    }

    protected abstract void performSystemSpecificInitialization(GenericMobileType user) throws UserShouldBeIgnoredException;

    public void positionSystem() {
        translateCellCoordinates(getLocationX(), getLocationY());
    }

    /** position the user into the cellular system
     * <ol>
     *    <li>calculate the location</li>
     *    <li>generate the user</li>
     *    <li>Recursive call if user is outside system</li>
     * </ol>
     * @param user
     */
    protected void positionUser(GenericMobileType user) {
        // Calculate location
        double userAng = userAngle.trial();
        double userDist = 0.0;
        if(systemSettings.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP){
            userDist = (getNumberOfTiers() + 0.5) * getInterCellDistance() * userLocation.trial();
        }else{
            userDist = (getNumberOfTiers() + 1) * getInterCellDistance() * userLocation.trial();
        }

        double x = userDist * Mathematics.cosD(userAng);
        double y = userDist * Mathematics.sinD(userAng);

        // Generate user
        user.setPosition(new Point2D(locationX + x, locationY + y));

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

        for (AbstractDmaBaseStation[] cell : cells) {
            Point2D position = user.getPosition();
            if(systemSettings.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP){
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

    /**
     * Moves entire system to new position specified by center (x,y)
     */
    public void repositionSystem(Point2D p) {
        double d = getInterCellDistance();

        if (referenceCell == null) {
            setReferenceCell( cells[0][0] );
        }

        if ( systemSettings.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP ) {
            setLocation( GridPositionCalculator.ppg2(true, referenceCell.getCellLocationId(), p, d));
        } else {
            setLocation( GridPositionCalculator.standard(true, referenceCell.getCellLocationId(), p, d) );
        }

        for ( int j=0; j<cells.length; j++) {
            for (int i = 0; i < cells[0].length; i++) {
                if ( systemSettings.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP ) {
                    setPosition(cells[j][i], GridPositionCalculator.ppg2(false, j, getLocation(), d));
                } else {
                    setPosition(cells[j][i], GridPositionCalculator.standard(false, j, getLocation(), d));
                }
            }
        }
    }

    private void setPosition(AbstractDmaBaseStation bs, Point2D p) {
        bs.setPosition(p);
        bs.calculateHexagon(getCellRadius());
    }

    public void resetSystem() {
        activeUsers.clear();
        selectedCellActiveUsers.clear();
        selectedCell.clear();
        noLinkLevelFoundUsers.clear();

        useridcount = 0;
        for (AbstractDmaBaseStation base : getAllBaseStations()) {
            base.resetBaseStation();
            base.setUpLinkMode(isUplink());
        }

        externalInterferenceActive = false;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public Point2D getLocation() {
        return new Point2D(locationX, locationY);
    }

    public void setLocation(Point2D p ){
        locationX = p.getX();
        locationY = p.getY();
    }

    public void setLocation(double x, double y) {
        setLocationX(x);
        setLocationY(y);
    }

    public void setLocationX(double locationX) {
        this.locationX = locationX;
    }

    public void setLocationY(double locationY) {
        this.locationY = locationY;
    }

    public void setVictimSystem(boolean victimSystem) {
        this.victimSystem = victimSystem;
    }

    public abstract void simulate();

    /**
     * Translates location of entire CDMA system to new center coordinate specified by (x,y)
     *
     * @param x double x of new system center
     * @param y double y of new system center
     */
    public void translateCellCoordinates(double x, double y) {
        double diffX = (int) x - (int) locationX;
        double diffY = (int) y - (int) locationY;
        for (AbstractDmaBaseStation[] cell : cells) {
            for (AbstractDmaBaseStation aCell : cell) {
                aCell.translateLocation(new Point2D(diffX,diffY));
            }
        }
        for (GenericMobileType activeUser : activeUsers) {
            activeUser.translate(new Point2D(diffX, diffY));
        }
        locationX = cells[0][0].getPosition().getX();
        locationY = cells[0][0].getPosition().getY();
    }

    /**
     * get the number of served mobiles in the reference cell
     * @return referenceCell.countServedUsers()
     */
    public int getNumberOfServedMobilesInReferenceCell() {
        return referenceCell.countServedUsers();
    }


    /**
     * get the number of active served mobiles in the reference cell
     * @return referenceCell.countActiveUsers()
     */
    public int getNumberOfActiveServedMobilesInReferenceCell() {
        return referenceCell.countActiveUsers();
    }

    public boolean getWorstCell(int j, int k) {
        return cells[j][k].isWorstCell();
    }

    public void resetWorstCell(int j, int k) {
        cells[j][k].setWorstCell(false);
    }


    /**
     * get the number of served mobiles in a specific cell
     * @return cells[CellID][triSectorCellSelection].countServedUsers()
     */
    public int getNumberOfServedMobilesInCell(int CellID, int triSectorCellSelection) {
        return cells[CellID][triSectorCellSelection].countServedUsers();
    }

    public int getNumberOfServedMobilesInWorstCell() {
        int result = 0;
        boolean isNoWorthCell = false;
        for (AbstractDmaBaseStation[] cell : cells) {
            for (AbstractDmaBaseStation aCell : cell) {
                if (aCell.isWorstCell()) {
                    result = aCell.countServedUsers();
                    isNoWorthCell = true;
                }
            }
        }

        if(!isNoWorthCell){ // in this case there is no affected cell, hence no loss so the number of Served mobile should be the same as before interference introduced.
            result = -1;
        }
        return result;
    }

    /**
     * get the number of served mobiles in the all network (i.e. system)
     *<p></p>
     * loop and add overall all the cells the countServedUsers
     *
     * @return the number of served mobiles in the all network (i.e. system)
     */

    public int getNumberOfServedMobilesInSystem() {
        int result = 0;
        for (AbstractDmaBaseStation[] cell : cells) {
            for (AbstractDmaBaseStation aCell : cell) {
                if (aCell != null) {
                    result += aCell.countServedUsers();
                }
            }
        }
        return result;
    }

    /**
     * get the number of active served mobiles in the all network (i.e. system)
     *<p></p>
     * loop and add overall all the cells the countActiveUsers
     *
     * @return the number of active served mobiles in the all network (i.e. system)
     */
    public int getNumberOfActiveServedMobilesInSystem() {
        int result = 0;
        for (AbstractDmaBaseStation[] cell : cells) {
            for (AbstractDmaBaseStation aCell : cell) {
                if (aCell != null) {
                    result += aCell.countActiveUsers();
                }
            }
        }
        return result;
    }

    public boolean isDownlink() {
        return !isUplink();
    }
}
