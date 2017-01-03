package org.seamcat.presentation.systems.cdma;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class CDMAEditModel implements TableModel {

	public static final String[] COLUMN_NAMES = new String[] { "Geometry*",
	      "AWGN", "3 km/h", "30 km/h", "100 km/h" };
	private Vector<Vector<Double>> data;
	private final List<TableModelListener> tableModelListeners = new ArrayList<TableModelListener>();

	public CDMAEditModel() {
		super();
	}

	public void addRow() {
		Vector<Double> row = new Vector<Double>();
		row.add(null);
		row.add(null);
		row.add(null);
		row.add(null);
		row.add(null);
		data.add(row);
	}

	public void addTableModelListener(TableModelListener l) {
		if (!tableModelListeners.contains(l)) {
			tableModelListeners.add(l);
		}
	}

	public void deleteRow(int index) {
		if (index > -1 && index < data.size()) {
			data.remove(index);
			fireChangeListeners(index, TableModelEvent.DELETE);
		}
	}

	public void fireChangeListeners(int row, int column, int type) {
		TableModelEvent tme = new TableModelEvent(this, row, row, column, type);
		for ( TableModelListener listener : tableModelListeners ) {
			listener.tableChanged( tme );
		}
	}
	
	public void fireChangeListeners(int row, int type) {
		TableModelEvent tme = new TableModelEvent(this, row, type);
		for ( TableModelListener listener : tableModelListeners ) {
			listener.tableChanged( tme );
		}
	}

	public Class<?> getColumnClass(int columnIndex) {
		return Double.class;
	}

	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	public String getColumnName(int columnIndex) {
		return COLUMN_NAMES[columnIndex];
	}

	public int getRowCount() {
		return data.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return data.get(rowIndex).get(columnIndex);
	}

	public XYSeriesCollection getXYSeriesCollection() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int x = 1, stop = getColumnCount(); x < stop; x++) {
			XYSeries series = new XYSeries(COLUMN_NAMES[x]);
			for (int y = 0, _stop = getRowCount(); y < _stop; y++) {
				if (getValueAt(y, x) != null && getValueAt(y, 0) != null) {
					double a = (Double) getValueAt(y, 0);
					double b = (Double) getValueAt(y, x);
					series.add(a, b);
				}
			}
			dataset.addSeries(series);
		}

		return dataset;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
	}

	public void removeTableModelListener(TableModelListener l) {
		this.tableModelListeners.remove(l);
	}

	public void setData(Vector<Vector<Double>> vector) {
		data = vector;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		Vector<Double> row = data.get(rowIndex);
		row.set(columnIndex, Double.valueOf( aValue.toString() ));
		fireChangeListeners(rowIndex, columnIndex, TableModelEvent.UPDATE);
	}
}