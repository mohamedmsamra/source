package org.seamcat.presentation.components;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.*;


public class DiscreteFunction2TableModelAdapter implements TableModel, XYDataset,
        DiscreteFunctionTableModelAdapterInterface{

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
    private static final String[] COL_NAMES = { STRINGLIST.getString("FIRST_COLUMN_TITLE"), STRINGLIST.getString("SECOND_COLUMN_TITLE"), STRINGLIST.getString("THIRD_COLUMN_TITLE")};


    private static final int SERIES1 = 0;
    private static final String SERIES1_NAME = STRINGLIST.getString("FIRST_SERIES_TITLE");

    private static final int SERIES2 = 1;
    private static final String SERIES2_NAME = STRINGLIST.getString("SECOND_SERIES_TITLE");

    private final List<DatasetChangeListener> datasetChangeListeners = new ArrayList<DatasetChangeListener>();
    private DatasetGroup datasetGroup = new DatasetGroup();

    protected EmissionMaskImpl function;

    protected final DatasetChangeEvent stdDatasetChangeEvent = new DatasetChangeEvent(this, this);
    protected final TableModelEvent stdTableModelEvent = new TableModelEvent(this);
    protected final List<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();

    public DiscreteFunction2TableModelAdapter() {
        this(new EmissionMaskImpl());
    }

    public DiscreteFunction2TableModelAdapter(EmissionMaskImpl function) {
        this.function = function;
        ensureMinimumRows();
    }

    public void addChangeListener(DatasetChangeListener datasetChangeListener) {
        if (!datasetChangeListeners.contains(datasetChangeListener)) {
            datasetChangeListeners.add(datasetChangeListener);
        }
    }

    public void addRow() {
        addRow(true);
    }

    private void addRow(boolean fireChangeListeners) {
        List<Point2D> list = function.points();
        if ( list != null && list.size() > 0 ) {
            Point2D lastPoint = list.get( list.size()-1 );
            function.addPoint( new Point2D( lastPoint.getX()+1, lastPoint.getY()), function.getMask(lastPoint));
        } else {
            function.addPoint(new Point2D(0, 0), 1000);
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
        return 3;
    }

    public String getColumnName(int columnIndex) {
        return COL_NAMES[columnIndex];
    }

    public EmissionMaskImpl getDiscreteFunction2() {
        return function;
    }

    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }

    public EmissionMaskImpl getFunction() {
        sortPoints();
        return function;
    }

    @Override
    public void setFunction(Function points) {
        List<Point2D> points2d = new ArrayList<Point2D>();
        List<Double> mask = new ArrayList<Double>();
        EmissionMask maskFunction = null;
        if (points instanceof EmissionMask ) {
            maskFunction = (EmissionMask) points;
        }
        for (Point2D point : points.getPoints()) {
            if ( maskFunction != null ) {
                mask.add( maskFunction.getMask( point) );
            }
            points2d.add(point);
        }
        setDiscreteFunction2(new EmissionMaskImpl(points2d, mask));
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
        return 2;
    }

    @SuppressWarnings("rawtypes")
    public Comparable getSeriesKey(int arg0) {
        return getSeriesName(arg0);
    }

    public String getSeriesName(int series) {
        switch (series) {
            case SERIES1: {
                return SERIES1_NAME;
            }
            case SERIES2: {
                return SERIES2_NAME;
            }
            default: {
                throw new IllegalArgumentException("Illegal series index");
            }
        }
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
            case 2: {
                value = function.getMask(function.points().get(rowIndex));
                break;
            }
            default: {
                throw new IllegalArgumentException("Point3D only has three columns");
            }
        }
        return value;
    }

    public Number getX(int arg0, int arg1) {
        return getXValue(arg0, arg1);
    }

    public double getXValue(int series, int item) {
        if (series == SERIES1 || series == SERIES2) {
            return function.points().get(item).getX();
        } else {
            throw new IllegalArgumentException("Illegal series");
        }
    }

    public Number getY(int arg0, int arg1) {
        return getYValue(arg0, arg1);
    }

    public double getYValue(int series, int item) {
        switch (series) {
            case SERIES1: {
                return function.points().get(item).getY();
            }
            case SERIES2: {
                Point2D p = function.points().get(item);
                Double mask = function.getMask(p);
                if ( mask == null ) {
                    mask = 1000.0;
                }
                return p.getY() + Mathematics.linear2dB(1000 / mask);
            }
            default: {
                throw new IllegalArgumentException("Illegal series");
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public int indexOf(Comparable ser) {
        if (ser.equals(SERIES1_NAME)) {
            return 0;
        } else {
            return 1;
        }
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

    public void setDiscreteFunction2(EmissionMaskImpl function) {
        this.function = function;
        ensureMinimumRows();
        sortPoints();
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
                    function.updatePoint(old, new Point2D(number.doubleValue(), old.getY()));
                    break;
                }
                case 1: {
                    function.updatePoint( old, new Point2D(old.getX(), number.doubleValue()));
                    break;
                }
                case 2: {
                    function.setMask(old, number.doubleValue());
                    break;
                }
                default: {
                    throw new IllegalArgumentException(
                            "Point3D only has three columns");
                }
            }
            sortPoints();
            fireChangeListeners();
        } else if (aValue == null) {
            // just return
        } else {
            throw new IllegalArgumentException(
                    "TableModel only accepts instances of class Number "
                            + "[Passed argument was of type: " + aValue.getClass()
                            + "]");
        }
    }

    public void sortPoints() {
        Collections.sort(function.points());
    }
}
