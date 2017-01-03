package org.seamcat.presentation.systems.cdma.tablemodels;

import org.seamcat.cdma.CdmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.ofdma.UplinkOfdmaBaseStation;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.model.mathematics.Mathematics.round;

public class CDMACellTableModel implements TableModel {

	private List<TableModelListener> listeners = new ArrayList<TableModelListener>();

	private AbstractDmaBaseStation selectedCell;

	private List<CDMAElementTableValue> tableentries = new ArrayList<CDMAElementTableValue>();

	public CDMACellTableModel() {
		
	}

	public void initTableModel() {
		tableentries.clear();
		
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Cell ID";
				} else {
					return Integer.valueOf(selectedCell.getCellid());
				}
			}
		});

		tableentries.add(new CDMAElementTableValue() {
			
			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Number of served users";
				} else {
					return new Integer(selectedCell.countServedUsers())
					+ " users";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {
			
			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Antenna height";
				} else {
					return selectedCell.getAntennaHeight() + " meters";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {
			
			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Number of dropped users";
				} else {
					return selectedCell.countDroppedUsers() + " users";
				}
			}
		});
		
		tableentries.add(new CDMAElementTableValue() {
			
			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Position";
				} else {
					return "(" + round(selectedCell.getPosition().getX()) + ", "
					+ round(selectedCell.getPosition().getY()) + ")";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (selectedCell.isUpLinkMode()) {
					if (columnIndex == 0) {
						return "External Interference, Selectivity";
					} else {
						return round(selectedCell.getExternalInterferenceBlocking()) + " dBm";
					}
				} else {
					if (columnIndex == 0) {
						return "Number of active users";
					} else {
						return selectedCell.countActiveUsers() + " users";
					}
				}
			}
		});

		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (selectedCell.isUpLinkMode()) {
					if (columnIndex == 0) {
						return "External Interference, Unwanted";
					} else {
						return round(selectedCell.getExternalInterferenceUnwanted()) + " dBm";
					}
				} else {
					if (columnIndex == 0) {
						return "Transmit Power";
					} else {
						return round(selectedCell.getCurrentTransmitPower_dBm()) + " dBm";
					}
				}
			}
		});
				
		tableentries.add(new CDMAElementTableValue() {
			
			public Object getValue(int columnIndex) {
				if (selectedCell.isUpLinkMode() && selectedCell instanceof UplinkOfdmaBaseStation) {
					if (columnIndex == 0) {
						return "Antenna Gain towards first external interferer";
					} else {
						UplinkOfdmaBaseStation<?> up = (UplinkOfdmaBaseStation<?>) selectedCell;
						
						if (up.getExternalInterferers().size() > 0) {
							return round(up.getExternalInterferers().get(0).getRx_gain()) + " dB";
						} else {
							return "0 dBm";
						}
					}
				} else {
					if (columnIndex == 0) {
						return "Number of external interferers";
					} else {
						return selectedCell.getSystem().getExternalInterferers().size() + "";
					}
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Active Connections";
				} else {
					if ( selectedCell.getActiveConnections() != null) {
                        return selectedCell.getActiveConnections().size()
                                + " connections";
                    } else {
                        return "0 connections";
                    }
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Is Reference Cell";
				} else {
					return Boolean.toString((selectedCell == selectedCell.getSystem().getReferenceCell()));
				}
			}
		});
		if (selectedCell instanceof CdmaBaseStation) {
			final CdmaBaseStation selectedCell = (CdmaBaseStation) this.selectedCell;
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (selectedCell.isUpLinkMode()) {
						if (columnIndex == 0) {
							return "Total Interference";
						} else {
							return round(selectedCell.getTotalInterference()) + " dBm";
						}
					} else {
						if (columnIndex == 0) {
							return "Transmit Power";
						} else {
							selectedCell.calculateCurrentChannelPower_dBm();

							if (selectedCell.getCurrentTransmitPower_dBm() > selectedCell
							      .getMaximumTransmitPower()) {

							}

							return round(selectedCell.getCurrentTransmitPower_dBm()) + " dBm";
						}
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (selectedCell.isUpLinkMode()) {
						if (columnIndex == 0) {
							return "InterSystem Interference";
						} else {
							return round(selectedCell.getInterSystemInterference()) + " dBm";
						}
					} else {
						if (columnIndex == 0) {
							return "Pilot Channel Power";
						} else {
							return round(Mathematics.fromWatt2dBm(selectedCell.getPilotPower_Watt())) + " dBm";
						}
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (selectedCell.isUpLinkMode()) {
						if (columnIndex == 0) {
							return "External Interference, Unwanted";
						} else {
							return round(selectedCell.getExternalInterferenceUnwanted())
							      + " dBm";
						}
					} else {
						if (columnIndex == 0) {
							return "Overhead Channel Power";
						} else {
							return round(Mathematics.fromWatt2dBm(selectedCell.getOverheadPower_Watt())) + " dBm";
						}
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {
				
				public Object getValue(int columnIndex) {
					if (selectedCell.isUpLinkMode()) {
						if (columnIndex == 0) {
							return "Noise Rise over Noise Floor";
						} else {
							return round(selectedCell
								.calculateNoiseRiseOverThermalNoise_dB())
								+ " dB";
						}
					} else {
						if (columnIndex == 0) {
							return "Outage percentage";
						} else {
							return round(selectedCell.getOutagePercentage()) + " %";
						}
					}
				}
			});
			
		}
		
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
		if (selectedCell == null) {
			return 0;
		}
		return tableentries.size();
	}

	public AbstractDmaBaseStation getSelectedCell() {
		return selectedCell;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Object obj = tableentries.get(rowIndex).getValue(columnIndex);
		
		if (obj instanceof Double) {
			obj = Mathematics.round((Double) obj);
		}
		
		return obj;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return rowIndex == 8;
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	public void setSelectedCell(AbstractDmaBaseStation c) {
		this.selectedCell = c;
		initTableModel();
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).tableChanged(new TableModelEvent(this));
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

}
