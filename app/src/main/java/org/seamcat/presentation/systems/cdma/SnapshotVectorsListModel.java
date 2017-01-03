package org.seamcat.presentation.systems.cdma;

import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.simulation.result.Interferer;
import org.seamcat.ofdma.OfdmaExternalInterferer;
import org.seamcat.ofdma.OfdmaMobile;
import org.seamcat.ofdma.OfdmaUplink;
import org.seamcat.ofdma.OfdmaVictim;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.*;


public class SnapshotVectorsListModel implements ListModel {

	protected List<String> vectorNames;
	protected List<ListDataListener> listeners;
	protected Map<String, SnapshotVectorGenerator> generators;

	protected CDMAPlotModel model;

	public SnapshotVectorsListModel(CDMAPlotModel model) {
		this.model = model;

		listeners = new ArrayList<>();
		generators = new HashMap<>();

		init();

		vectorNames = new ArrayList<String>(generators.keySet());
		Collections.sort(vectorNames);
	}

	protected void init() {
		initGeneric();
		if (model.getActiveUsers().size() > 0) {
			initGenericActive();
		}
        CellularSystem system = model.getCellularSystem();
        if (system.getOFDMASettings() != null) {
			initGenericOfdma();
			if (!system.isUpLink()) {
				if (model.getActiveUsers().size() > 0) {
					initGenericOfdmaActiveSystem();
					initGenericOfdmaDownlink_ReferenceCellHasActiveConnections();
				}
				initGenericDownlink();
				initOfdmaDownlink();
			} 
			if (system.isUpLink()) {
				initGenericUplink();
				initOfdmaUplink();
			}
		}
		if (system.getCDMASettings() != null) {
			initGenericCdma();
			if (!system.isUpLink()) {
				initGenericDownlink();
				initCdmaDownlink();
			} 
			if (system.isUpLink()) {
				initGenericUplink();
				initCdmaUplink();
			}
		}


	}

