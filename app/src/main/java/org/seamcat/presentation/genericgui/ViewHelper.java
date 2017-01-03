package org.seamcat.presentation.genericgui;

import java.awt.Component;

import javax.swing.JSplitPane;

public class ViewHelper {

	public static JSplitPane vSplit( Component top, Component bottom, int splitPosition ) {
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setDividerLocation( splitPosition );
		split.add( top );
		split.add( bottom );
		return split;
	}

	public static JSplitPane hSplit( Component left, Component right, int splitPosition ) {
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.setDividerLocation( splitPosition );
		split.add( left );
		split.add( right );
		return split;
	}
	
}
