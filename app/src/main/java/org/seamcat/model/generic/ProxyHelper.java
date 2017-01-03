package org.seamcat.model.generic;

import org.apache.log4j.Logger;
import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.ConstantDistributionImpl;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.factory.Model;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.plugin.*;
import org.seamcat.model.systems.CalculatedValue;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.model.types.*;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.CoverageRadiusConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.presentation.genericgui.panelbuilder.Cache;
import org.seamcat.presentation.genericgui.panelbuilder.ChangeListener;
import org.seamcat.simulation.coverageradius.UserDefinedCoverageRadius;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static org.seamcat.model.factory.Factory.*;
import static org.seamcat.model.factory.SeamcatFactory.antennaGain;
import static org.seamcat.model.factory.SeamcatFactory.propagation;

public class ProxyHelper {

    private static final Logger LOG = Logger.getLogger(ProxyHelper.class);

    public static <T> T newInstance( Class<T> clazz ) {
        return proxy(clazz, defaultValues( clazz ));
    }

    public static <T> T newInstance( Class<T> clazz, Map<Method, Object> defaults, Map<String, Object> values ) {
       /* for (Method method : defaults.keySet()) {
            if ( values.containsKey(method.getName())) {
                // check types
                Object suggested = values.get(method.getName());
                if (suggested != null && defaults.get(method).getClass().isAssignableFrom(suggested.getClass())) {
                    defaults.put(method, values.get(method.getName()));
                }
            }
        }*/
        return proxy(clazz, defaults );
    }

    private static Map<String, Object> staticDefaultValues(Class modelClass) {
        Map<String, Object> defaultValues = new HashMap<String, Object>();
        for (Field field : modelClass.getDeclaredFields()) {
            if ( Modifier.isStatic(field.getModifiers()) ) {
                try {
                    if (!ChangeListener.class.isAssignableFrom(field.getType())) {
                        defaultValues.put( field.getName(), field.get(null));
                    }
                } catch (IllegalAccessException e) {
                    LOG.error("error getting default", e);
                }
            }
        }
        return defaultValues;
    }

    public static LinkedHashMap<Method, Object> trueDefaultValues(Class modelClass) {
        return defaultValues(modelClass, true);
    }

    /**
     * Returns an ordered hash map with the default values of the clazz.
     *
     * The methods must be annotated with @Config to be considered and also
     * the return type must be valid plugin types.
     */
    public static LinkedHashMap<Method, Object> defaultValues(Class modelClass) {
        return defaultValues(modelClass, false);
    }

