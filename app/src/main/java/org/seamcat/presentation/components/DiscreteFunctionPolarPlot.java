package org.seamcat.presentation.components;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.PolarChartPanel;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PolarAxisLocation;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.DefaultPolarItemRenderer;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.Point2D;
import org.seamcat.presentation.AntennaPatterns;
import org.seamcat.presentation.ChartSaver;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class DiscreteFunctionPolarPlot extends DiscreteFunctionPlot {

    private final PolarChartPanel polarChartPanel;
    private JFreeChart polarChart;
    private XYSeriesCollection result;
    private DiscreteFunctionTableModelAdapter dataset;
    private AntennaPatterns type;

    public DiscreteFunctionPolarPlot(DiscreteFunctionTableModelAdapter dataset, AntennaPatterns type) {
        setLayout(new BorderLayout());
        this.type = type;

        this.dataset = dataset;

        result = new XYSeriesCollection();
        PolarPlot plot = new PolarPlot( result, new NumberAxis(), new DefaultPolarItemRenderer()) {

            @SuppressWarnings("rawtypes")
            @Override
            protected List refreshAngleTicks() {
                int delta = (int) this.getAngleTickUnit().getSize();
                switch ( DiscreteFunctionPolarPlot.this.type ) {
                    case VERTICAL:
                        return verticalTicks( delta );
                    case SPHERICAL:
                        return sphericalTicks( delta );
                    default:
                        return horizontalTicks( delta );
                }
            }
        };
        plot.setAngleTickUnit(new NumberTickUnit(15));
        switch (type ) {
            case SPHERICAL:
                plot.setAxisLocation(PolarAxisLocation.NORTH_RIGHT);
            default: break;
        }
        polarChart = new JFreeChart( plot );

        dataset.addChangeListener(this);

        polarChartPanel = new PolarChartPanel(polarChart);
        polarChartPanel.setMouseZoomable(true, true);
        polarChartPanel.setPreferredSize(new Dimension(800, 600));
        polarChart.removeLegend();
        PolarPlot pp = (PolarPlot) polarChart.getPlot();
        pp.getAxis().setTickLabelsVisible(true);
        pp.getAxis().setVerticalTickLabels(true);

        add(polarChartPanel, BorderLayout.CENTER );
        dataset.fireChangeListeners();
    }

    private void shiftAndRotateCoordinates( ) {
        XYSeries series = new XYSeries( "0" );
        Bounds bounds = type.getBounds();
        DiscreteFunction function = dataset.getFunction();
        Bounds functionBounds = function.getBounds();
        List<Point2D> allAngles = new ArrayList<>();
        for ( int i= (int) bounds.getMin(); i<bounds.getMax(); i++) {
            if ( functionBounds.contains( i ) ) {
                allAngles.add( new Point2D(i, function.evaluate(i)));
            }
        }

        for ( Point2D point : allAngles ) {
            series.add(rotate(point.getX(), type), point.getY());
        }
        result.removeAllSeries();
        result.addSeries( series );
        polarChart.fireChartChanged();
    }

    private double rotate( double value, AntennaPatterns type) {
        switch (type) {
            case VERTICAL: return (360 + 90 - value) % 360;
            default: return (360 - value) % 360;
        }
    }

    private List<NumberTick> horizontalTicks( int delta ) {
        List<NumberTick> ticks = new ArrayList<NumberTick>();
        for (int t = 0; t < 360; t += delta) {
            ticks.add(new NumberTick(
                    (double) t, String.valueOf((int)rotate(t, AntennaPatterns.HORIZONTAL)),
                    TextAnchor.CENTER, TextAnchor.CENTER, 0.0));
        }
        return ticks;
    }

    private List<NumberTick> verticalTicks( int delta ) {
        List<NumberTick> ticks = new ArrayList<NumberTick>();
        for (int t = 0; t < 360; t += delta) {
            double tp = rotate(t, AntennaPatterns.VERTICAL);
            if (tp <= 90 || tp >= 270) {
                if ( tp >= 270 ) tp = tp - 360;

                ticks.add(new NumberTick(
                        (double) t, String.valueOf((int)tp),
                        TextAnchor.CENTER, TextAnchor.CENTER, 0.0));
            }
        }
        return ticks;
    }

    @Override
    public void setAxisNames(String xAxis, String yAxis) {
        // no axis names for now
    }

    private List<NumberTick> sphericalTicks(int delta ) {
        List<NumberTick> ticks = new ArrayList<NumberTick>();
        for (int t = 0; t < 360; t += delta) {
            double tp = rotate(t, AntennaPatterns.SPHERICAL);
            if (tp <= 180) {
                ticks.add( new NumberTick(
                        (double) t, String.valueOf((int)tp),
                        TextAnchor.CENTER, TextAnchor.CENTER, 0.0) );
            }
        }
        return ticks;
    }

    @Override
    public void drawGraphToGraphics(Graphics2D g, Rectangle2D r) {
        polarChart.setBackgroundPaint(null);
        polarChart.draw(g, r);
    }

    @Override
    public DiscreteFunctionTableModelAdapter getDataSet() {
        return dataset;
    }

    @Override
    public void datasetChanged(DatasetChangeEvent datasetChangeEvent) {
        polarChart.getPlot().datasetChanged(datasetChangeEvent);
        shiftAndRotateCoordinates( );
    }

    @Override
    public void saveChartImage() {
        ChartSaver.saveChart(polarChartPanel);
    }
}
