package org.seamcat.simulation.generic;

import org.apache.log4j.Logger;
import org.seamcat.dmasystems.ActiveInterferer;
import org.seamcat.dmasystems.LinkCalculator;
import org.seamcat.model.correlation.CorrelationModeCalculator;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.*;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.systems.ParallelSimulation;
import org.seamcat.model.systems.SystemPlugin;
import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.model.types.Receiver;
import org.seamcat.model.types.Transmitter;
import org.seamcat.simulation.result.MutableAntennaResult;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.MutableLinkResult;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;
import static org.seamcat.model.mathematics.Mathematics.*;
import static org.seamcat.model.systems.SystemPlugin.CorrelationMode.*;

public class GenericSystemPlugin implements SystemPlugin<SystemModelGeneric, GenericSystem> {

    private static final Logger LOG = Logger.getLogger(GenericSystemPlugin.class);

    @Override
    public GenericSystem convert(SystemModelGeneric generic) {
        return null;
    }

    @Override
    public ParallelSimulation simulationInstance() {
        return null;
    }

    @Override
    public List<CorrelationMode> getCorrelationModes() {
        return Arrays.asList( NONE, UNIFORM, CLOSEST, CORRELATED);
    }

    @Override
    public List<String> getCorrelationPointNames() {
        return Arrays.asList("Rx", "Tx");
    }


    /**
     * Calculates the dRSS distribution when the distance between the victim receiver and the wanted transmitter is <br>
     *     assumed to be variable. (EGE/2070)
     *<p>
     * the equation is as follow
     *  <p>
     *      <code>dRSS = Vt_Power + Vt_Gain + Vr_Gain - PathLoss;</code>
     *
     * @param result mutable link result
     * @param system generic system type
     * @return return the dRSS value in dBm
     */
    public static double dRSSLinkBudgetDef(MutableLinkResult result, GenericSystem system) {
        double rWtPower;
        double rWtGain, rVrGain;
        double rPathLoss;
        double rSens, rPcmax;
        double rdRSSValue;

        wtTrial(result, system.getTransmitter());
        vrTrial(result, system);
        calculateRelativeTxRxLocation(result, system.getLink(), "VLT -> VLR");
        calculatePathAntAziElev(result, system.getTransmitter(), system.getReceiver(), "Victim System Link");
        pathAntGains(result, system.getReceiver(), system.getTransmitter());


        PropagationModel propagationModel = system.getLink().getPropagationModel();
        result.setTxRxPathLoss(LinkCalculator.calculatePropagationLoss(propagationModel, result, "pathloss between VLT and VLR"));

        rWtPower = result.getTxPower();
        rWtGain = result.txAntenna().getGain();
        rVrGain = result.rxAntenna().getGain();
        rPcmax = system.getReceiver().getPowerControlThreshold();
        rSens = system.getReceiver().getSensitivity();

        rPathLoss = result.getTxRxPathLoss();

        rdRSSValue = rWtPower + rWtGain + rVrGain - rPathLoss;

        if (LOG.isDebugEnabled()) {
            LOG.debug("dRSS Value = " + rdRSSValue + " [(wtPower = " + rWtPower + ") " +
                    "+ (wtGain = " + rWtGain + ") + (VrGain = " + rVrGain + ") - (pathloss = " + rPathLoss + ")]");
        }

        if (system.getReceiver().isUsingPowerControlThreshold() && rdRSSValue > rSens + rPcmax) {
            rdRSSValue = rSens + rPcmax;
            if (LOG.isDebugEnabled()) {
                LOG .debug("(getVictimLinkReceiver().getCheckPcMax()) && (rdRSSValue > (Sensitivity + Pcmax)) is true -> dRSS Value ["
                        + rdRSSValue + "] = Sensitivity [" + rSens + "] + Pcmax [" + rPcmax + "]");
            }

        }

        return rdRSSValue;
    }