    private static LinkedHashMap<Method, Object> defaultValues(Class modelClass, boolean trueDefaults) {
        LinkedHashMap<Method, Object> instance = new LinkedHashMap<Method, Object>();
        Map<String, Object> defaultValues;
        if ( trueDefaults ) {
            defaultValues = new HashMap<String, Object>();
        } else {
            defaultValues = staticDefaultValues(modelClass);
        }
        if ( defaultValues.containsKey("variations")) {
            Object variations = defaultValues.get("variations");
            if ( variations instanceof Boolean) {
                PropagationModelConfiguration.addVariations(modelClass, (Boolean) variations);
            }
        }
        if ( defaultValues.containsKey("peakGain")) {
            Object gain = defaultValues.get("peakGain");
            if ( gain instanceof Integer || gain instanceof Double) {
                AntennaGainConfiguration.addPeakGain(modelClass, (Double) gain);
            }
        }
        Map<Method, Object> unorderedValues = new HashMap<Method, Object>();
        // default translations
        SortedMap<Integer, Method> map = new TreeMap<Integer, Method>();

        for (Method method : modelClass.getDeclaredMethods()) {
            Class<?> type = method.getReturnType();
            Config con = method.getAnnotation(Config.class);
            if (con == null ) continue;

            map.put(con.order(), method);
            List<String> selection = null;
            String unit;
            String rangeUnit;
            unit = con.unit().isEmpty() ? null : con.unit();
            String values = con.values();
            if ( !values.isEmpty() ) {
                selection = new ArrayList<>();
                Collections.addAll(selection, values.split(","));
            }
            rangeUnit = con.rangeUnit().isEmpty() ? "X" : con.rangeUnit();

            Object defaultValue = null;
            if ( type.isPrimitive() ) {
                if ( boolean.class.isAssignableFrom(type)) {
                    defaultValue = false;
                } else if (double.class.isAssignableFrom(type)) {
                    defaultValue = 0.0;
                } else if (int.class.isAssignableFrom(type)){
                    defaultValue = 0;
                }
            } else {
                if ( Boolean.class.isAssignableFrom(type)) {
                    defaultValue = false;
                } else if (AbstractDistribution.class.isAssignableFrom( type )) {
                    defaultValue = new ConstantDistributionImpl(33);
                } else if (String.class.isAssignableFrom(type)) {
                    if ( selection == null ) {
                        defaultValue = "";
                    } else {
                        defaultValue = selection.get(0);
                    }
                } else if (EmissionMask.class.isAssignableFrom(type)) {
                    EmissionMaskImpl mask = new EmissionMaskImpl();
                    mask.addPoint(new Point2D(0,0), 0);
                    defaultValue = mask;
                } else if (BlockingMask.class.isAssignableFrom(type)) {
                    defaultValue = new BlockingMaskImpl(0);
                } else if (Function.class.isAssignableFrom(type)) {
                    defaultValue = handleFunction(new DiscreteFunction(0), method);
                } else if (OptionalFunction.class.isAssignableFrom(type)) {
                    defaultValue = new OptionalFunction(true, handleFunction(new DiscreteFunction(0), method));
                } else if (OptionalDoubleValue.class.isAssignableFrom(type)){
                    defaultValue = new OptionalDoubleValue(true, 0.0);
                } else if (Distribution.class.isAssignableFrom(type)) {
                    defaultValue = new ConstantDistributionImpl(0);
                } else if ( Double.class.isAssignableFrom(type)) {
                    defaultValue = 0.0;
                } else if (Integer.class.isAssignableFrom(type)) {
                    defaultValue = 0;
                } else if (PropagationModel.class.isAssignableFrom(type)) {
                    defaultValue = propagation().getHataSE21();
                } else if ( AntennaGain.class.isAssignableFrom(type)) {
                    defaultValue = antennaGain().getPeakGainAntenna();
                } else if ( OptionalMaskFunction.class.isAssignableFrom(type)) {
                    defaultValue = new OptionalMaskFunction(true, new EmissionMaskImpl());
                } else if ( OptionalDistribution.class.isAssignableFrom(type)) {
                    defaultValue = new OptionalDistribution(false, Factory.distributionFactory().getConstantDistribution(0.0));
                } else if ( List.class.isAssignableFrom( type)) {
                    defaultValue = new ArrayList<Object>();
                } else if (CDMALinkLevelData.class.isAssignableFrom(type)) {
                    if ( Model.getInstance().getLibrary() != null ) {
                        List<CDMALinkLevelData> llds = Model.getInstance().getLibrary().getCDMALinkLevelData();
                        CDMALinkLevelData some = null;
                        for (CDMALinkLevelData lld : llds) {
                            if ( lld.getLinkType() == CDMALinkLevelData.LinkType.DOWNLINK && con.downLink()) {
                                some = lld;
                                if ( lld.getFrequency() == 850.0) {
                                    defaultValue = lld;
                                }

                            }
                            if ( lld.getLinkType() == CDMALinkLevelData.LinkType.UPLINK && !con.downLink()) {
                                some = lld;
                                if ( lld.getFrequency() == 835.0 ) {
                                    defaultValue = lld;
                                }
                            }
                        }
                        if ( defaultValue == null ) {
                            defaultValue = some;
                        }
                    }
                    if ( defaultValue == null ) {
                        defaultValue = new CDMALinkLevelData();
                    }
                } else if (CalculatedValue.class.isAssignableFrom(type)) {
                    defaultValue = new CalculatedValue(null);
                } else if ( Enum.class.isAssignableFrom( type)) {
                    Object[] enumConstants = type.getEnumConstants();
                    defaultValue = enumConstants[0];
                }

            }

            if ( defaultValue == null ) {
                throw new RuntimeException("No default value for type: " + type);
            }

            if ( defaultValues.containsKey( method.getName() ) ) {
                unorderedValues.put(method, defaultValues.get(method.getName()));
            } else {
                unorderedValues.put(method, defaultValue);
            }
        }

        for (Method method : map.values()) {
            instance.put( method, unorderedValues.get(method));
        }

        return instance;
    }

    private static DiscreteFunction handleFunction(DiscreteFunction function, Method method) {
        ensureDefault(function, method, Horizontal.class, 0, 360);
        ensureDefault(function, method, Vertical.class, -90, 90);
        ensureDefault(function, method, Spherical.class, 0, 180);
        return function;
    }

    private static void ensureDefault(DiscreteFunction function, Method method, Class<? extends Annotation> ann, int min, int max) {
        Annotation annotation = method.getAnnotation(ann);
        if ( annotation != null ) {
            function.setPoints(Arrays.asList(new Point2D(min, 0.0), new Point2D(max, 0.0)));
        }
    }


    public static <T> T copy( Class<T> clazz, T model ) {
        return copyInternal(clazz, model, new LinkedHashMap<Method, Object>());
    }

    private static <T> T copyInternal( Class<T> clazz, T model, Map<Method, Object> values ) {
        for (Method m : clazz.getDeclaredMethods()) {
            try {
                Object value = m.invoke(model);
                if ( value instanceof PluginConfiguration ) {
                    values.put(m, ((PluginConfiguration) value).deepClone());
                } else {
                    values.put(m, m.invoke(model));
                }
            } catch (Exception e) {
                // log error
            }
        }
        return proxy(clazz, values);
    }

