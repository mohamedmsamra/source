package org.seamcat.cdma;

import org.seamcat.cdma.exceptions.ScalingException;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;

public abstract class CdmaBaseStation extends AbstractDmaBaseStation {

	protected CDMASystem cdmasystem;
    private int inactiveSize, activeSize;
    private double cacheValue;


	public CdmaBaseStation(Point2D position, CDMASystem _system, int _cellid, double antHeight, double antennaTilt,
	      int sectorId) {
		super(position, _system, _cellid, antHeight, antennaTilt, sectorId);
		cdmasystem = _system;
        inactiveSize = 0;
        activeSize = 0;
        cacheValue = 0;
	}

    /**
     * calculate the summation over the active connection the transmitt traffic channel power in Watt as part of the <br>
     *     soft handover process
     *
     * @return the current channel transmit power
     */
	public double calculateCurrentChannelPower_dBm() {
        if ( activeSize < activeConnections.size() ) {
            for ( int i=activeSize; i<activeConnections.size(); i++ ) {
                cacheValue += activeConnections.get(i).getTransmittedTrafficChannelPowerWatt();
                activeSize++;
            }
        }
        setCurrentChannelTransmitPower(Mathematics.fromWatt2dBm(cacheValue));
		return getCurrentChannelTransmitPower();
	}

	/**
	 * Uplink specific method that calculates interference caused by all other mobiles in system.<br>
     * <p>
	 * Given as the :
     * <p></p>
     * <code>SUM (Mobile TX - Individual Pathloss)</code>
	 * 
	 * @return Sum of inter system interference
	 */
	public double calculateInterSystemInterference_dBm(AbstractDmaLink excludelink) {
        if ( activeSize < activeConnections.size() ) {
            for ( int i=activeSize; i<activeConnections.size(); i++) {
                cacheValue = Mathematics.powerSummation(cacheValue, activeConnections.get( activeSize ).calculateCurrentReceivePower_dBm() );
                activeSize++;
            }
        }
        if ( inactiveSize < inActiveConnections.size() ) {
            for ( int i=inactiveSize; i<inActiveConnections.size(); i++) {
                cacheValue = Mathematics.powerSummation(cacheValue, inActiveConnections.get( inactiveSize ).calculateCurrentReceivePower_dBm() );
                inactiveSize++;
            }
        }
        if ( excludelink != null && !activeConnections.contains(excludelink) && !inActiveConnections.contains(excludelink)) {
            setInterSystemInterference( Mathematics.powerSummation(cacheValue, excludelink.calculateCurrentReceivePower_dBm()));
            excludelink = null;
        }

        if ( cacheValue == 0 ) {
            setInterSystemInterference(-1000);
        } else {
            setInterSystemInterference(cacheValue);
        }

        if ( excludelink == null ) {
            if ( cacheValue == 0 ) {
                return -1000;
            }
            return cacheValue;
        } else {
            double v = Mathematics.powerSubtract(cacheValue, excludelink.calculateCurrentReceivePower_dBm());
            if ( Math.abs(v) < 0.001 ) {
                return -1000;
            } else {
                return v;
            }
        }
	}

    /**
     * update the inter system interference after removing active users (i.e. active links). It is used in both the <br>
     *     uplink and downlink
     * <ol>
     *     <li>in uplink: <code>subtract(calculateCurrentReceivePower_dBm)</code></li>
     *     <li>in downlink: <code>substract(TransmittedTrafficChannelPowerWatt)</code></li>
     * </ol>
     *
     * @param link
     */
    @Override
    public void removeActive(AbstractDmaLink link) {
        super.removeActive( link );
        if ( isUpLinkMode() ) {
            cacheValue = Mathematics.powerSubtract(cacheValue, link.calculateCurrentReceivePower_dBm());
            activeSize--;
        } else {
            if ( activeSize > 0 ) {
                cacheValue -= link.getTransmittedTrafficChannelPowerWatt();
                activeSize--;
            }
        }
        setInterSystemInterference(cacheValue);
    }

