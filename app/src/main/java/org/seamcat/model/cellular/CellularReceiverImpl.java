package org.seamcat.model.cellular;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.systems.ofdma.ReceiverSettings;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.plugin.AntennaGainConfiguration;

import java.util.ArrayList;
import java.util.List;

public class CellularReceiverImpl implements CellularReceiver {

    private ReceiverSettings ui;
    private double noiseFigure;
    private BlockingMask blockingMask;
    private final double bandwidth;
    private List<LocalEnvironment> environments;
    private AntennaGainConfiguration antennaGain;
    private Distribution height;

    public CellularReceiverImpl( ReceiverSettings ui, double noiseFigure, BlockingMask blockingMask,
                                 double bandwidth, List<LocalEnvironment> environments,
                                 AntennaGainConfiguration antennaGain, Distribution height) {
        this.ui = ui;
        this.noiseFigure = noiseFigure;
        this.blockingMask = blockingMask;
        this.bandwidth = bandwidth;
        this.environments = environments;
        this.antennaGain = antennaGain;
        this.height = height;
    }


    @Override
    public double standardDesensitisation() {
        return ui.standardDesensitisation();
    }

    @Override
    public double targetINR() {
        return ui.targetINR();
    }

    @Override
    public BlockingMask getBlockingMask() {
        return blockingMask;
    }

    @Override
    public double getBandwidth() {
        return bandwidth;
    }

    @Override
    public List<LocalEnvironment> getLocalEnvironments() {
        return environments;
    }

    @Override
    public AntennaGainConfiguration getAntennaGain() {
        return antennaGain;
    }

    public void setAntennaGain( AntennaGainConfiguration antennaGain ) {
        this.antennaGain = antennaGain;
    }

    @Override
    public Distribution getHeight() {
        return height;
    }

    @Override
    public Function getPseudoBlockingMask() {
        if ( blockingMask.isConstant() ) {
            return Factory.functionFactory().constantFunction(maskValue(blockingMask.getConstant()));
        } else {
            List<Point2D> pseudoMaskPoints = new ArrayList<>();
            for (Point2D maskPoint : blockingMask.getPoints()) {
                pseudoMaskPoints.add( new Point2D( maskPoint.getX(), maskValue(maskPoint.getY())));
            }
            return Factory.functionFactory().discreteFunction(pseudoMaskPoints);
        }

    }

    private double maskValue( double IoobStandard ) {
        if (IoobStandard < 0) {
            // convert negative values
            double IoobTarget = IoobStandard - standardDesensitisation() + 10 * Math.log10(Math.pow(10, targetINR() / 10) + 1);

            double k = 1.38e-23; // Boltzmann constant
            double T = 293;
            double B = getBandwidth();
            double F = noiseFigure;

            double ktTBF = 10 * Math.log10(k * T * B * 1000) + F + 30;

            return IoobTarget - ktTBF - targetINR();
            //TODO B is in Hz (given in kHz), kTB is 10*log10 , F increases the noise floor, +30 to convert to dBm
        } else {
            return IoobStandard;
        }
    }
}