    public static <T> T proxy(Class<T> clazz, final Map<Method, Object> values) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new SeamcatInvocationHandler(clazz, values));
    }

    public static SeamcatInvocationHandler getHandler(Object object ) {
        if ( object instanceof Proxy ) {
            InvocationHandler handler = Proxy.getInvocationHandler(object);
            if ( handler instanceof SeamcatInvocationHandler ) {
                return (SeamcatInvocationHandler) handler;
            }
        }

        return null;
    }

    public static void parameterCallback( Class<?> clazz, Object instance, Class<? extends Annotation> annotation, ParameterCallback cb ) {
        for (Method method : clazz.getDeclaredMethods()) {
            Annotation ann = method.getAnnotation(annotation);
            if ( ann != null ) {
                try {
                    cb.handle(method.getName(), method.invoke(instance));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static <T> T copy( Class<T> clazz, T model, String methodName, Object value) {
        LinkedHashMap<Method, Object> values = new LinkedHashMap<Method, Object>();
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        T t = copyInternal(clazz, model, values);
        if ( method != null && values.containsKey( method )) {
            values.put( method, value );
        }
        return t;
    }

    public static <T extends LibraryItem> T newComposite(Class<T> compositeClass, String name) {
        Map<String, Object> defaults = staticDefaultValues(compositeClass);

        Map<Method, Object> values = new LinkedHashMap<Method, Object>();
        for (Method method : compositeClass.getDeclaredMethods()) {
            if (Description.class.isAssignableFrom( method.getReturnType())) {
                Description prototype = prototype(Description.class);
                when(prototype.name()).thenReturn(name);
                values.put( method, build( prototype ));
                continue;
            }

            if ( defaults.containsKey(method.getName())) {
                values.put(method, defaults.get( method.getName()));
                continue;
            }

            if ( method.getAnnotation(UITab.class) != null ) {
                values.put( method, newComposite( method.getReturnType() ));
            } else if ( method.getAnnotation(UIPosition.class) != null ) {
                Class<?> type = method.getReturnType();
                if ( AntennaGain.class.isAssignableFrom( type)) {
                    values.put( method, antennaGain().getPeakGainAntenna());
                } else if ( PropagationModel.class.isAssignableFrom(type)) {
                    values.put(method, propagation().getHataSE21());
                } else if (CoverageRadius.class.isAssignableFrom(type)) {
                    values.put( method, CoverageRadiusConfiguration.coverage(UserDefinedCoverageRadius.class));
                } else {
                    values.put( method, newInstance( method.getReturnType()) );
                }
            }

        }
        return proxy(compositeClass, values);
    }


    public static <T> T newComposite(Class<T> compositeClass) {
        // UITab => recurse
        // UIPosition => Proxy
        Map<Method, Object> values = new LinkedHashMap<Method, Object>();
        for (Method method : compositeClass.getDeclaredMethods()) {
            if ( method.getAnnotation(UITab.class) != null ) {
                values.put( method, newComposite( method.getReturnType() ));
            } else if ( method.getAnnotation(UIPosition.class) != null ) {
                Class<?> type = method.getReturnType();
                if ( AntennaGain.class.isAssignableFrom( type)) {
                    values.put( method, antennaGain().getPeakGainAntenna());
                } else if ( PropagationModel.class.isAssignableFrom(type)) {
                    values.put( method, propagation().getHataSE21());
                } else if (CoverageRadius.class.isAssignableFrom(type)) {
                    values.put( method, CoverageRadiusConfiguration.coverage(UserDefinedCoverageRadius.class));
                } else {
                    values.put( method, newInstance( method.getReturnType()) );
                }
            }
        }
        return proxy(compositeClass, values);
    }

    public static <T> T deepCloneComposite(Class<T> compositeClass, T composite) {
        return deepCloneComposite(compositeClass, composite, null, null);
    }

    public static <T> T deepCloneComposite(Class<T> compositeClass, T composite, Class<?> returnType, Object returnValue) {
        Map<Method, Object> values = new LinkedHashMap<Method, Object>();
        for (Method method : Cache.ordered(compositeClass)) {
            if ( method.getReturnType() == returnType ) {
                values.put( method, returnValue );
                continue;
            }
            Object invoke = null;
            try {
                invoke = method.invoke(composite);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ( method.getAnnotation(UITab.class) != null ) {
                values.put( method, deepCloneComposite((Class<Object>) method.getReturnType(), invoke));
            } else if ( method.getAnnotation(UIPosition.class) != null ) {
                Class<?> type = method.getReturnType();
                if ( invoke instanceof PluginConfiguration ) {
                    values.put( method, ((PluginConfiguration) invoke).deepClone());
                } else {
                    values.put( method, invoke );
                }
            }
        }
        return proxy(compositeClass, values);
    }

    public interface ParameterCallback{
        void handle(String name, Object parameter);
    }

    public static <T> T classInstance(Class<T> clazz ) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating plugin", e);
        }
    }
}
