package org.seamcat.simulation.result;

import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.SimulationElement;

public abstract class MutableSimulationElement implements SimulationElement {

    private Point2D position;
    private double antennaHeight;
    private double antennaTilt;
    private double externalInterferenceBlocking;
    private double externalInterferenceUnwanted;
    private double interSystemInterference;
    private double totalInterference;

    public MutableSimulationElement() {
        externalInterferenceBlocking = -1000;
        externalInterferenceUnwanted = -1000;
        antennaTilt = 0;
    }

    @Override
    public Point2D getPosition() {
        return position;
    }

    @Override
    public double getAntennaHeight() {
        return antennaHeight;
    }

    @Override
    public double getAntennaTilt() {
        return antennaTilt;
    }

    @Override
    public double getExternalInterferenceBlocking() {
        return externalInterferenceBlocking;
    }

    @Override
    public double getExternalInterferenceUnwanted() {
        return externalInterferenceUnwanted;
    }

    @Override
    public double getInterSystemInterference() {
        return interSystemInterference;
    }

    @Override
    public double getTotalInterference() {
        return totalInterference;
    }

    @Override
    public abstract double getFrequency();

    @Override
    public abstract double getReferenceBandwidth();

    public void setPosition(Point2D position) {
        this.position = position;
    }

    public void setAntennaHeight(double antennaHeight) {
        this.antennaHeight = antennaHeight;
    }

    public void setAntennaTilt(double antennaTilt) {
        this.antennaTilt = antennaTilt;
    }

    public void setExternalInterferenceBlocking(double externalInterferenceBlocking) {
        this.externalInterferenceBlocking = externalInterferenceBlocking;
    }

    public void setExternalInterferenceUnwanted(double externalInterferenceUnwanted) {
        this.externalInterferenceUnwanted = externalInterferenceUnwanted;
    }

    public void setInterSystemInterference(double interSystemInterference) {
        this.interSystemInterference = interSystemInterference;
    }

    public void setTotalInterference(double totalInterference) {
        this.totalInterference = totalInterference;
    }

    @Override
    public double getExternalInterference() {
        return Mathematics.linear2dB(Math.pow(10, externalInterferenceUnwanted / 10) + Math.pow(10, externalInterferenceBlocking / 10));
    }

    public abstract double calculateAntennaGainTo(double horizontalAngle, double verticalAngle);

}
