package org.seamcat.propagation;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.function.TestUtil;
import org.seamcat.model.factory.TestFactory;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.plugin.propagation.P452ver14Input;
import org.seamcat.model.propagation.P452ver14PropagationModel;
import org.seamcat.simulation.result.MutableLinkResult;


public class P452Test {
	TestUtil util;
	
	@Before
	public void setup() {
        TestFactory.initialize();

		util = new TestUtil( 0.0001 );
	}

	@Test
	public void testevaluate() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();
        MutableLinkResult linkResult = new MutableLinkResult();
        linkResult.setFrequency(3600);
        linkResult.setTxRxDistance(0.001243740356253809);
        linkResult.txAntenna().setHeight(30);
        linkResult.rxAntenna().setHeight(1.5);

        P452ver14Input input = SeamcatFactory.propagation().getITU_R_P_452_14().getModel();
        double rResult = calculator.evaluate(linkResult, true, input );
		
		util.assertDoubleEquals(45.52849, rResult );
	}

	@Test
	public void testeffectiveEarthRadiusCalculation() {
		P452ver14PropagationModel calculator = new P452ver14PropagationModel();
		double rRefrIndexGradient = 55;

		double rResult = calculator.medianEffectiveEarthRadiusCalculation(rRefrIndexGradient);
		
		util.assertDoubleEquals(9806.343137254902, rResult );
	}
	
	@Test
	public void testcalculateDistMedianDiffractionLoss() {
        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double[] rDisti;
		rDisti = new double [402];
		rDisti[0]=0;
		for (int i=1; i < rDisti.length ; i++){
			rDisti[i] = rDisti[i-1] + 0.1;
		}
		double rDist = rDisti[rDisti.length-1];
		double rhrs = 30;
		double rhts = 30;
		double rFreq = 10; 
		double rhi = 0;
		double rAe = 9806.343137254902;
		
		double rResult = calculator.calculateDistMedianDiffractionLoss(rDist,rDisti,rhrs,rhts,rFreq,rhi,rAe);
	
		util.assertDoubleEquals(20, rResult);
	}


	@Test
	public void testcalculateMedianDiffractionLoss() {
        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double[] rDisti;
		rDisti = new double [402];
		rDisti[0]=0;
		for (int i=1; i < rDisti.length ; i++){
			rDisti[i] = rDisti[i-1] + 0.1;
		}
		double rDist = rDisti[rDisti.length-1];
		double rhrs = 30;
		double rhts = 30;
		double rFreq = 10; 
		double rhi = 0;
		double rAe = 9806.343137254902;
		
		double rResult = calculator.calculateMedianDiffractionLoss(rDist, rDisti, rhrs, rhts, rFreq, rhi, rAe);
	
		util.assertDoubleEquals(0.1782, rResult);
	}
	@Test
	public void testcalculateOverallPrediction() {
        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double[] rDisti;
		rDisti = new double [11];
		rDisti[0]=0;
		for (int i=1; i < rDisti.length ; i++){
			rDisti[i] = rDisti[i-1] + 0.1;
		}
		double rDist = rDisti[rDisti.length-1];
		double rhrs = 30;
		double rhts = 30;
		double rFreq = 10000; // in MHz
		double rhi = 0;
		double rAe = 9806.343137254902;
		double rLbfsg = 132.6522;
		double rLbd = 130.3547;
		double rLba = 133.1990;
		double rLbs = 174.5000;
		double rAht = 0;
		double rAhr = 0;
		double rDistt = 5;
		double rDistr = 5;
		double rP = 2;
		double rThetat = -0.510;
		double rThetar = -0.510;
		double rW = 0;
		double rBeta0 = 9.6509;
		double rLB0p = 130.3547;
		double rLdp = 0;
		double rLb0Beta = 131.3128;
		double rLbd50 = 132.6522;

		
		double rResult = calculator.calculateOverallPrediction(rLbfsg, rLbd, rLba, rLbs, rAht, rAhr, rBeta0, rDisti, rDist, rDistt, rDistr, 
				rP, rAe, rThetat, rThetar, rW, rhrs, rhts, rFreq / 1000, rhi, rLB0p, rLdp, rLb0Beta, rLbd50);
	
		util.assertDoubleEquals(130.3547, rResult);
	}

	@Test
	public void testangularDistance() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double rDist = 1;
		double rThetat = -0.0510;
		double rThetar = -0.0510;
		double rAe = 9806.343137254902;
		
		double rResult = calculator.angularDistance(rDist,rAe,rThetat,rThetar);
	
		util.assertDoubleEquals(-2.518777861867938E-5, rResult);
	}

	@Test
	public void testtroposhericScatter() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double rFreq = 10;
		double rDist = 1;
		double rHTx = 30;
		double rHRx = 30;
		double rTimePercentage = 2;
		double rGRx = 30;
		double rGTx = 30;
		double rSeaLevelSurfaceRefractivity = 320;
		double rThetat = -0.051;
		double rThetar = -0.051;
		double rAe = 9806.343137254902;
		double rRho = 3;
		double rPressure = 1013;
		double rTemperature = 10;
		

		double rResult = calculator.troposhericScatter(rFreq, rDist, rHTx, rHRx, 
				rTimePercentage, rGRx, rGTx, rSeaLevelSurfaceRefractivity, rThetat, 
				rThetar, rAe, rRho, rPressure, rTemperature);

		util.assertDoubleEquals(154.4029, rResult);
	}
	
	@Test
	public void testattenuationDueToWaterVapour() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double rFreq = 10;
		double tempPressureInput  = 1013;
		double tempTemperatureInput = 10;
		double tempRhoInput = 3;
		
		double rPressure = tempPressureInput / 1013;
		double rTemperature = 288/(273 + tempTemperatureInput);
		double rRho = tempRhoInput;
		
		double rResult = calculator.attenuationDueToWaterVapour(rFreq, rPressure, rTemperature, rRho);

		util.assertDoubleEquals(0.0025, rResult);
	}

	@Test
	public void testattenuationDueToDryAir() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double[] rFreq;
		rFreq = new double [7];

		rFreq[0] = 10;
		rFreq[1] = 56;
		rFreq[2] = 61;
		rFreq[3] = 63;
		rFreq[4] = 67;
		rFreq[5] = 121;
		rFreq[6] = 351;
		
		double[] expectedResult;
		expectedResult = new double [7];
		expectedResult[0] = 0.0083;
		expectedResult[1] = 6.8365;
		expectedResult[2] = 15.2398;
		expectedResult[3] = 10.8483;
		expectedResult[4] = 1.0596;
		expectedResult[5] = 0.5512;
		expectedResult[6] = 0;

		double tempPressureInput  = 1013;
		double tempTemperatureInput = 10;
		
		double rPressure = tempPressureInput / 1013;
		double rTemperature = 288/(273 + tempTemperatureInput);
		
		for (int i = 0 ; i < rFreq.length ; i++){
			double rResult = calculator.attenuationDueToDryAir(rFreq[i], rPressure, rTemperature);
			util.assertDoubleEquals(expectedResult[i], rResult);
		}
	}

	@Test
	public void testfunctionG() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double rFreq = 10;
		double rFreqi = 100;

		double rResult = calculator.functionG(rFreq, rFreqi);

		util.assertDoubleEquals(1.6694, rResult);
	}


	@Test
	public void testIfunction() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double[] rInput;
		rInput = new double [8];

		rInput[0] = 0.10;
		rInput[1] = 0.56;
		rInput[2] = 0.61;
		rInput[3] = 0.63;
		rInput[4] = 0.67;
		rInput[5] = 0.121;
		rInput[6] = 0.351;
		rInput[7] = 1;
		
		double[] expectedResult;
		expectedResult = new double [8];
		expectedResult[0] = -1.2817;
		expectedResult[1] = 0.1503;
		expectedResult[2] = 0.2778;
		expectedResult[3] = 0.3299;
		expectedResult[4] = 0.4367;
		expectedResult[5] = -1.1701;
		expectedResult[6] = -0.3822;
		expectedResult[7] = 2.5155;

		for (int i = 0 ; i < rInput.length ; i++){
			double rResult = calculator.Ifunction(rInput[i]);
			util.assertDoubleEquals(expectedResult[i], rResult);
		}
	}

	@Test
	public void testJfunction() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double[] rInput;
		rInput = new double [8];

		rInput[0] = 0.10;
		rInput[1] = 0.56;
		rInput[2] = 0.61;
		rInput[3] = 0.63;
		rInput[4] = 0.67;
		rInput[5] = 0.121;
		rInput[6] = 0.351;
		rInput[7] = 1;
		
		double[] expectedResult;
		expectedResult = new double [8];
		expectedResult[0] = 6.9000;
		expectedResult[1] = 10.7665;
		expectedResult[2] = 11.1573;
		expectedResult[3] =  11.3114;
		expectedResult[4] = 11.6158;
		expectedResult[5] = 7.0824;
		expectedResult[6] = 9.0579;
		expectedResult[7] = 13.9257;

		for (int i = 0 ; i < rInput.length ; i++){
			double rResult = calculator.Jfunction(rInput[i]);
			util.assertDoubleEquals(expectedResult[i], rResult);
		}
	}

	@Test
	public void testFi() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double[] rP;
		rP = new double [6];

		rP[0] = 2.0;
		rP[1] = 50.0;
		rP[2] = 35.0;
		rP[3] = 15.0;
		rP[4] = 45.0;
		rP[5] = 25.0;
		
		double[] rBeta0;
		rBeta0 = new double [6];

		rBeta0[0] = 2.1706;
		rBeta0[1] = 2.1706;
		rBeta0[2] = 10.0;
		rBeta0[3] = 20.0;
		rBeta0[4] = 10.0;
		rBeta0[5] = 10.0;
		
		double[] expectedResult;
		expectedResult = new double [6];
		expectedResult[0] = 1.0;
		expectedResult[1] = 0.0;
		expectedResult[2] = 0.3003;
		expectedResult[3] = 1.0;
		expectedResult[4] = 0.0978;
		expectedResult[5] = 0.5260;

		for (int i = 0 ; i < rP.length ; i++){
			double rResult = calculator.Fi(rP[i], rBeta0[i]);
			util.assertDoubleEquals(expectedResult[i], rResult);
		}
	}
	@Test
	public void testfunctionPhi() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double rPressure = 1;
		double rTemperature = 1.0177;
		double a = 0.0717;
		double b = -1.8132;
		double c = 0.0156;
		double d = -1.6515;

		double rResult = calculator.functionPhi(rPressure,rTemperature,a,b,c,d);

		util.assertDoubleEquals(0.9974, rResult);
	}

	@Test
	public void testtotalFixedCoulingLosses() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double rFreq = 10;
		double rDistt = 0.5;
		double rDistr = 0.5;
		double rThetat = -0.051;
		double rThetar = -0.051;

		double rResult = calculator.totalFixedCoulingLosses(rFreq, rDistt, rDistr, rThetat, rThetar);

		util.assertDoubleEquals(122.45, rResult);
	}
	
	
	@Test
	public void testangularDistanceDependentLoss() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double rFreq = 10;
		double rDistt = 0.5;
		double rDistr = 0.5;
		double rP = 2;
		double rThetat = -0.051;
		double rThetar = -0.051;
		double rHTx = 30;
		double rHRx = 30;
		double rDist = 1;
		double rAe = 9806.343137254902;

		double rResult = calculator.angularDistanceDependentLoss(rFreq, rDist, rDistt, rDistr, rP, rThetat, rThetar, rAe, rHTx, rHRx);

		util.assertDoubleEquals(-10.5363, rResult);
	}
	
	@Test
	public void testductingLayerReflection() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		double rFreq = 10;
		double rP = 2;
		double rHTx = 30;
		double rHRx = 30;
		double rAe = 9806.343137254902;
		double rRho = 7.5;
		double rPressure = 1013;
		double rTemperature = 10;

		double[] rDist;
		double[] rDistt;
		double[] rDistr;
		double[] rThetat;
		double[] rThetar;
		
		rDist = new double [2];
		rDistt = new double [2];
		rDistr = new double [2];
		rThetat = new double [2];
		rThetar = new double [2];
		
		rDist[0] = 10;
		rDistt[0] = 5.0;
		rDistr[0] = 5.0;
		rThetat[0] = -0.51;
		rThetar[0] = -0.51;

		rDist[1] = 1;
		rDistt[1] = 0.5;
		rDistr[1] = 0.5;
		rThetat[1] = -0.051;
		rThetar[1] = -0.051;

		double[] expectedResult;
		expectedResult = new double [2];
		expectedResult[0] = 133.1988;
		expectedResult[1] = 111.9289;

		for (int i = 0 ; i < expectedResult.length ; i++){
			double rResult = calculator.ductingLayerReflection(rFreq, rDistt[i], rDistr[i], rP, 
					rThetat[i], rThetar[i], rHTx, rHRx, rDist[i], rAe, rRho, rPressure, rTemperature);
			util.assertDoubleEquals(expectedResult[i], rResult);
		}
	}
	
	@Test
	public void testdiffractionLossP452() {

        P452ver14PropagationModel calculator = new P452ver14PropagationModel();

		
		double rFreq = 10;
		double rDistt = 5;
		double rDistr = 5;
		double rP = 2;
		double rAe = 9806.343137254902;
		double rABeta = 19113;

		double[] rDisti;
		rDisti = new double [11];
		rDisti[0]=0;
		for (int i=1; i < rDisti.length ; i++){
			rDisti[i] = rDisti[i-1] + 0.1;
		}
		double rDist = rDisti[rDisti.length-1];
		double rLbfsg = 132.6522;
		double rhrs = 30;
		double rhts = 30;
		double rhi = 0;
		double rBeta0 = 9.6509;
		
		double rResult = calculator.diffractionLossP452(rFreq, rDist, rP, rDistt, rDistr, rLbfsg, rDisti, rhrs, rhts, rhi, rAe, rABeta, rBeta0);

		util.assertDoubleEquals(130.3547, rResult);
	}
}
