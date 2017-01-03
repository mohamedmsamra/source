package org.seamcat.presentation.components;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.presentation.AclrDatasetWrapper;
import org.seamcat.presentation.ChartSaver;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class UnwantedEmissionGraph2 extends JPanel implements DatasetChangeListener {

	private static final Logger LOG = Logger.getLogger(UnwantedEmissionGraph2.class);
	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);

	private JFreeChart chart;
	private ChartPanel chartPanel;
	
	private DiscreteFunction2TableModelAdapter dataset;
	private AclrDatasetWrapper aclrDataset;

	private XYBarRenderer brenderer;
	private IntervalMarker victimMarker;
	private NumberAxis axis2;
	
	private boolean showACLR = false;
	private boolean aclrEnabled = false;
	
	private double frequencyOffset;
	private double victimBandwidth;
	
	public UnwantedEmissionGraph2(DiscreteFunction2TableModelAdapter dataset) {
		super(new GridLayout());
		this.dataset = dataset;
		chart = ChartFactory.createXYLineChart(
				STRINGLIST.getString("EMISSION_GRAPH_TITLE"), 
				STRINGLIST.getString("EMISSION_GRAPH_AXIX_TITLE_X"), 
				STRINGLIST.getString("EMISSION_GRAPH_AXIX_TITLE_Y"), 
				dataset,
		      PlotOrientation.VERTICAL, true, true, false
		);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
		renderer.setSeriesShapesVisible(0, true);
		renderer.setSeriesShapesFilled(0, true);

		axis2 = new NumberAxis("Normalized ACLR (dB)");
		XYPlot plot = chart.getXYPlot();

		plot.setRangeAxis(1, axis2);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

		aclrDataset = new AclrDatasetWrapper(dataset);

		plot.setDataset(1, aclrDataset);
		plot.mapDatasetToRangeAxis(1,1);

		brenderer = new XYBarRenderer();

		brenderer.setDrawBarOutline(true);
		brenderer.setSeriesFillPaint(0, new Color(0,183,239,100));
		brenderer.setSeriesItemLabelsVisible(0, true);
		brenderer.setSeriesItemLabelGenerator(0, aclrDataset);

		plot.setRenderer(1, brenderer);

		victimMarker = new IntervalMarker(-5.0, 5.0,  new Color(0, 0, 255, 25), new BasicStroke(0.5f), new Color(0, 0, 255, 25), new BasicStroke(1.0f),1.0f);
		victimMarker.setLabel("Victim System");
		victimMarker.setLabelFont(new Font("Dialog", Font.PLAIN, 11));
		victimMarker.setLabelTextAnchor(TextAnchor.TOP_CENTER);
		//	victimMarker.setLabelOffset(new RectangleInsets(2, 5, 2, 5));
		plot.addDomainMarker(victimMarker, Layer.FOREGROUND);

		dataset.addChangeListener(this);
		chartPanel = new ChartPanel(this.chart);
		chartPanel.setMouseZoomable(true, false);
		chartPanel.setPreferredSize(new Dimension(400, 350));
		chartPanel.getChart().getXYPlot().getRangeAxis().setAutoRange(true);
		DiscreteFunctionGraph.applyStyles(chartPanel, this.getFont(), true);
		add(chartPanel);
	}

	public void datasetChanged(DatasetChangeEvent dce) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Chart received DatasetChangeEvent");
		}
		
		if (showACLR) {
			EmissionMaskImpl f = dataset.getDiscreteFunction2();
			setAclrEnabled(f.getBounds().getMax() >= 0 && f.getBounds().getMin() <= 0, false);
			if (aclrEnabled) {
				aclrDataset.setDiscreteFunction2(f);
			}
		}
		
		ItemLabelPosition position = new ItemLabelPosition(ItemLabelAnchor.CENTER, TextAnchor.CENTER_RIGHT, TextAnchor.CENTER_RIGHT, Math.PI / 2);
		brenderer.setPositiveItemLabelPositionFallback(position);
      brenderer.setNegativeItemLabelPositionFallback(position);

      brenderer.setSeriesPositiveItemLabelPosition(0, position);
      brenderer.setSeriesNegativeItemLabelPosition(0, position);

      this.chart.getPlot().datasetChanged(dce);
	}
	
	public EmissionMaskImpl getDiscreteFunction2() {
		return dataset.getDiscreteFunction2();
	}

	public void setLabels(String xUnit, String yUnit) {
		chart.getXYPlot().getDomainAxis().setLabel(xUnit);
		chart.getXYPlot().getRangeAxis().setLabel(yUnit);
	}
	
	public void setAclrEnabled(boolean enabled, boolean refresh) {
		if (aclrEnabled ^ enabled || refresh) {
			this.aclrEnabled = enabled;
			// Aclr changed to enabled
			if (enabled) {
				chart.getXYPlot().setDataset(1, aclrDataset);
				chart.getXYPlot().setRangeAxis(1, axis2, true);
				chart.getXYPlot().mapDatasetToRangeAxis(1, 1);
			   if (frequencyOffset == -1) { //Signals Ignore value
			   	victimMarker.setStartValue(0);
			   	victimMarker.setEndValue(0);
			   	victimMarker.setAlpha(0f);
			   } else {
				   double start = frequencyOffset - (victimBandwidth / 2);
				   double end = frequencyOffset + (victimBandwidth / 2);

				   victimMarker.setStartValue(start);
				   victimMarker.setEndValue(end);
			   	victimMarker.setAlpha(1f);
			   }
			}
			// Aclr changed to disabled
			else {
				chart.getXYPlot().setRangeAxis(1, null, true);
				chart.getXYPlot().setDataset(1, null);
		   	victimMarker.setStartValue(0);
		   	victimMarker.setEndValue(0);
		   	victimMarker.setAlpha(0f);
			}
		}
	}

	public void setVictimCharacteristics(double victimBandwidth, double frequencyOffset, boolean showACLR) {
		this.showACLR = showACLR;
		this.frequencyOffset = frequencyOffset;
		this.victimBandwidth = victimBandwidth;
		setAclrEnabled(showACLR, true);
   }

	public void setInterferingBandwidth(double valueInMHz) {
	   aclrDataset.setInterfererBandwidth(valueInMHz);
	   repaint();
   }

	public void setAdjacentChannel(double valueInMHz) {
	   aclrDataset.setAdjacentChannel(valueInMHz);
	   repaint();
   }

	
	public void refreshChart() {
		if ( chart != null ) {
			chart.fireChartChanged();
		}
	}

	public void drawGraphToGraphics(Graphics2D g, Rectangle r) {
		chart.setBackgroundPaint(null);
		chart.draw(g, r);
   }

    public void saveChartImage() {
        ChartSaver.saveChart( chartPanel );
    }
}
