package org.seamcat.propagation;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.function.TestUtil;
import org.seamcat.model.propagation.SDPropagationModel;


public class SDModelTest {
	TestUtil util;
	
	@Before
	public void setup() {
		util = new TestUtil( 0.0001 );
	}

	@Test
	public void testfunction_f() {
		SDPropagationModel calculator = new SDPropagationModel();
		double[] rX;
		rX = new double [2];
		rX[0] = 1.1;
		rX[1] = 2.1;

		double[] expectedResult;
		expectedResult = new double [2];
		expectedResult[0] = -7.298397406;
		expectedResult[1] = -25.63778071;

		for (int i = 0 ; i < rX.length ; i++){
			double rResult = calculator.f(rX[i]);
			util.assertDoubleEquals(expectedResult[i], rResult);
		}
	}
	
	//oxygenLinearLoss: not tested as only used in gazLoss. gazloss not used

	@Test
	public void testfunction_EffectiveEarthRadiusCalculation() {
        SDPropagationModel calculator = new SDPropagationModel();
		double rRefrIndexGradient = 40;
		double rRefrLayerProb = 1;
		
		double[] rTimePercentage;
		rTimePercentage = new double [3];
		rTimePercentage[0] = 35;
		rTimePercentage[1] = 50;
		rTimePercentage[2] = 75;
		
		double[] expectedResult;
		expectedResult = new double [3];
		expectedResult[0] = 10686.84407;
		expectedResult[1] = 8563.24022;
		expectedResult[2] = 8549.119658;
		
		for (int i = 0 ; i < rTimePercentage.length ; i++){
			double rResult = calculator.EffectiveEarthRadiusCalculation(rRefrIndexGradient, rRefrLayerProb, rTimePercentage[i]);
			util.assertDoubleEquals(expectedResult[i], rResult);
		}
	}

	@Test
	public void testfunction_delta() {
        SDPropagationModel calculator = new SDPropagationModel();

		double rY = 3.2837;
		double rBeta = 2.3435;
		double expectedResult = 0.269972496;

		double rResult = calculator.delta(rY, rBeta);
		util.assertDoubleEquals(expectedResult, rResult);
		
	}
	
	@Test
	public void testfunction_g() {
        SDPropagationModel calculator = new SDPropagationModel();

		double rBeta = 2.3435;
		calculator.setEarthSurfaceAdmittance(0.15);
		
		double[] rY;
		rY = new double [4];
		rY[0] = 3.2837;
		rY[1] = 0.7;
		rY[2] = 0.3;
		rY[3] = 0.003;
		
		double[] expectedResult;
		expectedResult = new double [4];
		expectedResult[0] = 33.10310149;
		expectedResult[1] = 6.369228993;
		expectedResult[2] = -4.389219889;
		expectedResult[3] = -14.47817482;
		
		for (int i = 0 ; i < rY.length ; i++){
			double rResult = calculator.g(rY[i], rBeta * rY[i]);
			util.assertDoubleEquals(expectedResult[i], rResult);
		}
	}

	@Test
	public void testfunction_diffractionLoss() {
        SDPropagationModel calculator = new SDPropagationModel();

		double[] rFreq;//in MHz
		rFreq = new double [2];
		rFreq[0] = 19;
		rFreq[1] = 3000;
		
		double rHTx = 20; // in m
		double rHRx = 20; // in m
		double rTimePercentage = 50; // in %

		calculator.setEarthSurfaceAdmittance(0.15);
		calculator.setRefrIndexGradient(40);
		calculator.setRefrLayerProb(1);
		
		double[] rDist;
		rDist = new double [4];
		rDist[0] = 1;
		rDist[1] = 10;
		rDist[2] = 50;
		rDist[3] = 100;
		
		double[] expectedResult;
		expectedResult = new double [8];
		expectedResult[0] = 0;
		expectedResult[1] = 15.96574989;
		expectedResult[2] = 32.72950555;
		expectedResult[3] = 43.96880833;
		expectedResult[4] = 0;
		expectedResult[5] = 0;
		expectedResult[6] = 37.61073571;
		expectedResult[7] = 103.655234;
		
		int k = 0;
		for (int j = 0 ; j < rFreq.length ; j++){
			for (int i = 0 ; i < rDist.length ; i++){
				double rResult = calculator.diffractionLoss(rFreq[j], rDist[i], rHTx, rHRx, rTimePercentage);
				util.assertDoubleEquals(expectedResult[k], rResult);
				k++;
			}
		}
	}

	@Test
	public void testattenuationDueToWaterVapour() {
        SDPropagationModel calculator = new SDPropagationModel();

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
        SDPropagationModel calculator = new SDPropagationModel();

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
        SDPropagationModel calculator = new SDPropagationModel();

		double rFreq = 10;
		double rFreqi = 100;

		double rResult = calculator.functionG(rFreq, rFreqi);

		util.assertDoubleEquals(1.6694, rResult);
	}

	@Test
	public void testfunctionPhi() {
        SDPropagationModel calculator = new SDPropagationModel();

		double rPressure = 1;
		double rTemperature = 1.0177;
		double a = 0.0717;
		double b = -1.8132;
		double c = 0.0156;
		double d = -1.6515;

		double rResult = calculator.functionPhi(rPressure,rTemperature,a,b,c,d);

		util.assertDoubleEquals(0.9974, rResult);
	}


}

