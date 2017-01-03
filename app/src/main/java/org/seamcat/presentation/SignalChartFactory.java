package org.seamcat.presentation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class SignalChartFactory {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle(
	      "stringlist", Locale.ENGLISH);

    public static synchronized ChartPanel createVectorChart(String xTitle, String yTitle,
                                                            Font font, XYSeriesCollection xySeriesCollection) {
        return createVectorChart(xTitle, yTitle, font, xySeriesCollection, true);
    }

	public static synchronized ChartPanel createVectorChart(String xTitle, String yTitle,
	      Font font, XYSeriesCollection xySeriesCollection, boolean legend) {
		JFreeChart chart;
		ChartPanel vectorChart;
		chart = ChartFactory.createXYLineChart(
		      STRINGLIST.getString("VECTOR_GRAPH_TITLE"), xTitle, yTitle,
		      xySeriesCollection, PlotOrientation.VERTICAL, legend, true, false);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart
		      .getXYPlot().getRenderer();
		renderer.setSeriesShapesVisible(0, false);
		renderer.setSeriesShapesFilled(0, false);
		vectorChart = new ChartPanel(chart);
        vectorChart.setMouseZoomable(true, false);
		applyStyles(vectorChart, font, true);
		return vectorChart;
	}

	protected static synchronized ChartPanel createVectorLogChart(String xTitle,
	      String yTitle, XYSeriesCollection collection, double EVENT_LIMIT,
	      Font font) {

		JFreeChart chart;
		ChartPanel vectorLogChart;
		chart = ChartFactory.createXYLineChart(
		      STRINGLIST.getString("VECTOR_GRAPH_TITLE"), xTitle, yTitle,
		      collection, PlotOrientation.VERTICAL, true, true, false);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart
		      .getXYPlot().getRenderer();

		renderer.setSeriesShapesVisible(0, false);
		renderer.setSeriesShapesFilled(0, false);
		LogAxis xAxis = new LogAxis(xTitle);

		xAxis.setRange(new Range(0, EVENT_LIMIT));
		chart.getXYPlot().setDomainAxis(xAxis);
		vectorLogChart = new ChartPanel(chart);
		vectorLogChart.setMouseZoomable(true, false);
		applyStyles(vectorLogChart, font, true);
		return vectorLogChart;
	}

	protected static synchronized ChartPanel createDensityGraph(String xTitle, String yTitle,
	      Font font, HistogramDataset collection) {
		JFreeChart chart;
		ChartPanel densityChart;

		chart = ChartFactory.createHistogram(
		      STRINGLIST.getString("HISTOGRAM_TITLE"), yTitle,
		      STRINGLIST.getString("HISTOGRAM_AXIX_TITLE_Y"), collection,
		      PlotOrientation.VERTICAL, true, false, false);
		densityChart = new ChartPanel(chart);
		applyStyles(densityChart, font, true);
		return densityChart;

	}

	protected static synchronized ChartPanel createCumulativeChart(String xTitle,
	      String yTitle, XYSeriesCollection cumulativeSeries, Font font,
	      boolean signalIsConstant, String title, String unit) {
		JFreeChart chart;
		ChartPanel cumulativeChart;

		chart = ChartFactory.createXYLineChart(
		      STRINGLIST.getString("CUMULATIVE_DISTRIBUTION_TITLE"), yTitle,
		      STRINGLIST.getString("CUMULATIVE_DISTRIBUTION_AXIX_TITLE_Y"),
		      cumulativeSeries, PlotOrientation.VERTICAL, true, true, false);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart
		      .getXYPlot().getRenderer();

		if (signalIsConstant) {
			((NumberAxis) chart.getXYPlot().getRangeAxis()).setRange(new Range(0,
			      1));
		}

		chart.setTitle(title);
		chart.getXYPlot().getDomainAxis().setLabel(unit);

		renderer.setSeriesShapesVisible(0, false);
		cumulativeChart = new ChartPanel(chart);
		cumulativeChart.setMouseZoomable(true, false);
		applyStyles(cumulativeChart, font, true);
		return cumulativeChart;
	}

	public static void applyStyles(ChartPanel chartPanel, Font font,
	      boolean legend) {
		JFreeChart xyChart = chartPanel.getChart();
		if (!legend) {
			xyChart.removeLegend();
		}
		xyChart.setBackgroundPaint(chartPanel.getBackground());

		XYPlot p = chartPanel.getChart().getXYPlot();
		p.setBackgroundPaint(Color.WHITE);
		p.setRangeGridlinePaint(chartPanel.getForeground());
		p.setDomainGridlinePaint(chartPanel.getForeground());

		p.getDomainAxis().setTickLabelFont(font);
		p.getDomainAxis().setTickLabelPaint(chartPanel.getForeground());
		p.getDomainAxis().setLabelFont(font);
		p.getDomainAxis().setLabelPaint(chartPanel.getForeground());

		p.getRangeAxis().setTickLabelFont(font);
		p.getRangeAxis().setTickLabelPaint(chartPanel.getForeground());
		p.getRangeAxis().setLabelFont(font);
		p.getRangeAxis().setLabelPaint(chartPanel.getForeground());

	}

}
