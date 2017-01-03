package org.seamcat.plugin;

import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.plugin.antenna.AntennaGainPlugin;
import org.seamcat.model.plugin.coverageradius.CoverageRadiusPlugin;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;

import java.lang.reflect.ParameterizedType;

public class PluginClass {

    private Class<?> clazz;
    private JarConfigurationModel jar;

    public PluginClass( JarConfigurationModel jar, Class<?> clazz ) {
        this.jar = jar;
        this.clazz = clazz;
        if (!EventProcessingPlugin.class.isAssignableFrom(clazz) &&
                !PropagationModelPlugin.class.isAssignableFrom(clazz) &&
                !AntennaGainPlugin.class.isAssignableFrom(clazz) &&
                !CoverageRadiusPlugin.class.isAssignableFrom(clazz)) {
            throw new RuntimeException("Not a plugin type");
        }
    }
    public PluginConfiguration configuration() {
        return configuration(null);
    }

    public PluginLocation getPluginLocation() {
        return new PluginLocation(jar.getHash(), getClassName());
    }

    public PluginConfiguration configuration(Object model) {
        if ( EventProcessingPlugin.class.isAssignableFrom(clazz)) {
            return new EventProcessingConfiguration(clazz, model);
        } else if ( PropagationModelPlugin.class.isAssignableFrom(clazz)) {
            return SeamcatFactory.propagation().getByClass((Class<? extends PropagationModelPlugin<Object>>) clazz).setModel(model);
        } else if ( AntennaGainPlugin.class.isAssignableFrom(clazz)) {
            return SeamcatFactory.antennaGain().getByClass((Class<? extends AntennaGainPlugin<Object>>) clazz).setModel(model);
        } else if ( CoverageRadiusPlugin.class.isAssignableFrom(clazz)) {
            return new CoverageRadiusConfiguration(clazz, model);
        }

        throw new RuntimeException("Error: unknown type");
    }

    public String getClassName() {
        return clazz.getName();
    }

    public Class<?> getPluginClass() {
        return clazz;
    }

    public Class<?> getModelClass() {
        try {
            Object plugin = clazz.newInstance();
            return (Class<?>) ((ParameterizedType)plugin.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
