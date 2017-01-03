package org.seamcat.dmasystems;

import org.apache.log4j.Logger;
import org.seamcat.mathematics.Constants;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericReceiver;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.InterfererDensity;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.model.types.Receiver;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;

import static org.seamcat.model.mathematics.Mathematics.calculateKartesianAngle;

public class LinkCalculator {

    private static final Logger LOG = Logger.getLogger(LinkCalculator.class);

	/**
	 * Calculate the tilt of the antenna with a correction factor with azimuth.
     * <p>
	 *  it calculates
     *<p>
     *  <code>tilt = antennaElevation * cosD(antennaAzimuth);</code>
     *
	 * @param antennaElevation
	 * @param antennaAzimuth
	 * @return angle in Degree
	 */
	public static double calculateElevationWithCorrectionFactorFromAzimuth(double antennaElevation, double antennaAzimuth) {
//		return antennaElevation * cosD(antennaAzimuth);
		return 0;
	}


    /**
     * Evaluates the propagation loss for any propagation model (built-in or plug-in).
     * <p>
     *  It contains a log statement
     *
     * @param propagationModel search for the appropriate propagation model
     * @param linkResult  extract information for the link where the propagation model is used
     * @param string string field to indicate in the log statement which link is used.
     * @return the propagation loss in dB
     */
    public static double calculatePropagationLoss(PropagationModel propagationModel, MutableLinkResult linkResult, String string ) {
        linkResult.trialTxRxInSameBuilding();
        double rLoss = propagationModel.evaluate(linkResult);
        if (LOG.isDebugEnabled() ) {
            LOG.debug(string + "= " + rLoss + " = " + propagationModel.getPluginClass().getSimpleName()
                    + ".evaluate( freq: " + linkResult.getFrequency() + ", dist: " + linkResult.getTxRxDistance() + ", height Tx: " + linkResult.txAntenna().getHeight() + ", height Rx: " + linkResult.rxAntenna().getHeight() +")");
        }
        return rLoss;
    }

    /**
     * Generic conversion of the vertical angle so that it remains within the [-90,+90] range definition
     * @param angle any angle in degree
     * @return angle within [-90,+90]
     */
    public static double convertAngleToConfineToVerticalDefinedRange(double angle){
        if (angle < -90) {
            return -(angle += 180);
        } else if (angle > 90) {
            return -(angle -= 180);
        }
        if ( Double.isNaN(angle))return 0.0;
        return angle;
    }

    /**
     * Generic conversion of the horizontal angle so that it remains within the [0,360] range definition
     * @param angle any angle in degree
     * @return angle within [0,360]
     */
    public static double convertAngleToConfineToHorizontalDefinedRange(double angle){
        // Check if horiAngle is within [0 - 360] range - otherwise translate
        if (angle < 0) {
            angle += 360 * ((int) Math.abs(angle / 360) + 1);
        } else if (angle > 360) {
            angle -= 360 * (int) (angle / 360);
        }
        if (angle == 360){
            angle = 0;
        }
        if ( Double.isNaN(angle))return 0.0;
        return angle;
    }

	/**
	 * Calculate the resulting elevation angle including the tilt of the antenna and any correction due to the Azimuth <br>
     *     setting of the antenna
     *<p>
	 * <b>Condition:</b> Antennas are not pointing at each other i.e. there exist an elevation angle between each other
     *
     * @param from point "from"
     * @param fromAntHeight  antenna height of the point "from"
     * @param to  point "to"
     * @param toAntHeight antenna height of the point "to"
     * @param tilt antenna tilt as input elevation parameters
     * @param azimuth azimuth shift as input azimuth parameters
     * @param string text field for the log statement
     * @return angle in degree
	 */
	public static double calculateElevationWithTilt(Point2D from, double fromAntHeight, Point2D to,double toAntHeight, double tilt, double azimuth, String string) {
		double elevation;
		double tiltWithAzimuthCorrection;
		double elevationCorrected;

		elevation = Mathematics.calculateElevation(from, fromAntHeight, to, toAntHeight);
		tiltWithAzimuthCorrection = LinkCalculator.calculateElevationWithCorrectionFactorFromAzimuth(tilt, azimuth);
			
		elevationCorrected = elevation - tiltWithAzimuthCorrection;

        if (LOG.isDebugEnabled()) {
            LOG.debug(string + " Elevation Result = " + elevationCorrected + " = elevation (" + elevation + ") - tiltCorrection (" + tiltWithAzimuthCorrection+")");
        }
		return elevationCorrected;
	}


