package org.seamcat.model.generic;

import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.MaskFunction;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.plugin.OptionalFunction;
import org.seamcat.model.plugin.OptionalMaskFunction;
import org.seamcat.model.plugin.antenna.HorizontalVerticalInput;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.model.types.result.DescriptionImpl;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.scenario.MutableLocalEnvironment;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.seamcat.model.factory.Factory.*;
import static org.seamcat.model.factory.SeamcatFactory.antennaGain;

public class Defaults {

    public static AntennaGain<HorizontalVerticalInput> defaultAntennaGain() {
        HorizontalVerticalInput prototype = prototype(HorizontalVerticalInput.class);
        List<Point2D> hor = asList(new Point2D(0, 0),new Point2D(360, 0));
        when(prototype.horizontal()).thenReturn(new OptionalFunction(false, functionFactory().discreteFunction(hor)));
        List<Point2D> ver = asList(new Point2D(-90, 0), new Point2D(90, 0));
        when(prototype.vertical()).thenReturn(new OptionalFunction(false, functionFactory().discreteFunction(ver)));
        AntennaGainConfiguration<HorizontalVerticalInput> antenna = antennaGain().getHorizontalVerticalAntenna(build(prototype), 20);

        antenna.setDescription( new DescriptionImpl("DEFAULT_ANT", antenna.description().description()));
        return antenna;
    }

    public static EmissionMask defaultEmissionMask() {
        List<Point2D> points = new ArrayList<Point2D>();
        List<Double> mask = new ArrayList<Double>();
    
        points.add(new Point2D(-10, -0));
        mask.add(20000.0);
        points.add(new Point2D(10, -0));
        mask.add(20000.0);
      
        return functionFactory().emissionMask(points, mask);
    }

    public static OptionalMaskFunction defaultEmissionFloor() {
        List<Point2D> points = new ArrayList<Point2D>();
        List<Double> mask = new ArrayList<Double>();
        points.add(new Point2D(-10, -60));
        mask.add(20.0);
        points.add(new Point2D(10, -60));
        mask.add(20.0);

        return new OptionalMaskFunction(false, functionFactory().emissionMask(points, mask));
    }


    public static MaskFunction defaultEirpMax() {
        List<Point2D> points = new ArrayList<Point2D>();
        List<Double> mask = new ArrayList<Double>();
        points.add(new Point2D(-100, 0)); mask.add(1250.0);
        points.add(new Point2D(  -1, 0)); mask.add(1250.0);
        points.add(new Point2D(   1, 0)); mask.add(1250.0);
        points.add(new Point2D( 100, 0)); mask.add(1250.0);

        return functionFactory().emissionMask( points, mask );
    }

    public static Function defaultOFDMABitRateMapping() {
        List<Point2D> points = new ArrayList<Point2D>();
        points.add(new Point2D(-12.0, 0.0));
        points.add(new Point2D(-11.0,0.0));
        points.add(new Point2D(-10.0,0.06));
        points.add(new Point2D(-9.0,0.07));
        points.add(new Point2D(-8.0,0.08));
        points.add(new Point2D(-7.0,0.1));
        points.add(new Point2D(-6.0,0.13));
        points.add(new Point2D(-5.0,0.16));
        points.add(new Point2D(-4.0,0.19));
        points.add(new Point2D(-3.0,0.23));
        points.add(new Point2D(-2.0,0.28));
        points.add(new Point2D(-1.0,0.34));
        points.add(new Point2D(0.0,0.4));
        points.add(new Point2D(1.0,0.47));
        points.add(new Point2D(2.0,0.55));
        points.add(new Point2D(3.0,0.63));
        points.add(new Point2D(4.0,0.72));
        points.add(new Point2D(5.0,0.82));
        points.add(new Point2D(6.0,0.93));
        points.add(new Point2D(7.0,1.04));
        points.add(new Point2D(8.0,1.15));
        points.add(new Point2D(9.0,1.26));
        points.add(new Point2D(10.0,1.38));
        points.add(new Point2D(11.0,1.51));
        points.add(new Point2D(12.0,1.63));
        points.add(new Point2D(13.0,1.76));
        points.add(new Point2D(14.0,1.88));
        points.add(new Point2D(15.0,2.0));
        points.add(new Point2D(16.0,2.0));
        points.add(new Point2D(17.0,2.0));
        points.add(new Point2D(18.0,2.0));
        points.add(new Point2D(19.0,2.0));
        points.add(new Point2D(20.0,2.0));
        points.add(new Point2D(21.0,2.0));
        points.add(new Point2D(22.0,2.0));
        points.add(new Point2D(23.0,2.0));
        points.add(new Point2D(24.0,2.0));
        points.add(new Point2D(25.0,2.0));

        return functionFactory().discreteFunction(points);
    }

    public static Distribution defaultMobility() {
        List<Point2D> points = new ArrayList<Point2D>();
        points.add(new Point2D(0,0.25));
        points.add(new Point2D(3,0.5));
        points.add(new Point2D(30,0.75));
        points.add(new Point2D(100,1.0));
        return Factory.distributionFactory().getUserDefinedStair(functionFactory().discreteFunction(points));
    }

    public static List<LocalEnvironment> defaultEnvironment() {
        List<LocalEnvironment> result = new ArrayList<>();
        MutableLocalEnvironment env = new MutableLocalEnvironment();
        env.setWallLoss( 10 );
        env.setWallLossStdDev( 5 );
        result.add(env);
        return result;
    }
}
