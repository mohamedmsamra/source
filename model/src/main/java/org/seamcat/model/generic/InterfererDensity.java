package org.seamcat.model.generic;

import org.seamcat.model.functions.Function;

public interface InterfererDensity {

    double getDensityTx();

    double getProbabilityOfTransmission();

    Function getActivity();

    double getHourOfDay();

}
