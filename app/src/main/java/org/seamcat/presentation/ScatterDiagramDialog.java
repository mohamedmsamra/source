package org.seamcat.presentation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.types.result.ScatterDiagramResultType;
import org.seamcat.presentation.components.SaveFileChooser;
import org.seamcat.presentation.displaysignal.ControlButtonPanel;
import org.seamcat.presentation.layout.VerticalSubPanelLayoutManager;
import org.seamcat.tabulardataio.FileDataIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ScatterDiagramDialog extends EscapeDialog {

    public ScatterDiagramDialog(final ScatterDiagramResultType scatter) {
        super(MainWindow.getInstance(), true);

        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries series = new XYSeries( scatter.getTitle() );
        for (Point2D point2D : scatter.getScatterPoints()) {
            series.add( point2D.getX(), point2D.getY() );
        }
        collection.addSeries( series );
        JFreeChart scatterPlot = ChartFactory.createScatterPlot(scatter.getTitle(), scatter.getxLabel(), scatter.getyLabel(), collection, PlotOrientation.VERTICAL, false, false, false);
        XYPlot p = scatterPlot.getXYPlot();
        scatterPlot.setBackgroundPaint(getBackground());
        p.setBackgroundPaint(Color.white);


        final ChartPanel chartPanel = new ChartPanel(scatterPlot);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));

        getContentPane().add(chartPanel, BorderLayout.CENTER);
        setTitle(scatter.getTitle());

        JPanel right = new JPanel();
        right.setLayout(new VerticalSubPanelLayoutManager());
        right.setPreferredSize(new Dimension(200, right.getPreferredSize().height));
        right.add(new ControlButtonPanel(this, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                FileDataIO fileIO = SaveFileChooser.chooseFile(getParent());
                if (fileIO != null) {
                    fileIO.savePoints(scatter.getScatterPoints());
                }

            }
        }, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChartSaver.saveChart(chartPanel);
            }
        }));
        getContentPane().add( right, BorderLayout.EAST );


        JDialog.setDefaultLookAndFeelDecorated(true);
        setPreferredSize(new Dimension(getPreferredSize().width, 475));
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

}
