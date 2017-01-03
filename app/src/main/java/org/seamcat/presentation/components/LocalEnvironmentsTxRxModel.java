package org.seamcat.presentation.components;

import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.scenario.MutableLocalEnvironment;

import java.util.ArrayList;
import java.util.List;

public class LocalEnvironmentsTxRxModel {

    private List<LocalEnvironment> receiverEnvs;
    private List<LocalEnvironment> transmitterEnvs;

    public LocalEnvironmentsTxRxModel() {
        receiverEnvs = new ArrayList<LocalEnvironment>();
        receiverEnvs.add( new MutableLocalEnvironment());
        transmitterEnvs = new ArrayList<LocalEnvironment>();
        transmitterEnvs.add( new MutableLocalEnvironment());
    }

    public LocalEnvironmentsTxRxModel( List<LocalEnvironment> receiverEnvs, List<LocalEnvironment> transmitterEnvs) {
        this.receiverEnvs = receiverEnvs;
        this.transmitterEnvs = transmitterEnvs;
    }

    public List<LocalEnvironment> getReceiverEnvs() {
        return receiverEnvs;
    }

    public List<LocalEnvironment> getTransmitterEnvs() {
        return transmitterEnvs;
    }

}
