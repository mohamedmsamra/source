package org.seamcat.ofdma;

import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.dmasystems.UserShouldBeIgnoredException;
import org.seamcat.marshalling.FunctionMarshaller;
import org.seamcat.model.generic.Defaults;
import org.seamcat.scenario.OFDMASettingsImpl;

import java.util.List;

public abstract class OfdmaSystem<GenericMobileType extends OfdmaMobile>
        extends AbstractDmaSystem<GenericMobileType>  {

    private void init() {
        getSystemSettings().setCDMASettings(null);
        getSystemSettings().setOFDMASettings(new OFDMASettingsImpl(Defaults.defaultOFDMABitRateMapping()));
    }

    protected OfdmaSystem(AbstractDmaSystem<?> system) {
        super(system);
        init();
        if (system instanceof OfdmaSystem<?>) {
            OfdmaSystem<?> of = (OfdmaSystem<?>) system;

            getSystemSettings().getOFDMASettings().setMaxSubCarriersPerBaseStation( of.getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation());
            externalInterferenceActive = of.externalInterferenceActive;

            getSystemSettings().getOFDMASettings().setNumberOfSubCarriersPerMobileStation(of.getSystemSettings().getOFDMASettings().getNumberOfSubCarriersPerMobileStation());
            getSystemSettings().getOFDMASettings().setBandwidthOfResourceBlock(of.getSystemSettings().getOFDMASettings().getBandwidthOfResourceBlock());
            getSystemSettings().getOFDMASettings().setBitrateMapping( FunctionMarshaller.copy( of.getSystemSettings().getOFDMASettings().getBitrateMapping()));
            getSystemSettings().getOFDMASettings().setPathLossCorrelation( of.getSystemSettings().getOFDMASettings().getPathLossCorrelation());
        } else {
            getSystemSettings().setUsersPerCell(20);
            getSystemSettings().setHandoverMargin(3);
            getSystemSettings().setBandwidth(10);
        }
    }

    @Override
    public void calculateThermalNoise() {
        super.calculateThermalNoise();
        for (GenericMobileType mobile : getActiveUsers()) {
            mobile.calculateThermalNoise();
        }
    }

    @Override
    public double getReferenceCellMeasurement() {
        return ((OfdmaBaseStation)referenceCell).calculateAggregateBitrateAchieved();
    }

    /**
     * high level method to prepare the connection. Used in the simulate() method
     *
     */
    protected boolean initiateConnections() {
        boolean systemIsLoaded = true;
        for (AbstractDmaBaseStation base : getAllBaseStations()) {
            systemIsLoaded = systemIsLoaded && ((OfdmaBaseStation)base).initialConnect();
        }
        return systemIsLoaded;
    }

    protected abstract void simulateLinkSpecifics();

    @Override
    protected void generateAndPositionMobiles() {
        for (int i = 0, stop = getSystemSettings().getUsersPerCell() * getNumberOfBaseStations(); i < stop; i++) {
            generateInitializedMobile();
        }
    }

    @Override
    public void performPreSimulationTasks(double frequency) {
        super.performPreSimulationTasks(frequency);

        // maybe put in preSimulationResults
        getSystemSettings().getOFDMASettings().getPathLossCorrelation().initDistribution();
    }

    @Override
    public void simulate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Positioning the Networks");
        }

        positionSystem();
        do {
            if (LOG.isDebugEnabled()) {
                LOG.debug(" Generating And Positioning mobiles: "+toString());
                LOG.debug(" System Description: OFDMA system");
                LOG.debug(" System Minimum coupling loss: "+getSystemSettings().getMinimumCouplingLoss()+" dBm");
            }

            generateAndPositionMobiles();
        }
        while (!initiateConnections());

        //Debug code end
        if (LOG.isDebugEnabled()) {
            LOG.debug("calculateThermalNoise()");
        }
        calculateThermalNoise();
        simulateLinkSpecifics();
    }

    @Override
    protected void performSystemSpecificInitialization(GenericMobileType user) throws UserShouldBeIgnoredException {
        user.setRequestedSubCarriers(getSystemSettings().getOFDMASettings().getNumberOfSubCarriersPerMobileStation());
    }

    /**
     * convert resource blocks from kHz to MHz
     *
     */
    public double getResourceBlockSizeInMHz() {
        return getSystemSettings().getOFDMASettings().getBandwidthOfResourceBlock() * 0.001; //From kHz to MHz
    }

    /**
     * compute the average bit rate over all the base station
     *
     */
    public double calculateAverageAchievedBitrate() {
        double sum = 0;
        for (AbstractDmaBaseStation base : getAllBaseStations()) {
            sum += ((OfdmaBaseStation)base).calculateAggregateBitrateAchieved();
        }
        return sum / getNumberOfBaseStations();
    }

    public abstract List<OfdmaVictim> getVictims();

    /**
     * compute the operating center frequency of each UE in OFDMA UL such as:
     *<p></p>
     * <code>UE_freq = systemFrequency - (systemBandwidth/2) + (diff/2) + (numberOfSubCarrierPerMobile * resourceBlockBandwidth)/2 * (linkIndex * 2 + 1)</code>
     *<p></p>
     * where <code>diff = systemBandwidth - (numberOfSubCarriersPerBaseStation * resourceBlockBandwidth)</code>
     *
     * @param linkIndex
     */
    public double calculateFrequency(int linkIndex) {
        double systemBandwidth = getSystemSettings().getBandwidth();
        double systemFrequency = getFrequency();
        double numberOfSubCarrierPerMobile = getSystemSettings().getOFDMASettings().getNumberOfSubCarriersPerMobileStation();
        double numberOfSubCarriersPerBaseStation = getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation();
        double resourceBlockBandwidth = getResourceBlockSizeInMHz();

        double diff = systemBandwidth - (numberOfSubCarriersPerBaseStation * resourceBlockBandwidth);

        return systemFrequency - (systemBandwidth/2) + (diff/2) + (numberOfSubCarrierPerMobile * resourceBlockBandwidth)/2 * (linkIndex * 2 + 1);
    }
}
