package org.seamcat.model.correlation;

import org.apache.log4j.Logger;
import org.seamcat.dmasystems.LinkCalculator;
import org.seamcat.mathematics.Constants;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.distributions.RayleighDistributionImpl;
import org.seamcat.model.distributions.UniformPolarDistanceDistributionImpl;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.GenericTransmitter;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.generic.RelativeLocation;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.InterferenceLink;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;

import static org.seamcat.model.generic.InterferingLinkRelativePosition.CorrelationMode.*;
import static org.seamcat.model.mathematics.Mathematics.cosD;
import static org.seamcat.model.mathematics.Mathematics.sinD;

public class CorrelationModeCalculator {

    private static final Logger LOG = Logger.getLogger(CorrelationModeCalculator.class);

    /**
     * Method that calculates the antenna azimuths and elevations relative to the interfering link transmitter-victim <br>
     *     link receiver path. (EGE/3090)
     *
     * @param result
     */
    public static void itVrPathAntAziElev(MutableInterferenceLinkResult result, RadioSystem vs, GenericSystem is ) {
        double rILangle, rILAzi, rVLangle, rVLAzi, rItVrangle, rVrAntHeight, rItAntHeight, rVrAntTilt, rItAntTilt;
        double rResultVrAzi, rResultItAzi, rResultVrElev, rResultItElev;

        LinkResult victim = result.getVictimSystemLink();
        LinkResult interferer = result.getInterferingSystemLink();

        GenericTransmitter it = is.getTransmitter();
        rVrAntTilt = victim.rxAntenna().getTilt();
        if ( vs instanceof GenericSystem ) {
            GenericSystem gvs = (GenericSystem) vs;
            if(gvs.getReceiver().getAntennaPointing().getAntennaPointingElevation()){
                rVrAntTilt = victim.rxAntenna().getElevationCompensation();
                if ( Double.isNaN(rVrAntTilt)) rVrAntTilt = 0.0;
            }
        }

        rVrAntHeight = victim.rxAntenna().getHeight();
        rItAntHeight = interferer.txAntenna().getHeight();

        if(it.getAntennaPointing().getAntennaPointingElevation()){
            rItAntTilt = interferer.txAntenna().getElevationCompensation();
            if ( Double.isNaN(rItAntTilt)) rItAntTilt = 0.0;
        }else{
            rItAntTilt = interferer.txAntenna().getTilt();
        }

        rILangle = interferer.getTxRxAngle();
        rILAzi = interferer.txAntenna().getAzimuth();
        rVLangle = victim.getTxRxAngle();
        rVLAzi = victim.rxAntenna().getAzimuth();
        rItVrangle = result.getTxRxAngle();

        // Azimuth VLR -> ILT
        rResultVrAzi = LinkCalculator.calculateItVictimAzimuth(rVLangle, rVLAzi, rItVrangle, "VLR -> ILT");
        rResultVrAzi = LinkCalculator.convertAngleToConfineToHorizontalDefinedRange(rResultVrAzi);

        // Azimuth ILT -> VLR
        rResultItAzi = LinkCalculator.calculateItVictimAzimuth(rILangle, rILAzi, rItVrangle, "ILT -> VLR");
        rResultItAzi = LinkCalculator.convertAngleToConfineToHorizontalDefinedRange(rResultItAzi);

        // Elevations VLR -> ILT
        rResultVrElev = LinkCalculator.calculateElevationWithTilt( interferer.txAntenna().getPosition(), rItAntHeight, victim.rxAntenna().getPosition(), rVrAntHeight, rVrAntTilt, rResultVrAzi,"VLR ->ILT");
        rResultVrElev = LinkCalculator.convertAngleToConfineToVerticalDefinedRange(rResultVrElev);

        // Elevations ILT -> VLR
        rResultItElev = LinkCalculator.calculateElevationWithTilt(victim.rxAntenna().getPosition(), rVrAntHeight, interferer.txAntenna().getPosition(), rItAntHeight, rItAntTilt, rResultItAzi,"ILT -> VLR");
        rResultItElev = LinkCalculator.convertAngleToConfineToVerticalDefinedRange(rResultItElev);

        result.txAntenna().setAzimuth(rResultItAzi);
        result.txAntenna().setElevation(rResultItElev);
        result.rxAntenna().setAzimuth(rResultVrAzi);
        result.rxAntenna().setElevation(rResultVrElev);

        result.txAntenna().setElevationCompensation( interferer.txAntenna().getElevationCompensation() );
        result.rxAntenna().setElevationCompensation(victim.rxAntenna().getElevationCompensation());
    }