	protected void initGeneric() {
		if (model.isVictimSystem()) {
			generators.put("TX Power External Interferers", new SnapshotVectorGenerator("External Interferer", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (Interferer inter : system.getExternalInterferers()) {
						data.add(inter.getLinkResult().getTxPower());
					}
					return data;
				}
			});
			
			generators.put("Number of dropped users per BS", new SnapshotVectorGenerator("BS", "Number of dropped users per BS") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						data.add((double)base.countDroppedUsers());
					}
					return data;
				}
			});

		}
	}

	protected void initGenericActive() {
		generators.put("Effective Pathloss, active links (all cells)", new SnapshotVectorGenerator("Active Users","dB") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();
				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
						double value = link.getEffectivePathloss();

						data.add(value);
					}
				}
				return data;
			}
		});

		generators.put("Effective Pathloss to Ext. Interferer (ref cell, all interferers)", new SnapshotVectorGenerator("Active Links", "dB") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				AbstractDmaBaseStation referenceCell = system.getReferenceCell();
				List<AbstractDmaLink> activeConnections = referenceCell.getOldTypeActiveConnections();
					for (AbstractDmaLink link : activeConnections ) {
						double value = link.getEffectivePathloss();

						data.add(value);
					}
				return data;
			}
		});

		generators.put("Calculated Pathloss, active links (all cells)", new SnapshotVectorGenerator("Active Links", "dB") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();


				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
						double value = link.getTxRxPathLoss();

						data.add(value);
					}
				}
				return data;
			}
		});

		generators.put("Size of activelist, active users (all cells)", new SnapshotVectorGenerator("Active Users", "") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
						data.add((double) link.getUserTerminal().getActiveList().size());
					}
				}
				return data;
			}
		});
		generators.put("Distance to first basestation in active list, active users (all cells)", new SnapshotVectorGenerator("Active Users", "km") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
						data.add(link.getDistance());
					}
				}
				return data;
			}
		});

		generators.put("RX Power, active links (all cells)", new SnapshotVectorGenerator("Active Links", "dBm") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
						data.add(link.getTotalReceivedPower());
					}
				}
				return data;
			}
		});

	}

	protected void initGenericDownlink() {


		generators.put("TX Power, basestations", new SnapshotVectorGenerator("BaseStations", "dBm") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					data.add(base.getCurrentTransmitPower_dBm());
				}
				return data;
			}
		});

		if (model.isVictimSystem()) {
			if (model.getReferenceCell().countActiveUsers() > 0) {
				generators.put("External Interference, active users (Ref Cell)", new SnapshotVectorGenerator("Active Users", "dBm") {

					@Override
					public List<Double> generateVector(CDMAPlotModel system) {
						List<Double> data = new ArrayList<Double>();

						for (AbstractDmaLink link : system.getReferenceCell().getOldTypeActiveConnections()) {
							data.add(link.getUserTerminal().getExternalInterference());
						}
						return data;
					}
				});
			}
			if (model.getReferenceCell().countDroppedUsers() > 0) {
				generators.put("External Interference, dropped users (Ref Cell)", new SnapshotVectorGenerator("Dropped Users", "dBm") {

					@Override
					public List<Double> generateVector(CDMAPlotModel system) {
						List<Double> data = new ArrayList<Double>();

						for (AbstractDmaLink link : system.getReferenceCell().getDroppedUsers()) {
							data.add(link.getUserTerminal().getExternalInterference());
						}
						return data;
					}
				});
			}
		}
		if (model.getActiveUsers().size() > 0) {
			if (model.isVictimSystem()) {
				generators.put("External Interference, active users (all cells)", new SnapshotVectorGenerator("Active Users","dBm") {

					@Override
					public List<Double> generateVector(CDMAPlotModel system) {
						List<Double> data = new ArrayList<Double>();

						for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
							for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
								data.add(link.getUserTerminal().getExternalInterference());
							}
						}
						return data;
					}
				});
			}

		}

		if (model.getDroppedUsers().size() > 0) {
			if (model.isVictimSystem()) {
				generators.put("External Interference, dropped users (all cells)", new SnapshotVectorGenerator("Dropped Users", "dBm") {

					@Override
					public List<Double> generateVector(CDMAPlotModel system) {
						List<Double> data = new ArrayList<Double>();

						for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
							for (AbstractDmaLink link : base.getDroppedUsers()) {
								data.add(link.getUserTerminal().getExternalInterference());
							}
						}
						return data;
					}
				});
			}
		}
	}

	protected void initGenericUplink() {

		generators.put("Inter System Interference, base stations", new SnapshotVectorGenerator("BaseStations", "dBm") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					data.add(base.getInterSystemInterference());
				}
				return data;
			}
		});
		if (model.getActiveUsers().size() > 0) {
			generators.put("TX Power, active users (all cells)", new SnapshotVectorGenerator("Active Users", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
							data.add(link.getUserTerminal().getCurrentTransmitPowerIndBm());
						}
					}
					return data;
				}
			});
		}
	}

	protected void initGenericOfdma() {

		if (model.isVictimSystem()) {
			generators.put("Effective Pathloss to Ext. Interferer (all victims, all interferers)", new SnapshotVectorGenerator("All Victim Receivers", "dB") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();
					for (OfdmaVictim victim : model.getOfdmaVictims()) {
						for (OfdmaExternalInterferer ext : victim.getExternalInterferers()) {
							data.add(ext.getPathloss());
						}
					}
					return data;
				}
			});
			
			generators.put("External Interference - blocking (all victims, all interferers)", new SnapshotVectorGenerator("All Interfering Signals", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();
					for (OfdmaVictim victim : model.getOfdmaVictims()) {
						for (OfdmaExternalInterferer ext : victim.getExternalInterferers()) {
							data.add(ext.getExternalBlocking());
						}
					}


					double[] rawdata = new double[data.size()];

					for (int i = 0;i < data.size();i++) {
						rawdata[i] = data.get(i);
					}

					return data;
				}
			});
			generators.put("External Interference - unwanted (all victims, all interferers)", new SnapshotVectorGenerator("All Interfering Signals", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();
					for (OfdmaVictim victim : model.getOfdmaVictims()) {
						for (OfdmaExternalInterferer ext : victim.getExternalInterferers()) {
							data.add(ext.getExternalUnwanted());
						}
					}
					return data;
				}
			});
		}

	}

	private void initGenericOfdmaDownlink_ReferenceCellHasActiveConnections() {
		if (model.isVictimSystem()) {
			generators.put("External Interference, Blocking (ref cell)", new SnapshotVectorGenerator("Active Links", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaLink link : model.getReferenceCell().getOldTypeActiveConnections()) {
						OfdmaVictim victim = (OfdmaVictim) link.getUserTerminal();
						data.add(victim.getExternalBlocking_dBm());
					}
					return data;
				}
			});

			generators.put("External Interference, Unwanted (ref cell)", new SnapshotVectorGenerator("Active Links", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaLink link : model.getReferenceCell().getOldTypeActiveConnections()) {
						OfdmaVictim victim = (OfdmaVictim) link.getUserTerminal();
						data.add(victim.getExternalUnwanted_dBm());
					}
					return data;
				}
			});
		}
		generators.put("Achieved SINR, active users (Ref Cell)", new SnapshotVectorGenerator("Active Users", "dB") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaLink link : system.getReferenceCell().getOldTypeActiveConnections()) {
					data.add(link.getUserTerminal().getSINRAchieved());
				}
				return data;
			}
		});

	}

	private void initGenericOfdmaActiveSystem() {
		generators.put("Achieved SINR, active users (all cells)", new SnapshotVectorGenerator("Active Users", "dB") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
						data.add(link.getUserTerminal().getSINRAchieved());
					}
				}
				return data;
			}
		});

		generators.put("Achieved bitrate, active users (all cells)", new SnapshotVectorGenerator("Active Users", "kbps") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
						data.add(((OfdmaMobile) link.getUserTerminal()).calculateAchievedBitrate());
					}
				}
				return data;
			}
		});

	}

	protected void initGenericCdma() {
		generators.put("Geometry, active users (all cells)", new SnapshotVectorGenerator("Active Users", "dB") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
						data.add(link.getUserTerminal().getGeometry());
					}
				}
				return data;
			}
		});
	}

	protected void initOfdmaDownlink() {
		if (model.isVictimSystem()) {
			generators.put("External Interference, Blocking (all victims)", new SnapshotVectorGenerator("All Victim Receivers", "dBm") {
				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
							data.add(link.getUserTerminal().getExternalInterferenceBlocking());
						}
					}
					return data;
				}
			});

			generators.put("External Interference, Unwanted (all victims)", new SnapshotVectorGenerator("All Victim Receivers", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						for (AbstractDmaLink link : base.getOldTypeActiveConnections()) {
							data.add(link.getUserTerminal().getExternalInterferenceUnwanted());
						}
					}
					return data;
				}
			});

		}
	}
	
	protected void initOfdmaUplink() {
		if (model.isVictimSystem()) {
			generators.put("External Interference, Blocking (all victims)", new SnapshotVectorGenerator("All Victim Receivers", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						List<AbstractDmaLink> list = base.getOldTypeActiveConnections();
						for (AbstractDmaLink l : list) {
                            OfdmaUplink link = (OfdmaUplink) l;
                            data.add(link.calculateExternalInterferenceBlocking_dBm());
						}
					}
					return data;
				}
			});

			generators.put("External Interference, Unwanted (all victims)", new SnapshotVectorGenerator("All Victim Receivers", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						List<AbstractDmaLink> list = base.getOldTypeActiveConnections();
						for (AbstractDmaLink l : list) {
                            OfdmaUplink link = (OfdmaUplink) l;
                            data.add(link.calculateExternalInterferenceUnwanted_dBm());
						}
					}
					return data;
				}
			});
			
			generators.put("External Interference, all cells", new SnapshotVectorGenerator("BaseStations", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						List<AbstractDmaLink> list = base.getOldTypeActiveConnections();
						for (AbstractDmaLink l : list) {
                            OfdmaUplink link = (OfdmaUplink) l;
                            data.add(link.calculateExternalInterference_dBm());
						}
					}
					return data;
				}
			});
			generators.put("Frequency of Mobiles", new SnapshotVectorGenerator("Active Links", "MHz") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						List<AbstractDmaLink> list = base.getOldTypeActiveConnections();
						for (AbstractDmaLink l : list) {
                            OfdmaUplink link = (OfdmaUplink) l;
                            data.add(link.calculateFrequency());
						}
					}
					return data;
				}
			});
		}



	}

	protected void initCdmaUplink() {
		if (model.isVictimSystem()) {
			generators.put("External Interference, all cells", new SnapshotVectorGenerator("BaseStations", "dBm") {

				@Override
				public List<Double> generateVector(CDMAPlotModel system) {
					List<Double> data = new ArrayList<Double>();

					for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
						data.add(base.getExternalInterference());
					}
					return data;
				}
			});
		}

		generators.put("Noise Rise over Noise Floor", new SnapshotVectorGenerator("Noise Rise over Noise Floor", "dB") {

			@Override
			public List<Double> generateVector(CDMAPlotModel system) {
				List<Double> data = new ArrayList<Double>();

				for (AbstractDmaBaseStation base : system.getAllBaseStations()) {
					data.add(base.calculateNoiseRiseOverThermalNoise_dB());
				}
				return data;
			}
		});
	}

	protected void initCdmaDownlink() {

	}

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
	}

	@Override
	public Object getElementAt(int index) {
		return vectorNames.get(index);
	}

	@Override
	public int getSize() {
		return vectorNames.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

	public double[] getVectorData(String name) {
		List<Double> data = generators.get(name).generateVector(model);
		double[] rawdata = new double[data.size()];

		for (int i = 0;i < data.size();i++) {
			rawdata[i] = data.get(i);
		}

		return rawdata;
	}

	public String getUnit(String name) {
		return generators.get(name).getUnit();
	}

	public String getLabel(String name) {
		return generators.get(name).getXLabel();
	}

	public static abstract class SnapshotVectorGenerator {

		protected boolean hasData = true;
		protected String errorMessage = "";

		protected String xLabel;
		private String unit;

		public SnapshotVectorGenerator(String x, String unit) {
			xLabel = x;
			this.unit = unit;
		}

		public abstract List<Double> generateVector(CDMAPlotModel system);

		public final String getUnit() {
			return unit;
		}

		public boolean hasData() {
			return hasData;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public String getXLabel() {
			return xLabel;
		}
	}
}
