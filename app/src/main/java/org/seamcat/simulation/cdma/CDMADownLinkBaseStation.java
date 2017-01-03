package org.seamcat.simulation.cdma;

import org.seamcat.cdma.exceptions.ScalingException;
import org.seamcat.model.cellular.cdma.CDMADownLink;
import org.seamcat.model.core.GridPositionCalculator;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.simulation.result.MutableAntennaResult;
import org.seamcat.simulation.result.MutableLinkResult;

import java.util.ArrayList;
import java.util.List;

import static org.seamcat.model.mathematics.Mathematics.fromWatt2dBm;
import static org.seamcat.model.mathematics.Mathematics.fromdBm2Watt;

public class CDMADownLinkBaseStation {

    private CDMADownLinkSimulation simulation;
    private final Point2D location;
    private final int cellid;
    private final double height;
    private final double tilt;
    private final int sector;
    private final boolean triSector;
    private int cellLocationId;
    private double pilotTransmitPower;
    private double overheadTransmitPower;
    private double currentChannelTransmitPower;
    private Point2D position;

    private int inactiveSize, activeSize;
    private double cacheValue;

    private List<CDMADownLinkLink> activeConnections = new ArrayList<>();
    private List<CDMADownLinkLink> droppedUsers = new ArrayList<>();
    private List<CDMADownLinkLink> inActiveConnections = new ArrayList<>();

    private final int HEXAGONAL = 6;
    private Point2D[] geometry = new Point2D[ HEXAGONAL ];
    private double interSystemInterference;

    public CDMADownLinkBaseStation(CDMADownLinkSimulation simulation, Point2D location, int cellid, double height, double tilt, int sector, boolean triSector) {
        this.simulation = simulation;
        this.location = location;
        this.cellid = cellid;
        this.height = height;
        this.tilt = tilt;
        this.sector = sector;
        this.triSector = triSector;
    }

    /**
     * calculate the summation over the active connection the transmitt traffic channel power in Watt as part of the <br>
     *     soft handover process
     *
     * @return the current channel transmit power
     */
    public double calculateCurrentChannelPower_dBm() {
        if ( activeSize < activeConnections.size() ) {
            for ( int i=activeSize; i<activeConnections.size(); i++ ) {
                cacheValue += fromdBm2Watt(activeConnections.get(i).transmittedTrafficChannelPowerdBm);
                activeSize++;
            }
        }
        currentChannelTransmitPower = fromWatt2dBm(cacheValue);
        return currentChannelTransmitPower;
    }


    /**
     * compute the denominator part of the scaling ratio used in downlink (power control in soft handover)
     * <p></p>
     * <code>P_calculate = calculateCurrentChannelPower_dBm()</code>
     * <p></p>
     * <code>pilot_fraction = getPilotTransmitPower()</code>
     * <p></p>
     * <code>overhead_fraction = getOverheadTransmitPower()</code>
     *
     * @return denominator part of the scaling ratio used in downlink
     */
    public double getCurrentTransmitPower_dBm() {
        return Mathematics.powerSummation(calculateCurrentChannelPower_dBm(), overheadTransmitPower, pilotTransmitPower );
    }


    public void setCellLocationId(int cellLocationId) {
        this.cellLocationId = cellLocationId;
    }

    /**
     * Initialise the transmit power for CDMA downlink
     */
    public void initializeTransmitPowerLevels(CDMADownLink settings) {
        double maxPowerInWatts = fromdBm2Watt(settings.getMaximumBroadcastChannel());
        // Convert absolute watt values to dBm
        pilotTransmitPower = ( fromWatt2dBm( maxPowerInWatts * settings.getPilotChannelFraction()) );
        overheadTransmitPower = ( fromWatt2dBm( maxPowerInWatts * settings.getOverheadChannelFraction()) );
        currentChannelTransmitPower = ( fromWatt2dBm(0) );
    }

