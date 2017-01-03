package org.seamcat.simulation.cellular;

import org.seamcat.dmasystems.LinkCalculator;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.marshalling.FunctionMarshaller;
import org.seamcat.model.AntennaPatternConverter;
import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericReceiver;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.plugin.Horizontal;
import org.seamcat.model.plugin.OptionalFunction;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Receiver;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.scenario.CellularSystemImpl;
import org.seamcat.simulation.result.MutableInterferenceLinkResult;
import org.seamcat.simulation.result.MutableLinkResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.seamcat.mathematics.Constants.SQRT3;
import static org.seamcat.model.cellular.CellularLayout.SystemLayout.*;
import static org.seamcat.model.mathematics.Mathematics.calculateKartesianAngle;
import static org.seamcat.model.mathematics.Mathematics.distance;

public class CellularCalculations {

    /**
     * Find the least distance form MobileStation to BaseStation in the wrap around cluster.
     * <p>
     * For more information see STG(03)13r3 document from QUALCOMM.
     *
     * @param mobile coordinate of the MobileStation
     * @param base coordinate of the BaseStation
     * @param intercellDistance
     * @param layout
     * @param usingWrapAround
     * @param sector
     * @return the coordinate of the new BS
     */
    public static Point2D findNewCoordinate(
            Point2D mobile, Point2D base, double intercellDistance,
            CellularLayout.SystemLayout layout, boolean usingWrapAround, CellularLayout.SectorSetup sector) {

        Point2D baseStationNewCoordinate = new Point2D(base);
        // Wrap-Around EQ A (standard lineary distance):
        // Network-Edge EQ A:
        double currentMinimum = distance(mobile, base);

        double temp;
        Point2D tempP;
        if ( usingWrapAround ) {
            // Wrap-Around EQ B:
            // Network-Edge EQ B:
            if (layout == CenterOfInfiniteNetwork || layout == RightHandSideOfNetworkEdge) {
                if(sector != CellularLayout.SectorSetup.TriSector3GPP){
                    tempP = new Point2D( base.getX() + 3 * intercellDistance / SQRT3, base.getY() + 4 * intercellDistance );
                }else{
                    tempP = new Point2D( base.getX() + 0.5 * intercellDistance , base.getY() + 5 * SQRT3 * intercellDistance / 2);
                }
                temp = distance( mobile, tempP );
                if (temp < currentMinimum) {
                    currentMinimum = temp;
                    baseStationNewCoordinate = tempP;
                }
            }

            // Wrap-Around EQ C:
            // Not include in network edge case
            if (layout == CenterOfInfiniteNetwork || layout == LeftHandSideOfNetworkEdge) {
                if(sector != CellularLayout.SectorSetup.TriSector3GPP){
                    tempP = new Point2D( base.getX() - 3 * intercellDistance / SQRT3, base.getY() - 4 * intercellDistance );
                }else{
                    tempP = new Point2D( base.getX() - 0.5 * intercellDistance , base.getY() - 5 * SQRT3 * intercellDistance / 2);
                }
                temp = distance( mobile, tempP );
                if (temp < currentMinimum) {
                    currentMinimum = temp;
                    baseStationNewCoordinate = tempP;
                }
            }

            // Wrap-Around EQ D:
            // Network-Edge EQ C:
            if (layout == CenterOfInfiniteNetwork || layout == LeftHandSideOfNetworkEdge) {
                if(sector != CellularLayout.SectorSetup.TriSector3GPP){
                    tempP = new Point2D( base.getX() - 4.5 * intercellDistance / SQRT3, base.getY() + 7 * intercellDistance / 2 );
                }else{
                    tempP = new Point2D( base.getX() - 4 * intercellDistance , base.getY() - SQRT3 * intercellDistance  );
                }
                temp = distance( mobile, tempP );
                if (temp < currentMinimum) {
                    currentMinimum = temp;
                    baseStationNewCoordinate = tempP;
                }
            }

            // Wrap-Around EQ E:
            // Not included in Network-Edge
            if (layout == CenterOfInfiniteNetwork || layout == RightHandSideOfNetworkEdge) {
                if(sector != CellularLayout.SectorSetup.TriSector3GPP) {
                    tempP = new Point2D( base.getX() + 4.5 * intercellDistance / SQRT3, base.getY() - 7 * intercellDistance / 2 );
                }else{
                    tempP = new Point2D( base.getX() + 4 * intercellDistance , base.getY() + SQRT3 * intercellDistance  );
                }
                temp = distance( mobile, tempP );
                if (temp < currentMinimum) {
                    currentMinimum = temp;
                    baseStationNewCoordinate = tempP;
                }
            }

            // Wrap-Around EQ F:
            // Network-Edge EQ D:
            if (layout == CenterOfInfiniteNetwork || layout == LeftHandSideOfNetworkEdge) {
                if(sector != CellularLayout.SectorSetup.TriSector3GPP){
                    tempP = new Point2D( base.getX() - 7.5 * intercellDistance / SQRT3, base.getY() - intercellDistance / 2 );
                }else{
                    tempP = new Point2D( base.getX() - 3.5 * intercellDistance , base.getY() + 3 * SQRT3 * intercellDistance / 2 );
                }
                temp = distance( mobile, tempP );
                if (temp < currentMinimum) {
                    currentMinimum = temp;
                    baseStationNewCoordinate = tempP;
                }
            }
            // Wrap-Around EQ G:
            // Not included in Network-Edge
            if (layout == CenterOfInfiniteNetwork || layout == RightHandSideOfNetworkEdge) {
                if(sector != CellularLayout.SectorSetup.TriSector3GPP){
                    tempP = new Point2D( base.getX() + 7.5 * intercellDistance / SQRT3, base.getY() + intercellDistance / 2 );
                }else{
                    tempP = new Point2D( base.getX() + 3.5 * intercellDistance , base.getY() - 3 * SQRT3 * intercellDistance / 2 );
                }
                temp = distance( mobile, tempP );
                if (temp < currentMinimum) {
                    currentMinimum = temp;
                    baseStationNewCoordinate = tempP;
                }
            }
        } else {
            // If no wrap-around structure do nothing
        }

        return baseStationNewCoordinate;
    }


