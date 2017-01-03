package org.seamcat.simulation.result;

import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.LocalEnvironmentResult;

public class MutableAntennaResult implements AntennaResult {

    private double antGain, antAzimuth, antElevation, elevationCompensation, tilt, antennaHeight;
    private Point2D position;
    private LocalEnvironmentResult localEnv;

    public MutableAntennaResult() {
        position = new Point2D(0,0);
        localEnv = new MutableLocalEnvironmentResult();
    }

    @Override
    public double getGain() {
        return antGain;
    }

    public MutableAntennaResult setGain(double antennaGain ) {
        this.antGain = antennaGain;
        return this;
    }

    @Override
    public double getAzimuth() {
        return antAzimuth;
    }

    public MutableAntennaResult setAzimuth( double antennaAzimuth ) {
        this.antAzimuth = antennaAzimuth;
        return this;
    }

    @Override
    public double getElevation() {
        return antElevation;
    }

    public MutableAntennaResult setElevation( double antennaElevation ) {
        this.antElevation = antennaElevation;
        return this;
    }

    @Override
    public double getElevationCompensation() {
        return elevationCompensation;
    }

    public MutableAntennaResult setElevationCompensation(double elevationCompensation ) {
        this.elevationCompensation = elevationCompensation;
        return this;
    }

    @Override
    public double getTilt() {
        return tilt;
    }

    public MutableAntennaResult setTilt( double tilt ) {
        this.tilt = tilt;
        return this;
    }

    @Override
    public double getHeight() {
        return antennaHeight;
    }

    public MutableAntennaResult setHeight( double antennaHeight ) {
        this.antennaHeight = antennaHeight;
        return this;
    }

    @Override
    public Point2D getPosition() {
        return position;
    }

    public MutableAntennaResult setPosition( Point2D position ) {
        this.position = position;
        return this;
    }

    @Override
    public LocalEnvironmentResult getLocalEnvironment() {
        return localEnv;
    }

    public MutableAntennaResult setLocalEnvironment( LocalEnvironmentResult localEnvironmentResult ) {
        this.localEnv = localEnvironmentResult;
        return this;
    }
}
