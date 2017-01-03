package org.seamcat.model.systems.generic;

import org.seamcat.model.generic.Defaults;
import org.seamcat.model.generic.InterferenceCriteria;
import org.seamcat.model.generic.RXAntennaPointingUI;
import org.seamcat.model.generic.ReceptionCharacteristics;
import org.seamcat.model.generic.Train_Defaults;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.Description;
import org.seamcat.model.types.LibraryItem;

public interface T_ReceiverModel extends LibraryItem{

	  @UIPosition(row = 1, col = 1, name = "Receiver identification", height = 200, width = 400)
	    Description description();

	    @UIPosition(row = 2, col = 1, name = "Antenna pointing")
	    RXAntennaPointingUI antennaPointing();

	    @UIPosition(row = 1, col = 2, name = "Antenna Patterns Identification", width = 300)
	    AntennaGain antennaGain(); 
	    AntennaGain antennaGain = Train_Defaults.defaultAntennaGain();
	    

	    @UIPosition(row = 1, col = 3, name = "Reception Characteristics", height = 330)
	    ReceptionCharacteristics receptionCharacteristics();
	    

	    @UIPosition(row = 2, col = 3, name = "Interference Criteria")
	    InterferenceCriteria interferenceCriteria();
	
	
	
}
