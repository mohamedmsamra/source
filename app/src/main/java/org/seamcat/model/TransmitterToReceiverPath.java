package org.seamcat.model;

import org.seamcat.marshalling.CoverageRadiusMarshaller;
import org.seamcat.marshalling.PropagationModelMarshaller;
import org.seamcat.marshalling.types.TypeMarshaller;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.GenericLink;
import org.seamcat.model.generic.RelativeLocation;
import org.seamcat.model.generic.RelativeLocationUI;
import org.seamcat.plugin.CoverageRadiusConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.simulation.coverageradius.UserDefinedCoverageRadius;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TransmitterToReceiverPath implements GenericLink {

    private RelativeLocationUI relativeLocationUI;
    private PropagationModelConfiguration propagationModel = SeamcatFactory.propagation().getHataSE21();
    private CoverageRadiusConfiguration coverageRadius = CoverageRadiusConfiguration.coverage(UserDefinedCoverageRadius.class);

    public TransmitterToReceiverPath() {
        super();
    }

    public TransmitterToReceiverPath(Element element) {
        relativeLocationUI = TypeMarshaller.fromElement( RelativeLocationUI.class, element );
        propagationModel = PropagationModelMarshaller.fromElement((Element) element.getElementsByTagName("PropagationModel").item(0).getFirstChild());

        NodeList list = element.getElementsByTagName("CoverageRadius");
        if ( list != null && list.getLength() > 0 ) {
            coverageRadius = CoverageRadiusMarshaller.fromElement((Element) list.item(0).getFirstChild());
        } else {
            coverageRadius = CoverageRadiusConfiguration.coverage(UserDefinedCoverageRadius.class);
        }
    }

    public CoverageRadiusConfiguration getCoverageRadius() {
        return coverageRadius;
    }

    public void setCoverageRadius(CoverageRadiusConfiguration coverageRadius ) {
        this.coverageRadius = coverageRadius;
    }

    public void setPropagationModel(PropagationModelConfiguration propagationModel) {
        this.propagationModel = propagationModel;
    }

    public Element toElement(Document doc, boolean withCoverageRadius) {
        Element element = doc.createElement("TransmitterToReceiverPath");
        TypeMarshaller.toElement(RelativeLocationUI.class, doc, element, relativeLocationUI);

        Element propagationModelElement = doc.createElement("PropagationModel");
        propagationModelElement.appendChild(PropagationModelMarshaller.toElement(doc, propagationModel));
        element.appendChild(propagationModelElement);

        if ( withCoverageRadius) {
            Element coverageRadiusElement = doc.createElement("CoverageRadius");
            coverageRadiusElement.appendChild(CoverageRadiusMarshaller.toElement(doc, coverageRadius));
            element.appendChild( coverageRadiusElement);
        }

        return element;
    }

    @Override
    public PropagationModelConfiguration getPropagationModel() {
        return propagationModel;
    }

    @Override
    public RelativeLocation getRelativeLocation() {
        return new RelativeLocation() {
            public boolean useCorrelatedDistance() {
                return relativeLocationUI.useCorrelatedDistance();
            }

            public Point2D getDeltaPosition() {
                return new Point2D(relativeLocationUI.deltaX().trial(), relativeLocationUI.deltaY().trial());
            }

            public AbstractDistribution getPathAzimuth() {
                return (AbstractDistribution) relativeLocationUI.pathAzimuth();
            }

            public AbstractDistribution getPathDistanceFactor() {
                return (AbstractDistribution) relativeLocationUI.pathDistanceFactor();
            }

            public boolean usePolygon() {
                return relativeLocationUI.usePolygon();
            }

            public Shape shape() {
                return relativeLocationUI.shape();
            }

            public Distribution turnCCW() {
                return relativeLocationUI.turnCCW();
            }
        };
    }

    public RelativeLocationUI getRelativeLocationUI() {
        return relativeLocationUI;
    }

    public void setRelativeLocationUI(RelativeLocationUI relativeLocationUI) {
        this.relativeLocationUI = relativeLocationUI;
    }
}
