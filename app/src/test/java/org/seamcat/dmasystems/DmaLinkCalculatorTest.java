package org.seamcat.dmasystems;

import org.junit.Before;
import org.junit.Test;
import org.seamcat.function.TestUtil;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.simulation.cellular.CellularCalculations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.seamcat.model.cellular.CellularLayout.SystemLayout.CenterOfInfiniteNetwork;


public class DmaLinkCalculatorTest {
	
	TestUtil util;
	
	@Before
	public void setup() {
		util = new TestUtil( 0.0001 );
	}
	
	@Test
	public void testWrapAround() {
		Point2D mobile = new Point2D(0.0, 0.0);
		Point2D base = new Point2D(10, 10);
		Point2D newBase = CellularCalculations.findNewCoordinate(mobile, base, 3, CenterOfInfiniteNetwork, true, CellularLayout.SectorSetup.SingleSector);
		
		util.assertDoubleEquals(5.203551820234652, Mathematics.distance(mobile, newBase) );
	}

	@Test
	public void testSkipWrapAround() {
		Point2D mobile = new Point2D(0.0, 0.0);
		Point2D base = new Point2D(10, 10);
		Point2D newBase = CellularCalculations.findNewCoordinate(mobile, base, 3, CenterOfInfiniteNetwork, false,CellularLayout.SectorSetup.SingleSector);
		
		util.assertDoubleEquals(Mathematics.distance(mobile, base), Mathematics.distance(mobile, newBase ) );
	}
	
	@Test
	public void testWrapAround_various_cases() {
		Map<Point2D, Double> points = new HashMap<Point2D, Double>(7);
		points.put( new Point2D(1, 2), 2.828427125);
		points.put( new Point2D(8, 16), 0.196152423);
		points.put( new Point2D(-3, -8), 0.803847577);
		points.put( new Point2D(10, -6), 0.938508989);
		points.put( new Point2D(-5, 14), 0.540686466);
		points.put( new Point2D(16, 5.5), 0.009618943);
		points.put( new Point2D(-9, 2.5), 0.990381057);
		
		Point2D base = new Point2D(3, 4);
		for (Entry<Point2D, Double> entry : points.entrySet()) {
			Point2D newBase = CellularCalculations.findNewCoordinate(entry.getKey(), base, 3, CenterOfInfiniteNetwork, true,CellularLayout.SectorSetup.SingleSector);
			double temp = Mathematics.distance(entry.getKey(), newBase);
			util.assertDoubleEquals("distance is outside allowed tolerance (the result is " + temp + " and expected value is " + entry.getValue(), 
					entry.getValue(), temp);
		}
	}

	@Test
	public void testElevationCalculation() {
		Point2D mobile = new Point2D(0.0, 0.0);
		Point2D base = new Point2D(10, 10);
		double expectedValue = -0.036462806;
		double elevation = Mathematics.calculateElevation(mobile,1.0, base,10.0);
		util.assertDoubleEquals("elevation is outside allowed tolerance (the result is " + elevation + " and expected value is " + expectedValue, 
				expectedValue, elevation);
	}

	@Test
	public void testCalculateKartesianAngle() {
		List<Point2D> mobilePoints = new ArrayList<Point2D>(18);
		mobilePoints.add( new Point2D(1, 1));
		mobilePoints.add( new Point2D(-1, 1));
		mobilePoints.add( new Point2D(-1, -1));
		mobilePoints.add( new Point2D(1, -1));
		mobilePoints.add( new Point2D(-2.07, 2.962));
		mobilePoints.add( new Point2D(-0.089, 4.105));
		mobilePoints.add( new Point2D(1, 0));
		mobilePoints.add( new Point2D(0, 1));
		mobilePoints.add( new Point2D(-1, 0));
		mobilePoints.add( new Point2D(0, -1));
		mobilePoints.add( new Point2D(1, 0.5));
		mobilePoints.add( new Point2D(0.5, 1));
		mobilePoints.add( new Point2D(-0.5, 1));
		mobilePoints.add( new Point2D(-1, 0.5));
		mobilePoints.add( new Point2D(-1, -0.5));
		mobilePoints.add( new Point2D(-0.5, -1));
		mobilePoints.add( new Point2D(0.5, -1));
		mobilePoints.add( new Point2D(1, -0.5));
		
		List<Point2D> basePoints = new ArrayList<Point2D>(18);
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(2.0, 3.46));
		basePoints.add( new Point2D(2.0, 3.46));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		basePoints.add( new Point2D(0, 0));
		
		double[] expectedAngle;
		expectedAngle = new double [18];
		expectedAngle[0] = 45;
		expectedAngle[1] = 135;
		expectedAngle[2] = 225;
		expectedAngle[3] = 315;
		expectedAngle[4] = 186.97596238;
		expectedAngle[5] = 162.84139528;
		expectedAngle[6] = 0;
		expectedAngle[7] = 90;
		expectedAngle[8] = 180;
		expectedAngle[9] = 270;
		expectedAngle[10] = 26.565;
		expectedAngle[11] = 63.4349488;
		expectedAngle[12] = 116.565051;
		expectedAngle[13] = 153.434948;
		expectedAngle[14] = 206.565051;
		expectedAngle[15] = 243.434948;
		expectedAngle[16] = 296.565051;
		expectedAngle[17] = 333.434948;
		
		double angle;
		for(int i=0; i<=17; i++){
			angle = Mathematics.calculateKartesianAngle(mobilePoints.get(i),basePoints.get(i));
			util.assertDoubleEquals("angle is outside allowed tolerance (the result is " + angle + " and expected value is " + expectedAngle[i], 
					expectedAngle[i], angle);
		}
	}

	@Test
	public void testDistance() {
		List<Point2D> mobilePoints = new ArrayList<Point2D>(7);
		mobilePoints.add( new Point2D(1, 1));
		mobilePoints.add( new Point2D(-1, 1));
		mobilePoints.add( new Point2D(-1, -1));
		mobilePoints.add( new Point2D(1, -1));
		
		double expectedDistance = 1.414213562;
		
		for ( Point2D point : mobilePoints ) {
			double distance = Mathematics.distance( point );
			util.assertDoubleEquals("distance is outside allowed tolerance (the result is " + distance + " and expected value is " + expectedDistance, 
					expectedDistance, distance);
		}
	}

	
	@Test
	public void testDistanceOneParameter() {
		Point2D point = new Point2D(2.34, 55.3 );
		Point2D origin = new Point2D( 0,0 );
		
		double expected = Mathematics.distance( point, origin );
		util.assertDoubleEquals( expected, Mathematics.distance( point ) );
	}
	
	@Test
	public void testKartesianAngleOneParameter() {
		Point2D point = new Point2D(2.34, 55.3 );
		Point2D origin = new Point2D( 0,0 );
		
		double expected = Mathematics.calculateKartesianAngle( point, origin );
		util.assertDoubleEquals(expected, Mathematics.calculateKartesianAngle( point ) );
	}
	
	@Test
	public void testAverageLossCalculation1() {
		int sampleSize = 10;
		
		int[] non = new int[sampleSize];
		int[] inter = new int[sampleSize];
		
		for (int i = 0;i < sampleSize; i++) {
			non[i] = 20;
			inter[i] = 1;
		}
		
		//Assert.assertEquals("Average loss is not correct", 100.0, LinkCalculator.calculateLossAvgPercentage(non, inter) );
	}
}
