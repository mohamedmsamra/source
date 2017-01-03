package org.seamcat.simulation.generic;

public class IRSSOverloadingValue {

    private double frequency;
    private double iRSSo;

    public IRSSOverloadingValue(double frequency, double iRSSo) {
        this.frequency = frequency;
        this.iRSSo = iRSSo;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getiRSSo() {
        return iRSSo;
    }
}
