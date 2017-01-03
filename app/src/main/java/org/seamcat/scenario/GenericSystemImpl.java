package org.seamcat.scenario;

import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.generic.*;

public class GenericSystemImpl implements GenericSystem {


    private String name;
    private GenericTransmitter transmitter;
    private GenericReceiver receiver;
    private GenericLink link;
    private final InterfererDensity density;
    private AbstractDistribution frequency;

    public GenericSystemImpl( String name, GenericTransmitter transmitter, GenericReceiver receiver,
                              GenericLink link, Distribution frequency, InterfererDensity density) {
        this.name = name;
        this.transmitter = transmitter;
        this.receiver = receiver;
        this.link = link;
        this.density = density;
        this.frequency = (AbstractDistribution) frequency;
    }

    public void setFrequency(Distribution frequency ) {
        this.frequency = (AbstractDistribution) frequency;
    }

    @Override
    public GenericReceiver getReceiver() {
        return receiver;
    }

    @Override
    public GenericTransmitter getTransmitter() {
        return transmitter;
    }

    @Override
    public GenericLink getLink() {
        return link;
    }

    @Override
    public AbstractDistribution getFrequency() {
        return frequency;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public InterfererDensity getInterfererDensity() {
        return density;
    }
}
