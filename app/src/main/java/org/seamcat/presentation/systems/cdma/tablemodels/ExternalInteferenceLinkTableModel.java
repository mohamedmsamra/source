package org.seamcat.presentation.systems.cdma.tablemodels;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.ofdma.OfdmaExternalInterferer;


public class ExternalInteferenceLinkTableModel implements TableModel {

	private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();

	private List<CDMAElementTableValue> tableentries = new ArrayList<CDMAElementTableValue>();

	private OfdmaExternalInterferer link;

	public ExternalInteferenceLinkTableModel() {
		

	}

	public void initTableModel() {
		tableentries.clear();
		
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Interferer Name";
				} else {
					return link.getExternalInterfererName();
				}

			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Pathloss (dB)";
				} else {
					return Double.toString(Mathematics.round(link.getPathloss()));
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Distance (km)";
				} else {
					return Double.toString(Mathematics.round(link.getDistance()));
				}

			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "TX Power (dBm)";
				} else {
					return Double.toString(Mathematics.round(link.getTxPower()));
				}

			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "External Blocking (dBm)";
				} else {
					return Double.toString(Mathematics.round(link.getExternalBlocking()));
				}

			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "External Unwanted (dBm)";
				} else {
					return Double.toString(Mathematics.round(link.getExternalUnwanted()));
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "TX antenna gain (dB)";
				} else {
					return Double.toString(Mathematics.round(link.getTx_gain()));
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "TX antenna height (m)";
				} else {
					return Double.toString(Mathematics.round(link.getTxAntHeight()));
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "RX antenna gain (dB)";
				} else {
					return Double.toString(Mathematics.round(link.getRx_gain()));
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "RX antenna (m)";
				} else {
					return Double.toString(Mathematics.round(link.getRxAntHeight()));
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {
			
			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "MCL (dB)";
				} else {
					return Double.toString(Mathematics.round(link.getMinimumCouplingLoss()));
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
		switch (columnIndex) {
			case 0: {
				return "Name";
			}
			case 1: {
				return "Value";
			}
			default: {
				return "Unknown";
			}
		}
	}

	public OfdmaExternalInterferer getLink() {
		return link;
	}

	public int getRowCount() {
		if (link == null) {
			return 0;
		}
		return tableentries.size();
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

	public void setLink(OfdmaExternalInterferer _link) {
		this.link = _link;
		initTableModel();
		for (int i = 0, stop = listeners.size(); i < stop; i++) {
			listeners.get(i).tableChanged(new TableModelEvent(this));
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

}
