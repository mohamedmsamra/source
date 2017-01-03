package org.seamcat.ofdma;

import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.dmasystems.UserShouldBeIgnoredException;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.types.result.DoubleResultType;
import org.seamcat.scenario.OFDMAUpLinkImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.*;
import static org.seamcat.model.mathematics.Mathematics.fromWatt2dBm;
import static org.seamcat.model.mathematics.Mathematics.fromdBm2Watt;

public class UplinkOfdmaSystem extends OfdmaSystem<UplinkOfdmaMobile> {

    public static final String COUPLING_LOSS_PERCENTILE = "Coupling loss percentile";

    @Override
    public UplinkOfdmaMobile generateUserTerminal() {
        return new UplinkOfdmaMobile(new Point2D(0, 0), this, useridcount++,
                getSystemSettings().getLink().getMobileStation().getAntennaGain().trial(),
                getSystemSettings().getLink().getMobileStation().getAntennaHeight().trial());
    }


    private void init() {
        getSystemSettings().getOFDMASettings().setDownLinkSettings(null);
        getSystemSettings().getOFDMASettings().setUpLinkSettings(new OFDMAUpLinkImpl());
        getSystemSettings().setUpLink( true );
    }

    public UplinkOfdmaSystem(AbstractDmaSystem<?> dma) {
        super(dma);
        init();
        if (dma instanceof UplinkOfdmaSystem) {
            UplinkOfdmaSystem us = (UplinkOfdmaSystem) dma;
            getSystemSettings().getOFDMASettings().getUpLinkSettings().setMaximumAllowedTransmitPowerOfMS(us.getSystemSettings().getOFDMASettings().getUpLinkSettings().getMaximumAllowedTransmitPowerOfMS());
            getSystemSettings().getOFDMASettings().getUpLinkSettings().setPowerScalingThreshold( us.getSystemSettings().getOFDMASettings().getUpLinkSettings().getPowerScalingThreshold());
            getSystemSettings().getOFDMASettings().getUpLinkSettings().setBalancingFactor( us.getSystemSettings().getOFDMASettings().getUpLinkSettings().getBalancingFactor());
            getSystemSettings().getOFDMASettings().getUpLinkSettings().setMinimumTransmitPowerOfMS(us.getSystemSettings().getOFDMASettings().getUpLinkSettings().getMinimumTransmitPowerOfMS());
        }
    }

    public List<UplinkOfdmaBaseStation> getAllBaseStations_Uplink() {
        List<UplinkOfdmaBaseStation> list = new ArrayList<UplinkOfdmaBaseStation>();
        if (cells != null) {
            for (AbstractDmaBaseStation[] bases : cells) {
                for (AbstractDmaBaseStation base : bases) {
                    list.add((UplinkOfdmaBaseStation) base);
                }
            }
        }
        return list;
    }

    @Override
    protected void configureBaseStation(AbstractDmaBaseStation base) {
        base.resetBaseStation();
    }

    @Override
    protected OfdmaBaseStation generateBaseStation(
            Point2D position, int cellid, double antennaHeight, double antennaTilt,int sectorid, boolean triSector) {
        return new UplinkOfdmaBaseStation<>(position, this, cellid, antennaHeight, antennaTilt, sectorid);
    }

    @Override
    protected OfdmaBaseStation[][] generateBaseStationArray() {
        return new UplinkOfdmaBaseStation[getNumberOfCellSitesInPowerControlCluster()][cellsPerSite()];
    }

    public List<AbstractDmaLink> getActiveConnections() {
        List<AbstractDmaLink> links = new ArrayList<AbstractDmaLink>(getActiveUsers().size());

        for (AbstractDmaBaseStation base : getAllBaseStations()) {
            links.addAll(base.getOldTypeActiveConnections());
        }

        return links;
    }

    /**
     * compute the SINR for the active users in uplink
     */
    protected void powerControl() {
        for (int i = 0;i < getActiveUsers().size();i++) {
            UplinkOfdmaMobile mobile = getActiveUsers().get(i);
            mobile.calculateSINR();
        }
    }

    /**
     * <ol>
     * <li>set the max Tx power of the UE (uplink direction)</li>
     * <li>set the current transmit power of the UE</li>
     * </ol>
     *
     *
     *
     * @param user
     * @throws UserShouldBeIgnoredException
     */
    @Override
    protected void performSystemSpecificInitialization(UplinkOfdmaMobile user) throws UserShouldBeIgnoredException {
        super.performSystemSpecificInitialization(user);
        user.setMaxTxPower(getSystemSettings().getOFDMASettings().getUpLinkSettings().getMaximumAllowedTransmitPowerOfMS());
        user.setCurrentTransmitPower_dBm(getSystemSettings().getOFDMASettings().getUpLinkSettings().getMaximumAllowedTransmitPowerOfMS());
    }

