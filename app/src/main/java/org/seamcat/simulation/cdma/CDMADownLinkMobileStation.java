package org.seamcat.simulation.cdma;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.cdma.CDMALinkLevelDataPoint;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.simulation.LocalEnvironmentSelector;
import org.seamcat.simulation.cellular.CellularCalculations;
import org.seamcat.simulation.result.MutableAntennaResult;
import org.seamcat.simulation.result.MutableLinkResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.seamcat.model.mathematics.Mathematics.fromWatt2dBm;
import static org.seamcat.model.mathematics.Mathematics.fromdBm2Watt;

public class CDMADownLinkMobileStation {


    private Point2D location;
    private final int id;
    private final double gain;
    private final double height;
    private String dropReason;
    private double thermalNoise;
    private Point2D position;
    private double mobilitySpeed;
    private CDMADownLinkLink[] links;
    private double achievedCI;
    private double achievedEcIor;
    private double requiredEcIor;
    private int linkQualityExceptions = 0;
    private boolean connected;
    private boolean dropped;

    private List<CDMADownLinkLink> activeList;
    private CDMADownLinkLink servingLink;

    private boolean isInSoftHandover;
    private double totalPowerReceivedFromBaseStationsInActiveSet;
    private double totalPowerReceivedFromBaseStationsNotInActiveSet;
    private double externalInterferenceBlocking = -1000;
    private double externalInterferenceUnwanted = -1000;
    private double geometry;
    private CDMALinkLevelDataPoint linkLevelData;
    private boolean linkLevelDataPointFound;
    private double receivedTrafficChannelPowerWatt;
    private int powerScaledUpCount;
    private CDMADownLinkSimulation simulation;


    public CDMADownLinkMobileStation(CDMADownLinkSimulation simulation, Point2D location, int id, double gain, double height) {
        this.simulation = simulation;
        this.location = location;
        this.id = id;
        this.gain = gain;
        this.height = height;
        activeList = new ArrayList<>(2);
    }

    public void setDropReason(String dropReason) {
        this.dropReason = dropReason;
    }

    public String getDropReason() {
        return dropReason;
    }

    public void setThermalNoise(double thermalNoise) {
        this.thermalNoise = thermalNoise;
    }

    public double getThermalNoise() {
        return thermalNoise;
    }

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public Point2D getPosition() {
        return position;
    }

    public void setMobilitySpeed(double mobilitySpeed) {
        this.mobilitySpeed = mobilitySpeed;
    }

    public double getMobilitySpeed() {
        return mobilitySpeed;
    }

    public void generateSortedBSLinks(CDMADownLinkBaseStation[][] cells, int numberOfBaseStations) {
        links = new CDMADownLinkLink[numberOfBaseStations];

        int linkid = 0;
        for (int i = 0; i < cells.length; i++) {

            double pathloss = 0;
            for (int j = 0; j < cells[i].length; j++, linkid++) {
                CDMADownLinkLink link = createLink(cells[i][j]);
                links[linkid] = link;
                //links[linkid] = new CDMADownlink((CdmaBaseStation) cells[i][j], this, (CDMASystem) getSystem());
                // Cache site pathloss in three sector case:
                if (j == 0) {
                    PropagationModel pm = simulation.getSystem().getLink().getPropagationModel();
                    MutableLinkResult ml = createLink(link);
                    setPathLosses( link, pm.evaluate(ml));
                    //links[linkid].determinePathLoss(getSystem().getSystemSettings().getLink().getPropagationModel());
                    //pathloss = links[linkid].getTxRxPathLoss();
                    pathloss = link.loss;
                } else {
                    setPathLosses( link, pathloss );
                    //links[linkid].setTxRxPathLoss(pathloss);
                }
            }
        }

        Arrays.sort( links, CDMADownLinkLink.LinkPathLossComparator);
    }

    private MutableLinkResult createLink( CDMADownLinkLink link ) {
        CellularSystem sys = simulation.getSystem();
        MutableLinkResult ml = new MutableLinkResult();
        ml.setFrequency( simulation.getFrequency() );
        ml.setTxRxDistance( link.distance );
        MutableAntennaResult tx = ml.txAntenna();
        tx.setPosition( link.txPosition );
        tx.setGain( link.txGain );
        tx.setHeight( link.bs.getHeight() );
        tx.setAzimuth( link.txAzimuth );
        tx.setElevation( link.txElevation );
        tx.setLocalEnvironment(LocalEnvironmentSelector.pickLocalEnvironment( sys.getTransmitter().getLocalEnvironments() ));
        MutableAntennaResult rx = ml.rxAntenna();
        rx.setPosition( position );
        rx.setGain( gain );
        rx.setHeight( height );
        rx.setLocalEnvironment(LocalEnvironmentSelector.pickLocalEnvironment( sys.getReceiver().getLocalEnvironments()));
        ml.trialTxRxInSameBuilding();


        return ml;
    }

