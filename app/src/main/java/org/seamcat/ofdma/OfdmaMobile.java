package org.seamcat.ofdma;

import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaMobile;
import org.seamcat.model.cellular.ofdma.OFDMASettings;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.FunctionException;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.scenario.OFDMASettingsImpl;
import org.seamcat.scenario.PathLossCorrelationImpl;
import org.seamcat.simulation.cellular.CellularCalculations;

import java.util.Random;

public abstract class OfdmaMobile extends AbstractDmaMobile {

    protected static final Random RANDOM = new Random();

    protected OfdmaSystem system;

    private double cache;

    public OfdmaMobile(Point2D point, OfdmaSystem _system, int _userid, double antGain, double antHeight) {
        super(point, _system, _userid, antGain, antHeight);
        system = _system;
        setCurrentTransmitPowerIndBm( 24 ); // dBm
    }

    public double calculateAchievedBitrate() {
        double sinr = calculateSINR();
        double maxAch = getMaxAchievableBitRate(sinr);

        setBitRateAchieved( getRequestedSubCarriers() * (maxAch / getSystem().getSystemSettings().getOFDMASettings().getMaxSubCarriersPerBaseStation()));
        return getBitRateAchieved();
    }

    public double calculateThermalNoise() {
        setThermalNoise( CellularCalculations.calculateThermalNoise(getRequestedSubCarriers() * system.getResourceBlockSizeInMHz(), system.getSystemSettings().getReceiverNoiseFigure()) );
        return getThermalNoise();
    }

    public abstract double calculateSINR();

