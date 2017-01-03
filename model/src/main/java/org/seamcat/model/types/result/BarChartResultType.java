package org.seamcat.model.types.result;

import java.util.ArrayList;
import java.util.List;

public class BarChartResultType {
    private String title;
    private String xLabel, yLabel;
    private List<BarChartValue> chartPoints;

    public BarChartResultType(String title, String xLabel, String yLabel) {
        this.title = title;
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        chartPoints = new ArrayList<>();
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

    public List<BarChartValue> getChartPoints() {
        return chartPoints;
    }

    @Override
    public String toString() {
        return "Bar Chart["+chartPoints.size()+"]";
    }
}
