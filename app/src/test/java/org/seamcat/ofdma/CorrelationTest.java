package org.seamcat.ofdma;

import junit.framework.Assert;

import org.junit.Test;
import org.seamcat.model.distributions.GaussianDistributionImpl;
import org.seamcat.model.mathematics.Mathematics;


public class CorrelationTest {

	@Test
	public void testCorrelation() {
		GaussianDistributionImpl dist = new GaussianDistributionImpl(0, 10);
		
		double desiredCorrelation = 0.3;
		
		double a = Math.sqrt(desiredCorrelation);
		double b = Math.sqrt(1 - desiredCorrelation);
		
		int number = 10000;
		
		double[] xv  = new double[number]; 
		double[] v1 = new double[number];
		double[] v2 = new double[number];
		
		for (int i = 0;i < v1.length;i++) {
			xv[i] = dist.trial();
		}
		
		for (int i = 0;i < v1.length;i++) {
			double y = dist.trial();
			double x = xv[i];
			
			double temp  = 0; 
			temp += a*x + b*y;
			v1[i] = temp;
		}
		
		for (int i = 0;i < v2.length;i++) {
			double y = dist.trial();
			double x = xv[i];
			
			double temp  = 0; 
			temp += a*x + b*y;
			v2[i] = temp;
		}
		
		double corr = Mathematics.calculateCorrelation(v1, v2);
		
		double diff = desiredCorrelation - corr;
		
		Assert.assertEquals("Correlation is not correct", true, (diff < 0.1));
	}
	
	@Test
	public void testCalculateCorrelation() {
		//MathLab generated vectors, correlation = -0.44
		double[] v1 = new double[] {0.6443, 0.3786, 0.8116, 0.5328, 0.3507, 0.9390, 0.8759, 0.5502, 0.6225, 0.5870};
		double[] v2 = new double[] {0.2077, 0.3012, 0.4709, 0.2305, 0.8443, 0.1948, 0.2259, 0.1707, 0.2277, 0.4357};
		
		double corr = Mathematics.calculateCorrelation(v1, v2);
		
		Assert.assertEquals("Correlation is not correct", -0.44, Mathematics.round(corr));
		
	}
	
}
