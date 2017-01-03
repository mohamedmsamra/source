package org.seamcat.model;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.generic.*;
import org.seamcat.model.plugin.antenna.HorizontalVerticalInput;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.plugin.AntennaGainConfiguration;

import java.util.List;

import static org.seamcat.model.generic.GenericReceiver.BlockingAttenuationMode.MODE_SENSITIVITY;
import static org.seamcat.model.generic.GenericReceiver.BlockingAttenuationMode.PROTECTION_RATIO;

public class Receiver implements GenericReceiver {

    private List<LocalEnvironment> localEnvironments;
    private InterferenceCriteria interferenceCriteria;
    private ReceptionCharacteristics receptionCharacteristics;
    private Distribution height;
    private AntennaGainConfiguration antennaGain;
    private RXAntennaPointingUI antennaPointing;
    AntennaGain antennaGainValue;

    public void setInterferenceCriteria( InterferenceCriteria interferenceCriteria ) {
        this.interferenceCriteria = interferenceCriteria;
    }

    public Receiver() {
        receptionCharacteristics = ProxyHelper.newInstance( ReceptionCharacteristics.class );
        interferenceCriteria = ProxyHelper.newInstance( InterferenceCriteria.class );
        antennaGain = SeamcatFactory.antennaGain().getPeakGainAntenna();
       
        antennaPointing = ProxyHelper.newInstance( RXAntennaPointingUI.class );
    }

    public AntennaGainConfiguration getAntennaGain() {
        return antennaGain;
    }

    public void setAntennaGain(AntennaGainConfiguration value) {
        antennaGain = value;
    }

    public void setAntennaPointing( RXAntennaPointingUI antennaPointing ) {
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
    public Function getIntermodulationRejection() {
        return receptionCharacteristics.intermodulation_rejection().getFunction();
    }

    @Override
    public boolean isIntermodulationRejectionOption() {
        return receptionCharacteristics.intermodulation_rejection().isRelevant();
    }

    @Override
	public double getExtendedProtectionRatio() {
		return interferenceCriteria.extended_protection_ratio();
	}

    @Override
	public double getInterferenceToNoiseRatio() {
		return interferenceCriteria.interference_to_noise_ratio();
	}

    @Override
	public double getNoiseAugmentation() {
		return interferenceCriteria.noise_augmentation();
	}

    @Override
    public double getProtectionRatio() {
        return interferenceCriteria.protection_ratio();
    }

    @Override
	public double getSensitivity() {
		return receptionCharacteristics.sensitivity();
	}

  

	public Function getOverloadingMask() {
		return receptionCharacteristics.overloading_mask();
	}

	public Function getReceiverFilter() {
		return receptionCharacteristics.receiver_filter();
	}

    @Override
    public Distribution getNoiseFloor() {
        return receptionCharacteristics.noiseFloor();
    }

    @Override
    public boolean isUsingPowerControlThreshold() {
        return receptionCharacteristics.receivePower().isRelevant();
    }

    @Override
    public double getPowerControlThreshold() {
        return receptionCharacteristics.receivePower().getValue();
    }

    @Override
    public boolean isUsingOverloading() {
        return receptionCharacteristics.use_receiver_overloading();
    }

    @Override
    public BlockingMask getBlockingMask() {
        return receptionCharacteristics.blockingMask();
    }

    @Override
    public double getBandwidth() {
        return receptionCharacteristics.reception_bandwith() / 1000;
    }

    @Override
    public BlockingAttenuationMode getBlockingAttenuationMode() {
        return receptionCharacteristics.blockingAttenuationMode();
    }

    @Override
    public List<LocalEnvironment> getLocalEnvironments() {
        return localEnvironments;
    }

    public void setLocalEnvironments( List<LocalEnvironment> localEnvironments ) {
        this.localEnvironments = localEnvironments;
    }

    public void setReceptionCharacteristics(ReceptionCharacteristics receptionCharacteristics) {
        this.receptionCharacteristics = receptionCharacteristics;
    }

    @Override
    public Distribution getHeight() {
        return height;
    }

    public void setHeight( Distribution height ) {
        this.height = height;
    }

    @Override
    public Function getPseudoBlockingMask() {
        Function maskOrigin = getBlockingMask();
        double offset = 0;
        if (getBlockingAttenuationMode() == MODE_SENSITIVITY) {
            offset = getExtendedProtectionRatio() - getSensitivity() - getInterferenceToNoiseRatio();
        } else if (getBlockingAttenuationMode() == PROTECTION_RATIO) {
            offset = getExtendedProtectionRatio() + getNoiseAugmentation() - getInterferenceToNoiseRatio();
        }
        return maskOrigin.offset(offset);
    }

	
}