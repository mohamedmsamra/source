package org.seamcat.presentation.components;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.seamcat.presentation.ChartSaver;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DiscreteFunctionXYPlot extends DiscreteFunctionPlot  {

    private final ChartPanel xyChartPanel;
    private JFreeChart xyChart;
    private DiscreteFunctionTableModelAdapter dataset;

    public DiscreteFunctionXYPlot(DiscreteFunctionTableModelAdapter dataset, String xCaption, String yCaption) {
        setLayout(new BorderLayout());
        this.dataset = dataset;

        String xx = xCaption == null ? "": xCaption;
        String yy = yCaption == null ? "": yCaption;
        dataset.setColumnName(0, xx);
        dataset.setColumnName(1, yy);

        xyChart = ChartFactory.createXYLineChart("", xx, yy, dataset,
                PlotOrientation.VERTICAL, true, true, false);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.xyChart.getXYPlot().getRenderer();
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesFilled(0, true);
        dataset.addChangeListener(this);
        xyChartPanel = new ChartPanel(xyChart);
        xyChartPanel.setMouseZoomable(true, false);
        xyChartPanel.setPreferredSize(new Dimension(800, 600));

        DiscreteFunctionGraph.applyStyles(xyChartPanel, this.getFont(), false);
        setAxisResolution();

        add(xyChartPanel, BorderLayout.CENTER);
        dataset.fireChangeListeners();
    }

    @Override
    public void datasetChanged(DatasetChangeEvent datasetChangeEvent) {
        xyChart.getPlot().datasetChanged(datasetChangeEvent);
        setAxisResolution();
    }

    public void drawGraphToGraphics(Graphics2D g, Rectangle2D r) {
        xyChart.setBackgroundPaint(null);
        xyChart.draw(g, r);
    }

    @Override
    public void setAxisNames(String xAxis, String yAxis) {
        xyChart.getXYPlot().getDomainAxis().setLabel(xAxis);
        xyChart.getXYPlot().getRangeAxis().setLabel(yAxis);
    }

    private boolean singular( Range range ) {
        return range == null || Math.abs(range.getUpperBound() - range.getLowerBound()) < 0.001;
    }

    public void setAxisResolution() {
        Range domain= xyChart.getXYPlot().getDomainAxis().getRange();
        Range range= xyChart.getXYPlot().getRangeAxis().getRange();

        if ( singular( range ) ) {
            xyChart.getXYPlot().getRangeAxis().setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        } else {
            xyChart.getXYPlot().getRangeAxis().setStandardTickUnits( NumberAxis.createStandardTickUnits() );
        }
        if ( singular( domain ) ) {
            xyChart.getXYPlot().getDomainAxis().setStandardTickUnits( NumberAxis.createIntegerTickUnits() );
        } else {
            xyChart.getXYPlot().getDomainAxis().setStandardTickUnits( NumberAxis.createStandardTickUnits() );
        }
    }

    public DiscreteFunctionTableModelAdapter getDataSet() {
        return dataset;
    }

    public void saveChartImage() {
        ChartSaver.saveChart(xyChartPanel);
    }

}
