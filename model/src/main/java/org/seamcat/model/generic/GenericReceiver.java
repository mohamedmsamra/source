package org.seamcat.model.generic;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Function;
import org.seamcat.model.types.Receiver;

public interface GenericReceiver extends Receiver {

    enum BlockingAttenuationMode {
        USER_DEFINED("User Defined"),
        PROTECTION_RATIO("Protection Ratio"),
        MODE_SENSITIVITY("Sensitivity");

        private final String name;

        BlockingAttenuationMode(String name){
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    AntennaPointing getAntennaPointing();

    Distribution getNoiseFloor();

    BlockingAttenuationMode getBlockingAttenuationMode();



    Function getIntermodulationRejection();

    boolean isIntermodulationRejectionOption();

    boolean isUsingPowerControlThreshold();

    double getPowerControlThreshold();

    double getSensitivity();
    
    //////////////////
    //void setSensitivity(double sensitivity);

    boolean isUsingOverloading();

    Function getOverloadingMask();

    Function getReceiverFilter();

    /* Interference Criteria */
    double getProtectionRatio();

    double getExtendedProtectionRatio();

    double getNoiseAugmentation();

    double getInterferenceToNoiseRatio();
}
