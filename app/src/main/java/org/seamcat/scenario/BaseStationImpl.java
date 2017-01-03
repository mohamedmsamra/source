package org.seamcat.scenario;

import org.seamcat.model.cellular.BaseStation;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.plugin.AntennaGainConfiguration;

public class BaseStationImpl implements BaseStation {

    private AbstractDistribution height;
    private AbstractDistribution tilt;
    private AntennaGainConfiguration antennaGain;

    public BaseStationImpl( AbstractDistribution height, AbstractDistribution tilt, AntennaGainConfiguration antennaGain ) {
        this.height = height;
        this.tilt = tilt;
        this.antennaGain = antennaGain;
    }

    @Override
    public AbstractDistribution getHeight() {
        return height;
    }

    public void setHeight(AbstractDistribution height ) {
        this.height = height;
    }

    @Override
    public AbstractDistribution getTilt() {
        return tilt;
    }

    public void setTilt(AbstractDistribution tilt ) {
        this.tilt = tilt;
    }

    @Override
    public AntennaGainConfiguration getAntennaGain() {
        return antennaGain;
    }

    public void setAntennaGain(AntennaGainConfiguration antennaGain ) {
        this.antennaGain = antennaGain;
    }
}