    private void setPathLosses( CDMADownLinkLink link, double loss ) {
        link.loss = loss;
        //link.realEffectiveLoss = loss - (link.txGain + gain);
        link.effectiveLoss = Math.max(loss - (link.txGain + gain), simulation.getSystem().getMinimumCouplingLoss());
    }

    private CDMADownLinkLink createLink(CDMADownLinkBaseStation bs) {
        CDMADownLinkLink link = simulation.createLink(bs, this);
        CellularSystem sys = simulation.getSystem();
        Point2D baseStationNewCoordinate = CellularCalculations.findNewCoordinate(position, bs.getPosition(),
                CellularCalculations.getInterCellDistance(sys),
                sys.getLayout().getSystemLayout(),
                sys.getLayout().generateWrapAround(),
                sys.getLayout().getSectorSetup());

        link.distance = Mathematics.distance(position, baseStationNewCoordinate);
        //link.txAntenna().setHeight( bs.getHeight() );
        //link.rxAntenna().setHeight(height);

        // Calculate Angle based on virtual wrap around position
        link.txAzimuth = Mathematics.calculateKartesianAngle(position, baseStationNewCoordinate);
        // Calculate Elevation based on virtual wrap around position
        link.txElevation = Mathematics.calculateElevation(position, height, baseStationNewCoordinate, bs.getHeight());
        link.txGain = bs.calculateAntennaGain( link.txAzimuth, link.txElevation );
        link.txPosition = baseStationNewCoordinate;
        //link.txAntenna().setLocalEnvironment(LocalEnvironmentSelector.pickLocalEnvironment( sys.getTransmitter().getLocalEnvironments() ));

        //link.rxAntenna().setPosition( position );
        link.rxGain = gain;
        //link.rxAntenna().setGain( gain );
        //link.rxAntenna().setLocalEnvironment(LocalEnvironmentSelector.pickLocalEnvironment( sys.getReceiver().getLocalEnvironments()));
        //link.trialTxRxInSameBuilding();

        return link;
    }

    public void selectActiveList(double handoverMargin) {
        activeList.add( links[0]);
        if (links.length > 1){
            if (Math.abs((links[0].loss - links[0].txGain - links[0].rxGain)
                    - (links[1].loss - links[1].txGain - links[1].rxGain)) < handoverMargin) {
                activeList.add(links[1]);
                isInSoftHandover = true;
            }
        }else{
            isInSoftHandover = false;
        }
    }

    public void calculateInitialReceivedPower(double maximumBroadcastChannel) {
        double activeSet = 0;
        double notInActive = 0;
        double initialTransmitPower_dbm = fromWatt2dBm(fromdBm2Watt(maximumBroadcastChannel) * 0.70);

        for (CDMADownLinkLink link : links) {
            if (activeList.contains(link)) {
                activeSet += fromdBm2Watt(initialTransmitPower_dbm - link.effectiveLoss);
            } else {
                notInActive +=
                        fromdBm2Watt(initialTransmitPower_dbm - link.effectiveLoss);
            }
        }
        totalPowerReceivedFromBaseStationsInActiveSet = activeSet;
        totalPowerReceivedFromBaseStationsNotInActiveSet = notInActive;
    }

