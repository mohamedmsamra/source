package org.seamcat.model.engines;

/**
 * this method is part of the sensing algorithm
 */
public class EIRPFrequencyValue implements Comparable<EIRPFrequencyValue> {

    private double eirp;
    private double sRSS;
    private final double frequency;

    public EIRPFrequencyValue(double frequency, double eirp, double sRSS) {
        this.eirp = eirp;
        this.frequency = frequency;
        this.sRSS = sRSS;
    }

    @Override
    // (-1 = this is less than)
    public int compareTo(EIRPFrequencyValue o) {
        return o != null ? (eirp < o.eirp ? -1 : (eirp == o.eirp ? 0 : 1)) : 1;
    }

    public double getEirp(){
        return eirp;
    }

    public void setEirp( double eirp ) {
        this.eirp = eirp;
    }

    public double getsRSS() {
        return sRSS;
    }

    public double getFrequency() {
        return frequency;
    }
}