    /**
     * update the inter system interference after removing inactive users (i.e. active links). perform a calculation for<br>
     *     uplink only. The inter system interference remains unchanged in downlink.
     * <p></p>
     * in uplink: <code>subtract(calculateCurrentReceivePower_dBm)</code>
     *
     * @param link
     */
    @Override
    public void removeInActive( AbstractDmaLink link ) {
        super.removeInActive(link);
        if ( isUpLinkMode() ) {
            cacheValue = Mathematics.powerSubtract(cacheValue, link.calculateCurrentReceivePower_dBm());
        }
        inactiveSize--;
        setInterSystemInterference(cacheValue);
    }

    @Override
    public void deinitializeConnection(AbstractDmaLink link) {
        if ( activeConnections.contains( link) && !inActiveConnections.contains(link)) {
            activeSize--;
            inactiveSize++;
        } else if (activeConnections.contains( link) && inActiveConnections.contains(link)) {
            throw new RuntimeException("Consistency error: link both in active and in-active connections");
        }
        super.deinitializeConnection(link);
    }

    @Override
    public void intializeConnection(AbstractDmaLink link) {
        if ( inActiveConnections.contains(link) && !activeConnections.contains(link)) {
            inactiveSize--;
            activeSize++;
            // link moved no changes to total interference
        } else if (inActiveConnections.contains(link) && activeConnections.contains(link)) {
            // should never happen
            throw new RuntimeException("Consistency error: link both in active and in-active connections");
        }
        super.intializeConnection(link);
    }

    /**
     * calculate the outage
     *<p></p>
     * <code>outage = dropped / (connected + dropped)</code>
     *
     * @return outage
     */

