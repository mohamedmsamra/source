package org.seamcat.presentation.components;

import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.FunctionType;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.presentation.AntennaPatterns;
import org.seamcat.presentation.DialogTableToDataSet;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserDefinedFunctionPanel extends JPanel {

    private JTable dataTable;
    private DiscreteFunctionGraph functionGraph;
    private double symmetryPoint = 0;
    private boolean usePropabilitySymmetrizeFunction = false;

    TableRowSorter<TableModel> sorter;

    public UserDefinedFunctionPanel(String borderTitle, String x, String y) {
        super(new GridBagLayout());
        functionGraph = new DiscreteFunctionGraph(new DiscreteFunctionTableModelAdapter(), x,y);

        dataTable = new SeamcatTable( functionGraph.getDataSet());
        JTableHeader header = dataTable.getTableHeader();
        header.setUpdateTableInRealTime(true);

        sorter = new MyTableRowSorter(functionGraph.getDataSet());
        dataTable.setRowSorter(sorter);

        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);

        JScrollPane dataTableScrollPane = new JScrollPane(dataTable);
        Dimension paneDims = new Dimension(150, 350);
        dataTableScrollPane.setMinimumSize(paneDims);
        dataTableScrollPane.setPreferredSize(paneDims);
        dataTableScrollPane.setMaximumSize(paneDims);

        GridBagConstraints constr = new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0);

        add(dataTableScrollPane, constr);
        add(new DialogFunctionButtonPanel(functionGraph.getDataSet()), constr);
        constr.fill = GridBagConstraints.BOTH;
        constr.weightx = 50;
        constr.weighty = 50;
        add(functionGraph, constr);
        setBorder(new TitledBorder(borderTitle));
    }

    public UserDefinedFunctionPanel(Function function, FunctionType functionType, String x, String y) {
        super(new GridBagLayout());
        boolean polarPlot = !functionType.isNone();
        if (polarPlot) {
            AntennaPatterns type = AntennaPatterns.HORIZONTAL;
            if ( functionType.isHorizontal() ) {
                type = AntennaPatterns.HORIZONTAL;
            } else if ( functionType.isVertical()) {
                type = AntennaPatterns.VERTICAL;
            } else if ( functionType.isSpherical()) {
                type = AntennaPatterns.SPHERICAL;
            }
            functionGraph = new DiscreteFunctionGraph(new DiscreteFunctionTableModelAdapter(), type, x, y);
        } else {
            functionGraph = new DiscreteFunctionGraph(new DiscreteFunctionTableModelAdapter(), x, y);
        }

        dataTable = new SeamcatTable( functionGraph.getDataSet());
        JTableHeader header = dataTable.getTableHeader();
        header.setUpdateTableInRealTime(true);

        sorter = new MyTableRowSorter(functionGraph.getDataSet());
        dataTable.setRowSorter(sorter);

        ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);

        JScrollPane dataTableScrollPane = new JScrollPane(dataTable);
        Dimension paneDims = new Dimension(150, 350);
        dataTableScrollPane.setMinimumSize(paneDims);
        dataTableScrollPane.setPreferredSize(paneDims);
        dataTableScrollPane.setMaximumSize(paneDims);

        GridBagConstraints constr = new GridBagConstraints(GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0);

        add(dataTableScrollPane, constr);
        add(new DialogFunctionButtonPanel(functionGraph.getDataSet()), constr);
        constr.fill = GridBagConstraints.BOTH;
        constr.weightx = 50;
        constr.weighty = 50;
        add(functionGraph, constr);
        setBorder(new TitledBorder(functionType.getTitle()));
        setDiscreteFunction((DiscreteFunction) function);
    }

    public void clear() {
        setDiscreteFunction(new DiscreteFunction());
    }

    public DiscreteFunction getDiscreteFunction() {
        return functionGraph.getDataSet().getFunction();
    }

    public TableModel getModel() {
        return functionGraph.getDataSet();
    }

    public void stopEditing() {
        if (dataTable.isEditing()) {
            dataTable.getCellEditor().stopCellEditing();
        }
    }

    public void setDiscreteFunction(DiscreteFunction d) {
        functionGraph.getDataSet().setDiscreteFunction(d);
    }

    public void setUsePropabilitySymmetrizeFunction(boolean usePropabilitySymmetrizeFunction) {
        this.usePropabilitySymmetrizeFunction = usePropabilitySymmetrizeFunction;
    }

    private class DialogFunctionButtonPanel extends FunctionButtonPanel {

        public DialogFunctionButtonPanel( DiscreteFunctionTableModelAdapterInterface model ) {
            super(model);
        }

        @Override
        public void saveChartImage() {
            functionGraph.saveImage();
        }

        @Override
        public void btnDeleteActionPerformed() {
            functionGraph.getDataSet().deleteRow(dataTable.getSelectedRow());
        }

        @Override
        public void btnSaveActionPerformed() {
            stopEditing();
            super.btnSaveActionPerformed();
        }

        @Override
        public void btnSymActionPerformed() {
            stopEditing();
            checkEntries();
            if (usePropabilitySymmetrizeFunction) {
                DialogTableToDataSet.symmetrizeFunction(functionGraph.getDataSet().getFunction(), symmetryPoint);
            } else {
                DialogTableToDataSet.symmetrize(functionGraph.getDataSet().getFunction().points(), symmetryPoint);
            }
            functionGraph.getDataSet().sortPoints();
            functionGraph.getDataSet().fireChangeListeners();

        }

        private void checkEntries() {
            List<Point2D> points = new ArrayList<>();
            points.addAll(functionGraph.getDataSet().getFunction().points());
            boolean hasBeenChanged = false;
            for (int i = 1; i < points.size(); i++) {
                if (Mathematics.equals(points.get(i).getX(), points.get(i - 1).getX(), 0.00001)) {
                    points.set(i, new Point2D(points.get(i).getX() + 0.0001, points.get(i).getY()));
                    hasBeenChanged = true;
                }
            }
            if (hasBeenChanged)
                setDiscreteFunction((DiscreteFunction) Factory.functionFactory().discreteFunction(points));
        }
    }

    public void setAxisNames(String xAxis, String yAxis) {
        functionGraph.setAxisNames(xAxis, yAxis);
    }

    private class MyTableRowSorter extends TableRowSorter<TableModel> {
        DiscreteFunctionTableModelAdapter model;
        private boolean sort = true;

        public MyTableRowSorter(DiscreteFunctionTableModelAdapter model) {
            super(model);
            this.model = model;
            setSortsOnUpdates(false);
        }

        @Override
        protected void fireSortOrderChanged() {
            if (sort) {
                model.fireChangeListeners();
            }
        }

        @Override
        public void sort() {
            if (sort) {
                super.sort();
                onSorted();
            }
            sort = true;
        }

        @Override
        public void toggleSortOrder(int column) {
            SortKey key = new SortKey(column, SortOrder.ASCENDING);
            setSortKeys(Collections.singletonList(key));
        }

        private void onSorted() {
            List<Point2D> list = new ArrayList<Point2D>();
            Point2D point;
            int rowCount = dataTable.getRowCount();
            double x, y;
            for (int i = 0; i < rowCount; i++) {
                x = (Double) dataTable.getValueAt(i, 0);
                y = (Double) dataTable.getValueAt(i, 1);
                point = new Point2D(x, y);
                list.add(point);
            }
            sort = false;
            model.setPoints(list);
        }
    }
}