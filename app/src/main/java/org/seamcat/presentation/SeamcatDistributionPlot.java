package org.seamcat.presentation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.eventbus.Subscriber;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.RSSEvent;
import org.seamcat.events.VectorValues;
import org.seamcat.model.Workspace;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.core.ScenarioOutlineModel;
import org.seamcat.model.engines.SimulationListener;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.InterferenceLinkSimulation;
import org.seamcat.model.simulation.VictimSystemSimulation;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.simulation.result.SimulationResult;
import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.result.VectorResult;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.DiscreteFunctionGraph;
import org.seamcat.presentation.components.EventStatisticsPanel;
import org.seamcat.presentation.components.EventStatusPanel;
import org.seamcat.scenario.WorkspaceScenario;
import org.seamcat.simulation.cellular.CellularInterfererInterferenceLinkSimulation;
import org.seamcat.simulation.cellular.CellularVictimSystemSimulation;
import org.seamcat.simulation.generic.GenericVictimSystemSimulation;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableInterferenceLinkResults;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;

import static org.seamcat.model.simulation.result.SimulationResult.*;

public class SeamcatDistributionPlot extends JPanel implements SimulationListener {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);

    private final DecimalFormat formatter;
    private final JFreeChart chart;
    private final LightweightChartPanel chartPanel;
    private JPanel charts;
    private JPanel DataPanel;
    private EventStatisticsPanel eventStatsPanel;
    private EventStatusPanel eventStatusPanel;
    private int eventsToBeCalculated;
    private JSplitPane labelsPanel;

    private int percentageFactor;
    private int percentageFactor100;
    public static int maxEventsToPlot = 1000;

    private SeamcatDistributionPlotRssPanel rssPanel;
    private Workspace workspace;
    private ScenarioOutlineModel scenarioOutlineModel = new ScenarioOutlineModel();

    private JSplitPane splitPaneH;

    public SeamcatDistributionPlot(Workspace workspace) {
        super(true);

        rssPanel = new SeamcatDistributionPlotRssPanel(workspace.getSimulationControl().numberOfEvents());
        formatter = new DecimalFormat();
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(3);
        formatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        this.workspace = workspace;
        setLinkTitles();
        eventStatsPanel = new EventStatisticsPanel(workspace);
        initComponents();
        chart = ChartFactory.createScatterPlot(STRINGLIST.getString("SCENARIO_PLOT_TITLE"), STRINGLIST.getString("SCENARIO_PLOT_AXIX_TITLE_X"), STRINGLIST.getString("SCENARIO_PLOT_AXIX_TITLE_Y"), scenarioOutlineModel, PlotOrientation.VERTICAL, true, true, false);

        setToolTipGenerator();
        XYPlot xyPlot = (XYPlot) chart.getPlot();
        xyPlot.getRenderer().setSeriesPaint(3, new Color(255, 204, 51));

        xyPlot.setRangeCrosshairVisible(true);
        xyPlot.setDomainCrosshairVisible(true);

        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domainAxis.setAutoRangeStickyZero(true);
        domainAxis.setAutoRangeIncludesZero(true);

        chartPanel = new LightweightChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        chartPanel.setVerticalAxisTrace(false);
        chartPanel.setHorizontalAxisTrace(false);

        Font f = new Font(this.getFont().getName(), this.getFont().getStyle(), 10);

        DiscreteFunctionGraph.applyStyles(chartPanel, f, true);

        charts = new JPanel(new BorderLayout());

        charts.add(chartPanel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new BorderPanel(charts, STRINGLIST.getString("SCENARIO_TITLE")), BorderLayout.CENTER);

        splitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        splitPaneH.setLeftComponent(centerPanel);
        splitPaneH.setRightComponent(rssPanel);

        add(labelsPanel, BorderLayout.NORTH);
        add(splitPaneH, BorderLayout.CENTER);

        splitPaneH.setDividerLocation(800);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                splitPaneH.setDividerLocation(0.5);
            }
        });
        setModel(workspace.getScenarioOutlineModel());

        Subscriber.subscribe(this);
    }

    public void destroy() {
        eventStatusPanel.destroy();
    }

    private void setLinkTitles() {
        String reference = "<" + workspace.getVictimSystemLink().getName() + ">";
        scenarioOutlineModel.setVictimReceiverTitle(reference + " VLR");
        scenarioOutlineModel.setVictimTransmitterTitle(reference + " VLT");

        // Set the interferer if we only have one
        List<?> interferenceLinks = workspace.getInterferenceLinks();
        if (interferenceLinks.size() == 1) {
            reference = "<" + ((InterferenceLink) interferenceLinks.get(0)).getInterferingSystem().getName() + ">";
            scenarioOutlineModel.setInterferingTransmitterTitle(reference + " ILT");
            scenarioOutlineModel.setInterferingReceiverTitle(reference + " ILR");
        }
    }

    private void setModel(ScenarioOutlineModel scenarioOutlineModel) {
        if (scenarioOutlineModel != null) {
            this.scenarioOutlineModel = scenarioOutlineModel;
            chart.getXYPlot().setDataset(scenarioOutlineModel);
            setToolTipGenerator();
            setDataForRssCharts();
        }
    }

    private void setToolTipGenerator() {
        final DecimalFormat formatter = new DecimalFormat();
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(3);
        formatter.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));

        XYToolTipGenerator toolTipGenerator = new XYToolTipGenerator() {

            @Override
            public String generateToolTip(XYDataset xyDataset, int series, int item) {
                StringBuilder sb = new StringBuilder();
                String customText = "";

                String seriesString = xyDataset.getSeriesKey(series).toString();
                seriesString = seriesString.replaceAll("<", "&lt;");
                seriesString = seriesString.replaceAll(">", "&gt;");

                Number x = xyDataset.getX(series, item);
                Number y = xyDataset.getY(series, item);

                XYSeries xySeries = scenarioOutlineModel.getSeries(series);
                if (xySeries instanceof ExtendableXYSeries) {
                    ExtendableXYSeries xySeries2 = (ExtendableXYSeries) xySeries;
                    List<Argument> args = xySeries2.getArgsForPoint(x, y);
                    if (args != null) {
                        customText = argumentToHTMLTable(args);
                    }
                }

                sb.append("<html><body>").append(seriesString).append(" (");
                sb.append(formatter.format(x.doubleValue())).append(", ");
                sb.append(formatter.format(y.doubleValue()));
                sb.append(") ").append(customText).append("</body></html>");

                return sb.toString();
            }
        };

        chart.getXYPlot().getRenderer().setBaseToolTipGenerator(toolTipGenerator);
    }

    private String argumentToHTMLTable(List<Argument> args) {
        StringBuilder strb = new StringBuilder();
        strb.append("<table border=\"0\">");
        for (Argument argument : args) {
            if (argument != null) strb.append(argument.toHTMLTableRow());
        }
        strb.append("</table>");
        return strb.toString();
    }

    public void clearAllElements() {
        scenarioOutlineModel.clearAllElements();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        eventStatusPanel = new EventStatusPanel();

        labelsPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        labelsPanel.setDividerLocation(700);
        DataPanel = new JPanel();
        DataPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        labelsPanel.add(new BorderPanel(eventStatsPanel, STRINGLIST.getString("SIMULATION_STATUS_TITLE")));
        labelsPanel.add(new BorderPanel(eventStatusPanel, STRINGLIST.getString("EVENTS_STATUS_TITLE")));
    }

    private void addValue(final int eventNumber, final String type, final Double value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                rssPanel.getPanel(type).appendValue(eventNumber, value);
            }
        });
    }

    private void setDataForRssCharts() {
        LinkedHashMap<String, VectorResult> vectors = new LinkedHashMap<>();
        SimulationResult results = workspace.getSimulationResults();
        addIfPresent(results, vectors, DRSS);
        addIfPresent(results, vectors, IRSS_UNWANTED);
        addIfPresent(results, vectors, IRSS_BLOCKING);
        addIfPresent(results, vectors, IRSS_SELECTIVITY);

        int total = workspace.getSimulationControl().numberOfEvents();
        initialize(total);
        for (int eventNb = 0; eventNb < total; eventNb++) {
            if (signalFilter(eventNb)) {
                for (Map.Entry<String, VectorResult> entry : vectors.entrySet()) {
                    rssPanel.getPanel(entry.getKey()).appendValue(eventNb, entry.getValue().get(eventNb));
                }
            }
        }
        for (Map.Entry<String, VectorResult> entry : vectors.entrySet()) {
            panelStatistics(workspace.getSimulationResults(), entry.getKey());
        }
    }

    private void addIfPresent(SimulationResult results, LinkedHashMap<String, VectorResult> vectors, String group) {
        if (results.hasSeamcatGroup(group)) {
            vectors.put(group, results.getSeamcatResult(group).getResultTypes().getVectorResultTypes().get(0).getValue());
        }
    }

    public void addVictimResult(LinkResult result) {
        if (scenarioOutlineModel.getWantedTransmitterItemCount() < maxEventsToPlot) {
            scenarioOutlineModel.addToWantedTransmitterSeries(0, 0, wantedTransmitterArguments(result));
        }
        if (scenarioOutlineModel.getVictimReceiverItemCount() < maxEventsToPlot) {
            Point2D receiverPosition = result.rxAntenna().getPosition();
            scenarioOutlineModel.addToVictimReceiverSeries(receiverPosition.getX(), receiverPosition.getY(), victimReceiverArguments(result));
        }
    }

    public void addInterfererResult(MutableInterferenceLinkResult linkResult) {
        LinkResult interfererResult = linkResult.getInterferingSystemLink();

        if (scenarioOutlineModel.getInterferingTransmitterItemCount() < maxEventsToPlot) {
            Argument[] arrayIT = new Argument[15];
            arrayIT[0] = argument("Antenna height:", interfererResult.txAntenna().getHeight(), "m");
            arrayIT[1] = argument("Transmit power:", interfererResult.getTxPower(), "dBm");
            arrayIT[2] = argument("Frequency:", interfererResult.getFrequency(), "MHz");
            antennaGain(arrayIT, 3, "ILT to ILR", interfererResult.txAntenna().getGain(), interfererResult.txAntenna().getAzimuth(), interfererResult.txAntenna().getElevation());
            arrayIT[6] = argument("Pathloss to ILR :", interfererResult.getTxRxPathLoss(), "dB");
            arrayIT[7] = new Argument("--------", "--------", "--");
            antennaGain(arrayIT, 8, "ILT to VLR", linkResult.txAntenna().getGain(), linkResult.txAntenna().getAzimuth(), linkResult.txAntenna().getElevation());
            arrayIT[11] = argument("Pathloss to VLR :", linkResult.getTxRxPathLoss(), "dB");
            arrayIT[12] = argument("Effective Pathloss to VLR (with MCL):", linkResult.getEffectiveTxRxPathLoss(), "dB");
            arrayIT[13] = argument("blocking attenuation :", linkResult.getBlockingAttenuation(), "dB");
            Point2D transmitterPosition = interfererResult.txAntenna().getPosition();
            scenarioOutlineModel.addToInterferingTransmitterSeries(transmitterPosition.getX(), transmitterPosition.getY(), arrayIT);
        }

        if (scenarioOutlineModel.getWantedReceiverItemCount() < maxEventsToPlot) {
            Argument[] arrayIR = new Argument[4];
            arrayIR[0] = argument("Antenna height:", interfererResult.rxAntenna().getHeight(), "m");
            antennaGain(arrayIR, 1, "ILR to ILT", interfererResult.rxAntenna().getGain(), interfererResult.rxAntenna().getAzimuth(), interfererResult.rxAntenna().getElevation());
            Point2D receiverPosition = interfererResult.rxAntenna().getPosition();
            scenarioOutlineModel.addToWantedReceiverSeries(receiverPosition.getX(), receiverPosition.getY(), arrayIR);
        }
    }

    private Argument[] victimReceiverArguments(LinkResult result) {
        Argument[] arrayVR = new Argument[6];
        arrayVR[0] = argument("Antenna height:", result.rxAntenna().getHeight(), "m");
        arrayVR[1] = argument("Frequency:", result.getFrequency(), "MHz");
        antennaGain(arrayVR, 2, "VLR to VLT", result.rxAntenna().getGain(), result.rxAntenna().getAzimuth(), result.rxAntenna().getElevation());
        arrayVR[5] = argument("blocking attenuation :", result.getBlockingAttenuation(), "dB");
        return arrayVR;
    }

    private Argument[] wantedTransmitterArguments(LinkResult result) {
        Argument[] arrayVT = new Argument[7];
        arrayVT[0] = argument("Antenna height:", result.txAntenna().getHeight(), "m");
        arrayVT[1] = argument("Transmit power:", result.getTxPower(), "dBm");
        arrayVT[2] = argument("Frequency:", result.getFrequency(), "MHz");
        antennaGain(arrayVT, 3, "VLT to VLR", result.txAntenna().getGain(), result.txAntenna().getAzimuth(), result.txAntenna().getElevation());
        arrayVT[6] = argument("Pathloss to VLR :", result.getTxRxPathLoss(), "dB");
        return arrayVT;
    }

    private Argument argument(String title, Object value, String unit) {
        return new Argument(title, String.valueOf(formatter.format(value)), unit);
    }

    private void antennaGain(Argument[] target, int index, String prefix, double gain, double azimuth, double elevation) {
        target[index] = argument(prefix + " antenna gain:", gain, "dB");
        target[index + 1] = argument(prefix + " azimuth angle:", azimuth, "deg");
        target[index + 2] = argument(prefix + " elevation angle:", elevation, "deg");
    }

    public void addInterferingTransmitter(Point2D point, Argument... args) {
        if (scenarioOutlineModel.getInterferingTransmitterItemCount() < maxEventsToPlot) {
            scenarioOutlineModel.addToInterferingTransmitterSeries(point.getX(), point.getY(), args);
        }
    }

    public void addWantedReceiver(Point2D point, Argument... args) {
        if (scenarioOutlineModel.getWantedReceiverItemCount() < maxEventsToPlot) {
            scenarioOutlineModel.addToWantedReceiverSeries(point.getX(), point.getY(), args);
        }
    }

    public ScenarioOutlineModel getModel() {
        return scenarioOutlineModel;
    }

    private void initialize(int totalEvents) {
        percentageFactor = totalEvents / maxEventsToPlot;
        percentageFactor100 = totalEvents / 100;
        eventsToBeCalculated = totalEvents;
    }

    @Override
    public void preSimulate(final int totalEvents) {
        initialize(totalEvents);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                clearAllElements();

                rssPanel.reset();

                ((XYPlot) chart.getPlot()).getRangeAxis().setRange(-1.0, 1.0);
                ((XYPlot) chart.getPlot()).getRangeAxis().setAutoRange(true);
                ((XYPlot) chart.getPlot()).getDomainAxis().setRange(-1.0, 1.0);
                ((XYPlot) chart.getPlot()).getDomainAxis().setAutoRange(true);
                eventStatsPanel.startingEventGeneration(workspace);
                eventStatusPanel.startingEventGeneration(eventsToBeCalculated);
            }
        });
    }

    private boolean filter(int eventNo) {
        return eventsToBeCalculated < maxEventsToPlot || eventNo % (percentageFactor) == 0;
    }

    private boolean signalFilter(int eventNo) {
        return eventsToBeCalculated < 100 || eventNo % (percentageFactor100) == 0;
    }

    @Override
    public void eventComplete(final EventResult eventResult, final VictimSystemSimulation victimSimulation, List<InterferenceLinkSimulation> interferenceSimulations) {
        final int eventNo = eventResult.getEventNumber();
        if (filter(eventNo)) {
            final AbstractDmaSystem victim;
            if (victimSimulation instanceof CellularVictimSystemSimulation) {
                victim = ((CellularVictimSystemSimulation) victimSimulation).getVictim();
            } else {
                victim = null;
            }
            final List<AbstractDmaSystem> interferingSystems = new ArrayList<>();
            for (InterferenceLinkSimulation sims : interferenceSimulations) {
                if (sims instanceof CellularInterfererInterferenceLinkSimulation) {
                    interferingSystems.add(((CellularInterfererInterferenceLinkSimulation) sims).getSystem());
                }
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    MutableEventResult result = (MutableEventResult) eventResult;

                    eventStatusPanel.eventCompleted();
                    if (signalFilter(eventNo)) {
                        handleAddPoint(result);
                    }


                    WorkspaceScenario scenario = workspace.getScenario();
                    if (scenario.getVictimSystem() instanceof GenericSystem) {
                        addVictimResult(result.getVictimSystemLinks().get(0));
                    } else {
                        boolean uplink = victim.isUplink();
                        AbstractDmaBaseStation[][] cells = victim.getBaseStationArray();
                        for (int t = 0, stop = cells.length; t < stop; t++) {
                            Point2D position = cells[t][0].getPosition();
                            if (uplink) {
                                scenarioOutlineModel.addToVictimReceiverSeries(position.getX(), position.getY());
                            } else {
                                scenarioOutlineModel.addToWantedTransmitterSeries(position.getX(), position.getY());
                            }

                        }

                    }

                    List<org.seamcat.model.types.InterferenceLink> interferenceLinks = scenario.getInterferenceLinks();
                    for (org.seamcat.model.types.InterferenceLink link : interferenceLinks) {
                        if (!(link.getInterferingSystem() instanceof CellularSystem)) {
                            MutableInterferenceLinkResults linkResult = result.getInterferenceLinkResult(link);
                            for (MutableInterferenceLinkResult interferenceLinkResult : linkResult.getInterferenceLinkResults()) {
                                addInterfererResult(interferenceLinkResult);
                            }
                        }
                    }
                    for (AbstractDmaSystem dmasystem : interferingSystems) {
                        boolean uplink = dmasystem.isUplink();
                        AbstractDmaBaseStation[][] cells = dmasystem.getBaseStationArray();
                        for (AbstractDmaBaseStation[] cell : cells) {
                            if (uplink) {
                                addWantedReceiver(cell[0].getPosition());
                            } else {
                                addInterferingTransmitter(cell[0].getPosition());
                            }
                        }
                    }
                }
            });
        }
    }

    private void handleAddPoint(MutableEventResult result) {
        addIfExist(result, SimulationResult.DRSSVector, DRSS);
        addIfExist(result, GenericVictimSystemSimulation.IRSSU, IRSS_UNWANTED);
        addIfExist(result, CellularVictimSystemSimulation.UNW, IRSS_UNWANTED);
        addIfExist(result, GenericVictimSystemSimulation.IRSSB, IRSS_BLOCKING);
        addIfExist(result, CellularVictimSystemSimulation.SEL, IRSS_BLOCKING);
    }

    private void addIfExist(EventResult result, String name, String type) {
        Double value = result.getValue(name);
        if (value != null) {
            addValue(result.getEventNumber(), type, value);
        }
    }

    @Override
    public void postSimulate() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RSSEvent rssEvent = new RSSEvent(workspace);
                SimulationResult simulationResult = workspace.getSimulationResults();
                if (workspace.getVictimSystem() instanceof SystemModelGeneric) {
                    rssEvent.setRss(GenericVictimSystemSimulation.calculate(simulationResult.getSeamcatResult(DRSS).getResultTypes().getVectorResultTypes().get(0).getValue().asArray()));
                    rssEvent.setIrssU(GenericVictimSystemSimulation.calculate(simulationResult.getSeamcatResult(IRSS_UNWANTED).getResultTypes().getVectorResultTypes().get(0).getValue().asArray()));
                    rssEvent.setIrssB(GenericVictimSystemSimulation.calculate(simulationResult.getSeamcatResult(IRSS_BLOCKING).getResultTypes().getVectorResultTypes().get(0).getValue().asArray()));
                } 
                
                else {
                    rssEvent.setRss(new VectorValues("N/A", "N/A", "N/A"));
                    rssEvent.setIrssU(GenericVictimSystemSimulation.calculate(simulationResult.getSeamcatResult(IRSS_UNWANTED).getResultTypes().getVectorResultTypes().get(0).getValue().asArray()));
                    rssEvent.setIrssB(GenericVictimSystemSimulation.calculate(simulationResult.getSeamcatResult(IRSS_SELECTIVITY).getResultTypes().getVectorResultTypes().get(0).getValue().asArray()));
                }

                rssEvent.setCurrentEvent(workspace.getScenario().numberOfEvents());
                eventStatsPanel.handle(rssEvent);
                eventStatusPanel.eventGenerationCompleted();

                panelStatistics(simulationResult, DRSS);
                panelStatistics(simulationResult, IRSS_UNWANTED);
                panelStatistics(simulationResult, IRSS_BLOCKING);
                panelStatistics(simulationResult, IRSS_SELECTIVITY);
            }
        });
    }

    private void panelStatistics( SimulationResult simulationResult, String group ) {
        if (simulationResult.hasSeamcatGroup(group)) {
            double[] values = simulationResult.getSeamcatResult(group).getResultTypes().getVectorResultTypes().get(0).getValue().asArray();
            double mean = Mathematics.getAverage(values);
            double stdDev = Mathematics.getStdDev(values);
            rssPanel.getPanel(group).addStatistics(mean, stdDev);
        }
    }

    @UIEventHandler
    public void handle(RSSEvent event) {
        if ( event.getContext() == workspace ) {
            eventStatsPanel.handle(event);
        }
    }
}