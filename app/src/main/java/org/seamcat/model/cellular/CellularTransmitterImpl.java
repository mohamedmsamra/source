package org.seamcat.model.cellular;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.model.types.Transmitter;
import org.seamcat.plugin.AntennaGainConfiguration;

import java.util.List;

public class CellularTransmitterImpl implements Transmitter {

    private final EmissionMask emissionMask;
    private final EmissionMask emissionFloor;
    private final boolean usingEmissionFloor;
    private final double bandwidth;
    private final Bounds bandwidthBounds;
    private final List<LocalEnvironment> localEnvironments;
    private final AntennaGainConfiguration antennaGain;
    private final Distribution height;

    public CellularTransmitterImpl(EmissionMask emissionMask, EmissionMask emissionFloor,
                                   boolean usingEmissionFloor,
                                   double bandwidth, Bounds bandwidthBounds, List<LocalEnvironment> localEnvironments,
                                   AntennaGainConfiguration antennaGain, Distribution height) {
        this.emissionMask = emissionMask;
        this.emissionFloor = emissionFloor;
        this.usingEmissionFloor = usingEmissionFloor;
        this.bandwidth = bandwidth;
        this.bandwidthBounds = bandwidthBounds;
        this.localEnvironments = localEnvironments;
        this.antennaGain = antennaGain;
        this.height = height;
    }

    @Override
    public EmissionMask getEmissionsMask() {
        return emissionMask;
    }

    @Override
    public EmissionMask getEmissionsFloor() {
        return emissionFloor;
    }

    @Override
    public boolean isUsingEmissionsFloor() {
        return usingEmissionFloor;
    }

    @Override
    public double getBandwidth() {
        return bandwidth;
    }

    @Override
    public Bounds getBandwidthBounds() {
        return bandwidthBounds;
    }

    @Override
    public List<LocalEnvironment> getLocalEnvironments() {
        return localEnvironments;
    }

    @Override
    public AntennaGainConfiguration getAntennaGain() {
        return antennaGain;
    }

    @Override
    public Distribution getHeight() {
        return height;
    }

    @Override
    public boolean useEmissionMaskAsBEM() {
        return false;
    }
}
