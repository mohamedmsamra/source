package org.seamcat.scenario;

import org.seamcat.model.cellular.ofdma.OFDMASettings;
import org.seamcat.model.functions.Function;

public class OFDMASettingsImpl implements OFDMASettings {

    private OFDMADownLinkImpl downLink;
    private OFDMAUpLinkImpl uplink;
    private int maxSubCarriersPerBaseStation;
    private int numberOfSubCarriersPerUser;
    private double bandwidthOfResourceBlock;
    private PathLossCorrelationImpl pathLossCorrelation;
    private Function bitrateMapping;

    public OFDMASettingsImpl(Function bitrateMapping) {
        maxSubCarriersPerBaseStation = 51;
        numberOfSubCarriersPerUser = 17;
        bandwidthOfResourceBlock = 180;
        pathLossCorrelation = new PathLossCorrelationImpl();
        this.bitrateMapping = bitrateMapping;
    }

    @Override
    public int getMaxSubCarriersPerBaseStation() {
        return maxSubCarriersPerBaseStation;
    }

    public void setMaxSubCarriersPerBaseStation( int maxNumberOfSubCarriersPerBaseStation ) {
        this.maxSubCarriersPerBaseStation = maxNumberOfSubCarriersPerBaseStation;
    }

    @Override
    public int getNumberOfSubCarriersPerMobileStation() {
        return numberOfSubCarriersPerUser;
    }

    public void setNumberOfSubCarriersPerMobileStation( int numberOfSubCarriersPerMobileStation ) {
        this.numberOfSubCarriersPerUser = numberOfSubCarriersPerMobileStation;
    }

    @Override
    public double getBandwidthOfResourceBlock() {
        return bandwidthOfResourceBlock;
    }

    public void setBandwidthOfResourceBlock( double bandwidthOfResourceBlock ) {
        this.bandwidthOfResourceBlock = bandwidthOfResourceBlock;
    }

    @Override
    public Function getBitrateMapping() {
        return bitrateMapping;
    }

    public void setBitrateMapping( Function bitrateMapping ) {
        this.bitrateMapping = bitrateMapping;
    }

    @Override
    public PathLossCorrelationImpl getPathLossCorrelation() {
        return pathLossCorrelation;
    }

    public void setPathLossCorrelation( PathLossCorrelationImpl pathLossCorrelation ) {
        this.pathLossCorrelation = pathLossCorrelation;
    }

    @Override
    public OFDMAUpLinkImpl getUpLinkSettings() {
        return uplink;
    }

    @Override
    public OFDMADownLinkImpl getDownLinkSettings() {
        return downLink;
    }

    public void setDownLinkSettings(OFDMADownLinkImpl downLink) {
        this.downLink = downLink;
    }

    public void setUpLinkSettings(OFDMAUpLinkImpl uplink) {
        this.uplink = uplink;
    }
}