    @Override
    public double calculateTotalInterference_Watt() {
        double I_thermal_dBm = getThermalNoise();
        double sum_Watt = cache;

        int baseIndex = -1;
        if (servingLink != null) {
            baseIndex = ((OfdmaBaseStation)servingLink.getBaseStation()).getLinkIndexOfActiveUser(servingLink);
        }

        if ( cache == 0 ) {
            for (AbstractDmaLink link : links) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("link: "+link+" baseindex: "+baseIndex);
                }

                double inter_dBm = link.calculateCurrentReceivePower_dBm();

                sum_Watt += Mathematics.fromdBm2Watt(inter_dBm);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Sum interference: "+Mathematics.fromWatt2dBm(sum_Watt)+" inter_dBm: "+inter_dBm);
                }
            }
            cache = sum_Watt;
        }
        // adjust for servingLink
        if ( servingLink != null ) {
            sum_Watt -= Mathematics.fromdBm2Watt( servingLink.calculateCurrentReceivePower_dBm() );
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("InterSystemInterference: "+Mathematics.fromWatt2dBm(sum_Watt));
        }
        setInterSystemInterference(Mathematics.fromWatt2dBm(sum_Watt));

        double I_external = 0;
        if (getSystem().isExternalInterferenceActive()) {
            I_external = getExternalInterference();
            if (LOG.isDebugEnabled()) {
                LOG.debug("External interferers is DMA: "+I_external);
            }

            sum_Watt += Mathematics.fromdBm2Watt(I_external);
        }

        double res = Mathematics.fromdBm2Watt(I_thermal_dBm) + sum_Watt;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Total External interference (dBm): "+Mathematics.fromWatt2dBm(res));
        }
        return res;
    }

    @Override
    public boolean connect() {
        AbstractDmaLink link = activeList.get(RANDOM.nextInt(activeList.size()));
        link.initializeConnection();
        servingLink = link;
        return true;
    }

    protected double getMaxAchievableBitRate(double sinrAchieved) {
        OFDMASettings settings = getSystem().getSystemSettings().getOFDMASettings();
        Function function = settings.getBitrateMapping();
        double value = 0; // returned value is bps/Hz

        if (sinrAchieved < function.getBounds().getMin()) {
            value = function.evaluateMin();
        } else if (sinrAchieved > function.getBounds().getMax()) {
            value = function.evaluateMax();
        } else {
            try {
                value = function.evaluate(sinrAchieved);
            } catch (FunctionException ex) {
                throw new RuntimeException(ex);
            }
        }
        return settings.getBandwidthOfResourceBlock() * settings.getNumberOfSubCarriersPerMobileStation() * value * 1/1000;
        //return getSystem().getSystemSettings().getBandwidth() * Mathematics.MHZ_TO_HZ * value / Mathematics.BPS_TO_KBPS;
    }

    /**
     * Method applying a hard handover for the OFDMA/LTE module
     *<p></p>
     * It is used in UL and DL
     *
     * @param handoverMargin
     */
    @Override
    public void selectActiveList(double handoverMargin) {
        if (activeList.size() != 0) {
            throw new IllegalStateException(
                    "Active List selection should only be performed once!");
        }

        links[0].getBaseStation().intializeConnection(links[0]);
        addToActiveList(links[0]);
        if (links.length > 1){
            int index = 1;

            double lowestLink = links[0].getEffectivePathloss();
            double nextLink = links[index].getEffectivePathloss();

            while (lowestLink + handoverMargin > nextLink) {
                if ( index == 1 ) {
                    addToActiveList(links[1]);
                }
                links[index].getBaseStation().intializeConnection(links[index]);
                setInSoftHandover( true ); //isInSoftHandover is not used in OFDMA/LTE
                index++;
                if (index < links.length){
                    nextLink = links[index].getEffectivePathloss();
                }else{
                    return;
                }
            }
        }
        else{
            setInSoftHandover( false ); //isInSoftHandover is not used in OFDMA/LTE
        }
    }

    public void initializeInActiveConnections(AbstractDmaLink activeLink) {
        for (AbstractDmaLink link : links) {
            if (link != activeLink) {
                link.connectToInActiveBaseStation();
            }
        }
    }

    public int getLinkIndex() {
        if (servingLink != null) {
            return ((OfdmaBaseStation)servingLink.getBaseStation()).getLinkIndexOfActiveUser(servingLink);
        }
        return -1;
    }

    @Override
    public double getFrequency() {
        return system.calculateFrequency(getLinkIndex());
    }

    public double getBandwidth() {
        return this.getRequestedSubCarriers() * system.getResourceBlockSizeInMHz();
    }

    /**
     * This method generate links to base stations
     *<p></p>
     * it considers the path loss correlation
     */
    @Override
    public void generateLinksToBaseStations() {
        OfdmaBaseStation[][] cells = (OfdmaBaseStation[][]) getSystem().getBaseStationArray();
        int linkid = 0;

        PathLossCorrelationImpl correlation = getSystem().getSystemSettings().getOFDMASettings().getPathLossCorrelation();
        double correlationInterSite = correlation.getCorrelationFactor();

        double a_inter = Math.sqrt(Math.abs(correlationInterSite));
        double b_inter = Math.sqrt(1 - Math.abs(correlationInterSite));


        double x = correlation.trial();

        for (int i = 0; i < cells.length; i++) {

            double pathloss = 0;

            for (int j = 0; j < cells[i].length; j++, linkid++) {
                double y = correlation.trial();

                if ( isUpLinkMode() ) {
                    links[linkid] = new OfdmaUplink(cells[i][j], this, system);
                } else {
                    links[linkid] = new OfdmaDownlink(cells[i][j], this, system);
                }

                if (system.getSystemSettings().getOFDMASettings().getPathLossCorrelation().isUsingPathLossCorrelation()) {
                    //assume 50% correlation between all sites
                    if (j == 0) {

                        links[linkid].determinePathLoss(system.getSystemSettings().getLink().getPropagationModel());

                        pathloss = links[linkid].getTxRxPathLoss();

                        //Modify calculated pathloss with user defined correlation factor
                        pathloss += a_inter*x + b_inter*y;

                        links[linkid].setTxRxPathLoss(pathloss);

                    } else {
                        links[linkid].setTxRxPathLoss(pathloss);
                    }
                } else {
                    // Cache site pathloss in three sector case:
                    if (j == 0) {
                        links[linkid].determinePathLoss(system.getSystemSettings().getLink().getPropagationModel());

                        pathloss = links[linkid].getTxRxPathLoss();
                    } else {
                        //correlation = 1
                        links[linkid].setTxRxPathLoss(pathloss);
                    }
                }

            }
        }
    }

    @Override
    public String toString() {
        return "Mobile #" + getUserId() + " Achived SINR: " + getSINRAchieved();
    }
}