    /**
     * Calculates the Thermal Noise figure in the receiver bandwidth. The Thermal Noise (N) is given as <br>
     *     <code>N = kTBF [W]</code> where <br>
     * <br>
     * k - Boltzmann's constant, k = 1.38x10<sup>-23</sup> Joules/K <br>
     * T - temparature, T = 290&#176;K <br>
     * B - receiver (channel)( bandwidth, Hz; <br>
     * F - receiver noise figure <br>
     * <br>
     * Scaled to dBm units (1 W = 30 dBm), N may then be calculated as: <br>
     * <br>
     *     <code>N [dBm] = 10*log(k*T*B*F) + 30 = -173,977 + 10*log(B[Hz]) + F[dB]</code>
     */
    public static double calculateThermalNoise(double bandwidth, double receiverNoiseFigure) {
        return -173.977 + Mathematics.linear2dB(bandwidth * 1000000) + receiverNoiseFigure;
    }

    /**
     * Set the elevation angle between the receiver and the transmitter
     * <p>
     * Use in the cellular calculation
     *
     * @param link Mutable interference link result
     * @param receiver any receiver system
     * @param point position of the user
     * @param antennaHeight antenna height of the user
     */
    public static void setRxTxAngleElevation(MutableInterferenceLinkResult link, Receiver receiver, Point2D point, double antennaHeight) {
        LinkResult result = link.getVictimSystemLink();
        Point2D vr = result.rxAntenna().getPosition();
        double horizontalAngle = calculateKartesianAngle(vr, point);
        horizontalAngle = -result.getTxRxAngle() + result.rxAntenna().getAzimuth() + horizontalAngle;
        horizontalAngle = LinkCalculator.convertAngleToConfineToHorizontalDefinedRange(horizontalAngle);

        double rVrAntTilt;
        if( link.getInterferenceLink().getVictimSystem() instanceof GenericSystem && ((GenericReceiver)receiver).getAntennaPointing().getAntennaPointingElevation()){
            rVrAntTilt = result.rxAntenna().getElevationCompensation();
            if ( Double.isNaN(rVrAntTilt)) rVrAntTilt = 0.0;
        }else{
            rVrAntTilt = result.rxAntenna().getTilt();
        }

        double elevation = LinkCalculator.calculateElevationWithTilt(vr, result.rxAntenna().getHeight(), point, antennaHeight, rVrAntTilt, result.rxAntenna().getAzimuth(), "Interferer is traditional");
        link.rxAntenna().setAzimuth(horizontalAngle);
        link.rxAntenna().setElevation(elevation);
    }

