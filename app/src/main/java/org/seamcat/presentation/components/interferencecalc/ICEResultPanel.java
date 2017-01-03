package org.seamcat.presentation.components.interferencecalc;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.seamcat.model.engines.ICEConfiguration;
import org.seamcat.model.functions.Point2D;
import org.seamcat.presentation.SeamcatJFileChooser;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.tabulardataio.FileDataIO;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ICEResultPanel extends JPanel {
    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
    private static final Logger LOG = Logger.getLogger(ICEResultPanel.class);

    private final JFreeChart chart;
    private ChartPanel chartPanel;
    private final XYSeriesCollection dataset = new XYSeriesCollection();
    private final JFormattedTextField probability = new JFormattedTextField(new Double(0));
    private JLabel probabiliyLabel = new JLabel("Probability");

    private final XYSeries resultSeries = new XYSeries("");

    private JButton btnSaveResults = new JButton(STRINGLIST.getString("BTN_CAPTION_SAVE_TRANSLATION"));
    private ICEConfiguration iceconf = null;

    public ICEResultPanel() {
        super(new BorderLayout());

        probability.setColumns(6);
        probability.setHorizontalAlignment(SwingConstants.RIGHT);
        probability.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#00.00%"))));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(probabiliyLabel);
        top.add(probability);
        top.add(new JLabel(""));

        dataset.addSeries(resultSeries);

        chart = ChartFactory.createXYLineChart("", "Translation points (dBm or dB - depending on the selected translation parameter)", "Probability (%)", dataset, PlotOrientation.VERTICAL, true, true, false);

        XYPlot xyPlot = (XYPlot) chart.getPlot();
        chart.removeLegend();
        xyPlot.setRangeCrosshairVisible(true);
        xyPlot.setDomainCrosshairVisible(true);

        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domainAxis.setAutoRangeStickyZero(true);
        domainAxis.setAutoRangeIncludesZero(true);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(300, 170));
        chartPanel.setVerticalAxisTrace(false);
        chartPanel.setHorizontalAxisTrace(false);

        chart.setBackgroundPaint( this.getBackground() );
        chart.getPlot().setBackgroundPaint( Color.WHITE);
        chart.getXYPlot().setRangeGridlinePaint( this.getForeground());
        chart.getXYPlot().setDomainGridlinePaint( this.getForeground() );

        chart.getXYPlot().getDomainAxis().setTickLabelFont( this.getFont() );
        chart.getXYPlot().getDomainAxis().setTickLabelPaint( this.getForeground() );
        chart.getXYPlot().getDomainAxis().setLabelFont( this.getFont() );
        chart.getXYPlot().getDomainAxis().setLabelPaint( this.getForeground() );

        chart.getXYPlot().getRangeAxis().setTickLabelFont( this.getFont() );
        chart.getXYPlot().getRangeAxis().setTickLabelPaint( this.getForeground() );
        chart.getXYPlot().getRangeAxis().setLabelFont( this.getFont() );
        chart.getXYPlot().getRangeAxis().setLabelPaint( this.getForeground() );

        btnSaveResults.addActionListener(new SaveTranslationResultsActionListener());
        btnSaveResults.setEnabled(false);
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        savePanel.add(btnSaveResults);

        add(new BorderPanel(top,STRINGLIST.getString("ICECONFIG_COMPATIBILITY_TITLE")), BorderLayout.NORTH);
        add(new BorderPanel(chartPanel,STRINGLIST.getString("ICECONFIG_TRANSLATION_TITLE")), BorderLayout.CENTER);
        add(savePanel, BorderLayout.SOUTH);
    }

    public void addTranslationResult(Point2D point) {
        resultSeries.add(point.getX(), point.getY());
        dataset.seriesChanged(new SeriesChangeEvent(resultSeries));
    }

    public void init(ICEConfiguration iceconf) {
        this.iceconf = iceconf;
        if (iceconf.getHasBeenCalculated()) {
            if (iceconf.calculationModeIsTranslation()){
                probability.setEnabled(false);
                btnSaveResults.setEnabled(iceconf.calculationModeIsTranslation());
                List<Point2D> l = iceconf.getTranslationResults();
                resultSeries.clear();
                for (Point2D p : l) {
                    addTranslationResult(p);
                }
            }else{
                probability.setEnabled(true);
                probability.setValue(iceconf.getPropabilityResult());
            }
        } else {
            resultSeries.clear();
            if (iceconf.calculationModeIsTranslation()){
                probability.setEnabled(false);
                probability.setValue(null);
                chart.getXYPlot().setRangeGridlinePaint( this.getForeground());
                chart.getXYPlot().setDomainGridlinePaint( this.getForeground() );
                chart.getXYPlot().getDomainAxis().setTickLabelPaint( this.getForeground() );
                chart.getXYPlot().getDomainAxis().setLabelPaint( this.getForeground() );
                chart.getXYPlot().getRangeAxis().setTickLabelPaint( this.getForeground() );
                chart.getXYPlot().getRangeAxis().setLabelPaint( this.getForeground() );
                btnSaveResults.setEnabled(false);

            }else{
                probability.setEnabled(true);
                probability.setValue(null);
                chartPanel.setEnabled(false);
                chart.getXYPlot().setRangeGridlinePaint( Color.GRAY );
                chart.getXYPlot().setDomainGridlinePaint( Color.GRAY );
                chart.getXYPlot().getDomainAxis().setTickLabelPaint( Color.GRAY );
                chart.getXYPlot().getDomainAxis().setLabelPaint( Color.GRAY );
                chart.getXYPlot().getRangeAxis().setTickLabelPaint( Color.GRAY );
                chart.getXYPlot().getRangeAxis().setLabelPaint( Color.GRAY );
                btnSaveResults.setEnabled(false);

            }
        }
    }

    public void setProbabilityResult(double result) {
        probability.setEnabled(true);
        probability.setValue(result);
    }

    /**
     * SaveTranslationResultsActionListener
     * @author Thomas Thorndahl
     */
    private class SaveTranslationResultsActionListener implements ActionListener {

        private final JFileChooser chooser = new SeamcatJFileChooser();

        @Override
        public void actionPerformed(ActionEvent e) {
            FileDataIO fileIO = new FileDataIO();

            int returnVal = chooser.showSaveDialog(ICEResultPanel.this);

            File selectedFile = chooser.getSelectedFile();
            fileIO.setFile(selectedFile);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                fileIO.savePoints(iceconf.getTranslationResults());
            }
        }
    }

    public void setElementStatusEnabled(boolean value) {
        probabiliyLabel.setEnabled(value);
        probability.setEnabled(value);
        probability.setValue(null);
        if (value){
            resultSeries.clear();
            btnSaveResults.setEnabled(false);
            chart.getXYPlot().setRangeGridlinePaint( Color.GRAY );
            chart.getXYPlot().setDomainGridlinePaint( Color.GRAY );
            chart.getXYPlot().getDomainAxis().setTickLabelPaint( Color.GRAY );
            chart.getXYPlot().getDomainAxis().setLabelPaint( Color.GRAY );
            chart.getXYPlot().getRangeAxis().setTickLabelPaint( Color.GRAY );
            chart.getXYPlot().getRangeAxis().setLabelPaint( Color.GRAY );
        }else{
            chart.getXYPlot().setRangeGridlinePaint( this.getForeground());
            chart.getXYPlot().setDomainGridlinePaint( this.getForeground() );
            chart.getXYPlot().getDomainAxis().setTickLabelPaint( this.getForeground() );
            chart.getXYPlot().getDomainAxis().setLabelPaint( this.getForeground() );
            chart.getXYPlot().getRangeAxis().setTickLabelPaint( this.getForeground() );
            chart.getXYPlot().getRangeAxis().setLabelPaint( this.getForeground() );
        }
    }
}
