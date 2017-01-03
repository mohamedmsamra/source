package org.seamcat.model.types.result;

import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.result.AntennaResult;
import org.seamcat.model.simulation.result.LocalEnvironmentResult;

public class AntennaResultImpl implements AntennaResult {

    private double antGain, antAzimuth, antElevation, elevationCompensation, tilt, antennaHeight;
    private Point2D position;
    private LocalEnvironmentResult localEnv;

    public AntennaResultImpl() {
        position = new Point2D(0,0);
        localEnv = new LocalEnvironmentResultImpl();
    }

    @Override
    public double getGain() {
        return antGain;
    }

    public void setGain(double antennaGain ) {
        this.antGain = antennaGain;
    }

    @Override
    public double getAzimuth() {
        return antAzimuth;
    }

    public void setAzimuth( double antennaAzimuth ) {
        this.antAzimuth = antennaAzimuth;
    }

    @Override
    public double getElevation() {
        return antElevation;
    }

    public void setElevation( double antennaElevation ) {
        this.antElevation = antennaElevation;
    }

    @Override
    public double getElevationCompensation() {
        return elevationCompensation;
    }

    public void setElevationCompensation(double elevationCompensation ) {
        this.elevationCompensation = elevationCompensation;
    }

    @Override
    public double getTilt() {
        return tilt;
    }

    public void setTilt( double tilt ) {
        this.tilt = tilt;
    }

    @Override
    public double getHeight() {
        return antennaHeight;
    }

    public void setHeight( double antennaHeight ) {
        this.antennaHeight = antennaHeight;
    }

    @Override
    public Point2D getPosition() {
        return position;
    }

    public void setPosition( Point2D position ) {
        this.position = position;
    }

    @Override
    public LocalEnvironmentResult getLocalEnvironment() {
        return localEnv;
    }

    public void setLocalEnvironment( LocalEnvironmentResult localEnvironmentResult ) {
        this.localEnv = localEnvironmentResult;
    }

}