    /**
     * Part of the uplink power control procedure
     *<p></p>
     * Scales the transmit power based on the pathloss

     * @param user
     * @param pathloss_limit
     */
    protected void scalePower(UplinkOfdmaMobile user, double pathloss_limit) {
        double minTxpower = getSystemSettings().getOFDMASettings().getUpLinkSettings().getMinimumTransmitPowerOfMS();
        double maxTxpower = getSystemSettings().getOFDMASettings().getUpLinkSettings().getMaximumAllowedTransmitPowerOfMS();
        double rMin_linear = fromdBm2Watt(minTxpower)/fromdBm2Watt(maxTxpower);

        double pathloss = user.getServingLink().getEffectivePathloss();

        double test_ratio_linear = fromdBm2Watt(pathloss) / fromdBm2Watt(pathloss_limit);

        double p_watt = fromdBm2Watt(getSystemSettings().getOFDMASettings().getUpLinkSettings().getMaximumAllowedTransmitPowerOfMS()) * min(1, max(rMin_linear, pow(test_ratio_linear, getSystemSettings().getOFDMASettings().getUpLinkSettings().getBalancingFactor())));

        user.setPL( pathloss );
        user.setPlilx(pathloss_limit);
        user.setCurrentTransmitPower_dBm(fromWatt2dBm(p_watt));
    }

    protected void resetMobiles() {
        for (UplinkOfdmaMobile mobile : getAllActiveUsers()) {
            mobile.setDisconnectAttempts(0);
        }
    }

    /**
     * calculate the COUPLING_LOSS_PERCENTILE as part of the OFDMA presimulation, to tune the input parameter of the power control
     */
    @Override
    public void performPreSimulationTasks(double frequency) {
        super.performPreSimulationTasks(frequency);
        if ( -1 == getResults().getPreSimulationResults().findDoubleValue(COUPLING_LOSS_PERCENTILE) ) {
            resetSystem();
            // coupling loss percentile is set on a minimum of 1000 active connections
            List<Double> effectivePathLoss = new ArrayList<Double>();
            while ( effectivePathLoss.size() < 1000 ) {
                setupSystem();
                for (int i = 0;i < getActiveUsers().size();i++) {
                    effectivePathLoss.add( getActiveUsers().get(i).getServingLink().getEffectivePathloss() );
                }
            }
            double[] values = new double[effectivePathLoss.size()];
            for ( int i=0; i<effectivePathLoss.size(); i++) {
                values[i] = effectivePathLoss.get(i);
            }
            Arrays.sort(values);
            getResults().getPreSimulationResults().getSingleValueTypes().add(new DoubleResultType(COUPLING_LOSS_PERCENTILE, "", calculatePowerControlledPathlossLimit(values)));
        }
    }

    /**
     * part of the finding of the COUPLING_LOSS_PERCENTILE from the cumulative distribution function of the path loss
     *
     * @param effectivePathLoss
     */
    protected double calculatePowerControlledPathlossLimit(double[] effectivePathLoss) {
        int floor = (int) Math.floor(getSystemSettings().getOFDMASettings().getUpLinkSettings().getPowerScalingThreshold() / ((double) 1 / effectivePathLoss.length));
        return effectivePathLoss[floor];
    }

    private void setupSystem() {
        super.performPreSimulationTasks(getFrequency());
        positionSystem();
        do {
            generateAndPositionMobiles();
        }
        while (!initiateConnections());

    }

    /**
     * method to simulate links
     * <ol>
     *    <li>reset the mobiles</li>
     *    <li>get the coupling loss percentile for the power control</li>
     *    <li>call the power control process of OFDMA uplink</li>
     * </ol>
     */
    @Override
    public void simulateLinkSpecifics() {
        resetMobiles();

        for (UplinkOfdmaMobile mobile : getActiveUsers()) {
            scalePower(mobile, getResults().getPreSimulationResults().findDoubleValue(COUPLING_LOSS_PERCENTILE));
        }
        powerControl();
    }

    @Override
    public List<OfdmaVictim> getVictims() {
        List<OfdmaVictim> victims = new ArrayList<OfdmaVictim>();
        victims.addAll(getAllBaseStations_Uplink());
        return victims;
    }
}
