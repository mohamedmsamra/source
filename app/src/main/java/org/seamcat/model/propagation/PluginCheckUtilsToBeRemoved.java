package org.seamcat.model.propagation;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.Scenario;
import org.seamcat.model.cellular.CellularSystem;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.InterfererDensity;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.types.InterferenceLink;

import java.util.List;

/**
 * Created by Karl Koch (adhoc@heiseka.de) on 10.02.2016.
 * Project: seamcat
 * Package: org.seamcat.model.checkutils
 */
public class PluginCheckUtilsToBeRemoved {

    private static String relativePosition = "", deltaPosition = "";
    private static double maxDistance, minDistance;
    private static Distribution pathDistanceFactor = null;
    private static Point2D offset;
    private static Distribution antennaHeigtsRX;
    private static Distribution antennaHeigtsTX;
    private static double antennaHeightTXmin;
    private static double antennaHeightTXmax;
    private static double antennaHeightRXmin;
    private static double antennaHeightRXmax;
    private static String hint;


    private static void doDistance(Scenario scenario, List<Object> path) {
        double distance = -10;
        boolean setILR_centre = false;
        relativePosition = "";
        pathDistanceFactor = null;
        if (path.size() > 1) {
            // is sensing link
            if (path.get(0) instanceof InterferenceLink) {
                if (((InterferenceLink) path.get(0)).getInterferingSystem() instanceof GenericSystem) {
                    // generic system interferes
                    InterferenceLink genericInterferer = (InterferenceLink) path.get(0);
                    distance = genericInterferer.getInterferingLinkRelativePosition().getSimulationRadius();
                    if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial() > 0) {
                        if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().isBounded())
                            distance += genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax();
                        else
                            distance += genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial();
                    }
                    pathDistanceFactor = genericInterferer.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor();
                }
            }
        } else if (path.size() > 0) {
            if (path.get(0) instanceof RadioSystem) {
                // system distances and antenna heights
                if (path.get(0) instanceof GenericSystem) {
                    GenericSystem genericSystem = (GenericSystem) path.get(0);
                    // generic system
                    offset = genericSystem.getLink().getRelativeLocation().getDeltaPosition();
                    distance = Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                    pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                    if (!genericSystem.getLink().getRelativeLocation().useCorrelatedDistance()) {
                        distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem);
                        pathDistanceFactor = genericSystem.getLink().getRelativeLocation().getPathDistanceFactor();
                    }
                } else if (path.get(0) instanceof CellularSystem) {
                    // cellular system
                    CellularSystem cellularSystem = (CellularSystem) path.get(0);
                    distance = cellularSystem.getLayout().getCellRadius() * cellularSystem.getLayout().getTierSetup().ordinal() * 3; // TODO  3GPP layout with 3*R1 for BS - BS distance
                    if (cellularSystem.getLayout().getSectorSetup().toString().contains("3GPP2"))
                        distance = cellularSystem.getLayout().getCellRadius() * cellularSystem.getLayout().getTierSetup().ordinal() * Math.sqrt(3); // TODO  3GPP2 layout with sqrt(3)* 2 * R1 for BS - BS distance
                    if (cellularSystem.getLayout().generateWrapAround())
                        distance *= 2; //TODO check whether this distance is simulated -> it is !!
                    distance += cellularSystem.getLayout().getCellRadius();
                    pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                }
            } else if (path.get(0) instanceof InterferenceLink) {
                // interfering link distances and antenna heights
                if (((InterferenceLink) path.get(0)).getInterferingSystem() instanceof GenericSystem) {
                    // generic system interferes
                    InterferenceLink genericInterferer = (InterferenceLink) path.get(0);
                    offset = genericInterferer.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                    distance = Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                    pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0); // will be updated according to the correlation mode
                    InterferingLinkRelativePosition.CorrelationMode mode = genericInterferer.getInterferingLinkRelativePosition().getCorrelationMode();
                    setILR_centre = genericInterferer.getInterferingLinkRelativePosition().isWrCenterOfItDistribution();
                    switch (mode) {
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR:
                        case VICTIM_DMA_INTERFERER_CLASSICAL_NONE: {
                            distance += genericInterferer.getInterferingLinkRelativePosition().getSimulationRadius();
                            if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial() > 0) {
                                if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().isBounded())
                                    distance += genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax();
                                else
                                    distance += genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial();
                            }
                            pathDistanceFactor = genericInterferer.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor();
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT: {
                            distance += genericInterferer.getInterferingLinkRelativePosition().getSimulationRadius();
                            if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial() > 0) {
                                if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().isBounded())
                                    distance += genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax();
                                else
                                    distance += genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial();
                            }
                            GenericSystem genericSystem = (GenericSystem) genericInterferer.getVictimSystem();
                            distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem)
                                    * genericSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            pathDistanceFactor = genericInterferer.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor();
                            relativePosition = "<HtMl><br/>Please note that ILT is set relative to VLT. ";
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR:
                        case VICTIM_DMA_INTERFERER_CLASSICAL_COR_IT: {
                            //do nothing as covered by offset
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT: {
                            GenericSystem genericSystem = (GenericSystem) genericInterferer.getVictimSystem();
                            offset = genericSystem.getLink().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            if (!genericSystem.getLink().getRelativeLocation().useCorrelatedDistance()) {
                                distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem)
                                        * genericSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            }
                            relativePosition = "<HtMl><br/>Please note that ILT is set relative to VLT. ";
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR:
                        case VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST: {
                            // VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR -> calculate simulation radius taking account of dens active and protection distance
                            InterfererDensity dens = ((GenericSystem)genericInterferer.getInterferingSystem()).getInterfererDensity();
                            double simRadius = Math.sqrt(1 / (Math.PI * dens.getDensityTx() * dens.getProbabilityOfTransmission() * dens.getActivity().evaluateMax() * dens.getHourOfDay()));
                            if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial() > 0) {
                                simRadius = Math.sqrt((simRadius * simRadius
                                        - genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax()
                                        * genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax())
                                        + genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax()
                                        * genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax());
                            }
                            distance += simRadius;
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT: {
                            // VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT -> calculate simulation radius taking account of dens active and protection distance
                            GenericSystem genericSystem = (GenericSystem) genericInterferer.getVictimSystem();
                            offset = genericSystem.getLink().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            if (!genericSystem.getLink().getRelativeLocation().useCorrelatedDistance()) {
                                distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem)
                                        * genericSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            }
                            InterfererDensity dens = ((GenericSystem)genericInterferer.getInterferingSystem()).getInterfererDensity();
                            double simRadius = Math.sqrt(1 / (Math.PI * dens.getDensityTx() * dens.getProbabilityOfTransmission() * dens.getActivity().evaluateMax() * dens.getHourOfDay()));
                            if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial() > 0) {
                                simRadius = Math.sqrt((simRadius * simRadius
                                        - genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax()
                                        * genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax())
                                        + genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax()
                                        * genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax());
                            }
                            distance += simRadius;
                            relativePosition = "<HtMl><br/>Please note that ILT is set relative to VLT. ";
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR:
                        case VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM: {
                            // VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR -> calculate simulation radius taking account of dens active and protection distance
                            InterfererDensity dens = ((GenericSystem)genericInterferer.getInterferingSystem()).getInterfererDensity();
                            double simRadius = Math.sqrt(genericInterferer.getInterferingLinkRelativePosition().getNumberOfActiveTransmitters() / (Math.PI * dens.getDensityTx() * dens.getProbabilityOfTransmission() * dens.getActivity().evaluateMax() * dens.getHourOfDay()));
                            if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial() > 0) {
                                simRadius = Math.sqrt((simRadius * simRadius
                                        - genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax()
                                        * genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax())
                                        + genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax()
                                        * genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax());
                            }
                            distance += simRadius;
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT: {
                            // VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT -> calculate simulation radius taking account of dens active and protection distance
                            GenericSystem genericSystem = (GenericSystem) genericInterferer.getVictimSystem();
                            offset = genericSystem.getLink().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            if (!genericSystem.getLink().getRelativeLocation().useCorrelatedDistance()) {
                                distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem)
                                        * genericSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            }
                            InterfererDensity dens = ((GenericSystem)genericInterferer.getInterferingSystem()).getInterfererDensity();
                            double simRadius = Math.sqrt(genericInterferer.getInterferingLinkRelativePosition().getNumberOfActiveTransmitters() / (Math.PI * dens.getDensityTx() * dens.getProbabilityOfTransmission() * dens.getActivity().evaluateMax() * dens.getHourOfDay()));
                            if (genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().trial() > 0) {
                                simRadius = Math.sqrt((simRadius * simRadius
                                        - genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax()
                                        * genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax())
                                        + genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax()
                                        * genericInterferer.getInterferingLinkRelativePosition().getProtectionDistance().getBounds().getMax());
                            }
                            distance += simRadius;
                            relativePosition = "<HtMl><br/>Please note that ILT is set relative to VLT. ";
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR:
                        case VICTIM_DMA_INTERFERER_CLASSICAL_COR_WR: {
                            // VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR -> calculate simulation radius taking account of coverage interfering system
                            GenericSystem genericSystem = (GenericSystem) genericInterferer.getVictimSystem();
                            offset = genericSystem.getLink().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            if (!genericSystem.getLink().getRelativeLocation().useCorrelatedDistance()) {
                                distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem)
                                        * genericSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            }
                            relativePosition = "<HtMl><br/>Please note that ILR is set relative to VLR. ";
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT: {
                            // VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR -> calculate simulation radius taking account of coverage interfering system
                            GenericSystem genericSystem = (GenericSystem) genericInterferer.getVictimSystem();
                            offset = genericSystem.getLink().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            if (!genericSystem.getLink().getRelativeLocation().useCorrelatedDistance()) {
                                distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem)
                                        * genericSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            }
                            genericSystem = (GenericSystem) genericInterferer.getInterferingSystem();
                            offset = genericSystem.getLink().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            if (!genericSystem.getLink().getRelativeLocation().useCorrelatedDistance()) {
                                distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem)
                                        * genericSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            }
                            relativePosition = "<HtMl><br/>Please note that ILR is set relative to VLT. ";
                            break;
                        }
                        case VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT: {
                            CellularSystem victimSystem = (CellularSystem) genericInterferer.getVictimSystem();
                            distance = victimSystem.getLayout().getCellRadius() * victimSystem.getLayout().getTierSetup().ordinal() * 3;
                            offset = genericInterferer.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            pathDistanceFactor = genericInterferer.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor();
                            break;
                        }
                        case VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR: {
                            GenericSystem genericSystem = (GenericSystem) genericInterferer.getInterferingSystem();
                            distance += genericSystem.getLink().getCoverageRadius().evaluate(genericSystem);
                            CellularSystem cellularSystem = (CellularSystem) genericInterferer.getVictimSystem();
                            distance += cellularSystem.getLayout().getCellRadius() * cellularSystem.getLayout().getTierSetup().ordinal() * 3;
                            pathDistanceFactor = genericInterferer.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor();
                            relativePosition = "<HtMl><br/>Please note that ILR is set relative to VLR. ";
                            break;
                        }

                        default: {
                            //keep offset and path distance factor = 1
                        }
                    }
                    if (setILR_centre) {
                        if (genericInterferer.getVictimSystem() instanceof GenericSystem) {
                            distance += ((GenericSystem) genericInterferer.getInterferingSystem()).getLink().getCoverageRadius().evaluate((GenericSystem) genericInterferer.getInterferingSystem());
                            if (relativePosition.isEmpty()) {
                                distance += ((GenericSystem) genericInterferer.getVictimSystem()).getLink().getCoverageRadius().evaluate((GenericSystem) genericInterferer.getVictimSystem());
                            }
                        } else if (genericInterferer.getVictimSystem() instanceof CellularSystem) {
                            distance += ((GenericSystem) genericInterferer.getInterferingSystem()).getLink().getCoverageRadius().evaluate((GenericSystem) genericInterferer.getInterferingSystem());
                            if (relativePosition.isEmpty()) {
                                distance += ((CellularSystem) genericInterferer.getVictimSystem()).getLayout().getCellRadius() * ((CellularSystem) genericInterferer.getVictimSystem()).getLayout().getTierSetup().ordinal() * 3;
                            }
                        }
                        relativePosition += getILRcentreInformation();
                    }
                } else if (((InterferenceLink) path.get(0)).getInterferingSystem() instanceof CellularSystem) {
                    // cellular system interferes
                    InterferenceLink interferenceLink = (InterferenceLink) path.get(0);
                    CellularSystem cellularSystem = (CellularSystem) interferenceLink.getInterferingSystem();
                    InterferingLinkRelativePosition.CorrelationMode mode = interferenceLink.getInterferingLinkRelativePosition().getCorrelationMode();
                    switch (mode) {
                        case VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR: {
                            offset = interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                            distance = Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            distance += cellularSystem.getLayout().getCellRadius() * cellularSystem.getLayout().getTierSetup().ordinal();
                            pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                            break;
                        }

                        case VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR: {
                            offset = interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                            distance = Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            distance += cellularSystem.getLayout().getCellRadius() * cellularSystem.getLayout().getTierSetup().ordinal();
                            // path distance in km accessible as path distance factor
                            distance += interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT: {
                            offset = interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                            distance = Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            distance += cellularSystem.getLayout().getCellRadius() * cellularSystem.getLayout().getTierSetup().ordinal();
                            GenericSystem victimSystem = (GenericSystem) interferenceLink.getVictimSystem();
                            double coverageRadius = victimSystem.getLink().getCoverageRadius().evaluate(victimSystem) *
                                    victimSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            distance += coverageRadius;
                            pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                            relativePosition = "<HtMl><br/>Please note that ILT is set relative to VLT. ";
                            break;
                        }
                        case VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT: {
                            offset = interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                            distance = Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            distance += cellularSystem.getLayout().getCellRadius() * cellularSystem.getLayout().getTierSetup().ordinal();
                            GenericSystem victimSystem = (GenericSystem) interferenceLink.getVictimSystem();
                            double coverageRadius = victimSystem.getLink().getCoverageRadius().evaluate(victimSystem) *
                                    victimSystem.getLink().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            distance += coverageRadius;
                            // path distance in km accessible as path distance factor
                            distance += interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                            relativePosition = "<HtMl><br/>Please note that ILT is set relative to VLT. ";
                            break;
                        }
                        case VICTIM_DMA_INTERFERER_DMA_COR: {
                            // VICTIM_DMA_INTERFERER_DMA_COR
                            CellularSystem victimSystem = (CellularSystem) interferenceLink.getVictimSystem();
                            distance = victimSystem.getLayout().getCellRadius() * victimSystem.getLayout().getTierSetup().ordinal() * 3;
                            offset = interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                            break;
                        }
                        case VICTIM_DMA_INTERFERER_DMA_DYN: {
                            // VICTIM_DMA_INTERFERER_DMA_DYN
                            CellularSystem victimSystem = (CellularSystem) interferenceLink.getVictimSystem();
                            distance = victimSystem.getLayout().getCellRadius() * victimSystem.getLayout().getTierSetup().ordinal() * 3;
                            offset = interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                            distance += Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            // path distance in km accessible as path distance factor
                            distance += interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getPathDistanceFactor().getBounds().getMax();
                            pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                            break;
                        }
                        default: {
                            offset = interferenceLink.getInterferingLinkRelativePosition().getRelativeLocation().getDeltaPosition();
                            distance = Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY());
                            distance += cellularSystem.getLayout().getCellRadius() * cellularSystem.getLayout().getTierSetup().ordinal();
                            pathDistanceFactor = Factory.distributionFactory().getConstantDistribution(1.0);
                        }
                    }
                }
            }
        }
        if (pathDistanceFactor != null) {
            if (pathDistanceFactor.getBounds().isBounded()) {
                maxDistance = distance * pathDistanceFactor.getBounds().getMax();
                minDistance = distance * pathDistanceFactor.getBounds().getMin();
            } else {
                maxDistance = distance * pathDistanceFactor.trial();
                minDistance = maxDistance;
            }
            if (offset != null && !offset.equals(new Point2D(0, 0))) {
                deltaPosition = "<HtMl><br/>Please note that this check takes account of the correlated position (Delta X and Delta Y).";
            } else deltaPosition = "";
        } else {
            maxDistance = distance;
            minDistance = distance;
            relativePosition = "Distance check not applicable to antenna plugins";
        }
    }

    private static void doAntennaHeihts(List<Object> path) {
        if (path.size() > 0) {
            if (path.get(0) instanceof RadioSystem) {
                // system antenna heights
                if (path.get(0) instanceof GenericSystem) {
                    GenericSystem genericSystem = (GenericSystem) path.get(0);
                    // generic system
                    antennaHeigtsRX = genericSystem.getReceiver().getHeight();
                    antennaHeigtsTX = genericSystem.getTransmitter().getHeight();
                } else if (path.get(0) instanceof CellularSystem) {
                    // cellular system
                    CellularSystem cellularSystem = (CellularSystem) path.get(0);
                    antennaHeigtsRX = cellularSystem.getReceiver().getHeight();
                    antennaHeigtsTX = cellularSystem.getTransmitter().getHeight();
                }
            }
            if (path.get(0) instanceof InterferenceLink) {
                // interfering link antenna heights
                if (((InterferenceLink) path.get(0)).getInterferingSystem() instanceof GenericSystem) {
                    // generic system interferes
                    InterferenceLink genericInterferer = (InterferenceLink) path.get(0);
                    antennaHeigtsRX = genericInterferer.getVictimSystem().getReceiver().getHeight();
                    antennaHeigtsTX = genericInterferer.getInterferingSystem().getTransmitter().getHeight();
                } else if (((InterferenceLink) path.get(0)).getInterferingSystem() instanceof CellularSystem) {
                    // cellular system interferes
                    InterferenceLink interferenceLink = (InterferenceLink) path.get(0);
                    antennaHeigtsRX = interferenceLink.getVictimSystem().getReceiver().getHeight();
                    antennaHeigtsTX = interferenceLink.getInterferingSystem().getTransmitter().getHeight();
                }
            }

            if (path.size() > 1) {
                // is sensing link -> change TX and RX antennas
                if (path.get(0) instanceof InterferenceLink) {
                    if (((InterferenceLink) path.get(0)).getInterferingSystem() instanceof GenericSystem) {
                        // generic system interferes
                        InterferenceLink genericInterferer = (InterferenceLink) path.get(0);
                        antennaHeigtsRX = genericInterferer.getInterferingSystem().getTransmitter().getHeight();
                        antennaHeigtsTX = genericInterferer.getVictimSystem().getTransmitter().getHeight();
                    }
                }
            }
        }
        antennaHeightRXmin = antennaHeigtsRX.getBounds().getMin();
        antennaHeightRXmax = antennaHeigtsRX.getBounds().getMax();
        antennaHeightTXmin = antennaHeigtsTX.getBounds().getMin();
        antennaHeightTXmax = antennaHeigtsTX.getBounds().getMax();
    }

    public static double getAntennaHeightTXmax(List<Object> path) {
        doAntennaHeihts(path);
        return antennaHeightTXmax;
    }

    public static double getAntennaHeightRXmin(List<Object> path) {
        doAntennaHeihts(path);
        return antennaHeightRXmin;
    }

    public static double getAntennaHeightRXmax(List<Object> path) {
        doAntennaHeihts(path);
        return antennaHeightRXmax;
    }

    public static double getAntennaHeightTXmin(List<Object> path) {
        doAntennaHeihts(path);
        return antennaHeightTXmin;
    }


    public static double getMaxDistance(Scenario scenario, List<Object> path) {
        doDistance(scenario, path);
        return maxDistance;
    }

    public static double getMinDistance(Scenario scenario, List<Object> path) {
        doDistance(scenario, path);
        return minDistance;
    }

    public static String getRelativePosition() {
        return relativePosition;
    }

    public static String getDeltaPosition() {
        return deltaPosition;
    }

    public static String getExceptionHint(){
        return  "<HtMl><br/><strong style = 'font-style: italic; color: red;'> This might cause a runtime exception, which will abort the simulation.</strong>";
    }

    public static String getManualReferene(String reason){
        return  "<HtMl><p style = 'font-style: italic; color: blue;'> Some parameters (" + reason + ") are outside of the scope of " +
                "applicability of the selected propagation model. <br/>This might cause inaccurate results.</p>";
    }

    private static String getILRcentreInformation(){
        return "<HtMl><br/>Please note that this check takes account of the coverage radius of the interfering link (ILR set at centre).";
    }
}