    public static void handleInterferenceLink(Point2D victimSystemPosition, MutableEventResult current, InterferenceLink<GenericSystem> link, MutableLinkResult iLink) {
        GenericSystem system = link.getInterferingSystem();
        itTrial(iLink, system.getTransmitter(), system.getFrequency());
        irTrial(iLink, system.getReceiver());
        calculateRelativeTxRxLocation(iLink, system.getLink(), "ILT -> ILR");

        if (link.getInterferingLinkRelativePosition().useCoLocatedWith()) {
            InterferenceLink coLocatedWith = link.getInterferingLinkRelativePosition().getCoLocatedWith();
            List<ActiveInterferer> interferingElements = current.getInterferingElements();
            for (ActiveInterferer element : interferingElements) {
                if ( element.getInterferenceLink() == coLocatedWith ) {
                    CorrelationModeCalculator.itVrColocated(iLink, element.getLinkResult(), link);
                    break;
                }
            }
        } else {
            CorrelationModeCalculator.itVrLoc(iLink, victimSystemPosition, link);
        }

        calculatePathAntAziElev(iLink, system.getTransmitter(), system.getReceiver(), "Interfering System Link");
        pathAntGains(iLink, system.getReceiver(), system.getTransmitter());
        iLink.setTxRxPathLoss(LinkCalculator.calculatePropagationLoss(system.getLink().getPropagationModel(), iLink, "pathloss between ILT and ILR"));

        if (system.getTransmitter().isUsingPowerControl()) {
            LOG.debug("Using Power Control");
            powerControlGain(iLink, system.getTransmitter());
        }

    }

    /**
     * Calculates (setter) trial value of the Victim Link Transmitter (it used to be called WT - wanted transmitter).
     *<p>
     * It sets trial value for the antenna height and transmitter power at each event
     *
     * @param result mutable interference link result
     * @param transmitter Generic transmitter
     */

    private static void wtTrial(MutableLinkResult result, GenericTransmitter transmitter ) {
        double rResultAntHeight, rResultPower;

        rResultAntHeight = transmitter.getHeight().trial();
        rResultPower = transmitter.getPower().trial();

        if (LOG.isDebugEnabled()) {
            LOG.debug(format("WT power trial = %f", rResultPower));
            LOG.debug(format("WT Antenna height trial = %f", rResultAntHeight));
        }
        result.txAntenna().setHeight(rResultAntHeight);
        result.setTxPower(rResultPower);
    }

    /**
     * Calculates (setter) trial value of the Interfering Link Transmitter (EGE/3010)
     * <p>
     * it sets trial value for the antenna height, frequency and transmitter at each event
     */
    private static void itTrial(MutableLinkResult result, GenericTransmitter transmitter, Distribution frequency) {
        result.txAntenna().setHeight(transmitter.getHeight().trial());

        result.setTxPower(transmitter.getPower().trial());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering Transmitter supplied power = " + result.getTxPower());
        }

