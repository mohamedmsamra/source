package org.seamcat.model;

import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.plugin.BuiltInPlugins;
import org.seamcat.plugin.JarConfigurationModel;
import org.seamcat.plugin.PluginClass;
import org.seamcat.plugin.PluginLocation;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global container of all plugin jar files in the system
 */
public class PluginJarFiles {

    private final static Map<String, JarConfigurationModel> pluginJarFiles = new LinkedHashMap<String, JarConfigurationModel>();
    private final static Map<Class, Map<Method, Object>> defaultValues = new HashMap<Class, Map<Method, Object>>();

    public static void addJarConfiguration(JarConfigurationModel model) {
        if ( !contains(model)) {
            pluginJarFiles.put(model.getHash(), model);
            // setting default values must be done in two steps
            // step 1: generate default values for the types
            // step 2: overlay with static field custom values
            // This is to avoid referencing un-instantiated classes
            // during instantiation
            for (PluginClass aClass : model.getPluginClasses()) {
                defaultValues.put( aClass.getModelClass(), ProxyHelper.trueDefaultValues(aClass.getModelClass()));
            }
            for (PluginClass aClass : model.getPluginClasses()) {
                Map<Method, Object> map = ProxyHelper.defaultValues(aClass.getModelClass());
                Map<Method, Object> values = defaultValues.get(aClass.getModelClass());

                for (Map.Entry<Method, Object> entry : values.entrySet()) {
                    if ( map.containsKey( entry.getKey() )) {
                        entry.setValue( map.get( entry.getKey() ));
                    }
                }
            }
        }
    }

    public static JarConfigurationModel getJarConfiguration( String id ) {
        if ( id.equals( PluginLocation.BUILTIN)) {
            return new BuiltInPlugins();
        }
        return pluginJarFiles.get(id);
    }

    private static boolean contains( JarConfigurationModel model ) {
        return pluginJarFiles.values().contains( model );
    }

    public static Map<Method, Object> getDefaultValues(Class<?> clazz ) {
        if ( !defaultValues.containsKey(clazz)) {
            // it is built in
            return ProxyHelper.defaultValues(clazz);
        }
        return new LinkedHashMap<Method, Object>(defaultValues.get(clazz));
    }

    public static PluginLocation findLocation( Class<?> clazz ) {
        for (JarConfigurationModel jar : pluginJarFiles.values()) {
            try {
                PluginClass aClass = jar.getPluginClass(clazz.getName());
                if ( aClass.getModelClass().getClassLoader() == clazz.getClassLoader() ) {
                    // class loader and class name match
                    return aClass.getPluginLocation();
                }
            } catch (RuntimeException e ) {
                // not found. Ignore
            }
        }
        // try built in:
        PluginClass aClass = new BuiltInPlugins().getPluginClass(clazz.getName());
        if ( aClass.getModelClass().getClassLoader() == clazz.getClassLoader()) {
            return aClass.getPluginLocation();
        }

        throw new RuntimeException("Could not find class location for: " + clazz);
    }

}
