package org.seamcat.ofdma;

import java.util.List;


public interface OfdmaVictim {

	List<OfdmaExternalInterferer> getExternalInterferers();
	
	double getExternalBlocking_dBm();
	
	double getExternalUnwanted_dBm();
	
	void addExternalInterferer(OfdmaExternalInterferer interferer);
	
}
