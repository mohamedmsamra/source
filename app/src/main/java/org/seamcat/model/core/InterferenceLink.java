package org.seamcat.model.core;

import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.interfaces.Identifiable;
import org.seamcat.model.RadioSystem;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.InterferingLinkRelativePosition;
import org.seamcat.model.generic.PathLossCorrelationUI;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.generic.RelativeLocation;
import org.seamcat.model.types.PathLossCorrelation;
import org.seamcat.model.workspace.RelativeLocationInterferenceUI;
import org.seamcat.plugin.PropagationModelConfiguration;

public class InterferenceLink implements Identifiable, org.seamcat.model.types.InterferenceLink {

    private SystemSimulationModel interferingLink;
    private SystemSimulationModel victimLink;
    private RelativeLocationInterferenceUI relativeLocation = ProxyHelper.newInstance(RelativeLocationInterferenceUI.class);
    private PropagationModelConfiguration propagationModel = SeamcatFactory.propagation().getHataSE21();
    private PathLossCorrelationUI pathLossCorrelation = ProxyHelper.newInstance(PathLossCorrelationUI.class);
    private double calculatedSimulationRadius;

    public void setRelativeLocation(RelativeLocationInterferenceUI relativeLocation) {
        this.relativeLocation = relativeLocation;
        this.calculatedSimulationRadius = relativeLocation.simulationRadius();
    }

    private InterferenceLink coLocation;

    public void setCoLocation( InterferenceLink coLocation ) {
        this.coLocation = coLocation;
    }

    public InterferenceLink(SystemSimulationModel victim, SystemSimulationModel interferer) {
        victimLink = victim;
        interferingLink = interferer;
    }

    public SystemSimulationModel getInterferingLink() {
        return interferingLink;
    }

    public SystemSimulationModel getVictimLink() {
        return victimLink;
    }

    public AbstractDmaSystem<?> getDMASystem() {
        return getInterferingLink().getDMASystem();
    }

    /**
     * method dealing with the input parameter of the Interfering Link Relative Position panel
     *
     */
    @Override
    public InterferingLinkRelativePosition getInterferingLinkRelativePosition() {
        return new InterferingLinkRelativePosition() {

            @Override
            public CorrelationMode getCorrelationMode() {
                return relativeLocation.mode();
            }

            @Override
            public boolean isWrCenterOfItDistribution() {
                return relativeLocation.setILRatTheCenter();
            }

            @Override
            public double getSimulationRadius() {
                // Note this is the UI field. But is also available in the result model
                // where it might be calculates depending on the settings.
                return relativeLocation.simulationRadius();
            }

            @Override
            public int getNumberOfActiveTransmitters() {
                return relativeLocation.numberOfActiveTransmitters();
            }

            @Override
            public boolean useCoLocatedWith() {
                return relativeLocation.isCoLocated();
            }

            @Override
            public InterferenceLink getCoLocatedWith() {
                return coLocation;
            }

            @Override
            public Point2D getCoLocationDeltaPosition() {
                return new Point2D(relativeLocation.coLocationX().trial(), relativeLocation.coLocationY().trial());
            }

            @Override
            public Distribution getMinimumCouplingLoss() {
                return relativeLocation.minimumCouplingLoss();
            }

            @Override
            public Distribution getProtectionDistance() {
                return relativeLocation.protectionDistance();
            }

            @Override
            public RelativeLocation getRelativeLocation() {
                return new RelativeLocation() {
                    public boolean useCorrelatedDistance() {
                        return false;
                    }

                    public Point2D getDeltaPosition() {
                        return new Point2D(relativeLocation.deltaX().trial(), relativeLocation.deltaY().trial());
                    }

                    public Distribution getPathAzimuth() {
                        return relativeLocation.pathAzimuth();
                    }

                    public Distribution getPathDistanceFactor() {
                        return relativeLocation.pathDistanceFactor();
                    }

                    public boolean usePolygon() {
                        return relativeLocation.usePolygon();
                    }
                    public Shape shape() {
                        return relativeLocation.shape();
                    }
                    public Distribution turnCCW() {
                        return relativeLocation.turnCCW();
                    }
                };
            }
        };
    }

    /**
     * method dealing with the input parameter of the Path Loss Correlation panel
     *
     */
    @Override
    public PathLossCorrelation getPathLossCorrelation() {
        return new PathLossCorrelation() {
            @Override
            public boolean isUsingPathLossCorrelation() {
                return pathLossCorrelation.usePathLossCorrelation();
            }

            @Override
            public double getPathLossVariance() {
                return pathLossCorrelation.pathLossVariance();
            }

            @Override
            public double getCorrelationFactor() {
                return pathLossCorrelation.correlationFactor();
            }
        };
    }

    public void setPathLossCorrelation( PathLossCorrelationUI pathLossCorrelation ) {
        this.pathLossCorrelation = pathLossCorrelation;
    }

    @Override
    public PropagationModelConfiguration getPropagationModel() {
        return propagationModel;
    }


    @Override
    public RadioSystem getVictimSystem() {
        return victimSystem;
    }

    @Override
    public RadioSystem getInterferingSystem() {
        return interferingSystem;
    }

    private RadioSystem victimSystem;
    private RadioSystem interferingSystem;

    public void setVictimSystem(RadioSystem victimSystem) {
        this.victimSystem = victimSystem;
    }

    public void setInterferingSystem(RadioSystem interferingSystem) {
        this.interferingSystem = interferingSystem;
    }

    public PathLossCorrelationUI getPathLossCorrelationUI() {
        return pathLossCorrelation;
    }

    public void setPropagationModel( PropagationModelConfiguration propagationModel ) {
        this.propagationModel = propagationModel;
    }

    @Override
    public String getReference() {
        return getInterferingLink().getName();
    }

    @Override
    public double getCalculatedSimulationRadius() {
        return calculatedSimulationRadius;
    }

    public void setCalculatedSimulationRadius(double calculatedSimulationRadius) {
        this.calculatedSimulationRadius = calculatedSimulationRadius;
    }
}
