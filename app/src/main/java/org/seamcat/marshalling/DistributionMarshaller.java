package org.seamcat.marshalling;

import com.rits.cloning.Cloner;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.model.distributions.*;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributionMarshaller {

    private static Map<Class<? extends AbstractDistribution>, Integer> classType = new HashMap<Class<? extends AbstractDistribution>, Integer>();

    public static final int TYPE_CONSTANT = 0;
    public static final int TYPE_USER_DEFINED = 1;
    public static final int TYPE_UNIFORM = 2;
    public static final int TYPE_GAUSSIAN = 3;
    public static final int TYPE_RAYLEIGH = 4;
    public static final int TYPE_UNIFORM_POLAR_DISTANCE = 5;
    public static final int TYPE_UNIFORM_POLAR_ANGLE = 6;
    public static final int TYPE_USER_DEFINED_STAIR = 7;
    public static final int TYPE_DISCRETE_UNIFORM = 8;


    static {
        classType.put(ConstantDistributionImpl.class, 0);
        classType.put(UserDefinedDistributionImpl.class, 1);
        classType.put(DiscreteUniformDistributionImpl.class, 8);
        classType.put(GaussianDistributionImpl.class, 3);
        classType.put(RayleighDistributionImpl.class, 4);
        classType.put(StairDistributionImpl.class, 7);
        classType.put(UniformDistributionImpl.class, 2);
        classType.put(UniformPolarAngleDistributionImpl.class, 6);
        classType.put(UniformPolarDistanceDistributionImpl.class, 5);
    }

    public static AbstractDistribution fromElement(Element element) {
        int type = Integer.parseInt(element.getAttribute("type").trim());
        if ( type == 0 ) {
            return new ConstantDistributionImpl(Double.parseDouble(element.getAttribute("constant")));
        }
        DistributionBuilder builder = new DistributionBuilder();

        if ( type == TYPE_USER_DEFINED ) {
            NodeList nl = element.getElementsByTagName("point2d");
            int size = nl.getLength();
            List<Point2D> _points = new ArrayList<Point2D>(size);
            for (int x = 0; x < size; x++) {
                _points.add( PointMarshaller.fromElement2D((Element) nl.item(x)));
            }
            builder.setFunction( new DiscreteFunction(_points));
        } else if ( type == TYPE_USER_DEFINED_STAIR ) {
            builder.setFunction( new DiscreteFunction());
            NodeList nlStair = element.getElementsByTagName("user-defined-stair");
            if (nlStair != null && nlStair.getLength() > 0) {
                NodeList nl = ((Element) nlStair.item(0))
                        .getElementsByTagName("point2d");
                List<Point2D> points = new ArrayList<>();
                for (int x = 0, size = nl.getLength(); x < size; x++) {
                    points.add(PointMarshaller.fromElement2D((Element) nl.item(x)));
                }
                builder.setFunction( new DiscreteFunction(points) );
            }
        }

        builder.setConstant(Double.parseDouble(element.getAttribute("constant")));
        builder.setMean(Double.parseDouble(element.getAttribute("mean")));
        builder.setStdDev(Double.parseDouble(element.getAttribute("std-dev")));
        builder.setMin(Double.parseDouble(element.getAttribute("min")));
        builder.setMax(Double.parseDouble(element.getAttribute("max")));
        builder.setMaxDistance(Double.parseDouble(element.getAttribute("max-distance")));
        builder.setMaxAngle(Double.parseDouble(element.getAttribute("max-angle")));
        builder.setStep(Double.parseDouble(element.getAttribute("step")));
        try {
            builder.setStepShift(Double.parseDouble(element.getAttribute("stepShift")));
        } catch (NumberFormatException e) {
            builder.setStepShift(builder.getStep()/2.0);
        }
        return builder.build( type );
    }

    public static AbstractDistribution copy( AbstractDistribution distribution ) {
        Cloner cloner = new Cloner();
        return cloner.deepClone( distribution );
    }


    public static Element toElement(Document doc, AbstractDistribution distribution) {
        Element element = doc.createElement("distribution");
        element.setAttribute("type", String.valueOf(classType.get(distribution.getClass())));
        element.setAttribute("constant", String.valueOf(distribution.getConstant()));
        element.setAttribute("mean", String.valueOf(distribution.getMean()));
        element.setAttribute("std-dev", String.valueOf(distribution.getStdDev()));
        element.setAttribute("min", String.valueOf(distribution.getMin()));
        element.setAttribute("max", String.valueOf(distribution.getMax()));
        element.setAttribute("max-distance", String.valueOf(distribution.getMaxDistance()));
        element.setAttribute("max-angle", String.valueOf(distribution.getMaxAngle()));
        element.setAttribute("step", String.valueOf(distribution.getStep()));
        element.setAttribute("stepShift", String.valueOf(distribution.getStepShift()));

        switch (classType.get(distribution.getClass())) {
            case TYPE_USER_DEFINED: {
                Element userDefined = doc.createElement("user-defined");
                Function cdf = ((UserDefinedDistributionImpl) distribution).getCdf();
                userDefined.appendChild(FunctionMarshaller.toElement(doc, cdf));
                element.appendChild(userDefined);
                break;
            }
            case TYPE_USER_DEFINED_STAIR: {
                Element userDefinedStair = doc.createElement("user-defined-stair");
                StairDistributionImpl sd = (StairDistributionImpl) distribution;
                DiscreteFunction cdf = (DiscreteFunction) sd.getCdf();
                List<Point2D> points = cdf.points();
                for (Point2D point : points) {
                    userDefinedStair.appendChild( PointMarshaller.toElement2D( doc, point));
                }
                element.appendChild(userDefinedStair);
                break;
            }
        }
        Element name = doc.createElement("description");
        name.appendChild(doc.createCDATASection(distribution.toString()));
        element.appendChild(name);

        return element;
    }

}