    public int getCellLocationId() {
        return cellLocationId;
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public Point2D getPosition() {
        return position;
    }

    public void calculateHexagon(double cellRadius) {
        GridPositionCalculator.calculateHexagon( getPosition(), cellRadius, geometry );
    }

    public boolean isInside(Point2D p, double shiftX, double shiftY){
        return GridPositionCalculator.isInside( p, shiftX, shiftY, geometry);
    }

    public double getHeight() {
        return height;
    }

    public double calculateAntennaGain(double azimuth, double elevation ) {
        double horizontalAngle = azimuth;
        if (horizontalAngle > 180) {
            horizontalAngle -= 360;
        }
        elevation = elevation - tilt;
        MutableAntennaResult direction = new MutableAntennaResult();
        direction.setAzimuth( horizontalAngle );
        direction.setElevation( elevation );
        MutableLinkResult result = new MutableLinkResult();
        result.setFrequency( simulation.getFrequency() );
        return simulation.getSystem().getTransmitter().getAntennaGain().evaluate(result, direction);
    }

    public void resetSummedInterference() {
        activeSize = 0;
        inactiveSize = 0;
        cacheValue = 0;
    }

    public void connect( CDMADownLinkLink link ) {
        activeConnections.add( link );
        inActiveConnections.remove( link );
    }

    public void inActiveConnect(CDMADownLinkLink link) {
        inActiveConnections.add( link );
    }

    public void scaleChannelPower() throws ScalingException {
        scaleChannelPower(calculateScaling());
    }

    /**
     * Method that calculates scaling factor that should be applied to the traffic channel power levels
     * <p></p>
     * <code>scaling = MaximumChannelPower_Watt/CurrentChannelTransmitPowerInWatts</code>
     * @return ratio
     */
    private double calculateScaling() {
        double pMax = getMaximumChannelPower_Watt();
        double pCalc = fromdBm2Watt( currentChannelTransmitPower );

        if (pCalc > pMax) {
            // downlink scaling factor (Lucent EQ(7)):
            return pMax / pCalc;
        }
        return 1;
    }

    /**
     * nominator part of the calculation for the scaling power ratio in downlink
     * <p></p>
     * <code>Maximum channel power = MaximumTransmitPower * (1 - (OverheadFraction + PilotFraction))</code>
     */
    public double getMaximumChannelPower_Watt() {
        CDMADownLink downLink = simulation.getSystem().getCDMASettings().getDownLinkSettings();
        return fromdBm2Watt(downLink.getMaximumBroadcastChannel())
                * (1 - (downLink.getOverheadChannelFraction() +
                downLink.getPilotChannelFraction()));
    }


    /**
     * CDMA downlink power scaling
     */
    private void scaleChannelPower(double scaleFactor) throws ScalingException {
        if (scaleFactor == 1) {
            return;
        } else if (scaleFactor > 1) {
            throw new ScalingException(
                    "CDMA Power levels cannot be scaled up. [Scale value was: "
                            + scaleFactor + "]");
        }
        cacheValue = 0;
        activeSize = 0;
        for (CDMADownLinkLink activeConnection : activeConnections) {
            scaleTransmitPower(activeConnection, scaleFactor);
        }

        calculateCurrentChannelPower_dBm();

        for (int i = 0; i < activeConnections.size(); i++) {
            CDMADownLinkLink link = activeConnections.get(i);
            CDMADownLinkMobileStation u = link.ms;
            try {
                u.calculateReceivedPower();
                u.calculateAchievedEcIor();

                if (!u.meetsEcIorRequirement(simulation.getSystem().getCDMASettings().getCallDropThreshold())) {
                    simulation.dropActiveUser(u, "Ec/Ior requirement does not meet while scaling the channel power");
                    i--;
                }

            } catch (Exception ex) {
                throw new ScalingException(ex);
            }
        }
    }

    /**
     * Downlink power balance scaling
     * <p></p>
     * it calculates:
     * <p></p>
     * <code>transmittedTrafficChannelPowerdB = transmittedTrafficChannelPowerdBm * scaleValue</code>
     * <p></p>
     * and update received power values to correspond to scaled transmit power
     * <p></p>
     * <code>receivedTrafficChannelPowerdBm = transmittedTrafficChannelPowerdBm - getEffectivePathloss</code>
     */
    private void scaleTransmitPower(CDMADownLinkLink link, double scaleValue) {
        double transmittedTrafficChannelPowerdBm = fromWatt2dBm(fromdBm2Watt(link.transmittedTrafficChannelPowerdBm) * scaleValue);
        double receivedTrafficChannelPowerdBm = transmittedTrafficChannelPowerdBm - link.effectiveLoss;
        link.transmittedTrafficChannelPowerdBm = transmittedTrafficChannelPowerdBm;
        link.receivedTrafficChannelPowerdBm = receivedTrafficChannelPowerdBm;
    }

    public void disconnectUser(CDMADownLinkLink linkToMobile) {
        if (activeConnections.contains(linkToMobile)) {
            removeActive(linkToMobile);
        } else {
            removeInActive(linkToMobile);
        }
    }

    public void removeActive( CDMADownLinkLink linkToMobile ) {
        droppedUsers.add(linkToMobile);
        activeConnections.remove(linkToMobile);
        if ( activeSize > 0 ) {
            cacheValue -= Mathematics.fromdBm2Watt(linkToMobile.transmittedTrafficChannelPowerdBm);
            activeSize--;

        }

        interSystemInterference = cacheValue;
    }

    public void removeInActive( CDMADownLinkLink linkToMobile ) {
        inActiveConnections.remove(linkToMobile);
    }


    public int countActiveUsers() {
        int capacity = 0;
        for (CDMADownLinkLink link : activeConnections) {
            if (simulation.mobileStationActive( link, this, 1 )){
                capacity++;
            }
        }
        return capacity;
    }

    public int countDroppedUsers() {
        int capacity = 0;
        for (CDMADownLinkLink link : droppedUsers) {
            if (simulation.mobileStationActive(link, this, 1)) {
                capacity++;
            }
        }
        return capacity;
    }

    /*public int countInActiveUsers() {
        int capacity = 0;
        for (MutableLinkResult link : inactiveUsers) {
            if (system.mobileStationActive(link, this, 0)) {
                capacity++;
            }
        }
        return capacity;
    }*/

    public int countServedUsers() {
        return countActiveUsers() + 0 /*countInActiveUsers()*/;
    }

    public void reset() {
        activeConnections.clear();
        inActiveConnections.clear();
        droppedUsers.clear();
        //inactiveUsers.clear();
        resetSummedInterference();
        currentChannelTransmitPower = Mathematics.fromWatt2dBm(0);

        //setExternalInterferenceUnwanted(-1000);
        //setExternalInterferenceBlocking(-1000);
    }
}
