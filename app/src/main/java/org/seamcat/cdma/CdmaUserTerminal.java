package org.seamcat.cdma;

import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaMobile;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

public class CdmaUserTerminal extends AbstractDmaMobile {

    private CDMALinkLevelDataPoint linkLevelData = null;

    public CdmaUserTerminal(Point2D point, CDMASystem _system, int _userid, double antGain, double antHeight) {
        super(point, _system, _userid, antGain, antHeight);
    }

    public void resetSummationAffectedBaseStations() {
        if ( servingLink != null ) {
            CdmaBaseStation station = (CdmaBaseStation) servingLink.getBaseStation();
            station.resetSummedInterference();
        }

        for (AbstractDmaLink link : activeList) {
            CdmaBaseStation s = (CdmaBaseStation) link.getBaseStation();
            s.resetSummedInterference();
        }
    }

    /**
     * Uplink link quality calculation.
     * <p></p>
     * If user is in softhandover with two sectors of the same mast, selection combining is used. This method will <br>
     *     choose the link with the best CI level as the active link
     *     <ol>
     *         <li>Test for softer handover (Softhandover between two sectors of the same Tri-Sector BaseStation location</li>
     *         <ul>In softerhandover achievedCI is sum of achievedCI from all links in active set</ul>
     *         <li>Test if inactive link is experiencing a better CI level than the active</li>
     *         <ul>Change achievedCI value to higher value</ul>
     *         <ul>Move active connection to inactive list of BaseStation</ul>
     *         <ul>Change activeUplink</ul>
     *         <ul>Move inactive link from BaseStation inactive list to active list</ul>
     *     </ol>
     * @return Achieved CI level
     */
    public double calculateAchievedCI() {
        setOldAchievedCI( getAchievedCI() );

        CDMAUplink link = (CDMAUplink) servingLink;
        setAchievedCI( link.calculateAchivedCI() );

        if (isInSoftHandover()) {
            for (AbstractDmaLink l : activeList) {
                // If user is in softhandover with two sectors of the same mast, selection combining is used.
                if (l != link) {
                    double achievedCI_inactiveLink = ((CDMAUplink) l).calculateAchivedCI();

                    // Test for softer handover (Softhandover between two sectors of the same Tri-Sector BaseStation location
                    if (l.getBaseStation().getCellLocationId() == link.getBaseStation().getCellLocationId()) {
                        // In softerhandover achievedCI is sum of achievedCI from all links in active set
                        setAchievedCI( Mathematics.powerSummation(getAchievedCI(), achievedCI_inactiveLink) );

                        // Test if inactive link is experiencing a better CI level than the active
                    } else if (achievedCI_inactiveLink > getAchievedCI()) {
                        // Change achievedCI value to higher value
                        setAchievedCI( achievedCI_inactiveLink );
                        // Move active connection to inactive list of BaseStation
                        servingLink.deinitilizeConnection();
                        // Change activeUplink
                        servingLink = l;
                        // Move inactive link from BaseStation inactive list to active
                        // list
                        servingLink.initializeConnection();
                    }
                }
            }
        }
        return getAchievedCI();
    }

