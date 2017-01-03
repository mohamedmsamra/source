package org.seamcat.model.types.result;

import org.seamcat.model.functions.Point2D;

import java.util.ArrayList;
import java.util.List;

public class ScatterDiagramResultType {
    private String title;
    private String xLabel, yLabel;
    private List<Point2D> scatterPoints;

    public ScatterDiagramResultType( String title, String xLabel, String yLabel ) {
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        scatterPoints = new ArrayList<Point2D>();
    }

    public String getTitle() {
        return title;
    }

    public String getxLabel() {
        return xLabel;
    }

    public String getyLabel() {
        return yLabel;
    }

    public List<Point2D> getScatterPoints() {
        return scatterPoints;
    }

    @Override
    public String toString() {
        return "Scatter["+scatterPoints.size()+"]";
    }
}
