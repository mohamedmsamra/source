package org.seamcat.presentation.systems;

import org.seamcat.model.core.GridPositionCalculator;
import org.seamcat.model.functions.Point2D;

public class CellUI {

    private Point2D position = new Point2D();
    private Point2D[] hexagon = new Point2D[6];


    public boolean isInside( Point2D position ) {
        return GridPositionCalculator.isInside( position, 0, 0, hexagon );
    }

    public void setPosition( Point2D position, double cellRadius ) {
        this.position = position;
        GridPositionCalculator.calculateHexagon( position, cellRadius, hexagon );
    }

    public Point2D getPosition() {
        return position;
    }
}
