package org.seamcat.model.distributions;

/**
 * This class is aimed for the representation of a constant distribution.
 *<p></p>
 * In this case a trial performed on this variable always returns the same constant value (may be integer or floating point)
 */
public interface ConstantDistribution extends Distribution {

    double getConstant();
}
