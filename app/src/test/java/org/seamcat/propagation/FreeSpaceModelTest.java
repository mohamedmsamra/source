package org.seamcat.propagation;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.function.TestUtil;
import org.seamcat.model.factory.TestFactory;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.plugin.propagation.FreespaceInput;
import org.seamcat.model.propagation.FreeSpacePropagationModel;
import org.seamcat.simulation.result.MutableLinkResult;


public class FreeSpaceModelTest {
	TestUtil util;
	
	@Before
	public void setup() {
		TestFactory.initialize();
        util = new TestUtil( 0.01 );
	}
	
	@Test
	public void testFreeSpaceFrequency() {
		FreeSpacePropagationModel model = new FreeSpacePropagationModel();

        MutableLinkResult result = new MutableLinkResult();
        result.setTxRxDistance( 10 );
        result.txAntenna().setHeight(30);
		result.rxAntenna().setHeight(30);

		double[] rFreq;
		rFreq = new double [21];
		//rFreq[0] = 3;
		rFreq[0] = 152.85;
		rFreq[1] = 152.85;
		rFreq[2] = 302.7;
		rFreq[3] = 452.55;
		rFreq[4] = 602.4;
		rFreq[5] = 752.25;
		rFreq[6] = 902.1;
		rFreq[7] = 1051.95;
		rFreq[8] = 1201.8;
		rFreq[9] = 1351.65;
		rFreq[10] = 1501.5;
		rFreq[11] = 1651.35;
		rFreq[12] = 1801.2;
		rFreq[13] = 1951.05;
		rFreq[14] = 2100.9;
		rFreq[15] = 2250.75;
		rFreq[16] = 2400.6;
		rFreq[17] = 2550.45;
		rFreq[18] = 2700.3;
		rFreq[19] = 2850.15;
		rFreq[20] = 3000;
		

		double[] expectedResultFreq;
		expectedResultFreq = new double [21];
		//expectedResultFreq[0] = 61.982;
		expectedResultFreq[0] = 96.125;
		expectedResultFreq[1] = 96.125;
		expectedResultFreq[2] = 102.06;
		expectedResultFreq[3] = 105.553;
		expectedResultFreq[4] = 108.038;
		expectedResultFreq[5] = 109.967;
		expectedResultFreq[6] = 111.545;
		expectedResultFreq[7] = 112.88;
		expectedResultFreq[8] = 114.037;
		expectedResultFreq[9] = 115.057;
		expectedResultFreq[10] = 115.971;
		expectedResultFreq[11] = 116.797;
		expectedResultFreq[12] = 117.551;
		expectedResultFreq[13] = 118.245;
		expectedResultFreq[14] = 118.888;
		expectedResultFreq[15] = 119.487;
		expectedResultFreq[16] = 120.046;
		expectedResultFreq[17] = 120.572;
		expectedResultFreq[18] = 121.068;
		expectedResultFreq[19] = 121.537;
		expectedResultFreq[20] = 121.982;


        FreespaceInput input = SeamcatFactory.propagation().getFreeSpace().getModel();
        for (int i = 0 ; i < rFreq.length ; i++){
            result.setFrequency(rFreq[i]);
            util.assertDoubleEquals(expectedResultFreq[i], model.evaluate(result, false, input));
		}
	}

	@Test
	public void testFreeSpaceDistance() {

		FreeSpacePropagationModel model = new FreeSpacePropagationModel();

        MutableLinkResult result = new MutableLinkResult();
        result.setFrequency(300);
		result.txAntenna().setHeight(30);
		result.rxAntenna().setHeight(30);

		double[] rDist;
		rDist = new double [21];
		rDist[0] = 0.1;
		rDist[1] = 0.595;
		rDist[2] = 1.09;
		rDist[3] = 1.585;
		rDist[4] = 2.08;
		rDist[5] = 2.575;
		rDist[6] = 3.07;
		rDist[7] = 3.565;
		rDist[8] = 4.06;
		rDist[9] = 4.555;
		rDist[10] = 5.05;
		rDist[11] = 5.545;
		rDist[12] = 6.04;
		rDist[13] = 6.535;
		rDist[14] = 7.03;
		rDist[15] = 7.525;
		rDist[16] = 8.02;
		rDist[17] = 8.515;
		rDist[18] = 9.01;
		rDist[19] = 9.505;
		rDist[20] = 10;

		
		double[] expectedResultDist;
		expectedResultDist = new double [21];
		expectedResultDist[0] = 61.982;
		expectedResultDist[1] = 77.473;
		expectedResultDist[2] = 82.731;
		expectedResultDist[3] = 85.983;
		expectedResultDist[4] = 88.344;
		expectedResultDist[5] = 90.198;
		expectedResultDist[6] = 91.725;
		expectedResultDist[7] = 93.024;
		expectedResultDist[8] = 94.153;
		expectedResultDist[9] = 95.152;
		expectedResultDist[10] = 96.048;
		expectedResultDist[11] = 96.86;
		expectedResultDist[12] = 97.603;
		expectedResultDist[13] = 98.287;
		expectedResultDist[14] = 98.922;
		expectedResultDist[15] = 99.513;
		expectedResultDist[16] = 100.066;
		expectedResultDist[17] = 100.586;
		expectedResultDist[18] = 101.077;
		expectedResultDist[19] = 101.541;
		expectedResultDist[20] = 101.982;

        FreespaceInput input = SeamcatFactory.propagation().getFreeSpace().getModel();
        for (int i = 0 ; i < rDist.length ; i++){
            result.setTxRxDistance(rDist[i]);
            util.assertDoubleEquals(expectedResultDist[i], model.evaluate(result, false, input));
		}
	}

	@Test
	public void testFreeSpaceHeight() {

		FreeSpacePropagationModel model = new FreeSpacePropagationModel();

        MutableLinkResult result = new MutableLinkResult();
        result.setFrequency(300);
		result.setTxRxDistance(10);
		result.rxAntenna().setHeight(30);

		double[] rHeight;
		rHeight = new double [11];
		rHeight[0] = 1;
		rHeight[1] = 100;
		rHeight[2] = 199;
		rHeight[3] = 298;
		rHeight[4] = 397;
		rHeight[5] = 496;
		rHeight[6] = 595;
		rHeight[7] = 694;
		rHeight[8] = 793;
		rHeight[9] = 892;
		rHeight[10] = 991;

		
		double[] expectedResultHeight;
		expectedResultHeight = new double [11];
		expectedResultHeight[0] = 101.982;
		expectedResultHeight[1] = 101.983;
		expectedResultHeight[2] = 101.984;
		expectedResultHeight[3] = 101.986;
		expectedResultHeight[4] = 101.988;
		expectedResultHeight[5] = 101.992;
		expectedResultHeight[6] = 101.996;
		expectedResultHeight[7] = 102.002;
		expectedResultHeight[8] = 102.008;
		expectedResultHeight[9] = 102.015;
		expectedResultHeight[10] = 102.022;

        FreespaceInput input = SeamcatFactory.propagation().getFreeSpace().getModel();
        for (int i = 0 ; i < rHeight.length ; i++){
            result.txAntenna().setHeight(rHeight[i]);
            util.assertDoubleEquals(expectedResultHeight[i], model.evaluate(result, false, input));
		}
	}
}