    /**
     * Downlink link quality parameters
     * <p></p>
     * set the achieved Ec/Ior as the <code>ReceivedTrafficChannelPower - TotalPowerReceivedFromBaseStationsActive</code>
     */
    public void calculateAchievedEcIor() {

        double pTraf_W = calculateReceivedTrafficChannelPowerWatt();
        double pTot_W = getTotalPowerReceivedFromBaseStationsActiveSetInWatt();

        double pTraf = Mathematics.fromWatt2dBm(pTraf_W);
        double pTot = Mathematics.fromWatt2dBm(pTot_W);

        setAchievedEcIor( pTraf - pTot );
        setRequiredEcIor( linkLevelData.getEcIor() );
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
     * @see #getGeometry()
     */
    public double calculateGeometry( double minGeometry, double maxGeometry ) {
        double extInt = Mathematics.fromdBm2Watt(getExternalInterference());

        double interference = getTotalPowerReceivedFromBaseStationsNotInActiveSet()
                + extInt;
        double absGeometry;
        // Lucent EQ2:
        if (isInSoftHandover() && getMobilitySpeed() == 0) {
            double c1 = Mathematics.fromdBm2Watt(activeList.get(0)
                    .getReceivePower_dB());// Power received from 1st active link
            double c2 = Mathematics.fromdBm2Watt(activeList.get(1)
                    .getReceivePower_dB());// Power received from 2nd active link

            absGeometry = c1 / (c2 + getThermalNoise() + interference) + c2
                    / (c1 + getThermalNoise() + interference);
            setGeometry( Mathematics.linear2dB(absGeometry) );

        } else {
            absGeometry = getTotalPowerReceivedFromBaseStationsInActiveSet()
                    / (getThermalNoise() + interference);
            setGeometry( Mathematics.linear2dB(absGeometry) );
        }

        // the value prevent generating unrealistic Ec/Ior
        setGeometry( Math.max( getGeometry(), minGeometry ) );
        setGeometry( Math.min( getGeometry(), maxGeometry ) );

        return getGeometry();
    }

    public void calculateInitialReceivedPower(double maximumBroadcastChannel) {
        double resultActive_W = 0;
        double resultInActive_W = 0;
        double initialTransmitPower_dbm = Mathematics.fromWatt2dBm(Mathematics.fromdBm2Watt(maximumBroadcastChannel) * 0.70);

        for (int x = 0; x < links.length; x++) {
            if (activeList.contains(links[x])) {
                resultActive_W += Mathematics.fromdBm2Watt(initialTransmitPower_dbm - links[x].getEffectivePathloss());
            } else {
                resultInActive_W += Mathematics
                        .fromdBm2Watt(initialTransmitPower_dbm - links[x].getEffectivePathloss());
            }
        }

        setTotalPowerReceivedFromBaseStationsInActiveSet(resultActive_W);
        setTotalPowerReceivedFromBaseStationsNotInActiveSet(resultInActive_W);
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
        for (int x = 0; x < links.length; x++) {
            CDMADownlink link = (CDMADownlink) links[x];
            if (activeList.contains(link)) {
                resultActive_W += Mathematics.fromdBm2Watt(link
                        .calculateCurrentReceivePower_dBm());
            } else {
                resultInActive_W += Mathematics.fromdBm2Watt(link
                        .calculateCurrentReceivePower_dBm());
            }
        }

        setTotalPowerReceivedFromBaseStationsInActiveSet(resultActive_W);
        setTotalPowerReceivedFromBaseStationsNotInActiveSet(resultInActive_W);
    }

    /**
     * Downlink
     *
     * Method that calculate the received traffic channel power based on the EC/Ior
     */
    public double calculateReceivedTrafficChannelPowerInWatt() {
        double EcIor = Mathematics.dB2Linear(linkLevelData.getEcIor());
        // Lucent EQ(4)
        setReceivedTrafficChannelPowerWatt( getTotalPowerReceivedFromBaseStationsInActiveSet()
                * EcIor );

        double receivedPerLink = getReceivedTrafficChannelPowerWatt();
        if (isInSoftHandover()) {
            receivedPerLink /= 2;
        }
        resetSummationAffectedBaseStations();
        for (AbstractDmaLink l : activeList) {
            CDMADownlink link = (CDMADownlink) l;
            link.setReceivedTrafficChannelPowerdBm(Mathematics
                    .fromWatt2dBm(receivedPerLink));
            link.calculateTransmittedTrafficChannelPowerIndBm();
        }

        // Call get method to update power received from each link
        return calculateReceivedTrafficChannelPowerWatt();
    }

    @Override
    public boolean connect() {
        if (isUpLinkMode()) {
            return this.connectToBaseStationsUplink();
        } else {
            return this.connectToBaseStationsDownlink();
        }
    }

    public boolean connectToBaseStationsDownlink() {
        links[0].initializeConnection();
        boolean powerScaledDown = links[0].isPowerScaledDownToMax();
        int i = 1;
        if (isInSoftHandover()) {
            links[1].initializeConnection();
            powerScaledDown = powerScaledDown || links[1].isPowerScaledDownToMax();
            i++;
        }
        for (int stop = links.length; i < stop; i++) {
            links[i].connectToInActiveBaseStation();
        }

        if (powerScaledDown) {
            setConnected( meetsEcIorRequirement(getSystem().getSystemSettings().getCDMASettings().getCallDropThreshold()) );
        } else {
            setConnected( true );
        }
        return isConnected();
    }

    /**
     * Connect to base station in uplink with the best CI level and initialize the inactive connections while considering <br>
     *     soft handover
     *
     */
    public boolean connectToBaseStationsUplink() {
        AbstractDmaLink firstLink = links[0];
        servingLink = firstLink;

        double achievedCI_firstLink = ((CDMAUplink) firstLink)
                .initializePowerLevels();
        setAchievedCI( achievedCI_firstLink );

        if (isInSoftHandover()) {
            AbstractDmaLink secondLink = links[1];

            double achievedCI_secondLink = ((CDMAUplink) secondLink)
                    .initializePowerLevels();

            // If second link is achieving better CI levels override values
            if (achievedCI_secondLink > achievedCI_firstLink) {
                servingLink = secondLink;
                setAchievedCI( achievedCI_secondLink );
            }
        }
        // Connect to BaseStation with the best CI level
        servingLink.initializeConnection();

        // Initialize the inactive connections
        for (int i = 0, stop = links.length; i < stop; i++) {
            if (servingLink != links[i]) {
                links[i].connectToInActiveBaseStation();
            }
        }
        setConnected( true );
        return isConnected();
    }

    public void dropCall() {
        for (AbstractDmaLink link : links) {
            link.disconnect();
        }
        setConnected( false );
        setDropped( true );
    }

    public CDMALinkLevelDataPoint findLinkLevelDataPoint(CDMALinkLevelData data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Supplied CDMALinkLevelData is null");
        }
        try {
            if (isUpLinkMode()) {
                this.linkLevelData = data
                        .getLinkLevelDataPoint(new CDMALinkLevelDataPoint(getSystem()
                                .getFrequency(), getMultiPathChannel(), 0,
                                getMobilitySpeed(), 0));
            } else {
                this.linkLevelData = data
                        .getLinkLevelDataPoint(new CDMALinkLevelDataPoint(getSystem()
                                .getFrequency(), activeList.size(), getGeometry(),
                                getMobilitySpeed(), 0));

            }
            setLinkLevelDataPointFound( true );
        } catch (IllegalStateException ex) {
            setLinkLevelDataPointFound( false );
        }
        if (linkLevelData == null) {
            setLinkLevelDataPointFound( false );
        }
        return linkLevelData;
    }

