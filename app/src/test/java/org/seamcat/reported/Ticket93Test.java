package org.seamcat.reported;

import org.junit.Assert;
import org.junit.Test;
import org.seamcat.cdma.CDMADownlinkSystem;
import org.seamcat.model.factory.TestFactory;


public class Ticket93Test {


	@Test
	public void testCopyScenarioForTriSectorReference() throws Exception {
        TestFactory.initialize();

        CDMADownlinkSystem cdma = new CDMADownlinkSystem();
		cdma.getSystemSettings().getLayout().setReferenceSector(2);
				
		CDMADownlinkSystem cdmaLoaded = new CDMADownlinkSystem(cdma);
		
		Assert.assertEquals("TriSector reference selection was not copied correctly", cdma.getSystemSettings().getLayout().getReferenceSector(),
				cdmaLoaded.getSystemSettings().getLayout().getReferenceSector());
	}
	
	
}