    /**
     * <strong>Applicable for Downlink CDMA only!</strong><br>
     *     <p></p>
     * Method that calculate the Geometry for this user.
     *     <p></p>
     * Geometry is a key parameter that is used to express the condition of the user.
     * <p></p>
     * Geometry is defined as:<br>
     * G = P<sub>active</sub> / (N<sub>t</sub> + P<sub>other</sub> + I<sub>ext</sub>)<br>
     * Where:<br>
     * G = Geometry <br>
     * P<sub>active</sub> = Total power received from base stations in active
     * set<br>
     * N<sub>t</sub> = Thermal Noise<br>
     * P<sub>other</sub> = Total power received from base stations NOT in the
     * active set<br>
     * I<sub>ext</sub> = External interference (out of system)<br>
     * <br>
     * Note that the higher the geometry, the more favorable the user's position is. <br>
     * <br>
     * Geometry in a SEAMCAT&reg; perspective is defined and discussed in document <strong>STG(03)12</strong> submitted <br>
     *     by Lucent Technologies
     *
     * @return The calculated Geometry
     */
    public double calculateGeometry( double minGeometry, double maxGeometry ) {
        double extInt = fromdBm2Watt(getExternalInterference());

        double interference = totalPowerReceivedFromBaseStationsNotInActiveSet + extInt;
        double absGeometry;
        // Lucent EQ2:
        if (isInSoftHandover && getMobilitySpeed() == 0) {

            double c1 = fromdBm2Watt(activeList.get(0).totalReceivedPowerdBm);
                    //activeList.get(0).getValue(TOTAL_RECEIVED_POWER_DBM));// Power received from 1st active link
            double c2 = fromdBm2Watt(activeList.get(1).totalReceivedPowerdBm);
                    //activeList.get(1).getValue(TOTAL_RECEIVED_POWER_DBM));// Power received from 2nd active link

            absGeometry = c1 / (c2 + getThermalNoise() + interference) + c2
                    / (c1 + getThermalNoise() + interference);
            geometry = Mathematics.linear2dB(absGeometry);

        } else {
            absGeometry = totalPowerReceivedFromBaseStationsInActiveSet
                    / (getThermalNoise() + interference);
            geometry = Mathematics.linear2dB(absGeometry);
        }

        // the value prevent generating unrealistic Ec/Ior
        geometry = Math.max( geometry, minGeometry );
        geometry = Math.min( geometry, maxGeometry );

        return geometry;
    }

    public double getExternalInterference() {
        return Mathematics.linear2dB(Math.pow(10, externalInterferenceUnwanted / 10) + Math.pow(10, externalInterferenceBlocking / 10));
    }

