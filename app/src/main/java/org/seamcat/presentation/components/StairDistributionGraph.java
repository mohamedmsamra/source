package org.seamcat.presentation.components;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.seamcat.presentation.ChartSaver;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class StairDistributionGraph extends JPanel implements DatasetChangeListener {

    private final ChartPanel chartPanel;
    private JFreeChart chart;

    public StairDistributionGraph(StairDistributionTableModelAdapter dataset) {
		super(new GridLayout());
        CategoryDataset dset = dataset.getCategoryDS();

		chart = ChartFactory.createBarChart(null, "Value",
		      "Cumulative probability", dset, PlotOrientation.VERTICAL, false, true, false);
		dset.addChangeListener(this);
		chartPanel = new ChartPanel(chart);
		chartPanel.setMouseZoomable(true, false);
		
		chartPanel.getChart().setBackgroundPaint(this.getBackground());
		CategoryPlot p = chartPanel.getChart().getCategoryPlot();
		p.setBackgroundPaint( Color.white );
		p.setRangeGridlinePaint( this.getForeground() );
		p.setDomainGridlinePaint( this.getForeground() );
		p.getDomainAxis().setTickLabelFont( this.getFont());
		p.getDomainAxis().setTickLabelPaint( chartPanel.getForeground() );
		p.getDomainAxis().setLabelFont( this.getFont() );
		p.getDomainAxis().setLabelPaint( chartPanel.getForeground() );
		p.getRangeAxis().setTickLabelFont( this.getFont() );
		p.getRangeAxis().setTickLabelPaint( chartPanel.getForeground() );
		p.getRangeAxis().setLabelFont( this.getFont() );
		p.getRangeAxis().setLabelPaint( chartPanel.getForeground() );
        alignTicks();

		add(chartPanel);
	}

	public void datasetChanged(DatasetChangeEvent dce) {
        chart.getPlot().datasetChanged(dce);
        alignTicks();
    }

    private void alignTicks() {
        Range range = chart.getCategoryPlot().getDataRange( chart.getCategoryPlot().getRangeAxis() );
        if ( range == null || Math.abs(range.getUpperBound() - range.getLowerBound()) < 0.001 ) {
            chart.getCategoryPlot().getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        } else {
            chart.getCategoryPlot().getRangeAxis().setStandardTickUnits(NumberAxis.createStandardTickUnits());
        }
    }

	public void drawGraphToGraphics(Graphics2D g, Rectangle2D r) {
		chart.setBackgroundPaint(null);
		chart.draw(g, r);
	}

    public void saveChartImage() {
        ChartSaver.saveChart(chartPanel);
    }
}
