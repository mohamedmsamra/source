package org.seamcat.presentation.systems.cdma.tablemodels;

import org.seamcat.dmasystems.ActiveInterferer;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.Interferer;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.model.mathematics.Mathematics.round;

public class CDMAInterfererTableModel implements TableModel {

	private List<TableModelListener> listeners = new ArrayList<TableModelListener>();

	private ActiveInterferer selectedInterferer;

	private List<CDMAElementTableValue> tableentries = new ArrayList<CDMAElementTableValue>();

	public CDMAInterfererTableModel() {
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Type";
				} else {
					return "Interfering Transmitter";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Transmit Power";
				} else {
					return round(selectedInterferer.getLinkResult().getTxPower()) + " dBm";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Position";
				} else {
					return "(" + round(selectedInterferer.getPoint().getX()) + ", "
					      + round(selectedInterferer.getPoint().getY()) + ")";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Frequency";
				} else {
					return selectedInterferer.getFrequency() + " MHz";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Antenna Peak Gain";
				} else {
					return selectedInterferer.getAntennaGain().peakGain() + " dB";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Antenna Height";
				} else {
					return selectedInterferer.getAntennaHeight() + " meters";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {
			
			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "MCL (dB)";
				} else {
					return Double.toString(Mathematics.round(selectedInterferer.getMinimumCouplingLoss()));
				}
			}
		});
	}

	public void addTableModelListener(TableModelListener l) {
		listeners.add(l);
	}

	public Class<?> getColumnClass(int columnIndex) {
		return Object.class;
	}

	public int getColumnCount() {
		return 2;
	}

	public String getColumnName(int columnIndex) {
		return columnIndex == 0 ? "Name" : "Value";
	}

	public int getRowCount() {
		if (selectedInterferer == null) {
			return 0;
		}
		return tableentries.size();
	}

	public Interferer getSelectedCell() {
		return selectedInterferer;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Object obj = tableentries.get(rowIndex).getValue(columnIndex);
		
		if (obj instanceof Double) {
			obj = Mathematics.round((Double) obj);
		}
		
		return obj;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	public void setSelectedInterferer(ActiveInterferer selectedInterferer) {
		this.selectedInterferer = selectedInterferer;
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).tableChanged(new TableModelEvent(this));
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

}
