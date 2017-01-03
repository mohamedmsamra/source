package org.seamcat.scenario;

import org.seamcat.model.cellular.MobileStation;
import org.seamcat.model.distributions.AbstractDistribution;

public class MobileStationImpl implements MobileStation {

    private AbstractDistribution antennaHeight;
    private AbstractDistribution antennaGain;
    private AbstractDistribution mobility;

    public MobileStationImpl( AbstractDistribution antennaHeight, AbstractDistribution antennaGain, AbstractDistribution mobility ) {
        this.antennaHeight = antennaHeight;
        this.antennaGain = antennaGain;
        this.mobility = mobility;
    }

    @Override
    public AbstractDistribution getAntennaHeight() {
        return antennaHeight;
    }

    public void setAntennaHeight(AbstractDistribution antennaHeight ) {
        this.antennaHeight = antennaHeight;
    }

    @Override
    public AbstractDistribution getAntennaGain() {
        return antennaGain;
    }

    public void setAntennaGain( AbstractDistribution antennaGain ) {
        this.antennaGain = antennaGain;
    }

    @Override
    public AbstractDistribution getMobility() {
        return mobility;
    }

    public void setMobility( AbstractDistribution mobility ) {
        this.mobility = mobility;
    }
}
