package org.seamcat.model;

import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.MaskFunction;
import org.seamcat.model.generic.*;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.model.types.SensingLink;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;

import java.util.List;

public class Transmitter implements GenericTransmitter {

    private final EmissionCharacteristics emissionCharacteristics;
    private List<LocalEnvironment> localEnvironments;
    private Distribution height;
    private double bandwidth;
    private Bounds bandwidthBounds;
    private AntennaGainConfiguration antennaGain;
    private TXAntennaPointingUI antennaPointing;

    public Transmitter(EmissionCharacteristics emissionCharacteristics, double bandwidth, Bounds bounds) {
        this.emissionCharacteristics = emissionCharacteristics;
        this.bandwidth = bandwidth;
        bandwidthBounds = bounds;
        antennaGain = SeamcatFactory.antennaGain().getPeakGainAntenna();
        antennaPointing = ProxyHelper.newInstance( TXAntennaPointingUI.class );
    }

    public AntennaGainConfiguration getAntennaGain() {
        return antennaGain;
    }

    public void setAntennaGain(AntennaGainConfiguration value) {
        antennaGain = value;
    }

    public void setAntennaPointing( TXAntennaPointingUI antennaPointing ) {
        this.antennaPointing = antennaPointing;
    }

    public AntennaPointing getAntennaPointing() {
        return new AntennaPointing() {
            @Override
            public Distribution getAzimuth() {
                return antennaPointing.azimuth();
            }

            @Override
            public Distribution getElevation() {
                return antennaPointing.elevation();
            }

            @Override
            public boolean getAntennaPointingAzimuth() {
                return antennaPointing.antennaPointingAzimuth();
            }

            @Override
            public boolean getAntennaPointingElevation() {
                return antennaPointing.antennaPointingElevation();
            }

        };
    }

    @Override
    public AbstractDistribution getPower() {
        return (AbstractDistribution) emissionCharacteristics.power();
    }

    @Override
    public boolean isInterfererCognitiveRadio() {
        return emissionCharacteristics.cognitiveRadio();
    }

    @Override
    public boolean isUsingPowerControl() {
        return emissionCharacteristics.powerControl();
    }

    @Override
    public double getPowerControlStepSize() {
        return emissionCharacteristics.stepSize();
    }

    @Override
    public double getPowerControlMinThreshold() {
        return emissionCharacteristics.minThreshold();
    }

    @Override
    public double getPowerControlDynamicRange() {
        return emissionCharacteristics.dynamicRange();
    }

    @Override
    public EmissionMaskImpl getEmissionsMask() {
        return (EmissionMaskImpl) emissionCharacteristics.emissionMask();
    }

    @Override
    public EmissionMaskImpl getEmissionsFloor() {
        return (EmissionMaskImpl) emissionCharacteristics.emissionFloor().getMaskFunction();
    }

    @Override
    public boolean isUsingEmissionsFloor() {
        return emissionCharacteristics.emissionFloor().isRelevant();
    }

    @Override
    public double getBandwidth() {
        return bandwidth;

    }

    @Override
    public Bounds getBandwidthBounds() {//TODO added by KK, required for blocking in case mask is asymmetrical
        return bandwidthBounds;
    }


    @Override
    public List<LocalEnvironment> getLocalEnvironments() {
        return localEnvironments;
    }

    public void setLocalEnvironments(List<LocalEnvironment> localEnvironments ) {
        this.localEnvironments = localEnvironments;
    }

    @Override
    public Distribution getHeight() {
        return height;
    }

    public void setHeight( Distribution height ) {
        this.height = height;
    }


    @Override
    public SensingLink getSensingLink() {
        return new SensingLink() {
            @Override
            public Function getDetectionThreshold() {
                return emissionCharacteristics.detectionThreshold();
            }

            @Override
            public double getProbabilityOfFailure() {
                return emissionCharacteristics.probabilityOfFailure();
            }

            @Override
            public double getBandwidth() {
                return emissionCharacteristics.receptionBandwidth();
            }

            @Override
            public MaskFunction getEIRPInBlockMask() {
                return emissionCharacteristics.eirpMax();
            }

            @Override
            public PropagationModelConfiguration getPropagationModel() {
                return (PropagationModelConfiguration) emissionCharacteristics.propagationModel();
            }
        };
    }

    @Override
    public boolean useEmissionMaskAsBEM() {
        return false;
    }
}
