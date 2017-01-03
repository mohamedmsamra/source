package org.seamcat.dmasystems;

import org.apache.log4j.Logger;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.simulation.LocalEnvironmentSelector;
import org.seamcat.simulation.cellular.CellularCalculations;
import org.seamcat.simulation.result.MutableLinkResult;

import java.util.Comparator;

/**
 * Description: Abstract base class for cellular link representations (i.e. CDMA and OFDMA).
 */
public abstract class AbstractDmaLink {

	protected static final Logger LOG = Logger.getLogger(AbstractDmaLink.class);

    /**
     * compare the pathloss of 2 CDMA links. It is used to sort them at a later stage.
     *<p></p>
     * <ol>
     *     <li>if( pathloss1 > pathloss2) return 1;</li>
     *     <li>if( pathloss1 < pathloss2) return -1;</li>
     *     <li>return 0;</li>
     *</ol>
     * <p></p>
     * <code>pathloss = txRxPathLoss - BsAntGain() - UserAntGain();</code>
     */
	public static Comparator<AbstractDmaLink> CDMALinkPathlossComparator = new Comparator<AbstractDmaLink>() {

		public int compare(AbstractDmaLink l1, AbstractDmaLink l2) {
			if (l1 == null && l2 == null) {
				return 0;
			}
			if (l1 == null) {
				return -1;
			}
			if (l2 == null) {
				return 1;
			}

			double l1Result = l1.txRxPathLoss - l1.getBsAntGain() - l1.getUserAntGain();
			double l2Result = l2.txRxPathLoss - l2.getBsAntGain() - l2.getUserAntGain();

			if( l1Result > l2Result) return 1;
			if( l1Result < l2Result) return -1;
			return 0;

		}

		@Override
		public boolean equals(Object obj) {
			return false;
		}
	};
    private final Point2D baseStationNewCoordinate;

    protected double bsAntGain = 0;
    protected double userAntGain = 0;

    private AbstractDmaBaseStation basestation;
    private AbstractDmaMobile user;

    protected boolean powerScaledDownToMax = false;
    protected double totalReceivedPower;
    protected double transmittedTrafficChannelPowerdBm;

    private double txAntHeight, rxAntHeight, distance, txRxAngle, txRxElevation, txRxPathLoss, frequency;
    protected double effectivePathLoss;

    private boolean upLink;
    private MutableLinkResult linkResult;

    public boolean isUpLink() {
        return upLink;
    }

    /**
     * Prepare the basic properties of a cellular link (CDMA or OFDMA)
     *<ol>
     * <li>position the base station new coordinate after wrap-around</li>
     * <li>calculate the distance between the user and the BS</li>
     * <li>set the Tx and Rx height depending on uplink and downlink direction</li>
     * <li>Calculate Angle based on virtual wrap around position</li>
     * <li>Calculate Elevation based on virtual wrap around position</li>
     * <li>get the antenna gain</li>
     *</ol>
     * @param upLink link direction flag
     * @param _cell represent the BS
     * @param _user represent the user (i.e. MS, UE)
     */
	public AbstractDmaLink(boolean upLink, AbstractDmaBaseStation _cell, AbstractDmaMobile _user) {
		this.upLink = upLink;
        user = _user;
		basestation = _cell;
		baseStationNewCoordinate = CellularCalculations.findNewCoordinate(user.getPosition(), basestation.getPosition(),
				user.getSystem().getInterCellDistance(),
				user.getSystem().getSystemSettings().getLayout().getSystemLayout(),
                user.getSystem().getSystemSettings().getLayout().generateWrapAround(),
				user.getSystem().getSystemSettings().getLayout().getSectorSetup());

        distance = Mathematics.distance(user.getPosition(), baseStationNewCoordinate);
        if ( upLink ) {
            txAntHeight = user.getAntennaHeight();
            rxAntHeight = basestation.getAntennaHeight();
        } else {
            txAntHeight = basestation.getAntennaHeight();
            rxAntHeight = user.getAntennaHeight();
        }

		// Calculate Angle based on virtual wrap around position
		txRxAngle = Mathematics.calculateKartesianAngle(user.getPosition(), baseStationNewCoordinate);
		// Calculate Elevation based on virtual wrap around position
		txRxElevation = Mathematics.calculateElevation(user.getPosition(), user.getAntennaHeight(), baseStationNewCoordinate, basestation.getAntennaHeight());
        userAntGain = user.calculateAntennaGainTo(0, 0);
        bsAntGain = basestation.calculateAntennaGainTo(txRxAngle, txRxElevation);
	}

    /**
     * calculate the current receive power. there are 4 implementations for CDMA downlink, CDMA uplink, OFDMA downlink <br>
     *     and OFDMA uplink
     * @return the receive power in dBm
     */
	public abstract double calculateCurrentReceivePower_dBm();

