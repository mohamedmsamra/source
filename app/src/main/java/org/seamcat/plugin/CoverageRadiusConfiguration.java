package org.seamcat.plugin;

import org.seamcat.exception.SimulationInvalidException;
import org.seamcat.model.PluginJarFiles;
import org.seamcat.model.generic.GenericSystem;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.coverageradius.CoverageRadiusPlugin;
import org.seamcat.model.types.Configuration;
import org.seamcat.model.types.CoverageRadius;

public class CoverageRadiusConfiguration<T> extends PluginConfiguration<CoverageRadiusPlugin<T>, T> implements CoverageRadius<T> {

    @Override
    public CoverageRadiusConfiguration<T> instance(T t) {
        return coverage(getPluginClass(), t);
    }

    public static <T> CoverageRadiusConfiguration<T> coverage(Class<? extends CoverageRadiusPlugin<T>> clazz,  T model) {
        return new CoverageRadiusConfiguration<T>(clazz, model);
    }

    public static <T> CoverageRadiusConfiguration<T> coverage(Class<? extends CoverageRadiusPlugin<T>> clazz ) {
        return coverage(clazz, null );
    }

    CoverageRadiusConfiguration(Class<? extends CoverageRadiusPlugin<T>> clazz, T model) {
        super(PluginJarFiles.findLocation(clazz), ProxyHelper.classInstance(clazz), model);
    }

    @Override
    public CoverageRadiusConfiguration<T> deepClone() {
        CoverageRadiusConfiguration<T> clone = instance(ProxyHelper.copy(getModelClass(), getModel()));
        clone.setDescription( description() );
        clone.setNotes( getNotes() );
        return clone;
    }

    @Override
    public Class<? extends Configuration> getTypeClass() {
        return CoverageRadius.class;
    }

    @Override
    public double evaluate(GenericSystem system) {
        try {
            return getPlugin().evaluate(system, getModel());
        } catch (RuntimeException e ) {
            throw new SimulationInvalidException("Propagation model "+getPlugin().description().name()+" failed.", e);
        }
    }
}