    /**
     *	itVrColocated
     * <p></p>
     * Used when collocated feature is selected.
     * <p></p>
     * Places:
     * <ol>
     * <li> the interferer link transmitter ILT on top of each other</li>
     * <li> the interferer link receiver ILR on top of each other</li>
     * </ol>
     * and recalculate the angle and Distance betwen ILT and ILR
     * <p></p>
     * One option to shift by Delta X and Delta Y from the reference IT.
     *
     */
    public static void itVrColocated(MutableLinkResult linkResult, LinkResult originResult, InterferenceLink link) {
        Point2D it = originResult.txAntenna().getPosition().add(link.getInterferingLinkRelativePosition().getCoLocationDeltaPosition());

        linkResult.txAntenna().setPosition(it);
        linkResult.rxAntenna().setPosition(linkResult.rxAntenna().getPosition().add(it));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Colocating IT");
            LOG.debug("IR - position: "+linkResult.rxAntenna().getPosition());
            LOG.debug("It - position: " + linkResult.txAntenna().getPosition());
        }
    }


    /**
     * itVrLoc
     * <p></p>
     * Calculates the absolute position of the interfering receiver and interfering transmitter. The victim system
     * position is used along with the correlation modes and positioning settings. <br>
     *     <p></p>
     *  <ol>
     *  <li> No correlation:  itVrLocNonCorrelated</li>
     *  <li> Closest interferer case: itVrLocClosest</li>
     *  <li> Uniform distribution: itVrLocUniform</li>
     *  <li> Correlated: itVrLocCorrelated</li>
     *  </ol>
     */
    public static void itVrLoc(MutableLinkResult result, Point2D victimSystemPosition, InterferenceLink link) {
        InterferingLinkRelativePosition.CorrelationMode mode = link.getInterferingLinkRelativePosition().getCorrelationMode();
        if ((mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR) ||
                (mode == VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR)||
                (mode == VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT)||
                (mode == VICTIM_DMA_INTERFERER_DMA_DYN) ||
                (mode == VICTIM_DMA_INTERFERER_CLASSICAL_NONE) ||
                (mode == VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT) ||
                (mode == VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR) ||
                (mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT)) {

            Point2D none;
            RelativeLocation rl = link.getInterferingLinkRelativePosition().getRelativeLocation();
            if ( rl.usePolygon() && (mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR ||
                    mode == VICTIM_DMA_INTERFERER_CLASSICAL_NONE ||
                    mode == VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT ||
                    mode == VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR ||
                    mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT)) {

                double rItVrDistTrial = rl.getPathDistanceFactor().trial();
                double rItVrAngleTrial = rl.getPathAzimuth().trial();

                double turnTrial = rl.turnCCW().trial();
                double rRsimu = link.getCalculatedSimulationRadius();
                rRsimu = shapeTransformer(turnTrial, rRsimu, rl.shape(), rItVrAngleTrial );

                none = new Point2D(cosD(rItVrAngleTrial), sinD(rItVrAngleTrial)).scale(rRsimu * rItVrDistTrial).add(rl.getDeltaPosition());
            } else {
                none = itVrLocNonCorrelated( link.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor(), link );
            }

            if( mode == VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR){
                none = none.subtract(result.rxAntenna().getPosition().subtract(result.txAntenna().getPosition()));
            }

            if ( mode != VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT
                    && mode != VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT) {
                // Origin of the victim is VR position
                none = victimSystemPosition.add(none);
            }

            result.txAntenna().setPosition(none);
            result.rxAntenna().setPosition( result.rxAntenna().getPosition().add(none));
        } else if ((mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR) ||
                (mode == VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST) ||
                (mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT)) {
            Point2D closest = itVrLocClosest(link);

            if (mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR){
                // Origin is VR position
                closest = victimSystemPosition.add(closest);
            }

            result.txAntenna().setPosition(closest);
            result.rxAntenna().setPosition(result.rxAntenna().getPosition().add(closest));

        } else if ((mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR) ||
                (mode == VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM) ||
                (mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT))  {
            Point2D uniform = itVrLocNonCorrelated(new UniformPolarDistanceDistributionImpl(1), link);

            if (mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR){
                // Origin is VR position
                uniform = victimSystemPosition.add(uniform);
            }

            result.txAntenna().setPosition(uniform);
            result.rxAntenna().setPosition(result.rxAntenna().getPosition().add(uniform));
        } else {
            itVrLocCorrelated(result, victimSystemPosition, link);
        }


        if ( link.getInterferingLinkRelativePosition().isWrCenterOfItDistribution() ) {


            RelativeLocation location = link.getInterferingLinkRelativePosition().getRelativeLocation();
            Point2D it = result.txAntenna().getPosition();

            if ( mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR ||
                    mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR ||
                    mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR ) {

                Point2D ir = victimSystemPosition.add(location.getDeltaPosition());

                result.rxAntenna().setPosition(ir);
                result.setTxRxAngle(Mathematics.calculateKartesianAngle(it, ir));
                result.setTxRxDistance(Mathematics.distance(it, ir));

            }

            if ( mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT ||
                    mode == VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST ||
                    mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT ||
                    mode == VICTIM_DMA_INTERFERER_CLASSICAL_NONE ||
                    mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT ||
                    mode == VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM) {

                Point2D ir = location.getDeltaPosition();

                result.rxAntenna().setPosition(ir);
                result.setTxRxAngle(Mathematics.calculateKartesianAngle(it, ir));
                result.setTxRxDistance(Mathematics.distance(it, ir));
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("IR - position: "+ result.rxAntenna().getPosition());
            LOG.debug("IT - position: "+ result.txAntenna().getPosition());
        }
    }


    /**
     *	itVrLocNonCorrelated
     *<p></p>
     * Calculates the absolute location of the interfering receiver and interfering transmitter when they are not correlated <br>
     *     with each other (i.e. using a specific distribution that the user can select)
     */
    private static Point2D itVrLocNonCorrelated(Distribution distanceFactor, InterferenceLink link ) {
        RelativeLocation location = link.getInterferingLinkRelativePosition().getRelativeLocation();
        double protectionDistance = link.getInterferingLinkRelativePosition().getProtectionDistance().trial();

        double rItVrDistTrial = distanceFactor.trial();
        double rItVrAngleTrial = location.getPathAzimuth().trial();

        double rRsimu = link.getCalculatedSimulationRadius();

        double rResultItVrDistance = Math.sqrt((Math.pow(rRsimu, 2) - Math.pow(protectionDistance, 2))
                * Math.pow(rItVrDistTrial, 2) + Math.pow(protectionDistance, 2));

        double value = rItVrAngleTrial * Math.PI / Constants.PID;
        return new Point2D( rResultItVrDistance * Math.cos(value), rResultItVrDistance * Math.sin(value)).add(location.getDeltaPosition());
    }

    /**
     *	itVrLocClosest
     *<p></p>
     * Calculates the absolute location of the interfering receiver and interfering transmitter using the closest location <br>
     *     mode (i.e. effectively using a Rayleigh distribution)
     */
    private static Point2D itVrLocClosest(InterferenceLink link) {
        RelativeLocation location = link.getInterferingLinkRelativePosition().getRelativeLocation();

        // In this correlation mode the interferer must be generic
        GenericSystem sys = (GenericSystem) link.getInterferingSystem();

        double dens = LinkCalculator.itDensityActive(sys.getInterfererDensity());
        double sDistanceStdDev = 1 / Math.sqrt(2 * Math.PI * dens);

        Distribution itVrDistance = new RayleighDistributionImpl(0, sDistanceStdDev);

        double protectionDistance = link.getInterferingLinkRelativePosition().getProtectionDistance().trial();
        double rItVictimDistTrial = 0;
        for (int i=0; i < 10; i++) {
            rItVictimDistTrial = itVrDistance.trial();
            if ( rItVictimDistTrial >= protectionDistance ) break;
            rItVictimDistTrial = protectionDistance;
        }
        double rItVrAngleTrial = location.getPathAzimuth().trial();

        Point2D rResultPos = new Point2D(rItVictimDistTrial * Math.cos(rItVrAngleTrial * Math.PI / Constants.PID),
                rItVictimDistTrial * Math.sin(rItVrAngleTrial * Math.PI / Constants.PID));
        return rResultPos.add(location.getDeltaPosition());
    }


    /**
     *	itVrLocCorrelated
     *<br>
     * Calculates the absolute location of the interfering receiver and interfering transmitter when they are correlated <br>
     *     with each other (EGE/3042)
     *
     */
    private static void itVrLocCorrelated(MutableLinkResult result, Point2D victimSystemPosition, InterferenceLink link ) {
        InterferingLinkRelativePosition.CorrelationMode sCorrelationMode;
        Point2D rDeltaItIr, delta;
        Point2D rIt = new Point2D(0,0), rIr = new Point2D(0,0);

        RelativeLocation location = link.getInterferingLinkRelativePosition().getRelativeLocation();
        rDeltaItIr = result.rxAntenna().getPosition().subtract( result.txAntenna().getPosition() );
        delta = location.getDeltaPosition();
        sCorrelationMode = link.getInterferingLinkRelativePosition().getCorrelationMode();

        if ((sCorrelationMode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT) ||
                (sCorrelationMode == VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT) ||
                (sCorrelationMode == VICTIM_DMA_INTERFERER_DMA_COR) ||
                (sCorrelationMode == VICTIM_DMA_INTERFERER_CLASSICAL_COR_IT)) {

            rIt = delta;
            rIr = rIt.add( result.rxAntenna().getPosition() );
        } else if ((sCorrelationMode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR) ||
                (sCorrelationMode == VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR)) {

            rIt = victimSystemPosition.add(delta);
            rIr = rIt.add(result.rxAntenna().getPosition());
        } else if ((sCorrelationMode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT) ||
                (sCorrelationMode == VICTIM_DMA_INTERFERER_CLASSICAL_COR_WR)) {

            rIt = delta.subtract( rDeltaItIr );
            rIr = delta;
        } else if (sCorrelationMode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR) {

            rIr = victimSystemPosition.add( delta );
            rIt = rIr.subtract( rDeltaItIr );
        }
        result.txAntenna().setPosition(rIt);
        result.rxAntenna().setPosition(rIr);
    }


    /**
     * Generic calculation of the radius when the shape of the distribution of the Rx to the Tx is change from its <br>
     *     original circle shape.
     *
     *
     * @param deltaPhi rotation of the shape
     * @param rRmax radius (either coverage or simulation radius) based on a circle shape
     * @param shape shape of the distribution
     * @param trialAngle Tx to Rx angle
     * @return the coverage or simulation radius after the shape transformation
     */
    public static double shapeTransformer(double deltaPhi, double rRmax, RelativeLocation.Shape shape, double trialAngle) {
        int n = edgeCount(shape);
        if (n > 2) {// at least a triangle, otherwise a circle is generated
            double sector = 360 / n; // part of 360 degree of a single triangle
            double rPhi = trialAngle - deltaPhi; // virtually turns the shape by deltaPhi
            int skip = (int) (rPhi / sector); // moves to that triangle the angle TX - Rx points to
            double rAngle = Math.abs(skip * sector - rPhi); // takes only that part of the angle within the triangle
            rAngle = sector / 2 - rAngle; // builds a rectangular triangle together with internalRadius
            double internalRadius = rRmax * Math.sin(Math.PI / n) / Math.tan(Math.PI / n); // rRmax is taken as circumcircle of the polygon
            rRmax = internalRadius / cosD(rAngle); // max distance within the polygon at the given angle
        }

        return rRmax;
    }

    private static int edgeCount(RelativeLocation.Shape shape) {
        switch (shape) {
            case Hexagon:
                return 6;
            case Heptagon:
                return 7;
            case Octagon:
                return 8;
            case Pentagon:
                return 5;
            case Square:
                return 4;
            case Triangle:
                return 3;
        }

        throw new RuntimeException("Unmapped shape: " + shape);
    }

}