    /**
     * Set the horizontal and elevation angle between the transmitter and the receiver
     * <p>
     * Used in the cellular calculation
     *
     * @param result mutable link result
     * @param translateAngles convert the horizontal angle by 180 deg
     * @param antennaHeight antenna height
     */
    public static void setTxRxAngleElevation(MutableLinkResult result, boolean translateAngles, double antennaHeight ) {
        double horiAngle = Mathematics.calculateKartesianAngle( result.txAntenna().getPosition(), result.rxAntenna().getPosition());

        if (translateAngles) {
            horiAngle -= 180;
        }

        double vertiAngle = Mathematics.calculateElevation( result.txAntenna().getPosition(), antennaHeight, result.rxAntenna().getPosition(), result.txAntenna().getHeight());
        double elevation = vertiAngle - result.txAntenna().getTilt();

        result.txAntenna().setAzimuth(horiAngle);
        result.txAntenna().setElevation(elevation);
    }



    public static AntennaGainConfiguration[] createSectorAntennas(CellularSystemImpl system) {
        boolean triSectorCells = system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.SingleSector;

        final Map<String, Object> map  = new HashMap<String, Object>();
        AntennaGainConfiguration gain = system.getLink().getBaseStation().getAntennaGain();
        ProxyHelper.parameterCallback(gain.getModelClass(), gain.getModel(), Horizontal.class,
                new ProxyHelper.ParameterCallback() {
                    public void handle(String name, Object parameter) {
                        if ( parameter instanceof Function) {
                            map.put(name, parameter);
                        } else if (parameter instanceof OptionalFunction) {
                            map.put(name, parameter);
                        }
                    }
                });

        Map<String, Object> copy1 = copy(map);

        AntennaGainConfiguration[] antennas;
        // generate the sector antennas
        if ( triSectorCells ) {
            antennas = new AntennaGainConfiguration[3];
            translate(1, copy1, system);
            antennas[0] = copy(gain, copy1 );
            Map<String, Object> copy2 = copy(map);
            translate(2, copy2, system);
            antennas[1] = copy(gain, copy2);
            Map<String, Object> copy3 = copy(map);
            translate(3, copy3, system);
            antennas[2] = copy(gain, copy3);
        } else {
            antennas = new AntennaGainConfiguration[1];
            translate(1, copy1, system);
            antennas[0] = copy(gain, copy1);
        }

        return antennas;
    }

    private static Map<String, Object> copy(Map<String, Object> orig) {
        Map<String,Object> copy = new HashMap<String, Object>();
        for (Map.Entry<String, Object> entry : orig.entrySet()) {
            Object value = entry.getValue();
            DiscreteFunction function;
            if ( value instanceof Function ) {
                copy.put(entry.getKey(), FunctionMarshaller.copy((Function) value));
                function = (DiscreteFunction) copy.get(entry.getKey());
            } else if ( value instanceof OptionalFunction ) {
                DiscreteFunction cp = FunctionMarshaller.copy(((OptionalFunction) value).getFunction());
                copy.put(entry.getKey(), new OptionalFunction(((OptionalFunction) value).isRelevant(), cp));
                function = (DiscreteFunction) ((OptionalFunction)copy.get(entry.getKey())).getFunction();
            } else {
                throw new IllegalArgumentException("No function argument found");
            }
            AntennaPatternConverter.convertFrom0360To180180( function );
        }
        return copy;
    }

