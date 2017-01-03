package org.seamcat.model.types;

import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.MaskFunction;

public interface SensingLink extends Link {

    Function getDetectionThreshold();

    double getProbabilityOfFailure();

    double getBandwidth();

    MaskFunction getEIRPInBlockMask();

}