    @Override
	public double calculateOutage() {
		double connected = countActiveUsers();
		connected += countInActiveUsers();
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

    /**
     * Method that calculates scaling factor that should be applied to the traffic channel power levels
     * <p></p>
     * <code>scaling = MaximumChannelPower_Watt/CurrentChannelTransmitPowerInWatts</code>
     * @return ratio
     */
	public double calculateScaling() {
		double pMax = getMaximumChannelPower_Watt();
		double pCalc = getCurrentChannelTransmitPowerInWatts();

		if (pCalc > pMax) {
			// downlink scaling factor (Lucent EQ(7)):
			return pMax / pCalc;
		}
		return 1;
	}

	/**
	 * Uplink calculation of total interference (Qualcomm EQ(5) - STG(03)13rev1)
	 * 
	 * @param link to exclude from interference calculations
	 *
	 * @return total interference in dBm's
	 */
	@Override
	public double calculateTotalInterference_dBm(AbstractDmaLink link) {
		double internalSystem = Mathematics.fromdBm2Watt(calculateInterSystemInterference_dBm(link));

		double external = getExternalInterference();
		double thermal = getSystem().getResults().getThermalNoise();

		external = Mathematics.fromdBm2Watt(external);

		setTotalInterference( Mathematics.fromWatt2dBm(internalSystem + external + thermal) );
		return getTotalInterference();
	}


	@Override
	public double calculateInterferenceWithoutExternal_dBm(AbstractDmaLink link) {
		double internalSystem = Mathematics.fromdBm2Watt(calculateInterSystemInterference_dBm(link));
		double thermal = getSystem().getResults().getThermalNoise();

		return Mathematics.fromWatt2dBm(internalSystem + thermal);
	}

	public CDMASystem getCdmasystem() {
		return (CDMASystem) getSystem();
	}

	public double getCurrentChannelTransmitPowerInWatts() {
		return Mathematics.fromdBm2Watt(getCurrentChannelTransmitPower());
	}

    /**
     * compute the denominator part of the scaling ratio used in downlink (power control in soft handover)
     * <p></p>
     * <code>P_calculate = calculateCurrentChannelPower_dBm()</code>
     * <p></p>
     * <code>pilot_fraction = getPilotTransmitPower()</code>
     * <p></p>
     * <code>overhead_fraction = getOverheadTransmitPower()</code>
     *
     * @return denominator part of the scaling ratio used in downlink
     */
	@Override
	public double getCurrentTransmitPower_dBm() {
        return Mathematics.powerSummation(calculateCurrentChannelPower_dBm(), getOverheadTransmitPower(), getPilotTransmitPower() );
	}

    /**
     * nominator part of the calculation for the scaling power ratio in downlink
     * <p></p>
     * <code>Maximum channel power = MaximumTransmitPower * (1 - (OverheadFraction + PilotFraction))</code>
     */
	public double getMaximumChannelPower_Watt() {
		return Mathematics.fromdBm2Watt(getMaximumTransmitPower())
		      * (1 - (getOverheadFraction() + getPilotFraction()));
	}

	public double getOverheadPower_Watt() {
		return Mathematics.fromdBm2Watt(getMaximumTransmitPower()) * getOverheadFraction();
	}

	public double getPilotPower_Watt() {
		return Mathematics.fromdBm2Watt(getMaximumTransmitPower()) * getPilotFraction();
	}

	/**
	 * Initialise the transmit power for CDMA downlink
     *
	 */
	public void initializeTransmitPowerLevels() {
		double maxPowerInWatts = Mathematics.fromdBm2Watt(getMaximumTransmitPower());
		// Convert absolute watt values to dBm
		setPilotTransmitPower( Mathematics.fromWatt2dBm( maxPowerInWatts * getPilotFraction() ) );
		setOverheadTransmitPower( Mathematics.fromWatt2dBm(maxPowerInWatts * getOverheadFraction()) );
		setCurrentChannelTransmitPower( Mathematics.fromWatt2dBm(0) );
	}

	@Override
	public void resetBaseStation() {
		resetCell();
	}

	public void resetCell() {
		activeConnections.clear();
		inActiveConnections.clear();
		droppedUsers.clear();
		inactiveUsers.clear();
        resetSummedInterference();
		setCurrentChannelTransmitPower( Mathematics.fromWatt2dBm(0) );
		setExternalInterferenceUnwanted(-1000);
        setExternalInterferenceBlocking(-1000);
	}

    public void resetSummedInterference() {
        activeSize = 0;
        inactiveSize = 0;
        cacheValue = 0;
    }

	public void scaleChannelPower() throws ScalingException {
		scaleChannelPower(calculateScaling());
	}

	/**
	 * CDMA downlink power scaling
	 */
	public void scaleChannelPower(double scaleFactor) throws ScalingException {
		if (scaleFactor == 1) {
			return;
		} else if (scaleFactor > 1) {
			throw new ScalingException(
			      "CDMA Power levels cannot be scaled up. [Scale value was: "
			            + scaleFactor + "]");
		}
        cacheValue = 0;
        activeSize = 0;
		for (int i = 0; i < activeConnections.size(); i++) {
			((CDMADownlink) activeConnections.get(i)).scaleTransmitPower(scaleFactor);
		}

		calculateCurrentChannelPower_dBm();

		for (int i = 0; i < activeConnections.size(); i++) {
			CdmaUserTerminal u = (CdmaUserTerminal) activeConnections.get(i).getUserTerminal();
			try {
				u.calculateReceivedPower();
				u.calculateAchievedEcIor();

				if (!u.meetsEcIorRequirement(cdmasystem.getSystemSettings().getCDMASettings().getCallDropThreshold())) {
					cdmasystem.dropActiveUser(u);
					u.setDropReason("Ec/Ior requirement does not meet while scaling the channel power");
					i--;
				}

			} catch (Exception ex) {
				throw new ScalingException(ex);
			}
		}
	}

	@Override
	public String toString() {
		return "CDMA Cell # " + getCellid() + " at " + getPosition() + (isUpLinkMode() ?
                " - Noise Rise: " + getNoiseRise() + "dB" : "") + (isUpLinkMode() ? " - Linear Noise Rise: "
                + getNoiseRiseLinearFactor() : "");
	}

}
