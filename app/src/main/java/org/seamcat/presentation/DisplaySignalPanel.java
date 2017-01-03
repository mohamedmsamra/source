package org.seamcat.presentation;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.LogAxis;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.seamcat.presentation.components.SaveFileChooser;
import org.seamcat.presentation.displaysignal.ControlButtonPanel;
import org.seamcat.presentation.displaysignal.DisplaySelectorPanel;
import org.seamcat.presentation.displaysignal.GroupSelectorPanel;
import org.seamcat.presentation.displaysignal.IdentificationPanel;
import org.seamcat.presentation.layout.VerticalSubPanelLayoutManager;
import org.seamcat.presentation.propagationtest.PropagationHolder;
import org.seamcat.tabulardataio.DataResultType;
import org.seamcat.tabulardataio.FileDataIO;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DisplaySignalPanel extends JPanel implements DisplaySignal {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
    private static final int EVENT_LIMIT = Integer.parseInt(STRINGLIST.getString("VECTOR_GRAPH_EVENT_LIMIT"));

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel graphPanel = new JPanel(cardLayout);
    private JPanel right;

    private IdentificationPanel idPanel = new IdentificationPanel();
    private final DisplaySelectorPanel selector = new DisplaySelectorPanel(this);
    private final GroupSelectorPanel groupSelectorPanel = new GroupSelectorPanel();

    private ChartPanel vectorChart;
    private ChartPanel vectorLogChart;
    private ChartPanel densityChart;
    private ChartPanel cumulativeChart;

    private List<PropagationHolder> propagations;

    private boolean hasSortedDistributions = true;
    private boolean signalIsConstant = false;
    private boolean showLegend = true;
    private boolean isUniform;
    private double minDataValue;
    private double maxDataValue;
    private int limit;
    private String label;
    private String unit;
    private String mtitle;
    private String xTitle;
    private String yTitle;
    private EscapeDialog owner;
    private boolean displayIndentificationPanel = true;
    private boolean displayDataSelectionPanel = true;

    /**
     *
     * @param owner
     *           if the owner is a window we show the close/save controls and
     *           call dispose on the windows on exit
     * @param xTitle
     * @param yTitle
     */
    public DisplaySignalPanel(EscapeDialog owner, String xTitle, String yTitle) {
        setLayout(new BorderLayout());

        this.xTitle = xTitle;
        this.yTitle = yTitle;
        this.owner = owner;
    }

    public void show(double[] _data, String title, String unit) {
        hasSortedDistributions = false;
        show(_data, null, title, unit, null);
    }

    public void show(List<PropagationHolder> propagations, String title, String unit) {
        hasSortedDistributions = false;
        show(propagations, title, unit, label, EVENT_LIMIT, -1d, -1d);

    }

    public void show(double[] _data, double[] sortedDistributions, String title, String unit) {
        show(_data, sortedDistributions, title, unit, null);
    }

    public void show(double[] _data, String title, String unit, double minDataValue, double maxDataValue) {
        hasSortedDistributions = false;
        show(_data, null, title, unit, null, minDataValue, maxDataValue);
    }

    public void show(double[] _data, double[] sortedDistributions, String title, String unit, double minDataValue, double maxDataValue) {
        show(_data, sortedDistributions, title, unit, null, minDataValue, maxDataValue);
    }

    public void show(double[] _data, String title, String unit, String label) {
        show(_data, null, title, unit, label, -1d, -1d);
    }

    public void show(double[] _data, double[] sortedDistributions, String title, String unit, String label) {
        show(_data, sortedDistributions, title, unit, label, -1d, -1d);
    }

    public void show(double[] _data, double[] sortedDistributions, String title, String unit, String label, double max, double min) {
        show(createList(_data, sortedDistributions, title), title, unit, label, max, min);
    }

    public void show(List<PropagationHolder> propagations, String title, String unit, String label, double max, double min, int limit) {
        show(propagations, title, unit, label, limit, min, max);
    }

    public void show(List<PropagationHolder> propagations, String title, String unit, String label, double max, double min) {
        show(propagations, title, unit, label, EVENT_LIMIT, min, max);
    }

    public void show(List<PropagationHolder> propagations, String title, String unit, String label, int limit, double minDataValue, double maxDataValue) {
        this.propagations = propagations;
        this.maxDataValue = maxDataValue;
        this.minDataValue = minDataValue;
        this.label = label;
        this.limit = limit;
        this.unit = unit;
        this.mtitle = title;

        isUniform = minDataValue != maxDataValue;

        initPropagations();
        createCharts();

        addControls(owner);
        groupSelectorPanel.setListData(propagations);
        setListeners();
        setRanges();

        selector.getCdf().doClick();
        if ( displayIndentificationPanel && propagations != null && propagations.size() >0 ) {
            setIdentificationPanel( propagations.get(0));
        }
    }

    private List<PropagationHolder> createList(double[] _data, double[] sortedDistributions, String title) {
        List<PropagationHolder> propagations = new ArrayList<PropagationHolder>();
        if (_data != null) {
            PropagationHolder propagationHolder = new PropagationHolder();
            propagationHolder.setData(_data);
            propagationHolder.setSortedDistributions(sortedDistributions);
            propagationHolder.setTitle(title);
            propagations.add(propagationHolder);
        }
        return propagations;
    }

    private void addControls(EscapeDialog parent) {
        right = new JPanel();
        right.setLayout(new VerticalSubPanelLayoutManager());
        right.setPreferredSize(new Dimension(200, right.getPreferredSize().height));

        if (displayDataSelectionPanel) {
            right.add(groupSelectorPanel);
        }
        right.add(selector);
        if (displayIndentificationPanel) {
            right.add(idPanel);
        }

        right.add(new ControlButtonPanel(parent, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileDataIO fileIO = SaveFileChooser.chooseFile(DisplaySignalPanel.this.getParent());
                if (fileIO != null) {
                    DataResultType type = DataResultType.vector;
                    if (selector.getVector().isSelected() && hasSortedDistributions) {
                        type = DataResultType.graph;
                    } else if (selector.getCdf().isSelected()) {
                        type = DataResultType.cdf;
                    } else if (selector.getDensity().isSelected()) {
                        type = DataResultType.pdf;
                        for (PropagationHolder holder : DisplaySignalPanel.this.getPropagationHolders()) {
                            holder.binCount = selector.getBinSize();
                        }
                    }
                    fileIO.savePropogationHolders(type, DisplaySignalPanel.this.getPropagationHolders());
                }
            }
        }, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selector.getVector().isSelected()) {
                    ChartSaver.saveChart( vectorChart );
                } else if (selector.getCdf().isSelected()) {
                    ChartSaver.saveChart(cumulativeChart);
                } else if (selector.getDensity().isSelected()) {
                    ChartSaver.saveChart(densityChart);
                }
            }
        }));

        add(right, BorderLayout.EAST);
    }

    private void createCharts() {
        removeAll();
        // get datasets
        XYSeriesCollection collection = DialogDisplaySignalHelper.getVectorSeriesCollection(propagations);

        XYSeriesCollection cumulativeCollection = DialogDisplaySignalHelper.getCumulativeSeriesCollection(propagations);

        // create charts with datasets
        boolean legend = showLegend;
        if ( propagations.size() > 6 ) {
            legend = false;
        }
        vectorChart = SignalChartFactory.createVectorChart(xTitle, yTitle, this.getFont(), collection, legend);
        vectorLogChart = SignalChartFactory.createVectorLogChart(xTitle, yTitle, null, EVENT_LIMIT, this.getFont());
        cumulativeChart = SignalChartFactory.createCumulativeChart(xTitle, yTitle, cumulativeCollection, this.getFont(), signalIsConstant, mtitle, unit);

        if (!signalIsConstant) {
            HistogramDataset histogramDataset = DialogDisplaySignalHelper.getDensityHistogram(propagations);
            if ( propagations.isEmpty() ) {
                selector.setBinSize( 10 );
            } else {
                selector.setBinSize( propagations.get(0).getDensityDataSeries().bin );
            }
            densityChart = SignalChartFactory.createDensityGraph(xTitle, unit, this.getFont(), histogramDataset);
            graphPanel.add(densityChart, "pdf");
        }

        // add to panel
        graphPanel.add(vectorChart, "vector");
        graphPanel.add(vectorLogChart, "vectorLog");
        graphPanel.add(cumulativeChart, "cumulative");

        add(graphPanel, BorderLayout.CENTER);

        setUpGraphLabels();
        setVisible(true);
        showVectorButtons(hasSortedDistributions);
    }

    public void showVectorButtons(boolean flag) {
        selector.getVectorLinear().setVisible(flag);
        selector.getVectorLog().setVisible(flag);
    }

    private void setUpGraphLabels() {

        // Set number format for log graphs
        DecimalFormat format = new DecimalFormat("0.000");
        DecimalFormatSymbols decimalSymbol = new DecimalFormatSymbols(Locale.getDefault());
        decimalSymbol.setDecimalSeparator('.');
        format.setDecimalFormatSymbols(decimalSymbol);

        ((LogAxis) vectorLogChart.getChart().getXYPlot().getDomainAxis()).setNumberFormatOverride(format);

        vectorChart.getChart().setTitle(mtitle);
        vectorLogChart.getChart().setTitle(mtitle);
        vectorChart.getChart().getXYPlot().getRangeAxis().setLabel(unit);
        vectorLogChart.getChart().getXYPlot().getRangeAxis().setLabel(unit);
        if (label != null) {
            vectorChart.getChart().getXYPlot().getDomainAxis().setLabel(label);
            vectorLogChart.getChart().getXYPlot().getDomainAxis().setLabel(label);
        } else {
            vectorChart.getChart().getXYPlot().getDomainAxis().setLabel("Events");
            vectorLogChart.getChart().getXYPlot().getDomainAxis().setLabel("Events");
        }

    }

    private void setIdentificationPanel(PropagationHolder model) {
        if (model != null) {
            idPanel.setModel(model);
        }
    }

    /**
     * Sets up the propagation details. Max, min, mean and so on.. order is
     * important
     */
    private void initPropagations() {
        hasSortedDistributions = !propagations.isEmpty() && propagations.get(0).getSortedDistributions() != null;
        DialogDisplaySignalHelper.setDataLimit(limit, propagations);
        DialogDisplaySignalHelper.setPropagationStatistics(propagations, minDataValue, maxDataValue, isUniform);
        signalIsConstant = allConstant();

        DialogDisplaySignalHelper.setDataSets(propagations, signalIsConstant);
    }

    private boolean allConstant() {
        if ( !propagations.isEmpty()) {
            for (PropagationHolder propagation : propagations) {
                if ( !idPanel.formatDouble(propagation.getStandardDeviation()).equals(idPanel.formatDouble(0.00000)) ) {
                    return false;
                }
            }
        }
        return false;
    }

    private void setRanges() {
        if (!propagations.isEmpty()) {
            DialogDisplaySignalHelper.setVectorDataRange(propagations, signalIsConstant, vectorChart);
            DialogDisplaySignalHelper.setLogDataRange(propagations, isUniform, minDataValue, maxDataValue, vectorLogChart, hasSortedDistributions);
        }
    }

    public List<PropagationHolder> getPropagationHolders() {
        return propagations;
    }

    private void setVisibleDataSeries(List<GroupSelectorPanel.GroupListItem> propagations) {
        List<PropagationHolder> holders = new ArrayList<PropagationHolder>();
        for (GroupSelectorPanel.GroupListItem propagation : propagations) {
            holders.add( propagation.getPropagation() );
        }

        XYSeriesCollection vectorCollection = DialogDisplaySignalHelper.getVectorSeriesCollection(holders);
        XYSeriesCollection cumulativeCollection = DialogDisplaySignalHelper.getCumulativeSeriesCollection(holders);
        HistogramDataset desityHistogram = DialogDisplaySignalHelper.getDensityHistogram(holders);
        vectorChart.getChart().getXYPlot().setDataset(vectorCollection);
        vectorLogChart.getChart().getXYPlot().setDataset(vectorCollection);
        cumulativeChart.getChart().getXYPlot().setDataset(cumulativeCollection);
        if (densityChart != null) {
            densityChart.getChart().getXYPlot().setDataset(desityHistogram);
        }
    }

    private void setListeners() {
        selector.getVector().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (hasSortedDistributions) {
                    selector.getVectorLinear().setEnabled(true);
                    selector.getVectorLog().setEnabled(true);
                }
                if (selector.getVectorLog().isVisible() && selector.getVectorLog().isEnabled() && selector.getVectorLog().isSelected()) {
                    cardLayout.show(graphPanel, "vectorLog");
                } else {
                    if (selector.getVectorLinear().isEnabled()) {
                        selector.getVectorLinear().setSelected(true);
                    }
                    cardLayout.show(graphPanel, "vector");
                }
            }
        });
        selector.getVectorLinear().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!selector.getVector().isSelected()) {
                    selector.getVector().setSelected(true);
                }
                cardLayout.show(graphPanel, "vector");
            }
        });
        selector.getVectorLog().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!selector.getVector().isSelected()) {
                    selector.getVector().setSelected(true);
                }
                cardLayout.show(graphPanel, "vectorLog");
                groupSelectorPanel.setListData(propagations);
            }
        });
        selector.getCdf().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (selector.getVectorLinear().isEnabled()) {
                    selector.getVectorLinear().setEnabled(false);
                    selector.getVectorLog().setEnabled(false);
                }
                cardLayout.show(graphPanel, "cumulative");
            }
        });
        selector.getDensity().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                density();
            }
        });

        groupSelectorPanel.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    JList jlist = (JList) event.getSource();
                    List<GroupSelectorPanel.GroupListItem> list = new ArrayList<GroupSelectorPanel.GroupListItem>();
                    for ( Object elem : jlist.getSelectedValuesList() ) {
                        list.add((GroupSelectorPanel.GroupListItem) elem);
                    }
                    setVisibleDataSeries( list );
                    setIdentificationPanel(averagePropgationsForIdentificationPanel(list));
                }
            }
        });
    }

    public void density() {
        if (selector.getVectorLinear().isEnabled()) {
            selector.getVectorLinear().setEnabled(false);
            selector.getVectorLog().setEnabled(false);
        }
        if ( selector.getDensity().isSelected() ) {
            if (signalIsConstant) {
                JOptionPane.showMessageDialog(DisplaySignalPanel.this, STRINGLIST.getString("HISTOGRAM_CONSTANTSIGNAL_WARNING"));
                selector.getVector().setSelected(true);
                cardLayout.show(graphPanel, "vector");
            } else {
                // update histogram
                int bin = selector.getBinSize();
                HistogramDataset data = DialogDisplaySignalHelper.getDensityHistogram(propagations, bin);
                if (densityChart != null) {
                    densityChart.getChart().getXYPlot().setDataset(data);
                }
                cardLayout.show(graphPanel, "pdf");
            }
        }
    }

    PropagationHolder averagePropgationsForIdentificationPanel(List<GroupSelectorPanel.GroupListItem> propagations) {
        PropagationHolder newPropagationHolder = new PropagationHolder();
        double mean = 0;
        double median = 0;
        double stdev = 0;
        double min = 0;
        double max = 0;
        double variance = 0;
        for (GroupSelectorPanel.GroupListItem propagation : propagations) {
            mean = mean + propagation.getPropagation().getAverage();
            median = median + propagation.getPropagation().getMedian();
            stdev = stdev + propagation.getPropagation().getStandardDeviation();
            min = min + propagation.getPropagation().getMin();
            max = max + propagation.getPropagation().getMax();
            variance += propagation.getPropagation().getVariance();
        }
        newPropagationHolder.setAverage(mean / propagations.size());
        newPropagationHolder.setMedian(median / propagations.size());
        newPropagationHolder.setStandardDeviation(stdev / propagations.size());
        newPropagationHolder.setVariance(variance / propagations.size());
        newPropagationHolder.setMin(min / propagations.size());
        newPropagationHolder.setMax(max / propagations.size());
        return newPropagationHolder;

    }

    public void displayDataSelectionPanel(boolean displayDataSelectionPanel) {
        this.displayDataSelectionPanel = displayDataSelectionPanel;
    }

    public void reset() {
        groupSelectorPanel.reset();
        vectorChart.getChart().getXYPlot().setDataset(null);
        vectorLogChart.getChart().getXYPlot().setDataset(null);
        cumulativeChart.getChart().getXYPlot().setDataset(null);
        densityChart.getChart().getXYPlot().setDataset(null);

    }

    public void setSelectPanelTitle(String title) {
        groupSelectorPanel.setBorderTitle(title);
    }

}
