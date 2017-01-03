package org.seamcat.presentation;

import java.awt.Component;
import java.util.List;

import org.seamcat.presentation.propagationtest.PropagationHolder;



public interface DisplaySignal {
	
	List<PropagationHolder> getPropagationHolders();
	
	Component getParent();

}