    public CDMALinkLevelDataPoint findLinkLevelDataPoint(CDMALinkLevelData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Supplied CDMALinkLevelData is null");
        }
        try {
            this.linkLevelData = data.getLinkLevelDataPoint(new CDMALinkLevelDataPoint(
                    simulation.getFrequency(), activeList.size(),
                    geometry, getMobilitySpeed(), 0));

            setLinkLevelDataPointFound(true);
        } catch (IllegalStateException ex) {
            setLinkLevelDataPointFound(false);
        }
        if (linkLevelData == null) {
            setLinkLevelDataPointFound(false);
        }
        return linkLevelData;
    }

    public void setLinkLevelDataPointFound(boolean linkLevelDataPointFound) {
        this.linkLevelDataPointFound = linkLevelDataPointFound;
    }

    public boolean isLinkLevelDataPointFound() {
        return linkLevelDataPointFound;
    }

    /**
     * Downlink
     *
     * Method that calculate the received traffic channel power based on the EC/Ior
     */
    public double calculateReceivedTrafficChannelPowerInWatt() {
        double EcIor = Mathematics.dB2Linear(linkLevelData.getEcIor());
        // Lucent EQ(4)
        receivedTrafficChannelPowerWatt = totalPowerReceivedFromBaseStationsInActiveSet * EcIor;

        double receivedPerLink = receivedTrafficChannelPowerWatt;
        if (isInSoftHandover) {
            receivedPerLink /= 2;
        }
        resetSummationAffectedBaseStations();
        for (CDMADownLinkLink link : activeList) {
            link.receivedTrafficChannelPowerdBm = fromWatt2dBm(receivedPerLink);
            calculateTransmittedTrafficChannelPowerIndBm(link);
        }
        // Call get method to update power received from each link
        return calculateReceivedTrafficChannelPowerWatt();
    }

    public double calculateReceivedTrafficChannelPowerWatt() {
        double received = 0;
        for (CDMADownLinkLink link : activeList) {
            received += fromdBm2Watt(link.receivedTrafficChannelPowerdBm);
        }
        receivedTrafficChannelPowerWatt = received;
        return received;
    }

    /**
     * performs a sort of power control if the calculated transmitted traffic channel power is higher than the max <br>
     *     traffic channel power
     *
     */
    private double calculateTransmittedTrafficChannelPowerIndBm(CDMADownLinkLink link ) {

        double receivedTrafficChannelPowerdBm = link.receivedTrafficChannelPowerdBm;
        double transmittedTrafficChannelPowerdBm = link.effectiveLoss + receivedTrafficChannelPowerdBm;

        double pMax = simulation.getMaxTrafficChannelPowerIndBm();
        if (transmittedTrafficChannelPowerdBm > pMax) {

            double difference = transmittedTrafficChannelPowerdBm - pMax;

            transmittedTrafficChannelPowerdBm = pMax;

            receivedTrafficChannelPowerdBm -= difference;
            link.receivedTrafficChannelPowerdBm = receivedTrafficChannelPowerdBm;

            link.powerScaledDownToMax = true;
        } else {
            link.powerScaledDownToMax = false;
        }

        link.transmittedTrafficChannelPowerdBm = transmittedTrafficChannelPowerdBm;
        return transmittedTrafficChannelPowerdBm;
    }

    private void resetSummationAffectedBaseStations() {
        if ( servingLink != null ) {
            servingLink.bs.resetSummedInterference();
        }

        for (CDMADownLinkLink link : activeList) {
            link.bs.resetSummedInterference();
        }
    }


    public boolean connect() {
        simulation.connect( links[0] );
        boolean powerScaledDown = links[0].powerScaledDownToMax;
        int i = 1;
        if (isInSoftHandover) {
            simulation.connect( links[1] );
            powerScaledDown = powerScaledDown || links[1].powerScaledDownToMax;
            i++;
        }
        for (int stop = links.length; i < stop; i++) {
            simulation.inActiveConnect( links[i]);
        }

        if (powerScaledDown) {
            connected = meetsEcIorRequirement(simulation.getSystem().getCDMASettings().getCallDropThreshold());
        } else {
            connected = true;
        }
        return connected;
    }

    public boolean meetsEcIorRequirement(double callDropThreshold) {
        return meetsEcIorRequirement(callDropThreshold, false);
    }

    /**
     * DownLink call quality test
     * using STG(03)13 Lucent EQ(6)
     */
    public boolean meetsEcIorRequirement(double callDropThreshold,
                                         boolean finalLoop) {
        // Lucent EQ(6)
        boolean value = achievedEcIor >= requiredEcIor - callDropThreshold;

        if (finalLoop || value) {
            linkQualityExceptions = 0;
        } else if (linkQualityExceptions < 5) {
            // Allow user a free pass for a specified number of power control loop
            // iterations
            linkQualityExceptions++;
            value = true;
        }

        return value;
    }

    public List<CDMADownLinkLink> getActiveList() {
        return activeList;
    }

    /**
     * Downlink scenario
     * <p></p>
     * Calculate total received power from Base stations in active list total received power from one BS is defined as <br>
     *     sum of pilot, overhead and all traffic channels
     */
    public void calculateReceivedPower() {
        double resultActive_W = 0;
        double resultInActive_W = 0;
        for (CDMADownLinkLink link : links) {
            CDMADownLinkBaseStation station = link.bs;
            double total = station.getCurrentTransmitPower_dBm() - link.effectiveLoss;
            link.totalReceivedPowerdBm = total;
            if (activeList.contains(link)) {
                resultActive_W += fromdBm2Watt(total);
            } else {
                resultInActive_W += fromdBm2Watt(total);
            }
        }

        totalPowerReceivedFromBaseStationsInActiveSet = resultActive_W;
        totalPowerReceivedFromBaseStationsNotInActiveSet = resultInActive_W;
    }

    /**
     * Downlink link quality parameters
     * <p></p>
     * set the achieved Ec/Ior as the <code>ReceivedTrafficChannelPower - TotalPowerReceivedFromBaseStationsActive</code>
     */
    public void calculateAchievedEcIor() {

        double pTraf_W = calculateReceivedTrafficChannelPowerWatt();
        double pTot_W = totalPowerReceivedFromBaseStationsInActiveSet;

        double pTraf = Mathematics.fromWatt2dBm(pTraf_W);
        double pTot = Mathematics.fromWatt2dBm(pTot_W);

        achievedEcIor = pTraf - pTot;
        requiredEcIor = linkLevelData.getEcIor();
    }

    public void dropCall() {
        for (CDMADownLinkLink link : links) {
            link.bs.disconnectUser(link);
        }
        connected = false;
        dropped = true;
    }

    public int getLinkQualityExceptions() {
        return linkQualityExceptions;
    }

    public void setLinkQualityExceptions(int linkQualityExceptions) {
        this.linkQualityExceptions = linkQualityExceptions;
    }

    public int getPowerScaledUpCount() {
        return powerScaledUpCount;
    }

    public void setPowerScaledUpCount(int powerScaledUpCount) {
        this.powerScaledUpCount = powerScaledUpCount;
    }


    @Override
    public String toString() {
        Point2D point = getPosition();
        return "MS #" + id + " at (" + Mathematics.round( point.getX())
                + ", " + Mathematics.round(point.getY()) + ")" + " [Geo = "
                + geometry + " dB]";
    }
}
