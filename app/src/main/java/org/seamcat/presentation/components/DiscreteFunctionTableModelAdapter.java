package org.seamcat.presentation.components;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.marshalling.FunctionMarshaller;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscreteFunctionTableModelAdapter implements TableModel, XYDataset, DiscreteFunctionTableModelAdapterInterface {

    private static final int SERIES1 = 0;

    private String col1Name = "X";
    private String col2Name = "Y";
    private final List<DatasetChangeListener> datasetChangeListeners = new ArrayList<DatasetChangeListener>();
    private DatasetGroup datasetGroup = new DatasetGroup();
    private DiscreteFunction function;
    private final DatasetChangeEvent stdDatasetChangeEvent = new DatasetChangeEvent(this, this);
    private final TableModelEvent stdTableModelEvent = new TableModelEvent(this);
    private final List<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();

    public DiscreteFunctionTableModelAdapter() {
        this(new DiscreteFunction());
    }

    public DiscreteFunctionTableModelAdapter(DiscreteFunction function) {
        this.function = function;
        ensureMinimumRows();
    }

    public void addChangeListener(DatasetChangeListener datasetChangeListener) {
        if (!datasetChangeListeners.contains(datasetChangeListener)) {
            datasetChangeListeners.add(datasetChangeListener);
        }
    }

    public void dispose() {
        datasetChangeListeners.clear();
        tableModelListeners.clear();

    }

    public void addRow() {
        addRow(true);
    }

    private void addRow(boolean fireChangeListeners) {
        List<Point2D> list = function.points();
        if ( list != null && list.size() > 0 ) {
            Point2D lastPoint = list.get( list.size()-1 );
            function.addPoint(new Point2D(lastPoint.getX() + 1, lastPoint.getY()));
        } else {
            function.addPoint(new Point2D(0, 0));
        }
        if (fireChangeListeners) {
            fireChangeListeners();
        }
    }

    public void addTableModelListener(TableModelListener l) {
        if (!tableModelListeners.contains(l)) {
            tableModelListeners.add(l);
        }
    }

    public void clear() {
        function.points().clear();
        fireChangeListeners();
    }

    public void deleteRow(int row) {
        if (row >= 0 && row < getRowCount()) {
            function.points().remove(row);
            ensureMinimumRows();
            fireChangeListeners();
        }
    }

    private void ensureMinimumRows() {
        if (getRowCount() == 0) {
            addRow(false);
        }
    }

    public void fireChangeListeners() {
        for (TableModelListener l : tableModelListeners) {
            l.tableChanged(stdTableModelEvent);
        }
        for (DatasetChangeListener l : datasetChangeListeners) {
            l.datasetChanged(stdDatasetChangeEvent);
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        return Double.class;
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        if ( columnIndex == 0 ) return col1Name;
        if ( columnIndex == 1 ) return col2Name;
        throw new IllegalArgumentException("Point2D only has two columns <" + columnIndex + ">");
    }

    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }

    public DiscreteFunction getFunction() {
        return function;
    }

    @Override
    public void setFunction(Function points) {
        setDiscreteFunction( new DiscreteFunction(points.getPoints()));
    }

    public DatasetGroup getGroup() {
        return datasetGroup;
    }

    public int getItemCount(int series) {
        return function.points().size();
    }

    public int getRowCount() {
        return function.points().size();
    }

    public int getSeriesCount() {
        return 1;
    }

    public Comparable<?> getSeriesKey(int arg0) {
        return getSeriesName(arg0);
    }

    public String getSeriesName(int series) {
        if ( series == SERIES1 ) return "";
        throw new IllegalArgumentException("Illegal series index");
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Double value;
        switch (columnIndex) {
            case 0: {
                value = function.points().get(rowIndex).getX();
                break;
            }
            case 1: {
                value = function.points().get(rowIndex).getY();
                break;
            }
            default: {
                throw new IllegalArgumentException("Point2D only has two columns");
            }
        }
        return value;
    }

    public Number getX(int series, int item) {
        return getXValue(series, item);
    }

    public double getXValue(int series, int item) {
        if (series == SERIES1) {
            return function.points().get(item).getX();
        } else {
            throw new IllegalArgumentException("Illegal series");
        }
    }

    public Number getY(int series, int item) {
        return getYValue(series, item);
    }

    public double getYValue(int series, int item) {
        switch (series) {
            case SERIES1: {
                return function.points().get(item).getY();
            }
            default: {
                throw new IllegalArgumentException("Illegal series");
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public int indexOf(Comparable arg0) {
        return 0;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public void removeChangeListener(DatasetChangeListener datasetChangeListener) {
        datasetChangeListeners.remove(datasetChangeListener);
    }

    public void removeTableModelListener(TableModelListener l) {
        tableModelListeners.remove(l);
    }


    public void setColumnName(int columnIndex, String name) {
        switch (columnIndex) {
            case 0: {
                col1Name = name;
                break;
            }
            case 1: {
                col2Name = name;
                break;
            }
            default: {
                throw new IllegalArgumentException("Point2D only has two columns <"
                        + columnIndex + ">");
            }
        }
    }

    public void setDiscreteFunction(DiscreteFunction _function) {
        this.function = FunctionMarshaller.copy(_function);
        ensureMinimumRows();
        sortPoints();
        fireChangeListeners();
    }

    public void setPoints(List<Point2D> points) {
        function.setPoints(points);
        fireChangeListeners();
    }

    public void setGroup(DatasetGroup datasetGroup) {
        this.datasetGroup = datasetGroup;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue instanceof Number) {
            Number number = (Number) aValue;
            Point2D old = function.points().get(rowIndex);
            switch (columnIndex) {
                case 0: {
                    function.points().set(rowIndex, new Point2D(number.doubleValue(), old.getY()));
                    break;
                }
                case 1: {
                    function.points().set(rowIndex, new Point2D(old.getX(), number.doubleValue()));
                    sortPoints();
                    break;
                }
                default: {
                    throw new IllegalArgumentException(
                            "Point2D only has two columns");
                }
            }
            fireChangeListeners();
        } else if (aValue != null) {
            throw new IllegalArgumentException(
                    "TableModel only accepts instances of class Number (Input was: "
                            + aValue.getClass().getName() + ")");
        }
    }

    public void sortPoints() {
        Collections.sort(function.points(), Point2D.POINTX_COMPARATOR);
    }
}