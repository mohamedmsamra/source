package org.seamcat.model.types;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.Function;

import java.util.List;

public interface Receiver {

    BlockingMask getBlockingMask();

    double getBandwidth();

    List<LocalEnvironment> getLocalEnvironments();

    AntennaGain getAntennaGain();

    Distribution getHeight();

    Function getPseudoBlockingMask();
}
