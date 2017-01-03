package org.seamcat.presentation.systems.cdma.tablemodels;

import org.seamcat.cdma.CdmaUserTerminal;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaMobile;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.ofdma.DownlinkOfdmaMobile;
import org.seamcat.ofdma.OfdmaMobile;
import org.seamcat.ofdma.UplinkOfdmaMobile;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.model.mathematics.Mathematics.fromWatt2dBm;
import static org.seamcat.model.mathematics.Mathematics.round;

public class CDMAUserInfoTableModel implements TableModel {

	private final List<TableModelListener> listeners = new ArrayList<TableModelListener>();

	private List<CDMAElementTableValue> tableentries = new ArrayList<CDMAElementTableValue>();

	private CdmaUserTerminal cuser;
	private UplinkOfdmaMobile ou_user;
	private DownlinkOfdmaMobile od_user;
	private OfdmaMobile ouser;

	private AbstractDmaMobile user;

	public CDMAUserInfoTableModel() {

	}

	protected void initTableModel() {
		tableentries.clear();
		tableentries.add(new CDMAElementTableValue() {
			public Object getValue(int columnIndex) {
				return columnIndex == 0 ? "Userid" : user.getUserId();
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Position";
				} else {
					return "(" + round(user.getPosition().getX()) + ", " + round(user.getPosition().getY()) + ")";
				}
			}
		});
		if (user instanceof CdmaUserTerminal) {
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Speed";
					} else {
						return Double.toString(user.getMobilitySpeed()) + " km/h";
					}
				}
			});
		}
		if (cuser != null) {
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Status";
					} else {
						if (user.isConnected()) {
							return "Connected";
						} else if (cuser != null && cuser.isDroppedAsHighest()) {
							return "Removed as highest transmitting user";
						} else if (user.isDropped()) {
							return "Dropped" + " - " + user.getDropReason();
						} else if (!user.isAllowedToConnect()) {
							return "Not allowed to connect";
						} else {
							return "Not connected";
						}
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (cuser.isUpLinkMode()) {
						if (columnIndex == 0) {
							return "Multi-Path";
						} else {
							return Integer.toString(cuser.getMultiPathChannel());
						}
					} else {
						if (columnIndex == 0) {
							return "Geometry";
						} else {
							return round(cuser.getGeometry()) + " dB";
						}
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (cuser.isUpLinkMode()) {
						if (columnIndex == 0) {
							return "Achieved CI";
						} else {
							return Double.toString(Mathematics.round(cuser.getAchievedCI())) + " dB";
						}
					} else {
						if (columnIndex == 0) {
							return "Achieved Ec/Ior";
						} else {
							return Double.toString(Mathematics.round(cuser.getAchievedEcIor()))+ " dB";
						}
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {
				public Object getValue(int columnIndex) {
					if (cuser.isUpLinkMode()) {
						return columnIndex == 0 ? "Required Eb/No" : Double.toString(round(cuser.getLinkLevelData().getEbNo())) + " dB";
					} else {
						return columnIndex == 0 ? "Required Ec/Ior" :Double.toString(round(cuser.getLinkLevelData().getEcIor()))+ " dB";
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Total Power Received from Inactive Set";
					} else {
						if (user.isUpLinkMode()) {
							return "Not applicable";
						} else {
							return round(cuser.getTotalPowerReceivedFromBaseStationsNotInActiveSetdBm()) + " dBm";
						}
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Total Power Received from Active Set";
					} else {
						if (user.isUpLinkMode()) {
							return "Not applicable";
						} else {
							return round(Mathematics
									.fromWatt2dBm(cuser.getTotalPowerReceivedFromBaseStationsActiveSetInWatt())) + " dBm";
						}
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Traffic Channel Power";
					} else {
						if (user.isUpLinkMode()) {
							return "Not applicable";
						} else {
							return round(Mathematics.fromWatt2dBm(cuser.getReceivedTrafficChannelPowerWatt()))+ " dBm";
						}
					}
				}
			});
		}

		if (ouser != null) {
			tableentries.add(new CDMAElementTableValue() {

				@Override
				public Object getValue(int column) {
					if (column == 0) {
						return "Achieved SINR";
					} else {
						return round(ouser.getSINRAchieved()) + "dB";
					}
				}

			});

		}
		if (ou_user != null) {
			tableentries.add(new CDMAElementTableValue() {

				@Override
				public Object getValue(int column) {
					if (column == 0) {
						return "Subcarrier Ratio";
					} else {
						return round(ou_user.getSubCarrierRatio()) + "";
					}
				}

			});
			tableentries.add(new CDMAElementTableValue() {

				@Override
				public Object getValue(int column) {
					if (column == 0) {
						return "Power Received (serving link)";
					} else {
						return round(ouser.getServingLink().getTotalReceivedPower()) + " dBm";
					}
				}

			});
			tableentries.add(new CDMAElementTableValue() {

				@Override
				public Object getValue(int column) {
					if (column == 0) {
						return "Total interference";
					} else {
						return round(fromWatt2dBm(ouser.getTotalInterference())) + " dBm";
					}
				}

			});


		}
		if (ouser != null) {
			tableentries.add(new CDMAElementTableValue() {

				@Override
				public Object getValue(int column) {
					if (column == 0) {
						return "Frequency";
					} else {
						return round(ouser.getFrequency()) + " MHz";
					}
				}

			});
			tableentries.add(new CDMAElementTableValue() {

				@Override
				public Object getValue(int column) {
					if (column == 0) {
						return "Bandwidth";
					} else {
						return round(ouser.getBandwidth()) + " MHz";
					}
				}

			});
		}


		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (user.isUpLinkMode()) {
					if (columnIndex == 0) {
						return "Transmit Power";
					} else {
						return round(user.getCurrentTransmitPowerIndBm()) + " dBm";
					}
				} else {
					if (columnIndex == 0) {
						return "External interference";
					} else {
						double extInf = user.getExternalInterference();
						if (extInf < -900) {
							return "No External Interference";
						} else {
							return Mathematics.round(extInf) + " dBm";
						}
					}
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Active List";
				} else {
					return user.getActiveList().size() + " connection(s)";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Connection List";
				} else {
					return user.getAllLinks().length - user.getActiveList().size() + " connection(s)";
				}
			}
		});
		if (user instanceof CdmaUserTerminal) {
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Is in softhandover";
					} else {
						return user.isInSoftHandover();
					}
				}
			});
		}

		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Distance to first cell in active list";
				} else {
					return round(user.getActiveList().get(0).getDistance()) + " km";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Angle from first cell in active list";
				} else {
					return Double.toString(round(user.getActiveList().get(0).getRxTxAngle())) + (char) 0x00B0;
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Elevation angle to first cell in active list";
				} else {
					return Double.toString(round(user.getActiveList().get(0).getTxRxElevation())) + (char) 0x00B0;
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Connected sector of first cell in active list";
				} else {
					AbstractDmaBaseStation cell = user.getActiveList().get(0).getBaseStation();

					if ( cell.getSystem().getSystemSettings().getLayout().getSectorSetup() == CellularLayout.SectorSetup.SingleSector ) {
                        return "N/A - Cell is Omni directional";
                    } else {
                        return cell.getSectorId();
                    }
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Antenna Gain from first BS in active list";
				} else {
					return round(user.getActiveList().get(0).getBsAntGain()) + " dB";
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

	public int getRowCount() {
		if (user == null) {
			return 0;
		}

		return tableentries.size();

	}

	public CdmaUserTerminal getUser() {
		return cuser;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Object obj = tableentries.get(rowIndex).getValue(columnIndex);

		if (obj instanceof Double) {
			obj = Mathematics.round((Double) obj);
		}

		return obj;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 1 && (rowIndex == 8 || rowIndex == 9);
	}

	public void removeTableModelListener(TableModelListener l) {
		listeners.remove(l);
	}

	public void setUserTerminal(AbstractDmaMobile _user) {
		user = _user;

		ouser = null;
		cuser = null;
		ou_user = null;
		od_user = null;


		if (_user instanceof CdmaUserTerminal) {
			cuser = (CdmaUserTerminal) _user;
		} else if (_user instanceof OfdmaMobile){
			ouser = (OfdmaMobile) _user;
			if (ouser.isUpLinkMode()) {
				ou_user = (UplinkOfdmaMobile) _user;
			} else {
				od_user = (DownlinkOfdmaMobile) _user;
			}

		}
		initTableModel();

		for (int i = 0, stop = listeners.size(); i < stop; i++) {
			listeners.get(i).tableChanged(new TableModelEvent(this));
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}
}