	/**
	 * Calculate the resulting azimuth angle between the victim and interfering link at one transceiver
	 *<p>
     * the equation is as follow
     * <p>
     *  <code>txRxAzimuth = -linkAngle + azimuthInput + Constants.PID + itVrLinkAngle;</code>
     *
	 * @param linkAngle the link angle to which the transceiver is calculated (i.e. victim system link or interfering system link
	 * @param azimuthInput the azimuth set by the user for this transceiver
	 * @param itVrLinkAngle the actual angle between the victim receiver and the interferering transmitter
	 * @param string text field for the log statement
     * @return angle in degree
     */
	public static double calculateItVictimAzimuth(double linkAngle, double azimuthInput, double itVrLinkAngle, String string) {
		double txRxAzimuth;
		txRxAzimuth = -linkAngle + azimuthInput + Constants.PID + itVrLinkAngle; //REVISIT check angle of 180 degree in case co-located

        if (LOG.isDebugEnabled()) {
            LOG.debug(string + " Azimuth Result = " + txRxAzimuth + " = - LinkAngle (" + linkAngle + ") + azimuthInput (" + azimuthInput+") + PI + itVrLinkAngle ("+itVrLinkAngle+")");
        }
		return txRxAzimuth;
	}




    /**
     * EGE/1020 :Calculates the simulation radius of the area where the interferers are spread over a uniform deployment <br>
     *     density of terminals/transmitters per km^2
     *<p>
     * the equation is as follow
     *  <p>
     *      <code>SQRT((nbActive / (Math.PI * rDensActive)) + (protectionDistance^2));</code>
     *
     * @param nbActive number of active transmitter
     * @param density density of interferers 1/km^2
     * @param protectionDistance protection distance in km
     */
    public static double itSimulationRadius(int nbActive, InterfererDensity density, double protectionDistance) {
        double rDensActive = itDensityActive( density );
        return Math.sqrt(((double) nbActive / (Math.PI * rDensActive)) + (protectionDistance * protectionDistance));
    }


    /**
     * Calculates the density of active transmitter per km^2
     *<p>
     * the equation is as follow
     *  <p>
     *      <code>DensityTx * ProbabilityOfTransmission * rActivity;</code>
     *
     * @param density interferer density variables like activity (hour per day), probability of transmission, density of transmitter
     * @return the density of active transmitter per km^2
     */
    public static double itDensityActive(InterfererDensity density) {
        double rActivity = density.getActivity().evaluate(density.getHourOfDay());
        return density.getDensityTx() * density.getProbabilityOfTransmission() * rActivity;
    }


    /**
     * Calculates (setter) the effective path loss between the interfering transmitter and the victim receiver.
     *<p>
     * method using: pathloss, antenna gain at ILT and VLT
     * <p>
     *     <code>calculate: max(loss - txG - rxG, minimumCouplingLoss)</code>
     *
     * @param result mutable interference link result (ILT to VLR link)
     * @param minimumCouplingLoss minimum coupling loss
     */
    public static void itVrPropagationLoss(MutableInterferenceLinkResult result, double minimumCouplingLoss) {
        result.setTxRxPathLoss(calculatePropagationLoss(result.getInterferenceLink().getPropagationModel(), result, "pathloss between ILT and VLR"));

        double loss = result.getTxRxPathLoss();
        double txG = result.txAntenna().getGain();
        double rxG = result.rxAntenna().getGain();

        result.setEffectiveTxRxPathLoss(Math.max(loss - txG - rxG, minimumCouplingLoss));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering Transmitter -> Victim Receiver Antenna Gain = " + txG);
            LOG.debug("Victim Receiver -> Interfering Transmitter Antenna Gain = " + rxG);
            LOG.debug("Interfering Transmitter -> Victim Receiver Path Loss = " + result.getTxRxPathLoss());
            LOG.debug("Interfering Transmitter -> Victim Receiver Effective Path Loss (with MCL)= " + result.getEffectiveTxRxPathLoss());
        }
    }



}

