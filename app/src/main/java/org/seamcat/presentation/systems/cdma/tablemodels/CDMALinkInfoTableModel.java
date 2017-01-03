package org.seamcat.presentation.systems.cdma.tablemodels;

import org.seamcat.cdma.CDMADownlink;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.ofdma.OfdmaMobile;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.model.mathematics.Mathematics.round;

public class CDMALinkInfoTableModel implements TableModel {

	private AbstractDmaLink link;

	private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();

	private List<CDMAElementTableValue> tableentries = new ArrayList<CDMAElementTableValue>();

	public CDMALinkInfoTableModel() {


	}

	public void initTableModel() {
		tableentries.clear();

		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Userid" :  link.getUserTerminal().getUserId();
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "In users active list" : link.getUserTerminal().getActiveList().contains(link);
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Cellid" : link.getBaseStation().getCellid();
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Cell location id" : link.getBaseStation().getCellLocationId();
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Connected sector of cell location" : link.getBaseStation().getSectorId();
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "User Position" :"(" + round(link.getUserTerminal().getPosition().getX()) + ", "
						+ round(link.getUserTerminal().getPosition().getY()) + ")";
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Cell Position" : "(" + round(link.getBaseStation().getPosition().getX()) + ", "
						+ round(link.getBaseStation().getPosition().getY()) + ")";
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Using WrapAround" :link.isUsingWrapAround();
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Distance" : round(link.getDistance()) + " km";
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "BS antenna gain" : round((link.getBsAntGain())) + " dB";
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "UE antenna gain" : link.getUserAntGain() + " dB";
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Pathloss" : link.getTxRxPathLoss() + " dB";
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Effective Pathloss" : round(link.getEffectivePathloss()) + " dB";
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Horizontal Angle" : round(link.getRxTxAngle()) + (char) 0x00B0;
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Vertical Angle" : round(link.getTxRxElevation()) + (char) 0x00B0;
			}
		});

		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (!link.isUpLink()) {
					return columnIndex == 0 ? "Total Transmit Power" :
						round(link.getBaseStation().getCurrentTransmitPower_dBm()) + " dBm";
				} else {
					return columnIndex == 0 ? "Total Transmit Power" :
						round(link.getUserTerminal().getCurrentTransmitPowerIndBm())+ " dBm";
				}
			}

		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Power scaled down to max" : link.isPowerScaledDownToMax();
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Power scaled down to max by" : link.isPowerScaledDownToMax() ? "CDMAlink.scaleTransmitPower" : "power not scaled";
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Total Received power" : round(link.getReceivePower_dB()) + " dBm";
			}
		});
		if ( link.getUserTerminal() instanceof OfdmaMobile ) {
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int column) {
					return column == 0 ? "Bitrate with interference" : round( ((OfdmaMobile)link.getUserTerminal()).calculateAchievedBitrate() ) + " kbps";
				}
			});
		}
		if ( link.getUserTerminal() instanceof OfdmaMobile ) {
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int column) {
					return column == 0 ? "SINR" : round( link.getUserTerminal().getSINRAchieved());
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

	public AbstractDmaLink getLink() {
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

	public void setCDMALink(AbstractDmaLink _link) {
		this.link = _link;
		initTableModel();
		for (int i = 0, stop = listeners.size(); i < stop; i++) {
			listeners.get(i).tableChanged(new TableModelEvent(this));
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}
}
