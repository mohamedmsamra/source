package org.seamcat.presentation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;
import org.seamcat.presentation.layout.VerticalSubPanelLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class VectorsOutlinePanel extends JPanel {

    private ChartPanel vectorChart;

	private String unit,mtitle,xTitle,yTitle;
    private XYSeriesCollection collection;
    private int numberOfEvents;

    public VectorsOutlinePanel(String xTitle, String yTitle, int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
        setLayout(new BorderLayout());
		this.xTitle = xTitle;
		this.yTitle = yTitle;
    }

	public void show(String title, String unit) {
        this.unit = unit;
		this.mtitle = title;

        createCharts();
        JPanel right = new JPanel();
        right.setLayout(new VerticalSubPanelLayoutManager());
        add(right, BorderLayout.EAST);
    }

    public void appendValue(int eventNumber, Double value ) {
        if ( collection.getSeriesCount() > 0 ) {
            XYSeries series = (XYSeries) collection.getSeries().get(0);
            //int count = series.getItemCount();
            series.add(eventNumber, value);
            vectorChart.getChart().getXYPlot().setDataset(collection);
        }
    }

    public void addStatistics( double mean, double stdDev ) {
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(create("mean - std. dev", mean - stdDev));
        collection.addSeries(create("mean", mean));
        collection.addSeries(create("mead + std. dev", mean + stdDev));
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesPaint(2, Color.BLACK);

        renderer.setSeriesStroke(0, getOdynStroke(1, 1));
        renderer.setSeriesStroke(1, getOdynStroke(2, 0));
        renderer.setSeriesStroke(2, getOdynStroke(1, 1));
        vectorChart.getChart().getXYPlot().setRenderer(1, renderer);
        vectorChart.getChart().getXYPlot().setDataset( 1, collection );
    }

    private float[][] pattern = {{10.0f},{10.0f,10.0f},{10.0f,10.0f,2.0f,10.0f},{1.0f,20.0f}};

    public Stroke[] strokes(float width) {
        Stroke[] strokes = new Stroke[] {
                new BasicStroke(width, BasicStroke.CAP_BUTT,  BasicStroke.JOIN_MITER, 10.0f                  ), // solid line
                new BasicStroke(width, BasicStroke.CAP_BUTT,  BasicStroke.JOIN_MITER, 10.0f, pattern[1], 0.0f), // dashed line
        };
        return strokes;
    }
    public Stroke getOdynStroke(float width, int type) {
        return strokes(width)[type];
    }

    private XYSeries create( String title, double value ) {
        XYSeries series = new XYSeries(title);
        series.add(0, value );
        series.add(numberOfEvents, value);
        return series;
    }

    private void createCharts() {
		removeAll();
		// get datasets

		collection = new XYSeriesCollection();
        collection.addSeries( new XYSeries("samples"));
        // create charts with datasets
        JFreeChart scatter = ChartFactory.createScatterPlot(mtitle, xTitle, yTitle, collection);
        XYItemRenderer renderer = scatter.getXYPlot().getRenderer(0);

        // 5x5 red pixel circle
        Shape shape  = new Ellipse2D.Double(0,0,4,4);
        renderer.setBaseShape(shape);
        renderer.setBasePaint(Color.red);

// set only shape of series with index i
        renderer.setSeriesShape(0, shape);
        vectorChart = new ChartPanel(scatter);
        SignalChartFactory.applyStyles(vectorChart, this.getFont(), true);
        add(vectorChart, BorderLayout.CENTER);

		setUpGraphLabels();
		setVisible(true);
	}

	private void setUpGraphLabels() {
		vectorChart.getChart().setTitle(mtitle);
		vectorChart.getChart().getXYPlot().getRangeAxis().setLabel(unit);
        vectorChart.getChart().getXYPlot().getDomainAxis().setLabel("Event samples out of "+numberOfEvents);
	}

	public void reset() {
		vectorChart.getChart().getXYPlot().setDataset(null);
	}
}
