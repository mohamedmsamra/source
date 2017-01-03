package org.seamcat.cdma;

import org.seamcat.model.functions.Point2D;

public class CDMATriSectorCell extends CdmaBaseStation {

	public CDMATriSectorCell(Point2D position, CDMASystem _system, int _cellid, double antHeight, double antennaTilt,int _sectorid) {
		super(position, _system, _cellid, antHeight, antennaTilt, _sectorid);
	}
}
