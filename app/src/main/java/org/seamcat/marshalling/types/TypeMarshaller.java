package org.seamcat.marshalling.types;

import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.marshalling.*;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.ConstantDistributionImpl;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.MaskFunction;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.*;
import org.seamcat.model.systems.CalculatedValue;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class TypeMarshaller {

    private static final Logger LOG = Logger.getLogger(TypeMarshaller.class);

    public static <T> T fromElement(Class<T> clazz, Element element) {
        final Map<Method, Object> values = new LinkedHashMap<Method, Object>();
        for (Method m : marshallOrder(clazz)) {
            values.put(m, unmarshall(clazz, element, m));
        }
        return ProxyHelper.proxy(clazz, values);
    }

    public static <T> void toElement( Class<T> clazz, Document doc, Element element, T model ) {
        for (Method method : marshallOrder(clazz)) {
            try {
                marshall(doc, element, method, method.invoke(model));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Method> marshallOrder(Class<?> clazz) {
        List<Method> marshallOrder = new ArrayList<Method>();
        TreeMap<Integer, Method> ordered = new TreeMap<Integer, Method>();
        for (Method method : clazz.getDeclaredMethods()) {
            Config config = method.getAnnotation(Config.class);
            if ( config != null ) {
                if ( attributeType( method )) {
                    marshallOrder.add( method );
                } else {
                    ordered.put( config.order(), method);
                }
            }
        }
        for (Method method : ordered.values()) {
            marshallOrder.add( method );
        }
        return marshallOrder;
    }

    private static boolean attributeType( Method method ) {
        Class<?> type = method.getReturnType();

        if ( type.isPrimitive()) {
            return true;
        }
        if (Double.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type)
                || Boolean.class.isAssignableFrom(type) || OptionalDoubleValue.class.isAssignableFrom(type)
                || String.class.isAssignableFrom(type)) {
            return true;
        }

        return false;
    }

    private static void marshall( Document doc, Element element, Method method, Object object ) {
        String name = method.getName();
        if ( object instanceof Double) {
            element.setAttribute(name, String.valueOf(object));
        } else if ( object instanceof Boolean ) {
            element.setAttribute(name, Boolean.toString((Boolean) object));
        } else if ( object instanceof Distribution ) {
            Element dist = doc.createElement(name);
            dist.appendChild(DistributionMarshaller.toElement(doc, (AbstractDistribution) object));
            element.appendChild( dist );
        } else if (object instanceof OptionalDoubleValue) {
            OptionalDoubleValue od = (OptionalDoubleValue) object;
            element.setAttribute( name, String.valueOf(od.getValue()));
            element.setAttribute( "use_"+name, Boolean.toString(od.isRelevant()));
        } else if (object instanceof String) {
            Config con = method.getAnnotation(Config.class);
            if ( con != null && !con.values().isEmpty()) {
                String[] values = con.values().split(",");
                int i=0;
                for (; i<values.length; i++) {
                    if ( values[i].equals(object)) {
                        break;
                    }
                }
                element.setAttribute(name, Integer.toString(i));
            } else {
                element.setAttribute(name, (String) object);
            }
        } else if ( object instanceof MaskFunction ) {
            MaskFunction mf = (MaskFunction) object;
            Element named = doc.createElement(name);
            FunctionMarshaller.toElement(named, doc, mf);
            element.appendChild(named);
        } else if ( object instanceof OptionalMaskFunction ) {
            OptionalMaskFunction mask = (OptionalMaskFunction) object;
            Element named = doc.createElement(name);
            named.setAttribute("enabled", Boolean.toString(mask.isRelevant()));
            FunctionMarshaller.toElement(named, doc, mask.getMaskFunction());
            element.appendChild( named );
        } else if (object instanceof BlockingMaskImpl) {
            element.appendChild(LibraryFunctionMarshaller.toElement((BlockingMaskImpl) object, doc));
        } else if (object instanceof OptionalFunction) {
            OptionalFunction mask = (OptionalFunction) object;
            Element named = doc.createElement(name);
            named.setAttribute("enabled", Boolean.toString(mask.isRelevant()));
            Element functionElement = doc.createElement("function");
            functionElement.appendChild(FunctionMarshaller.toElement(doc, mask.getFunction()));
            named.appendChild(functionElement);
            element.appendChild(named);
        } else if (object instanceof Function) {
            Element functionElement = doc.createElement(name);
            functionElement.appendChild(FunctionMarshaller.toElement(doc, (Function)object));
            element.appendChild(functionElement);

        } else if (object instanceof Integer ) {
            element.setAttribute(name, String.valueOf(object));
        } else if ( object instanceof PropagationModelConfiguration) {
            Element named = doc.createElement(name);
            named.appendChild(LibraryFunctionMarshaller.toElement((PropagationModelConfiguration) object, doc));
            element.appendChild( named );
        } else if ( object instanceof AntennaGainConfiguration) {
            Element named = doc.createElement(name);
            named.appendChild(LibraryFunctionMarshaller.toElement((AntennaGainConfiguration) object, doc));
            element.appendChild( named );
        } else if ( object instanceof OptionalDistribution) {
            OptionalDistribution mask = (OptionalDistribution) object;
            Element named = doc.createElement(name);
            named.setAttribute("enabled", Boolean.toString(mask.isRelevant()));
            named.appendChild(DistributionMarshaller.toElement(doc, (AbstractDistribution) mask.getValue()));
            element.appendChild(named);
        } else if ( object instanceof List ) {
            // this can only be of type LocalEnvironment
            Element named = doc.createElement(name);
            List<LocalEnvironment> list = (List<LocalEnvironment>) object;
            LocalEnvironmentMarshaller.toElement( doc, named, list);
            element.appendChild( named );
        } else if ( object instanceof CDMALinkLevelData) {
            CDMALinkLevelData lld = (CDMALinkLevelData) object;
            element.appendChild(lld.toElement(doc));
        } else if ( CalculatedValue.class.isAssignableFrom(method.getReturnType())) {

        } else if ( Enum.class.isAssignableFrom(method.getReturnType())) {
            Object[] enumConstants = method.getReturnType().getEnumConstants();
            int index = 0;
            for ( int i=0; i<enumConstants.length; i++) {
                if ( enumConstants[i] == object) {
                    index = i;
                }
            }
            element.setAttribute(name, Integer.toString(index));
        } else {
            throw new IllegalArgumentException("Cannot marshall object with class: " + object.getClass());
        }
    }


    private static <T> Object unmarshall(Class<T> clazz, Element element, Method method) {
        String name = method.getName();
        Class<?> type = method.getReturnType();
        try {
            if (type.isPrimitive()) {
                if (double.class.isAssignableFrom(type)) {
                    return Double.parseDouble(element.getAttribute(name));
                } else if (boolean.class.isAssignableFrom(type)) {
                    if (!element.hasAttribute(name)) {
                        Object o = defaultValue(clazz, method);
                        if (o != null) return o;
                    }
                    return Boolean.valueOf(element.getAttribute(name));
                } else if (int.class.isAssignableFrom(type)) {
                    return Integer.parseInt(element.getAttribute(name));
                }
            }
            if (Double.class.isAssignableFrom(type)) {
                return Double.parseDouble(element.getAttribute(name));
            } else if (Boolean.class.isAssignableFrom(type)) {
                return Boolean.valueOf(element.getAttribute(name));
            } else if (OptionalDoubleValue.class.isAssignableFrom(type)) {
                double value = Double.parseDouble(element.getAttribute(name));
                Boolean relevant = Boolean.valueOf(element.getAttribute("use_" + name));
                return new OptionalDoubleValue(relevant, value);
            } else if (Distribution.class.isAssignableFrom(type)) {
                NodeList elements = element.getElementsByTagName(name);
                if (elements.getLength() == 0) {
                    // look for default value
                    Object o = defaultValue(clazz, method);
                    if (o != null) return o;
                    return new ConstantDistributionImpl(0);
                } else {
                    return DistributionMarshaller.fromElement((Element) element.getElementsByTagName(name).item(0).getFirstChild());
                }
            } else if (String.class.isAssignableFrom(type)) {
                Config con = method.getAnnotation(Config.class);
                if (con != null && !con.values().isEmpty()) {
                    int i = 0;
                    try {
                        i = Integer.parseInt(element.getAttribute(name));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                    String[] values = con.values().split(",");
                    return values[i];
                } else {
                    return element.getAttribute(name);
                }
            } else if (EmissionMask.class.isAssignableFrom(type)) {
                return FunctionMarshaller.fromElement((Element) element.getElementsByTagName(name).item(0));
            } else if (BlockingMask.class.isAssignableFrom(type)) {
                return LibraryFunctionMarshaller.rbmFromElement((Element) element.getElementsByTagName(name).item(0));
            } else if (OptionalFunction.class.isAssignableFrom(type)) {
                boolean isRelevant = false;

                Element e = (Element) element.getElementsByTagName(name).item(0);

                if (e.hasAttribute("enabled")) {
                    isRelevant = Boolean.parseBoolean(e.getAttribute("enabled"));
                }
                DiscreteFunction function = FunctionMarshaller.fromFunctionElement(e);
                return new OptionalFunction(isRelevant, function);
            } else if (MaskFunction.class.isAssignableFrom(type)) {
                Element maskElement = (Element) element.getElementsByTagName(name).item(0);
                return FunctionMarshaller.fromElement(maskElement);
            } else if (Function.class.isAssignableFrom(type)) {
                Element overloading = (Element) element.getElementsByTagName(name).item(0);
                return FunctionMarshaller.fromFunctionElement(overloading);
            } else if (Integer.class.isAssignableFrom(type)) {
                return Integer.parseInt(element.getAttribute(name));
            } else if (PropagationModel.class.isAssignableFrom(type)) {
                Element e = (Element) element.getElementsByTagName(name).item(0);
                return LibraryFunctionMarshaller.fromPluginElement((Element) e.getFirstChild());
            } else if (AntennaGain.class.isAssignableFrom(type)) {
                Element e = (Element) element.getElementsByTagName(name).item(0);
                return LibraryFunctionMarshaller.fromPluginElement((Element) e.getFirstChild());
            } else if (OptionalMaskFunction.class.isAssignableFrom(type)) {
                boolean isRelevant = false;

                Element e = (Element) element.getElementsByTagName(name).item(0);

                if (e.hasAttribute("enabled")) {
                    isRelevant = Boolean.parseBoolean(e.getAttribute("enabled"));
                }
                EmissionMaskImpl emissionMask = FunctionMarshaller.fromElement(e);
                return new OptionalMaskFunction(isRelevant, emissionMask);
            } else if (OptionalDistribution.class.isAssignableFrom(type)) {
                boolean isRelevant = false;
                Element e = (Element) element.getElementsByTagName(name).item(0);

                if (e.hasAttribute("enabled")) {
                    isRelevant = Boolean.parseBoolean(e.getAttribute("enabled"));
                }
                Element distElement = (Element) e.getElementsByTagName("distribution").item(0);
                Distribution distribution = DistributionMarshaller.fromElement(distElement);

                return new OptionalDistribution(isRelevant, distribution);
            } else if (List.class.isAssignableFrom(type)) {
                Element named = DOMHelper.firstChild(element, name);
                return LocalEnvironmentMarshaller.fromElement(named);
            } else if (CDMALinkLevelData.class.isAssignableFrom(type)) {
                return new CDMALinkLevelData(DOMHelper.firstChild(element, "CDMA-Link-level-data"));
            } else if (CalculatedValue.class.isAssignableFrom(type)) {
                return new CalculatedValue(null);
            } else if ( Enum.class.isAssignableFrom(type)) {
                int i = 0;
                try {
                    i = Integer.parseInt(element.getAttribute(name));
                } catch (NumberFormatException e ) {
                    // ignore. default to 0
                }
                return type.getEnumConstants()[ i ];
                //return Enum.valueOf( (Class<? extends Enum>) type, element.getAttribute(name));
            }
        } catch (RuntimeException e ) {
            // revert to default value:
            try {
                return clazz.getField(name).get(null);
            } catch (NoSuchFieldException ee ) {
                LOG.error("error unmarshalling: " + name + ", " + method);
            } catch (IllegalAccessException e1) {
                LOG.error("unable to access field: ", e1);
            }
            throw e;
        }
        throw new IllegalArgumentException("Cannot unmarshall object with class: " + type);
    }

    private static Object defaultValue(Class<?> clazz, Method method) {
        for (Field field : clazz.getDeclaredFields()) {
            if ( field.getName().equals(method.getName())) {
                try {
                    return field.get(null);
                } catch (IllegalAccessException e) {
                }
            }
        }
        return null;
    }

}
