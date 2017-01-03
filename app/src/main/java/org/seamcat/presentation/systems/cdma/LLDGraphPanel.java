package org.seamcat.presentation.systems.cdma;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.seamcat.presentation.components.DiscreteFunctionGraph;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class LLDGraphPanel extends JPanel {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private final XYSeriesCollection dataSet = new XYSeriesCollection();
    private final JFreeChart chart = ChartFactory.createXYLineChart("",
            STRINGLIST.getString("LIBRARY_CDMA_LLD_DETAILS_GEOMETRY"), "Eb/No target [dB]", dataSet, PlotOrientation.VERTICAL, true, true,false);
    private CDMAEditModel model;

    public LLDGraphPanel() {
        super(new GridLayout());

        // Prep chart
        for (int x = 1; x < CDMAEditModel.COLUMN_NAMES.length; x++) {
            dataSet.addSeries(new XYSeries(CDMAEditModel.COLUMN_NAMES[x]));
        }

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
        renderer.setShapesVisible(true);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseZoomable(true, false);

        Font f = new Font(this.getFont().getName(), this.getFont().getStyle(), 12);
        DiscreteFunctionGraph.applyStyles(chartPanel, f, true);
        add(chartPanel);
    }

    public void setGraph(CDMAEditModel model) {
        this.model = model;
        dataSet.removeAllSeries();
        if (model != null) {
            XYSeriesCollection collection = model.getXYSeriesCollection();
            for (int x = 0, stop = collection.getSeriesCount(); x < stop; x++) {
                dataSet.addSeries(collection.getSeries(x));
            }
        }
    }

    public void setRangeLabel(String pct, String targetType) {
        String label = String.format(STRINGLIST.getString("LIBRARY_CDMA_LLD_DETAILS_GRAPH_RANGE_LABEL"), pct, targetType);
        chart.getXYPlot().getRangeAxis().setLabel(label);
    }

    public void updateGraph() {
        setGraph(model);
    }
}