        result.setFrequency(frequency.trial());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering Transmitter frequency = " + result.getFrequency());
        }
    }

    /**
     * Calculates (setter) trial value of the Victim Link Receiver (EGE/2020)
     *<p>
     * It sets trial value for the antenna height, frequency and noise floor at each event
     *
     * @param result mutable interference link result
     * @param system Generic system
     */
    private static void vrTrial(MutableLinkResult result, GenericSystem system ) {
        result.rxAntenna().setHeight(system.getReceiver().getHeight().trial());
        result.setFrequency(system.getFrequency().trial());
        result.setRxNoiseFloor(system.getReceiver().getNoiseFloor().trial());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Trialed VR antenna height = " + result.rxAntenna().getHeight());
            LOG.debug("Trialed VR frequency = " + result.getFrequency());
            LOG.debug("Trialed VR noise floor = " + result.getRxNoiseFloor() );
        }
    }

    /**
     * Trials relevant parameters of the Interfering Link Receiver (it used to be called WR - wanted receiver). (EGE/3020)
     *<p>
     * Set the receiver height
     *
     * @param result mutable link result
     * @param receiver generic receiver
     */
    private static void irTrial(MutableLinkResult result, Receiver receiver) {
        result.rxAntenna().setHeight( receiver.getHeight().trial()) ;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Interfering System Receiver antenna height = " + result.rxAntenna().getHeight());
        }
    }

    /**
     * Generic calculation of the relative positioning of a Rx to a Tx.
     * <p>
     * Tx is assumed to be (0,0)
     * <p>
     *     raw/temporary position of Rx is calculated
     * <p>
     *     The Tx and Rx positions will change depending on the Interferer/Victim path definition
     *
     * @param result use the mutable link result to set values
     * @param link get values from the link
     * @param string text field for the log statement
     */
    private static void calculateRelativeTxRxLocation(MutableLinkResult result, GenericLink link, String string){
        double rRmax;
        double rTxRxDistance;
        Point2D rRx;
        double rTxRxAngle;
        double rTxRxDistTrial;
        double rTxRxAngleTrial;

        RelativeLocation rl = link.getRelativeLocation();
        if (rl.useCorrelatedDistance()) {	// Correlated case
            rRx = rl.getDeltaPosition();
            if (LOG.isDebugEnabled()) {
                LOG.debug(string + " path is correlated");
            }
        } else {	// No Correlated case
            rTxRxDistTrial = rl.getPathDistanceFactor().trial();
            rTxRxAngleTrial = rl.getPathAzimuth().trial();
            rRmax = result.getValue(GenericSystem.COVERAGE_RADIUS);

            if ( rl.usePolygon() ) {
                double turnTrial = rl.turnCCW().trial();
                rRmax = CorrelationModeCalculator.shapeTransformer( turnTrial, rRmax, rl.shape(), rTxRxAngleTrial );
            }
          //  rRx = new Point2D(cosD(rTxRxAngleTrial),sinD(rTxRxAngleTrial)).scale(rRmax * rTxRxDistTrial).add( rl.getDeltaPosition());
            
            rRx = new Point2D(-15.0,4.7).add(rl.getDeltaPosition());

            result.setValue(GenericSystem.PATH_DISTANCE_FACTOR, rTxRxDistTrial);
            result.setValue(GenericSystem.PATH_AZIMUTH, rTxRxAngleTrial);
            if (LOG.isDebugEnabled()) {
                LOG.debug(string + " path is NOT correlated (i.e. random)");
            }
        }

        rTxRxAngle = Mathematics.calculateKartesianAngle( rRx );
        rTxRxDistance = distance(rRx);
        result.setTxRxAngle(rTxRxAngle);
        result.rxAntenna().setPosition(rRx);
        result.setTxRxDistance(rTxRxDistance);

        if (LOG.isDebugEnabled()) {
            LOG.debug(string + " angle = "+ rTxRxAngle);
            LOG.debug(string + " distance = " + rTxRxDistance);
            LOG.debug("raw/temporary position of Tx and Rx - these positions will change depending on the Interferer/victim path definition");
            LOG.debug(string + " Rx - temporary position: " + result.rxAntenna().getPosition());
            LOG.debug(string + " Tx - temporary position X: " + 0);
            LOG.debug(string + " Tx - temporary position Y: " + 0);
        }
    }


    /**
     * Generic calculation of azimuth and elevation for a path for the dRSS link (VLT to VLR) and for the Interfering <br>
     *     System Link (ILT to ILR)
     *
     * @param result generic mutable link result
     * @param transmitter generic transmitter variables
     * @param receiver generic receiver variables
     * @param string field text for the log file
     */
    private static void calculatePathAntAziElev(MutableLinkResult result, GenericTransmitter transmitter, GenericReceiver receiver, String string) {
        double rWtAntHeight, rVrAntHeight;
        double txRxAziTrial, rxTxAziTrial;
        double txRxElevTrial, rxTxElevTrial;
        double rTxRxAziResult, rRxTxAziResult;
        double rTxRxElevResult, rRxTxElevResult;
        double rRxTxTilt, rTxRxTilt;
        double rTxRxElevCompensation = 0.0, rRxTxElevCompensation = 0.0;

        rWtAntHeight = result.txAntenna().getHeight();
        rVrAntHeight = result.rxAntenna().getHeight();

        txRxAziTrial = -transmitter.getAntennaPointing().getAzimuth().trial();
        txRxElevTrial = transmitter.getAntennaPointing().getElevation().trial();
        rxTxAziTrial = -receiver.getAntennaPointing().getAzimuth().trial();
        rxTxElevTrial = receiver.getAntennaPointing().getElevation().trial();

        // Rx -> Tx
        String stringText = string +" Rx ->Tx";
        if (receiver.getAntennaPointing().getAntennaPointingAzimuth()){//pointing at each other
            rRxTxAziResult = rxTxAziTrial;
        }else{
            rRxTxAziResult = Mathematics.calculateKartesianAngle(result.txAntenna().getPosition(), result.rxAntenna().getPosition()) + rxTxAziTrial;//not pointing at each other
        }

        rRxTxTilt = rxTxElevTrial;//not pointing at each other
        rRxTxElevResult = LinkCalculator.calculateElevationWithTilt(result.txAntenna().getPosition(), rWtAntHeight, result.rxAntenna().getPosition(), rVrAntHeight, rRxTxTilt, rxTxAziTrial,stringText);
        if(receiver.getAntennaPointing().getAntennaPointingElevation()){//pointing at each other
            rRxTxElevCompensation = rRxTxElevResult;
            rRxTxTilt = rxTxElevTrial;
            rRxTxElevResult = calculateElevationWithTilt(rRxTxTilt, rxTxAziTrial,stringText);
        }

        // Tx -> Rx
        stringText = string +" Tx -> Rx";
        if (transmitter.getAntennaPointing().getAntennaPointingAzimuth()){//pointing at each other
            rTxRxAziResult = txRxAziTrial;
        }else{
            rTxRxAziResult = Mathematics.calculateKartesianAngle(result.rxAntenna().getPosition(), result.txAntenna().getPosition()) + txRxAziTrial;//not pointing at each other
        }

        rTxRxTilt = txRxElevTrial;
        rTxRxElevResult = LinkCalculator.calculateElevationWithTilt(result.rxAntenna().getPosition(), rVrAntHeight, result.txAntenna().getPosition(), rWtAntHeight, rTxRxTilt, txRxAziTrial,stringText);//not pointing at each other
        if(transmitter.getAntennaPointing().getAntennaPointingElevation()){//pointing at each other
            rTxRxElevCompensation = rTxRxElevResult;
            rTxRxTilt = txRxElevTrial;
            rTxRxElevResult = calculateElevationWithTilt(rTxRxTilt, txRxAziTrial,stringText);
        }

        rRxTxAziResult = LinkCalculator.convertAngleToConfineToHorizontalDefinedRange(rRxTxAziResult);
        rRxTxElevResult = LinkCalculator.convertAngleToConfineToVerticalDefinedRange(rRxTxElevResult);
        rTxRxAziResult = LinkCalculator.convertAngleToConfineToHorizontalDefinedRange(rTxRxAziResult);
        rTxRxElevResult = LinkCalculator.convertAngleToConfineToVerticalDefinedRange(rTxRxElevResult);

        rRxTxElevCompensation = LinkCalculator.convertAngleToConfineToVerticalDefinedRange(rRxTxElevCompensation);
        rTxRxElevCompensation = LinkCalculator.convertAngleToConfineToVerticalDefinedRange(rTxRxElevCompensation);

        setDirectionResult(result.rxAntenna(), rRxTxAziResult, rRxTxElevResult, rRxTxTilt, rRxTxElevCompensation);
        setDirectionResult(result.txAntenna(), rTxRxAziResult, rTxRxElevResult, rTxRxTilt, rTxRxElevCompensation);
    }

    /**
     * Calculate the resulting elevation angle including the tilt of the antenna and any correction due to the Azimuth <br>
     *     setting of the antenna
     * <p>
     * <b>Condition:</b> Antennas are pointing at each other i.e. there is no elevation angle between each other
     *
     * @param tilt antenna tilt as input elevation parameters
     * @param azimuth azimuth shift as input azimuth parameters
     * @param string text field for the log statement
     * @return angle in degree
     */
    private static double calculateElevationWithTilt(double tilt, double azimuth, String string) {
        double elevation;
        double tiltWithAzimuthCorrection;
        double elevationCorrected;

        elevation = 0; //Antennas are pointing at each other i.e. there is no elevation angle between each other
        tiltWithAzimuthCorrection = LinkCalculator.calculateElevationWithCorrectionFactorFromAzimuth(tilt, azimuth);

        elevationCorrected = elevation - tiltWithAzimuthCorrection;
        if (LOG.isDebugEnabled()) {
            LOG.debug(string + " Elevation Result = " + elevationCorrected + " = elevation (" + elevation + ") - tiltCorrection (" + tiltWithAzimuthCorrection+")");
        }
        return elevationCorrected;
    }


    private static void setDirectionResult(MutableAntennaResult res, double azimuth, double elevation, double tilt, double elevationComp) {
        res.setAzimuth(azimuth);
        res.setElevation(elevation);
        res.setTilt(tilt);
        res.setElevationCompensation(elevationComp);
    }


    /**
     * Calculates (setter) the antenna gains for the link for both the transmitter and the receiver
     *
     * @param result mutable link result
     * @param receiver any receiver type (generic or cellular)
     * @param transmitter any transmitter type (generic or cellular)
     */
    static void pathAntGains(MutableLinkResult result, Receiver receiver, Transmitter transmitter) {
        result.rxAntenna().setGain(receiver.getAntennaGain().evaluate(result, result.rxAntenna()));
        result.txAntenna().setGain(transmitter.getAntennaGain().evaluate(result, result.txAntenna()));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Link Rx->Tx Azimuth = " + result.rxAntenna().getAzimuth());
            LOG.debug("Link Rx->Tx Elevation = " + result.rxAntenna().getElevation());
            LOG.debug("Link Tx->Rx Azimuth = " + result.txAntenna().getAzimuth());
            LOG.debug("Link Tx->Rx Elevation = " + result.txAntenna().getElevation());
            LOG.debug("Link Receiver peak gain = " + receiver.getAntennaGain().peakGain());
            LOG.debug("Link Receiver Antenna Gain = " + result.rxAntenna().getGain());
            LOG.debug("Link Transmitter peak gain = " + transmitter.getAntennaGain().peakGain());
            LOG.debug("Link Transmitter Antenna Gain = " + result.txAntenna().getGain());
        }
    }


    /**
     * Calculates the power control gain of the interfering transmitter, for both unwanted and blocking interference <br>
     *     types. (EGE/3080)
     *
     * @param result mutable link result
     * @param transmitter generic transmitter variables
     */
    private static void powerControlGain(MutableLinkResult result, GenericTransmitter transmitter) {
        double rPinit, rPmin, rRange, rStep;
        double rPowerSupplied, rGainItWr, rGainWrIt, rPathLossItWr;
        double rResultPowerGain;

        rPowerSupplied = result.getTxPower();
        rGainItWr = result.txAntenna().getGain();
        rGainWrIt = result.rxAntenna().getGain();
        rPathLossItWr = result.getTxRxPathLoss();

        rStep = transmitter.getPowerControlStepSize();
        rPmin = transmitter.getPowerControlMinThreshold();
        rRange = transmitter.getPowerControlDynamicRange();

        rPinit = rPowerSupplied + rGainItWr - rPathLossItWr + rGainWrIt;

        if (rPinit > rPmin && rPinit < rPmin + rRange) {
            rResultPowerGain = -rStep * Math.floor((rPinit - rPmin) / rStep);
        } else if (rPinit <= rPmin) {
            rResultPowerGain = 0;
        } else {
            rResultPowerGain = -rRange;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("IT Power control initial Wr received power = " + rPinit);
            LOG.debug("IT Power control calculated gain = " + rResultPowerGain);
        }

        result.setValue(GenericSystem.TX_POWER_CONTROL_GAIN, rResultPowerGain);
    }
}