    private static AntennaGainConfiguration copy(AntennaGainConfiguration orig, Map<String,Object> sectorHorizontals) {
        Object model = ProxyHelper.copy(orig.getModelClass(), orig.getModel());
        for (Map.Entry<String, Object> entry : sectorHorizontals.entrySet()) {
            model = ProxyHelper.copy(orig.getModelClass(), model, entry.getKey(), entry.getValue());
        }
        return SeamcatFactory.antennaGain().getByClass(orig.getPluginClass(), model, orig.peakGain());
    }

    private static void translate(int sectorId, Map<String, Object> map, CellularSystem system) {
        for (Object entry : map.values()) {
            if ( entry instanceof DiscreteFunction ) {
                translateAntennaPatternToCurrentSector(sectorId, ((DiscreteFunction) entry).points(), system);
            } else if (entry instanceof OptionalFunction) {
                translateAntennaPatternToCurrentSector(sectorId, ((DiscreteFunction)((OptionalFunction) entry).getFunction()).points(), system);
            }
        }
    }

    private static void translateAntennaPatternToCurrentSector(int sectorId, List<Point2D> points, CellularSystem system) {
        double sectorOffset = 0;
        if(system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP){
            if (sectorId == 1) {
                sectorOffset = 60;
            } else if (sectorId == 2) {
                sectorOffset = 180;
            } else if (sectorId == 3) {
                sectorOffset = 300;
            } else {
                throw new IllegalStateException("Unknown sector id");
            }
        }else{
            if (sectorId == 1) {
                sectorOffset = 0;
            } else if (sectorId == 2) {
                sectorOffset = 120;
            } else if (sectorId == 3) {
                sectorOffset = 240;
            } else {
                throw new IllegalStateException("Unknown sector id");
            }
        }

        translateAntenna(sectorOffset, points);
    }

    private static void translateAntenna(double sectorOffset, List<Point2D> points ) {
        for (int i1 = 0; i1 < points.size(); i1++) {
            Point2D p = points.get(i1);
            if ( p.getX() + sectorOffset > 180 ) {
                points.set( i1, new Point2D((p.getX() + sectorOffset) - 360, p.getY() ));
            } else {
                points.set( i1, new Point2D(p.getX() + sectorOffset, p.getY() ));
            }
        }
        Collections.sort(points);
        Point2D first = points.get(0);
        Point2D last = points.get(points.size() - 1);
        if (last.getX() != 180) {
            if (first.getX() == -180) {
                points.add(new Point2D(180, first.getY()));
            } else {
                points.add(new Point2D(180, last.getY()));
                points.add(new Point2D(-180, first.getY()));
            }
        }
        Collections.sort(points);
        first = points.get(0);
        if (first.getX() != -180) {
            points.add(new Point2D(-180, last.getY()));
        }
        Collections.sort(points);

        // Remove identical points
        Point2D p = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            Point2D p1 = points.get(i);
            if (p.getX() == p1.getX()) {
                points.remove(i);
                i--;
            }
        }
    }

    /**
     * compute the inter cell distance
     *<p></p>
     * <ol>
     *     <ul>3 sector: interCellDistance = CellRadius() * SQRT3</ul>
     * <ul>omni: interCellDistance = CellRadius() * 3</ul>
     * </ol>
     */
    public static double getInterCellDistance( CellularSystem system) {
        double interCellDistance = 0.0;

        if (system.getLayout().getSectorSetup() != CellularLayout.SectorSetup.TriSector3GPP ){
            interCellDistance = system.getLayout().getCellRadius() * SQRT3;
        }else{
            interCellDistance = system.getLayout().getCellRadius() * 3;
        }
        return interCellDistance;
    }
}
