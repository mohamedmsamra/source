package org.seamcat.presentation.components;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.distributions.StairDistributionImpl;
import org.seamcat.model.functions.Point2D;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StairDistributionTableModelAdapter implements TableModel {

	private String col1Name;
	private String col2Name;
	private List<Point2D> points = new ArrayList<Point2D>();
	private CategoryDatasetImpl categoryDS = new CategoryDatasetImpl();
	private TableModelEvent stdTableModelEvent = new TableModelEvent(this);
	private List<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();

	public StairDistributionTableModelAdapter() {
		this(new Point2D[0]);
	}

	public StairDistributionTableModelAdapter(Point2D[] _points) {
        Collections.addAll(points, _points);
		ensureMinimumRows();
		col1Name = "Value";
		col2Name = "Cum. Prob.";
	}

	public void addRow() {
		addRow(true);
	}

	private void addRow(boolean fireChangeListeners) {
		points.add(new Point2D(0, 0));
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
        points.clear();
		fireChangeListeners();
	}

	public void deleteRow(int row) {
		if (row >= 0 && row < this.getRowCount()) {
            points.remove(row);
			ensureMinimumRows();
			fireChangeListeners();
		}
	}

	private void ensureMinimumRows() {
		if (this.getRowCount() == 0) {
			this.addRow(false);
		}
	}

	public void fireChangeListeners() {
		for ( TableModelListener listener : tableModelListeners ) {
			listener.tableChanged( stdTableModelEvent );
		}
		categoryDS.fireChangeListeners();
	}

	public CategoryDataset getCategoryDS() {
		return categoryDS;
	}

	public Class<?> getColumnClass(int columnIndex) {
		return Double.class;
	}

	public int getColumnCount() {
		return 2;
	}

	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case 0: return col1Name;
			case 1: return col2Name;
			default: {
				throw new IllegalArgumentException("Point2D only has two columns <"
				      + columnIndex + ">");
			}
		}
	}

	public int getIndex(Double key) {
		int index = -1;
		for (int x = 0, size = points.size(); x < size; x++) {
			if (new Double(points.get(x).getX()).compareTo(key) == 0) {
				index = x;
				break;
			}
		}
		return index;
	}

	public Comparable<Double> getKey(int index) {
		return points.get(index).getX();
	}

	public Point2D[] getPoints() {
		return points.toArray(new Point2D[points.size()]);
	}

	public List<Point2D> getPointsList() {
		return points;
	}

	public int getRowCount() {
		return points.size();
	}

	public Number getValue(int index) {
		return points.get(index).getY();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0: {
				return points.get(rowIndex).getX();
			}
			case 1: {
				return points.get(rowIndex).getY();
			}
			default: {
				throw new IllegalArgumentException("Point2D only has two columns");
			}
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public void removeTableModelListener(TableModelListener l) {
		tableModelListeners.remove(l);
	}

	public void setPoints(List<Point2D> _points) {
		points = _points;
		ensureMinimumRows();
		sortPoints();
		fireChangeListeners();
	}

	public void setPoints(StairDistributionImpl stairDist) {
		points.clear();
        DiscreteFunction cdf = (DiscreteFunction) stairDist.getCdf();
        for (Point2D d : cdf.points()) {
            points.add(d);
        }
		ensureMinimumRows();
		sortPoints();
		fireChangeListeners();
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue instanceof Number) {
			Number number = (Number) aValue;
			switch (columnIndex) {
				case 0: {
                    Point2D old = points.get(rowIndex);
                    points.set(rowIndex, new Point2D(number.doubleValue(), old.getY()));
					break;
				}
				case 1: {
					Point2D old = points.get(rowIndex);
                    points.set( rowIndex, new Point2D(old.getX(), number.doubleValue()));
					sortPoints(); // Only sort when Y column is modified
					break;
				}
				default: {
					throw new IllegalArgumentException(
					      "Point2D only has two columns");
				}
			}
			fireChangeListeners();
		} else {
			throw new IllegalArgumentException(
			      "TableModel only accepts instances of class Number");
		}
	}

	public void sortPoints() {
		Collections.sort(points, Point2D.POINTY_COMPARATOR);
	}

	private class CategoryDatasetImpl implements CategoryDataset {
		
		private List<Double> columnKeys = new ArrayList<Double>();
		private List<DatasetChangeListener> datasetChangeListeners = new ArrayList<DatasetChangeListener>();
		private DatasetGroup datasetGroup = new DatasetGroup();
		private List<Integer> rowKeys = new ArrayList<Integer>(1);
		private DatasetChangeEvent stdDatasetChangeEvent = new DatasetChangeEvent(
				this, this);
		
		public CategoryDatasetImpl() {
			rowKeys.add(0);
			
			// Update column keys
			updateColumnKeys();
		}
		
		public void addChangeListener(DatasetChangeListener datasetChangeListener) {
			if (!datasetChangeListeners.contains(datasetChangeListener)) {
				datasetChangeListeners.add(datasetChangeListener);
			}
		}
		
		public void fireChangeListeners() {
			// Update column keys
			updateColumnKeys();
			
			// Fire listeners
			for ( DatasetChangeListener listener : datasetChangeListeners ) {
				listener.datasetChanged( stdDatasetChangeEvent );
			}
		}
		
		public int getColumnCount() {
			return points.size();
		}
		
		@SuppressWarnings("rawtypes")
      public int getColumnIndex(Comparable key) {
			return columnKeys.indexOf(key);
		}
		
		public Comparable<Double> getColumnKey(int column) {
			return points.get(column).getX();
		}
		
		public List<Double> getColumnKeys() {
			return columnKeys;
		}
		
		public DatasetGroup getGroup() {
			return datasetGroup;
		}
		
		public int getRowCount() {
			return 1;
		}
		
		@SuppressWarnings("rawtypes")
      public int getRowIndex(Comparable key) {
			return rowKeys.indexOf(key);
		}
		
		public Comparable<Integer> getRowKey(int row) {
			return rowKeys.get(row);
		}
		
		public List<Integer> getRowKeys() {
			return rowKeys;
		}
		
		@SuppressWarnings("rawtypes")
      public Number getValue(Comparable rowKey, Comparable colKey) {
			return getValue(getRowIndex(rowKey), getColumnIndex(colKey));
		}
		
		public Number getValue(int row, int column) {
			return points.get(column).getY();
		}
		
		public void removeChangeListener(
				DatasetChangeListener datasetChangeListener) {
			this.datasetChangeListeners.remove(datasetChangeListener);
		}
		
		public void setGroup(DatasetGroup datasetGroup) {
			this.datasetGroup = datasetGroup;
		}
		
		private void updateColumnKeys() {
			columnKeys.clear();
			for (int x = 0, size = points.size(); x < size; x++) {
				columnKeys.add( (Double) getColumnKey(x) );
			}
		}
		
	}

	
	
}
