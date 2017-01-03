package org.seamcat.presentation.components;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.seamcat.presentation.AntennaPatterns;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DiscreteFunctionGraph extends JPanel {

    public DiscreteFunctionGraph(DiscreteFunctionTableModelAdapter hor, DiscreteFunctionTableModelAdapter ver,
                                 DiscreteFunctionTableModelAdapter sph ) {
        super(new BorderLayout());
        JTabbedPane pane = new JTabbedPane(SwingConstants.BOTTOM);
        pane.addTab("Horizontal", new DiscreteFunctionXYPlot(hor, "Degree", "Gain (dBi)" ));
        pane.addTab("Horizontal (polar)", new DiscreteFunctionPolarPlot(hor, AntennaPatterns.HORIZONTAL));
        pane.addTab("Vertical", new DiscreteFunctionXYPlot(ver, "Degree", "Gain (dBi)"));
        pane.addTab("Vertical (polar)", new DiscreteFunctionPolarPlot(ver, AntennaPatterns.VERTICAL));
        pane.addTab("Spherical", new DiscreteFunctionXYPlot(sph, "Degree", "Gain (dBi)"));
        pane.addTab("Spherical (polar)", new DiscreteFunctionPolarPlot(sph, AntennaPatterns.SPHERICAL));

        add( pane, BorderLayout.CENTER );
    }

    public DiscreteFunctionGraph(DiscreteFunctionTableModelAdapter dataset, AntennaPatterns type, String xCaption, String yCaption ) {
        super(new BorderLayout());
        JTabbedPane pane = new JTabbedPane(SwingConstants.BOTTOM);
        pane.addTab("X & Y", new DiscreteFunctionXYPlot(dataset, xCaption, yCaption));
        pane.addTab("Polar", new DiscreteFunctionPolarPlot(dataset, type));
        add(pane, BorderLayout.CENTER);
    }

    public DiscreteFunctionGraph(DiscreteFunctionTableModelAdapter dataset, String xCaption, String yCaption ) {
        super(new BorderLayout());
        add(new DiscreteFunctionXYPlot(dataset, xCaption, yCaption), BorderLayout.CENTER);
    }

    public DiscreteFunctionTableModelAdapter getDataSet() {
        return getSelectedComponent().getDataSet();
    }


    public void setAxisNames(String xAxis, String yAxis) {
        getSelectedComponent().setAxisNames(xAxis, yAxis);
    }

    public static void applyStyles( ChartPanel chartPanel , Font font, boolean legend) {
        JFreeChart xyChart = chartPanel.getChart();
        if ( !legend ) xyChart.removeLegend();
        xyChart.setBackgroundPaint( chartPanel.getBackground() );

        XYPlot p = chartPanel.getChart().getXYPlot();
        p.setBackgroundPaint( Color.WHITE );
        p.setRangeGridlinePaint( chartPanel.getForeground() );
        p.setDomainGridlinePaint( chartPanel.getForeground() );

        p.getDomainAxis().setTickLabelFont( font);
        p.getDomainAxis().setTickLabelPaint( chartPanel.getForeground() );
        p.getDomainAxis().setLabelFont( font );
        p.getDomainAxis().setLabelPaint( chartPanel.getForeground() );

        p.getRangeAxis().setTickLabelFont( font );
        p.getRangeAxis().setTickLabelPaint( chartPanel.getForeground() );
        p.getRangeAxis().setLabelFont( font );
        p.getRangeAxis().setLabelPaint( chartPanel.getForeground() );

    }

    public void drawGraphToGraphics(Graphics2D g, Rectangle2D r) {
        getSelectedComponent().drawGraphToGraphics(g, r);
    }

    public void saveImage() {
        getSelectedComponent().saveChartImage();
    }

    private DiscreteFunctionPlot getSelectedComponent() {
        Component component = getComponent(0);
        if ( component instanceof JTabbedPane ) {
            return (DiscreteFunctionPlot) ((JTabbedPane) component).getSelectedComponent();
        } else {
            return (DiscreteFunctionPlot) component;
        }

    }
}