    @Override
    protected AbstractDmaLink[] generateLinksArray() {
        if (getSystem().isUplink()) {
            return new CDMAUplink[getSystem().getNumberOfBaseStations()];
        } else {
            return new CDMADownlink[getSystem().getNumberOfBaseStations()];
        }
    }

    @Override
    public void generateLinksToBaseStations() {
        AbstractDmaBaseStation[][] cells = getSystem().getBaseStationArray();
        int linkid = 0;
        for (int i = 0; i < cells.length; i++) {

            double pathloss = 0;
            for (int j = 0; j < cells[i].length; j++, linkid++) {
                if (isUpLinkMode()) {
                    links[linkid] = new CDMAUplink((CdmaBaseStation) cells[i][j], this, (CDMASystem) getSystem());
                } else {
                    links[linkid] = new CDMADownlink((CdmaBaseStation) cells[i][j], this, (CDMASystem) getSystem());
                }
                // Cache site pathloss in three sector case:
                if (j == 0) {
                    links[linkid].determinePathLoss(getSystem().getSystemSettings().getLink().getPropagationModel());
                    pathloss = links[linkid].getTxRxPathLoss();
                } else {
                    links[linkid].setTxRxPathLoss(pathloss);
                }
            }
        }

    }

    public CDMALinkLevelDataPoint getLinkLevelData() {
        if (linkLevelData == null) {
            findLinkLevelDataPoint(((CDMASystem)getSystem()).getLinkLevelData());
        }
        return linkLevelData;
    }

    /**
     * Downlink calculation of the received traffic channel power
     */
    public double calculateReceivedTrafficChannelPowerWatt() {
        double received = 0;
        for (AbstractDmaLink l : activeList) {
            CDMADownlink link = (CDMADownlink) l;
            received += Mathematics.fromdBm2Watt(link.getReceivedTrafficChannelPowerdBm());
        }
        setReceivedTrafficChannelPowerWatt(received);
        return received;
    }

    public double getTotalPowerReceivedFromBaseStationsActiveSetInWatt() {
        return getTotalPowerReceivedFromBaseStationsInActiveSet();
    }

    public double getTotalPowerReceivedFromBaseStationsNotInActiveSetdBm() {
        return Mathematics.fromWatt2dBm( getTotalPowerReceivedFromBaseStationsNotInActiveSet());
    }

    public void increasePowerScaledUpCount() {
        setPowerScaledUpCount(getPowerScaledUpCount() + 1);
    }

    public boolean linkLevelDataPointFound() {
        return isLinkLevelDataPointFound();
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
        boolean value = getAchievedEcIor() >= getRequiredEcIor() - callDropThreshold;

        if (finalLoop || value) {
            setLinkQualityExceptions(0);
        } else if (getLinkQualityExceptions() < 5) {
            // Allow user a free pass for a specified number of power control loop
            // iterations
            setLinkQualityExceptions(getLinkQualityExceptions() + 1);
            value = true;
        }

        return value;
    }

    /**
     * select a user in the active list based on the soft handover margin
     * @param softHandoverMargin
     */
    @Override
    public void selectActiveList(double softHandoverMargin) {
        addToActiveList(links[0]);
        if (links.length > 1){
            if (Math.abs((links[0].getTxRxPathLoss() - links[0].getBsAntGain() - links[0].getUserAntGain())
                    - (links[1].getTxRxPathLoss() - links[1].getBsAntGain() - links[1].getUserAntGain())) < softHandoverMargin) {
                addToActiveList(links[1]);
                setInSoftHandover(true);
            }
        }else{
            setInSoftHandover(false);
        }
    }

    @Override
    public String toString() {
        Point2D point = getPosition();
        return "MS #" + getUserId() + " at (" + Mathematics.round( point.getX())
                + ", " + Mathematics.round(point.getY()) + ")" + " [Geo = "
                + getGeometry() + " dB]";
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof CdmaUserTerminal)) {
            return false;
        } else {
            CdmaUserTerminal term = (CdmaUserTerminal) obj;
            if (term.getUserId() != getUserId()) {
                return false;
            }
            Point2D other = term.getPosition();
            Point2D point = getPosition();
            if (other.getX() != point.getX() || other.getY() != point.getY()) {
                return false;
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        return getUserId();
    }

}