    /**
     * calculate the current receive power in Watt
     *<p></p>
     * <code>Mathematics.fromdBm2Watt(calculateCurrentReceivePower_dBm())</code>
     *
     * @return the receive power in Watt
     */
	public double calculateCurrentReceivePower_Watt() {
		return Mathematics.fromdBm2Watt(calculateCurrentReceivePower_dBm());
	}

	public double calculateExternalInterference_dBm() {
		throw new IllegalStateException("Super implementation should not be called!");
	}

	public abstract void activateLink();
	
	public void connectToInActiveBaseStation() {
		basestation.addInActiveConnection(this);
	}

	public void deinitilizeConnection() {
		basestation.deinitializeConnection(this);
	}

    /**
     * calculate the pathloss (without any log file) and the effective pathloss
     *<p></p>
     * <code>effectivePathLoss = max(txRxPathLoss - (bsAntGain + userAntGain), MinimumCouplingLoss)</code>
     *
     * @param propagationModel any propagation model
     */
    public void determinePathLoss( PropagationModel propagationModel ) {
		linkResult = createLink();

        linkResult.trialTxRxInSameBuilding();
        txRxPathLoss = propagationModel.evaluate(linkResult);
        effectivePathLoss = Math.max(txRxPathLoss - (bsAntGain + userAntGain), user.getSystem().getSystemSettings().getMinimumCouplingLoss());
    }

	public void disconnect() {
		basestation.disconnectUser(this);
	}

	public AbstractDmaBaseStation getBaseStation() {
        return basestation;
    }

	public double getBsAntGain() {
		return bsAntGain;
	}

	public double getEffectivePathloss() {
        return effectivePathLoss;
	}

	public double getReceivePower_dB() {
		return totalReceivedPower;
	}

	public double getTransmittedTrafficChannelPowerWatt() {
		return Mathematics.fromdBm2Watt(transmittedTrafficChannelPowerdBm);
	}

	public double getUserAntGain() {
		return userAntGain;
	}

	public AbstractDmaMobile getUserTerminal() {
		return user;
	}

	public void initializeConnection() {
		basestation.intializeConnection(this);
	}

	public boolean isPowerScaledDownToMax() {
		return powerScaledDownToMax;
	}

	public boolean isUsingWrapAround() {
		return user.getSystem().getSystemSettings().getLayout().generateWrapAround();
	}

   public double getTotalReceivedPower() {
   	return totalReceivedPower;
   }

    public void setFrequency( double frequency ) {
        this.frequency = frequency;
    }

    public double getTxRxPathLoss() {
        return txRxPathLoss;
    }

    /**
     * Setter for the path loss and the effective path loss
     *
     * @param txRxPathLoss
     */
    public void setTxRxPathLoss(double txRxPathLoss) {
        this.txRxPathLoss = txRxPathLoss;
        effectivePathLoss = Math.max(txRxPathLoss - (bsAntGain + userAntGain), user.getSystem().getSystemSettings().getMinimumCouplingLoss());
    }

    public double getRxTxAngle() {
        return txRxAngle;
    }

    public double getDistance() {
        return distance;
    }

    public double getTxRxElevation() {
        return txRxElevation;
    }

    public MutableLinkResult asLinkResult() {
        if ( linkResult == null ) {
            linkResult = createLink();
            linkResult.setTxRxPathLoss( txRxPathLoss );
            linkResult.setEffectiveTxRxPathLoss( effectivePathLoss );
        }

        return linkResult;
    }

    private MutableLinkResult createLink() {
        MutableLinkResult linkResult = new MutableLinkResult();
        linkResult.txAntenna().setLocalEnvironment(LocalEnvironmentSelector.pickLocalEnvironment( basestation.getSystem().getSystemSettings().getTransmitter().getLocalEnvironments() ));
        linkResult.rxAntenna().setLocalEnvironment(LocalEnvironmentSelector.pickLocalEnvironment( basestation.getSystem().getSystemSettings().getReceiver().getLocalEnvironments()));
        linkResult.setTxRxDistance(distance);
        linkResult.txAntenna().setHeight( txAntHeight );
        linkResult.rxAntenna().setHeight( rxAntHeight );
        if ( isUpLink() ) {
            linkResult.txAntenna().setPosition( user.getPosition() );
            linkResult.txAntenna().setGain( userAntGain );

            linkResult.rxAntenna().setPosition( baseStationNewCoordinate );
            linkResult.rxAntenna().setGain( bsAntGain );
        } else {
            linkResult.rxAntenna().setPosition( user.getPosition() );
            linkResult.rxAntenna().setGain( userAntGain );

            linkResult.txAntenna().setPosition( baseStationNewCoordinate );
            linkResult.txAntenna().setGain( bsAntGain );

        }
        linkResult.setFrequency(frequency);

        return linkResult;
    }
}
