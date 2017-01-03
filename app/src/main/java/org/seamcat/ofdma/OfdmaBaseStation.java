package org.seamcat.ofdma;

import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.model.factory.RandomAccessor;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OfdmaBaseStation extends AbstractDmaBaseStation {

    private List<AbstractDmaLink> candidateList;
    protected OfdmaSystem system;

    private final Random random;

    public OfdmaBaseStation(Point2D position, OfdmaSystem _system, int _cellid,
                            double antHeight, double antTilt, int sectorId) {
        super(position, _system, _cellid, antHeight, antTilt, sectorId);
        system = _system;

        candidateList = new ArrayList<AbstractDmaLink>();
        random = RandomAccessor.getRandom();
    }

    /**
     * aggregate the bitrate achieved by summing all the calculated achieved bit rate per link
     */
    public double calculateAggregateBitrateAchieved() {
        double sum = 0;

        Iterable<AbstractDmaLink> links = getOldTypeActiveConnections();
        for (AbstractDmaLink link : links) {
            sum += ((OfdmaMobile)link.getUserTerminal()).calculateAchievedBitrate();
        }
        setBitRateAchieved(sum);
        return sum;
    }

    /**
     * Compute the current transmit power as follow:
     *<p></p>
     * <code>CurrentTransmitPower = MaximumTransmitPower* Number_of_SubCarriersInUse_by_the_user / number_of_MaxSubCarriersPerBaseStation (in watt)</code>
     *
     */
    public double calculateCurrentTransmitPower_Watt() {
        return getMaximumTransmitPower_Watt() * getSubCarriersInUse() / getSystem().getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation();
    }

    /**
     * Compute the total interference in dBm from the pre-calculated value in Watt
     *
     * @return total interference in dBm
     */
    @Override
    public double calculateTotalInterference_dBm(AbstractDmaLink excludeLink) {
        return Mathematics.fromWatt2dBm(calculateTotalInterference_Watt(excludeLink));
    }

    @Override
    public double calculateInterferenceWithoutExternal_dBm(AbstractDmaLink excludeLink) {
        return 0; // not doing anything
    }

    private int activeSize, inActiveSize;
    private double cache;
    /**
     * the adjustmentFactorForNonCoLocatedResourceBlock is used to artificially suppress the received signal by 30 dB
     */
    private static double adjustmentFactorForNonCoLocatedResourceBlock = 30;

    /**
     * calculateTotalInterference_Watt for uplink only
     *<p></p>
     * this method take into account the active connections as well as inactive connections
     *
     * @param excludeLink
     */
    public double calculateTotalInterference_Watt(AbstractDmaLink excludeLink) {
        if (excludeLink == null) {
            throw new IllegalArgumentException("Ofdma does not support a null value for exclude link");
        }
        double I_thermal_dBm = excludeLink.getUserTerminal().getThermalNoise();
        double sum_Watt = cache;

        int baseIndex = ((OfdmaBaseStation)excludeLink.getBaseStation()).getLinkIndexOfActiveUser(excludeLink);

        if ( activeSize < activeConnections.size() ) {
            for ( int i=activeSize; i<activeConnections.size(); i++ ) {
                AbstractDmaLink link = activeConnections.get(i);
                double inter_dBm = link.calculateCurrentReceivePower_dBm();

                inter_dBm -= adjustmentFactorForNonCoLocatedResourceBlock;
                cache += Mathematics.fromdBm2Watt(inter_dBm);
                activeSize++;
            }
            sum_Watt = cache;
        }

        if ( inActiveSize < inActiveConnections.size() ) {
            for ( int i=inActiveSize; i<inActiveConnections.size(); i++ ) {
                AbstractDmaLink link = inActiveConnections.get(i);
                double inter_dBm = link.calculateCurrentReceivePower_dBm();
                inter_dBm -= adjustmentFactorForNonCoLocatedResourceBlock;
                cache += Mathematics.fromdBm2Watt(inter_dBm);
                inActiveSize++;
            }
            sum_Watt = cache;
        }

        if ( activeConnections.contains( excludeLink ) ) {
            double inter_dBm = excludeLink.calculateCurrentReceivePower_dBm() - adjustmentFactorForNonCoLocatedResourceBlock;
            sum_Watt -= Mathematics.fromdBm2Watt( inter_dBm );
        }
        if ( inActiveConnections.contains( excludeLink) ) {
            double inter_dBm = excludeLink.calculateCurrentReceivePower_dBm() - adjustmentFactorForNonCoLocatedResourceBlock;
            sum_Watt -= Mathematics.fromdBm2Watt( inter_dBm );
        }

        int index=0, users = activeConnections.size();
        for ( int i=0; i<users; i++ ) {
            if ( ((OfdmaMobile)activeConnections.get(i).getUserTerminal()).getLinkIndex() == baseIndex ) {
                index = i;
                break;
            }
        }

        int max = inActiveConnections.size() / users;
        for (int i=0; i<max; i++) {
            double inter_dBm = inActiveConnections.get((users * i) + index).calculateCurrentReceivePower_dBm();
            sum_Watt -= Mathematics.fromdBm2Watt(inter_dBm-adjustmentFactorForNonCoLocatedResourceBlock);
            sum_Watt += Mathematics.fromdBm2Watt(inter_dBm );

        }

        setInterSystemInterference(Mathematics.fromWatt2dBm(sum_Watt));

        double I_external = 0;
        if (getSystem().isExternalInterferenceActive()) {
            I_external = excludeLink.calculateExternalInterference_dBm();

            sum_Watt += Mathematics.fromdBm2Watt(I_external);
        }

        if (Double.isInfinite(I_external)) {
            throw new IllegalStateException("External interference is infinite");
        }

        double res = Mathematics.fromdBm2Watt(I_thermal_dBm) + sum_Watt;
        return res;
    }

    @Override
    public int countActiveUsers() {
        return activeConnections.size();
    }

    @Override
    public int countDroppedUsers() {
        return droppedUsers.size();
    }

    @Override
    public int countInActiveUsers() {
        return inactiveUsers.size();
    }

    @Override
    public double getCurrentTransmitPower_dBm() {
        return Mathematics.fromWatt2dBm(calculateCurrentTransmitPower_Watt());
    }

    /**
     * prepare the initial connection and measure the load factor of the network to simulate
     *
     */
    public boolean initialConnect() {
        int sum = getSubCarriersInUse();

        double loadFactor = (Math.rint(sum) /  Math.rint(system.getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation()));
        boolean underLoaded = loadFactor < 1;

        boolean candidatesAvailable = (candidateList.size() > 0);

        while (underLoaded && candidatesAvailable) {
            //Select random mobile from candidate list
            AbstractDmaLink candidate = fetchRandomCandidate();

            int temp = candidate.getUserTerminal().getRequestedSubCarriers();

            if (sum + temp <= system.getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation()) {
                // BaseStation is not over loaded -> add requirement to sum
                sum += temp;
                activeConnections.add(candidate);
                candidate.activateLink();
            }

            candidatesAvailable = (candidateList.size() > 0);

            loadFactor = (Math.rint(sum) /  Math.rint(system.getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation()));
            underLoaded = (loadFactor < 1) && (system.getSystemSettings().getOFDMASettings().getNumberOfSubCarriersPerMobileStation() <= (system.getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation() - sum));

        }

        setSubCarriersInUse( sum );
        return !underLoaded;
    }

    public AbstractDmaLink fetchRandomCandidate() {
        int i = random.nextInt( candidateList.size() );
        return candidateList.remove(i);
    }

    @Override
    public void resetBaseStation() {
        activeConnections.clear();
        inActiveConnections.clear();
        droppedUsers.clear();
        inactiveUsers.clear();
        candidateList.clear();
        setSubCarriersInUse(0);
    }

    @Override
    public void intializeConnection(AbstractDmaLink link) {
        candidateList.add(link);
    }

    public int getLinkIndexOfActiveUser(AbstractDmaLink link) {
        return activeConnections.indexOf(link);
    }

    @Override
    public String toString() {
        return "OfdmaBaseStation #" + getCellid() + " (" + activeConnections.size() + " active connections)";
    }

    @Override
    public void addInActiveConnection(AbstractDmaLink link) {
        super.addInActiveConnection(link);
        candidateList.remove(link);
    }
}
