package org.seamcat.presentation.compareVector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.seamcat.model.types.result.BarChartResultType;
import org.seamcat.model.types.result.BarChartValue;
import org.seamcat.presentation.ChartSaver;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.SaveFileChooser;
import org.seamcat.presentation.displaysignal.ControlButtonPanel;
import org.seamcat.presentation.layout.VerticalSubPanelLayoutManager;
import org.seamcat.tabulardataio.FileDataIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BarChartDialog extends EscapeDialog {

    private final DefaultCategoryDataset categoryDataset = new DefaultCategoryDataset();
    private ChartPanel barChart;


    public BarChartDialog( final BarChartResultType barChart ) {
        JFreeChart chart = ChartFactory.createBarChart(barChart.getTitle(), barChart.getxLabel(), barChart.getyLabel(), categoryDataset, PlotOrientation.VERTICAL, true, true, false);
        this.barChart = new ChartPanel(chart);
        this.barChart.setMouseZoomable(true, false);

        this.barChart.getChart().setBackgroundPaint(this.getBackground());
        CategoryPlot p = this.barChart.getChart().getCategoryPlot();
        p.setBackgroundPaint( Color.white );
        p.getDomainAxis().setTickLabelPaint( this.barChart.getForeground() );
        p.getDomainAxis().setLabelPaint( this.barChart.getForeground() );
        p.getRangeAxis().setTickLabelPaint( this.barChart.getForeground() );
        p.getRangeAxis().setLabelPaint( this.barChart.getForeground() );

        for (BarChartValue value : barChart.getChartPoints()) {
            categoryDataset.addValue( value.getValue(), barChart.getTitle(), value.getName() );
        }

        setLayout(new BorderLayout());
        getContentPane().add(this.barChart, BorderLayout.CENTER );

        JPanel right = new JPanel();
        right.setLayout(new VerticalSubPanelLayoutManager());
        right.setPreferredSize(new Dimension(200, right.getPreferredSize().height));
        right.add(new ControlButtonPanel(this, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                FileDataIO fileIO = SaveFileChooser.chooseFile(getParent());
                if (fileIO != null) {
                    fileIO.saveValues(barChart.getChartPoints());
                }

            }
        }, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChartSaver.saveChart(BarChartDialog.this.barChart);
            }
        }));
        getContentPane().add( right, BorderLayout.EAST );


        setTitle(barChart.getTitle());

        JDialog.setDefaultLookAndFeelDecorated(true);
        pack();
        setLocationRelativeTo(MainWindow.getInstance());
        setVisible(true);
    }

}
