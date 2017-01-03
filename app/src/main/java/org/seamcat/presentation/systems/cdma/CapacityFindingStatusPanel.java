package org.seamcat.presentation.systems.cdma;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.seamcat.eventbus.Subscriber;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.CapacityEndingTest;
import org.seamcat.events.CapacityEndingTrial;
import org.seamcat.events.CapacityStartingCapacityFinding;
import org.seamcat.events.CapacityStartingTest;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

@SuppressWarnings("serial")
public class CapacityFindingStatusPanel extends JPanel {

	private DefaultValueDataset numberOfTrialsData;
	private ChartPanel numberOfTrialsPanel;
	private MeterPlot numberOfTrialsPlot;
	private JFreeChart overallStatusChart;
	private DefaultCategoryDataset overallStatusData;
	private ChartPanel overallStatusPanel;
	private DefaultValueDataset successRateData;
	private ChartPanel successRatePanel;

	private MeterPlot successRatePlot;
	private int testCounter = 0;
	private boolean uplink;
	private DefaultValueDataset usersPerCellData;

	private ChartPanel usersPerCellPanel;
	private MeterPlot usersPerCellPlot;
    private Object context;

    public CapacityFindingStatusPanel( Object context ) {
		super();
        this.context = context;
        FormLayout formlayout1 = new FormLayout(
		      "FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE",
		      "CENTER:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:GROW(1.0),CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		setLayout(formlayout1);

		numberOfTrialsData = new DefaultValueDataset(0);
		successRateData = new DefaultValueDataset(0);
		usersPerCellData = new DefaultValueDataset(0);
		overallStatusData = new DefaultCategoryDataset();

		numberOfTrialsPlot = new MeterPlot(numberOfTrialsData);
		numberOfTrialsPlot.setUnits("Trials completed");
		numberOfTrialsPlot.setBackgroundPaint(Color.WHITE);
		numberOfTrialsPlot.setNeedlePaint(Color.BLACK);
		numberOfTrialsPlot.setOutlinePaint(Color.BLACK);
		numberOfTrialsPlot.setValuePaint(Color.BLUE);
		numberOfTrialsPlot.setTickPaint(Color.BLUE);
		numberOfTrialsPlot.setRange(new Range(0, 20));
		numberOfTrialsPlot.setDialShape(DialShape.CIRCLE);
		numberOfTrialsPlot.setDialBackgroundPaint(getBackground());
		numberOfTrialsPlot.setTickSize(2);
		numberOfTrialsPanel = new ChartPanel(new JFreeChart(numberOfTrialsPlot));

		usersPerCellPlot = new MeterPlot(usersPerCellData);
		usersPerCellPlot.setUnits("Users per Cell");
		usersPerCellPlot.setBackgroundPaint(Color.WHITE);
		usersPerCellPlot.setNeedlePaint(Color.BLACK);
		usersPerCellPlot.setOutlinePaint(Color.BLACK);
		usersPerCellPlot.setValuePaint(Color.BLUE);
		usersPerCellPlot.setTickPaint(Color.BLUE);
		usersPerCellPlot.setRange(new Range(0, 120));
		usersPerCellPlot.setDialShape(DialShape.CIRCLE);
		usersPerCellPlot.setDialBackgroundPaint(getBackground());
		usersPerCellPlot.setTickSize(10);
		usersPerCellPanel = new ChartPanel(new JFreeChart(usersPerCellPlot));

		successRatePlot = new MeterPlot(successRateData);
		successRatePlot.setRange(new Range(0, 20));
		successRatePlot.setBackgroundPaint(Color.WHITE);
		successRatePlot.setDialBackgroundPaint(getBackground());
		successRatePlot.setUnits("Successful trials");
		successRatePlot.setDialShape(DialShape.CIRCLE);
		successRatePlot.setNeedlePaint(Color.BLACK);
		successRatePlot.setValuePaint(Color.BLUE);
		successRatePlot.setTickSize(2);
		successRatePlot.setTickPaint(Color.BLUE);
		successRatePanel = new ChartPanel(new JFreeChart(successRatePlot));
		successRatePanel.getChart().removeLegend();

		overallStatusChart = ChartFactory.createStackedBarChart(
		      "Non interfered capacity finding status", "Users per cell",
		      "Successful Trials", overallStatusData, PlotOrientation.VERTICAL,
		      false, true, false);

		overallStatusPanel = new ChartPanel(overallStatusChart);

		add(numberOfTrialsPanel, cc.xy(2, 2));
		add(usersPerCellPanel, cc.xy(3, 2));
		add(successRatePanel, cc.xy(4, 2));
		add(overallStatusPanel, cc.xywh(2, 3, 3, 1));

		addFillComponents(this, new int[] { 1, 2, 3, 4 },
		      new int[] { 1, 2, 3, 4 });

		setBorder(new TitledBorder("CDMA Non-Interfered Capacity status"));

        Subscriber.subscribe(this);
	}

	/**
	 * Adds fill components to empty cells in the first row and first column of
	 * the grid. This ensures that the grid spacing will be the same as shown in
	 * the designer.
	 * 
	 * @param cols
	 *           an array of column indices in the first row where fill
	 *           components should be added.
	 * @param rows
	 *           an array of row indices in the first column where fill
	 *           components should be added.
	 */
	private void addFillComponents(Container panel, int[] cols, int[] rows) {
		Dimension filler = new Dimension(10, 10);

		boolean filled_cell_11 = false;
		CellConstraints cc = new CellConstraints();
		if (cols.length > 0 && rows.length > 0) {
			if (cols[0] == 1 && rows[0] == 1) {
				/** add a rigid area */
				panel.add(Box.createRigidArea(filler), cc.xy(1, 1));
				filled_cell_11 = true;
			}
		}

		for (int index = 0; index < cols.length; index++) {
			if (cols[index] == 1 && filled_cell_11) {
				continue;
			}
			panel.add(Box.createRigidArea(filler), cc.xy(cols[index], 1));
		}

		for (int index = 0; index < rows.length; index++) {
			if (rows[index] == 1 && filled_cell_11) {
				continue;
			}
			panel.add(Box.createRigidArea(filler), cc.xy(1, rows[index]));
		}

	}

    @UIEventHandler
    public void handleEndingTest( CapacityEndingTest event ) {
        if ( event.getContext() == context ) {
            overallStatusData.setValue(event.getSuccessRate(), "Row", ++testCounter + "#Users: " + event.getUsersPerCell());
        }
    }

    @UIEventHandler
    public void handleEndingTrial( CapacityEndingTrial event ) {
        if (uplink) {
            successRateData.setValue(event.getOutage());
        } else if (event.isSuccess()) {
            successRateData.setValue(successRateData.getValue().intValue() + 1);
        }
        numberOfTrialsData.setValue(event.getTrialid() + 1);
    }

    @UIEventHandler
    public void handleStarting( CapacityStartingCapacityFinding event ) {
        if ( event.getContext() == context ) {
            double allowableOutage = event.getAllowableOutage();
            int trials = event.getTrials();
            double target = event.getTarget();
            overallStatusChart.getCategoryPlot().clearRangeMarkers();
            successRatePlot.clearIntervals();
            uplink = event.isUplink();

            overallStatusChart.setBackgroundPaint( this.getBackground() );

            CategoryPlot p = overallStatusChart.getCategoryPlot();
            p.setBackgroundPaint( Color.WHITE );
            p.setRangeGridlinePaint( this.getForeground() );
            p.setDomainGridlinePaint( this.getForeground() );

            p.getDomainAxis().setTickLabelFont( this.getFont() );
            p.getDomainAxis().setTickLabelPaint( this.getForeground() );
            p.getDomainAxis().setLabelFont( this.getFont() );
            p.getDomainAxis().setLabelPaint( this.getForeground() );

            p.getRangeAxis().setTickLabelFont( this.getFont() );
            p.getRangeAxis().setTickLabelPaint( this.getForeground() );
            p.getRangeAxis().setLabelFont( this.getFont() );
            p.getRangeAxis().setLabelPaint( this.getForeground() );

            if (uplink) {
                overallStatusChart.getCategoryPlot().getRangeAxis().setLabel(
                      "Average Noiserise (dB)");
                overallStatusChart.getCategoryPlot().getRangeAxis().setRange(0, 10);
                successRatePlot.setRange(new Range(0, 20));
                successRatePlot.setUnits("dB of Average Noise-rise");
                successRatePlot.setTickSize(2);
                Marker marker;
                if (allowableOutage != 0.0) {
                    double min = target - allowableOutage;
                    double max = target;
                    marker = new IntervalMarker(min, max);
                    successRatePlot.addInterval(new MeterInterval(
                          "Try with more users per cell", new Range(0, min),
                          Color.BLACK, new BasicStroke(1), new Color(255, 85, 85)));
                    successRatePlot.addInterval(new MeterInterval(
                          "Non-interfered capacity found", new Range(min, max),
                          Color.BLACK, new BasicStroke(1), new Color(85, 255, 85)));
                    successRatePlot.addInterval(new MeterInterval(
                          "Try with fewer users per cell", new Range(max, 20),
                          Color.BLACK, new BasicStroke(1), new Color(255, 255, 85)));
                } else {
                    marker = new ValueMarker(target);
                    successRatePlot.addInterval(new MeterInterval(
                          "Try with more users per cell", new Range(0, target),
                          Color.BLACK, new BasicStroke(1), new Color(255, 85, 85)));
                    successRatePlot.addInterval(new MeterInterval(
                          "Try with fewer users per cell", new Range(target, 20),
                          Color.BLACK, new BasicStroke(1), new Color(255, 255, 85)));
                }
                marker.setPaint(new Color(180, 255, 255));
                overallStatusChart.getCategoryPlot().addRangeMarker(marker);
            } else {
                overallStatusChart.getCategoryPlot().getRangeAxis()
                      .setRange(0, trials);
                overallStatusChart.getCategoryPlot().getRangeAxis().setLabel(
                      "Successful Trials");
                overallStatusChart.getCategoryPlot().addRangeMarker(
                      new ValueMarker(trials * target));
                successRatePlot.setRange(new Range(0, trials));
                successRatePlot.setUnits("Successful trials");
                successRatePlot.setTickSize((double)(trials) / 10);
                successRatePlot.addInterval(new MeterInterval(
                      "Try with fewer users per cell", new Range(0,
                            (int) (trials * 0.8 - 1)), Color.BLACK,
                      new BasicStroke(1), new Color(255, 85, 85)));
                successRatePlot.addInterval(new MeterInterval(
                      "Non-interfered capacity found", new Range(
                            (int) (trials * 0.8 - 1), (int) (trials * 0.8 + 1)),
                      Color.BLACK, new BasicStroke(1), new Color(85, 255, 85)));
                successRatePlot.addInterval(new MeterInterval(
                      "Try with more users per cell", new Range(
                            (int) (trials * 0.8 + 1), trials), Color.BLACK,
                      new BasicStroke(1), new Color(255, 255, 85)));

            }
            overallStatusData.clear();
            testCounter = 0;
        }
    }

    @UIEventHandler
    public void handleStartingTest(CapacityStartingTest event ) {
        if ( event.getContext() == context ) {
            // Reset meters
            numberOfTrialsData.setValue(0);
            numberOfTrialsPlot.setRange(new Range(0, event.getTrials()));
            numberOfTrialsPlot.setTickSize((double)(event.getTrials()) / 10);
            numberOfTrialsPlot.setTickLabelsVisible(true);
            usersPerCellData.setValue(event.getUsersPrCell());
            successRateData.setValue(0);
        }
    }
}
