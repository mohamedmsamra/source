package org.seamcat.presentation.systems.cdma.tablemodels;

import org.seamcat.cdma.CDMASystem;
import org.seamcat.dmasystems.AbstractDmaMobile;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.result.BarChartResultType;
import org.seamcat.model.types.result.BarChartValue;
import org.seamcat.presentation.systems.cdma.CDMAPlotModel;
import org.seamcat.scenario.CDMASettingsImpl;
import org.seamcat.simulation.cellular.CellularVictimSystemSimulation;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.model.mathematics.Mathematics.round;

public class CDMASystemTableModel implements TableModel {

	private CDMAPlotModel cdma;
	private List<TableModelListener> listeners = new ArrayList<TableModelListener>();
	private List<CDMAElementTableValue> tableentries = new ArrayList<CDMAElementTableValue>();
	private CellularSystem system;

	public CDMASystemTableModel() {
		
	}
	
	protected void initTableModel() {
		tableentries.clear();
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Link Direction";
				} else {
					return system.isUpLink() ? "Uplink" : "Downlink";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Frequency";
				} else {
					return cdma.getFrequency() + " MHz";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Bandwidth";
				} else {
					return system.getBandwidth() + " MHz";
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Cell Radius";
				} else {
					return system.getLayout().getCellRadius() + " km";
				}
			}
		});
		
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Number of External Interferers";
				} else {
					return cdma.getExternalInterferers().size();
				}
			}
		});
		tableentries.add(new CDMAElementTableValue() {
			
			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Noise Floor";
				} else {
					return round(Mathematics.fromWatt2dBm(cdma.thermalNoise)) + " dBm";
				}
			}
		});
		
		tableentries.add(new CDMAElementTableValue() {

			public Object getValue(int columnIndex) {
				if (columnIndex == 0) {
					return "Propagation Model";
				} else {
					return system.getLink().getPropagationModel();
				}
			}
		});

		if (system.getCDMASettings() != null) {
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Bit rate";
					} else {
						return round(system.getCDMASettings().getVoiceBitRate()) + " kbps";
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Link Level Data";
					} else {
						return ((CDMASettingsImpl)system.getCDMASettings()).getLld();
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Total Non Interfered Capacity";
					} else {
						return cdma.getPreResults().findIntValue(CDMASystem.NON_INTERFERED_CAPACITY)
						      * cdma.getNumberOfBaseStations()
						      + " users";
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Non Interfered Capacity per Cell";
					} else {
						return cdma.getPreResults().findIntValue(CDMASystem.NON_INTERFERED_CAPACITY) + " users";
					}
				}
			});

			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Active users";
					} else {
						return cdma.getActiveUsers().size() + " users";
					}
				}
			});

			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Dropped users";
					} else {
						return cdma.getDroppedUsers().size() + " users";
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Users dropped before interference";
					} else {
						return cdma.getEventResult().getValue(CellularVictimSystemSimulation.droppedBeforeInterference) + " users";
					}
				}
			});

			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Number of trials during capacity test";
					} else {
						return system.getCDMASettings().getNumberOfTrials();
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return system.isUpLink() ? "Average Noise Rise, last event"
						      : "Obtained succes rate";
					} else {
                        if ( !system.isUpLink() ) {
                            BarChartResultType barChart = cdma.getPreResults().findBarChart(CDMASystem.CAPACITY_FINDING);
                            if ( barChart == null ) return "0";
                            BarChartValue value = barChart.getChartPoints().get(barChart.getChartPoints().size() - 1);
                            return value.getValue();
                        } else {
                            return "N/A";
                            //return ((CDMAUplinkSystem) cdma).calculateAverageNoiseRise_dB();
                        }
					}
				}
			});
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Number of ignored users (due to LLD)";
					} else {
						return cdma.getNumberOfLLDFound();
					}
				}
			});

			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Number of ignored users (due to LLD)";
					} else {
						return cdma.getNumberOfLLDFound();
					}
				}
			});
			
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return system.isUpLink() ? "Highest PC loop count"
						      : "Maximum Traffic Channel Proportion";
					} else {
						return system.isUpLink() ? Math.round(cdma.getEventResult().getValue(CellularVictimSystemSimulation.highestPCLoopCount))
                                : cdma.getMaxTrafficChannelPower()  + " dBm";
					}
				}
			});
            tableentries.add(new CDMAElementTableValue() {

                public Object getValue(int columnIndex) {
                    if (columnIndex == 0) {
                        return "Percentage of active users in soft handover";
                    } else {
                        List<AbstractDmaMobile> users = cdma.getActiveUsers();
                        int softHandover = 0;
                        for (AbstractDmaMobile user : users) {
                            if (user.isInSoftHandover()) {
                                softHandover++;
                            }
                        }

                        double percentage = (double) softHandover
                                / (double) users.size() * 100.0;

                        return round(percentage) + "%";
                    }
                }
            });
            tableentries.add(new CDMAElementTableValue() {

                public Object getValue(int columnIndex) {
                    if (columnIndex == 0) {
                        return "Percentage of dropped users in soft handover";
                    } else {
                        if (cdma.getDroppedUsers().size() > 0) {
                            List<AbstractDmaMobile> users = cdma.getDroppedUsers();
                            int softHandover = 0;
                            for (AbstractDmaMobile user : users) {
                                if (user.isInSoftHandover()) {
                                    softHandover++;
                                }
                            }

                            double percentage = (double) softHandover
                                    / (double) users.size() * 100.0;

                            return round(percentage) + "%";
                        } else {
                            return "No dropped Users";
                        }
                    }
                }
            });
		} else if (system.getOFDMASettings() != null) {
			tableentries.add(new CDMAElementTableValue() {

				public Object getValue(int columnIndex) {
					if (columnIndex == 0) {
						return "Processing Gain";
					} else {
						return round(cdma.getProcessingGain());
					}
				}
			});
            tableentries.add(new CDMAElementTableValue() {
                public Object getValue(int columnIndex) {
                    if (columnIndex == 0) {
                        return "Percentage of active users in handover";
                    } else {
                        List<AbstractDmaMobile> users = cdma.getActiveUsers();
                        int softHandover = 0;
                        for (AbstractDmaMobile user : users) {
                            if (user.isInSoftHandover()) {
                                softHandover++;
                            }
                        }

                        double percentage = (double) softHandover
                                / (double) users.size() * 100.0;

                        return round(percentage) + "%";
                    }
                }
            });
            tableentries.add(new CDMAElementTableValue() {
                public Object getValue(int columnIndex) {
                    if (columnIndex == 0) {
                        return "Percentage of dropped users in handover";
                    } else {
                        if (cdma.getDroppedUsers().size() > 0) {
                            List<AbstractDmaMobile> users = cdma.getDroppedUsers();
                            int softHandover = 0;
                            for (AbstractDmaMobile user : users) {
                                if (user.isInSoftHandover()) {
                                    softHandover++;
                                }
                            }

                            double percentage = (double) softHandover
                                    / (double) users.size() * 100.0;

                            return round(percentage) + "%";
                        } else {
                            return "No dropped Users";
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
		return tableentries.size();
	}

	public CDMAPlotModel getSelectedCell() {
		return cdma;
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

	public void setDmaSystem(CDMAPlotModel cdmasystem) {
		this.cdma = cdmasystem;
		this.system = cdmasystem.getCellularSystem();
		initTableModel();
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).tableChanged(new TableModelEvent(this));
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

}
