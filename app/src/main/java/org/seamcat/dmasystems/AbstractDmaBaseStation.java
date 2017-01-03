package org.seamcat.dmasystems;

import org.apache.log4j.Logger;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.simulation.result.MutableBaseStationResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractDmaBaseStation extends MutableBaseStationResult {

    protected static final Logger LOG = Logger.getLogger(AbstractDmaBaseStation.class);

	public static Comparator<AbstractDmaMobile> lowestTransmittingUser = new Comparator<AbstractDmaMobile>() {
		public int compare(AbstractDmaMobile u1, AbstractDmaMobile u2) {
			if (u1.getCurrentTransmitPower() < u2.getCurrentTransmitPower()) {
				return -1;
			} else if (u1.getCurrentTransmitPower() > u2.getCurrentTransmitPower()) {
				return 1;
			} else {
				return 0;
			}
		}

	};

	public static Comparator<AbstractDmaBaseStation> highestNoiseRise = new Comparator<AbstractDmaBaseStation>() {
		public int compare(AbstractDmaBaseStation u1, AbstractDmaBaseStation u2) {
			if (u1.getRelativeCellNoiseRise() > u2.getRelativeCellNoiseRise()) {
				return -1;
			} else if (u1.getRelativeCellNoiseRise() < u2.getRelativeCellNoiseRise()) {
				return 1;
			} else {
				return 0;
			}
		}
	};

    protected List<AbstractDmaLink> droppedUsers;
    protected List<AbstractDmaLink> activeConnections;
    protected List<AbstractDmaLink> inActiveConnections;
    protected List<AbstractDmaLink> inactiveUsers;

    private AbstractDmaSystem system;
    public AbstractDmaSystem getSystem() {
        return system;
    }

    @Override
    public double getFrequency() {
        return system.getFrequency();
    }

    @Override
    public double getReferenceBandwidth() {
        return system.getSystemSettings().getBandwidth();
    }

    public AbstractDmaBaseStation(Point2D position, AbstractDmaSystem _system, int _cellid, double antHeight, double antennaTilt, int sectorId) {
        super(_system.getResults());
        this.system = _system;
        setPosition(position);
        setAntennaHeight(antHeight);
        setAntennaTilt(antennaTilt);
        setSectorId(sectorId);
        setCellId(_cellid);

		activeConnections = new ArrayList<AbstractDmaLink>();
		inActiveConnections = new ArrayList<AbstractDmaLink>();
		inactiveUsers = new ArrayList<AbstractDmaLink>();
		droppedUsers = new ArrayList<AbstractDmaLink>();
	}

    public void addInActiveConnection(AbstractDmaLink link) {
		inActiveConnections.add(link);
	}

	public void addVoiceInActiveUser(AbstractDmaLink user) {
		inactiveUsers.add(user);
	}

    /**
     * Calculate the noise rise over the thermal noise in dB
     *<p></p>
     * <code>noise rise over the thermal noise = Itotal - Nt</code>
     *<p></p>
     * where
     * <p></p>
     * <ol>
     *     <ul>Itotal = calculateTotalInterference_dBm</ul>
     *     <ul>Nt = thermal noise</ul>
     *</ol>
     * @return noise rise over the thermal noise in dB
     */
	public double calculateNoiseRiseOverThermalNoise_dB() {
		double Nt = system.getResults().getThermalNoise();
		double Itotal = calculateTotalInterference_dBm(null);
		setNoiseRise(Itotal - Mathematics.fromWatt2dBm(Nt));
		return getNoiseRise();
	}

    /**
     * Calculate the noise rise over the thermal noise in linear
     *<p></p>
     * <code>Math.pow(10,calculateNoiseRiseOverThermalNoise_dB() / 10)</code>
     *
     * @return the noise rise over the thermal noise in linear
     */
	public double calculateNoiseRiseOverThermalNoise_LinearyFactor() {
		setNoiseRiseLinearFactor( Math.pow(10,calculateNoiseRiseOverThermalNoise_dB() / 10) );
		return getNoiseRiseLinearFactor();
	}

    /**
     * Calculate the noise rise over the thermal noise in dB where there is no external interference
     * <p></p>
     * <code>noise rise over the thermal noise = Itotal - Nt</code>
     *<p></p>
     * where
     * <p></p>
     * <ol>
     *     <ul>Itotal = calculateInterferenceWithoutExternal_dBm</ul>
     *     <ul>Nt = thermal noise()</ul>
     *</ol>
     * @return noise rise over the thermal noise in dB where there is no external interference
     */

    public double calculateNoiseRiseOverThermalNoiseWithoutExternal_dB() {
		double Nt = system.getResults().getThermalNoise();
		double Itotal = calculateInterferenceWithoutExternal_dBm(null);
        return Itotal - Mathematics.fromWatt2dBm(Nt);
	}

    /**
     * Calculate the noise rise over the thermal noise in linear where there is no external interference
     *<p></p>
     * <code>Math.pow(10,calculateNoiseRiseOverThermalNoiseWithoutExternal_dB() / 10)</code>
     *
     * @return the noise rise over the thermal noise in linear where there is no external interference
     */
    public double calculateNoiseRiseOverThermalNoiseWithoutExternal_LinearyFactor() {
		setNoiseRiseLinearFactor( Math.pow(10,calculateNoiseRiseOverThermalNoiseWithoutExternal_dB() / 10) );
		return getNoiseRiseLinearFactor();
	}

    /**
     * calculate the outage
     *<p></p>
     * <code>outage = dropped / (connected + dropped)</code>
     *
     * @return outage
     */
	public double calculateOutage() {
		double connected = countActiveUsers();
		if (connected < 1) {
			return 1;
		}
		double dropped = countDroppedUsers();
		double outage = 0.0;
		if ((connected + dropped) == 0.0 || dropped == 0.0){
			outage = dropped;
		}else{
			outage = dropped / (connected + dropped);
		}

		return outage;
	}

	public abstract double calculateTotalInterference_dBm(AbstractDmaLink excludeLink);

	public abstract double calculateInterferenceWithoutExternal_dBm(
            AbstractDmaLink excludeLink);

	public int countActiveUsers() {
		int capacity = 0;
		for (AbstractDmaLink link : activeConnections) {
			if (link.getUserTerminal().getActiveList().get(0).getBaseStation() == this ||
					(link.getUserTerminal().getActiveList().get(1).getBaseStation() == this)){
				capacity++;
			}
		}
		return capacity;
	}

	public int countDroppedUsers() {
		int capacity = 0;
		for (AbstractDmaLink link : droppedUsers) {
			if (link.getUserTerminal().getActiveList().get(0).getBaseStation() == this ||
					link.getUserTerminal().getActiveList().get(1).getBaseStation() == this) {
				capacity++;
			}
		}
		return capacity;
	}

	public int countInActiveUsers() {
		int capacity = 0;
		for (AbstractDmaLink link : inactiveUsers) {
			if ((link.getUserTerminal().getActiveList().get(0)).getBaseStation() == this) {
				capacity++;
			}
		}
		return capacity;
	}

	public int countServedUsers() {
		return countActiveUsers() + countInActiveUsers();
	}

	public void deinitializeConnection(AbstractDmaLink link) {
        activeConnections.remove(link);
		inActiveConnections.add(link);
	}

    public void intializeConnection(AbstractDmaLink link) {
        activeConnections.add(link);
        inActiveConnections.remove(link);

    }

	public void disconnectUser(AbstractDmaLink linkToMobile) {
		if (activeConnections.contains(linkToMobile)) {
            removeActive(linkToMobile);
		} else {
			removeInActive(linkToMobile);
		}
	}

    public void removeActive( AbstractDmaLink linkToMobile ) {
        droppedUsers.add(linkToMobile);
        activeConnections.remove(linkToMobile);
    }

    public void removeInActive( AbstractDmaLink linkToMobile ) {
        inActiveConnections.remove(linkToMobile);
    }

	public List<AbstractDmaLink> getOldTypeActiveConnections() {
		return activeConnections;
	}

	public int getCellid() {
		return getCellId();
	}

	public abstract double getCurrentTransmitPower_dBm();

	public List<AbstractDmaLink> getDroppedUsers() {
		return droppedUsers;
	}

    /**
     * Calculate the maximum transmit power in watt
     * @return Mathematics.fromdBm2Watt(getMaximumTransmitPower())
     */
	public double getMaximumTransmitPower_Watt() {
		return Mathematics.fromdBm2Watt(getMaximumTransmitPower());
	}

	public double getOutagePercentage() {
		return calculateOutage() * 100;
	}

	public abstract void resetBaseStation();

	public void setMaximumTransmitPower_dBm(double maximumTransmitPower) {
		setMaximumTransmitPower(maximumTransmitPower);
	}

    public void translateLocation(Point2D factor ) {
        setPosition( getPosition().add( factor ));
    }

	public void setCellNoiseRiseInitial( double initialCellNoiseRise) {
		setInitialCellNoiseRise(initialCellNoiseRise);
	}

	public double getCellNoiseRiseInitial( ) {
		return getInitialCellNoiseRise();
	}

}
