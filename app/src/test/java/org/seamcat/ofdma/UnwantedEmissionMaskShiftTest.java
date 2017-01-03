package org.seamcat.ofdma;

import org.junit.Test;


public class UnwantedEmissionMaskShiftTest {

	@Test
	public void testFormula() {
		double systemBandwidth = 10;
		double systemFrequency = 2010;
		double numberOfSubCarrierPerMobile = 8;
		double numberOfSubCarriersPerBaseStation = 24;
		
		double resourceBlockBandwidth = 0.375;
		
		double diff = systemBandwidth - (numberOfSubCarriersPerBaseStation * resourceBlockBandwidth);
		
		double numberOfUsers =  (numberOfSubCarriersPerBaseStation / numberOfSubCarrierPerMobile);
		
		
		
		if (diff < 0) {
			throw new RuntimeException("Check input: " + diff);
		}
		
		for (int index = 0;index < numberOfUsers;index++) {
			double result = systemFrequency - (systemBandwidth / 2) + ((((numberOfSubCarrierPerMobile * resourceBlockBandwidth) + (diff / numberOfUsers)) / 2) * ((index * 2) + 1));
			System.out.println("For index " + index + " result is: " + result);
		}
		
		
	}
	
	
}
