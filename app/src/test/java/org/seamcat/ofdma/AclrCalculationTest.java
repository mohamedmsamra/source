package org.seamcat.ofdma;

import junit.framework.Assert;
import org.junit.Test;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.presentation.DialogTableToDataSet;

import static org.seamcat.model.mathematics.Mathematics.dB2Linear;
import static org.seamcat.model.mathematics.Mathematics.linear2dB;

public class AclrCalculationTest {

	
	@Test
	public void repeatMatLabCalculationTest() throws Exception {
		double epsilon = 1e-12;
		double receiverBandwith = 5; //MHz
		
		EmissionMaskImpl mask = new EmissionMaskImpl();
		
		double frequencyShift = 2.5;
		
		//Define mask
		mask.addPoint(epsilon + frequencyShift, 0);
		mask.addPoint(epsilon + epsilon + frequencyShift, -23.77);
		mask.addPoint(1 + frequencyShift, -34);
		mask.addPoint(5 + frequencyShift, -37);
		mask.addPoint(6 + frequencyShift, -43);
		mask.addPoint(10 + frequencyShift, -57);
		mask.addPoint(160 + frequencyShift, -57);

        DialogTableToDataSet.symmetrizeFunction(mask, 0);
        mask.sortPoints();

        //Actual calculation
		double p_0_5_mhz = Mathematics.dB2Linear(mask.integrate(0, receiverBandwith)) * receiverBandwith;
		double p_1_5_mhz = Mathematics.dB2Linear(mask.integrate(5, receiverBandwith)) * receiverBandwith;
		double p_2_5_mhz = Mathematics.dB2Linear(mask.integrate(10, receiverBandwith)) * receiverBandwith;
		double p_3_5_mhz = Mathematics.dB2Linear(mask.integrate(15, receiverBandwith)) * receiverBandwith;
		double p_4_5_mhz = Mathematics.dB2Linear(mask.integrate(20, receiverBandwith)) * receiverBandwith;
		
		//Conversion to dB
		p_0_5_mhz = Mathematics.linear2dB(p_0_5_mhz);
		p_1_5_mhz = Mathematics.linear2dB(p_1_5_mhz);
		p_2_5_mhz = Mathematics.linear2dB(p_2_5_mhz);
		p_3_5_mhz = Mathematics.linear2dB(p_3_5_mhz);
		p_4_5_mhz = Mathematics.linear2dB(p_4_5_mhz);
		
		double[] ACLR_dB = new double[4];
		
		ACLR_dB[0] = p_0_5_mhz - p_1_5_mhz;
		ACLR_dB[1] = p_0_5_mhz - p_2_5_mhz;
		ACLR_dB[2] = p_0_5_mhz - p_3_5_mhz;
		ACLR_dB[3] = p_0_5_mhz - p_4_5_mhz;
		
		//Todo CP 21-09-2008: get real results from JP for comparison
		Assert.assertEquals("ACLR for Adjacent Channel 1 is wrong", 32.6019, ACLR_dB[0], 0.1);
		Assert.assertEquals("ACLR for Adjacent Channel 2 is wrong", 44.7577, ACLR_dB[1], 0.1);
		Assert.assertEquals("ACLR for Adjacent Channel 3 is wrong", 57.00, ACLR_dB[2], 0.1);
		Assert.assertEquals("ACLR for Adjacent Channel 4 is wrong", 57.00, ACLR_dB[3], 0.1);
		
	}
	
	
	@Test
	public void testAclrIntegration() throws Exception {
		EmissionMaskImpl mask = new EmissionMaskImpl();
		
		double refBw = 10000;
		
		mask.addPoint(new Point2D(-50, -57), refBw);
		mask.addPoint(new Point2D(-15, -57), refBw);
		mask.addPoint(new Point2D(-11, -43), refBw);
		mask.addPoint(new Point2D(-10, -37), refBw);
		mask.addPoint(new Point2D(-6, -34), refBw);
		mask.addPoint(new Point2D(-5, -23.77), refBw);
		mask.addPoint(new Point2D(-5, 0), refBw);
		mask.addPoint(new Point2D(5, 0), refBw);
		mask.addPoint(new Point2D(5, -23.77), refBw);
		mask.addPoint(new Point2D(6, -34), refBw);
		mask.addPoint(new Point2D(10, -37), refBw);
		mask.addPoint(new Point2D(11, -43), refBw);
		mask.addPoint(new Point2D(15, -57), refBw);
		mask.addPoint(new Point2D(50, -57), refBw);
		
		for (int i = 0; i < mask.points().size() - 1;i++) {
			double sum = calculateFrom(i, mask);
			
			System.out.println("From " + i + " = " + linear2dB(dB2Linear(sum) * refBw / 1000));
		}
		
	}
	
	private double calculateFrom(int index, EmissionMaskImpl mask) throws Exception {
		if (index >= mask.points().size()) {
			throw new IllegalArgumentException("index to high");
		} else if (index == mask.points().size() - 2) {
			return calc(index, mask);	
		} else {
			return calc(index, mask) + calculateFrom(index + 1, mask);
		}
	}
	
	
	private double calc(int index, EmissionMaskImpl mask) throws Exception {
		Point2D p1 = mask.points().get(index);
		Point2D p2 = mask.points().get(index + 1);
		double refBw = 10;
		double bwDif = 0;
		if(p2.getX()<0){
			bwDif = p2.getX() - ((Math.abs(p1.getX()) - Math.abs(p2.getX())) / 2);
		}else{
			bwDif = p2.getX() + ((Math.abs(p1.getX()) - Math.abs(p2.getX())) / 2);
		}
		
		double result = mask.integrate(bwDif, refBw);
		System.out.println("integrating " + bwDif + "  result:" + linear2dB(dB2Linear(result) * refBw / 1000));
		return result;
	}
}
