package org.seamcat.cdma;

import org.seamcat.model.functions.Point2D;

public class CDMAOmniDirectionalCell extends CdmaBaseStation {

	public CDMAOmniDirectionalCell(Point2D position, CDMASystem _system, int _cellid, double antHeight, double antennaTilt) {
		super(position, _system, _cellid, antHeight, antennaTilt, 0);
	}

}
