package org.seamcat.plugin;

import org.seamcat.exception.SimulationInvalidException;
import org.seamcat.model.PluginJarFiles;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.propagation.PropagationModelPlugin;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.types.Configuration;
import org.seamcat.model.types.PropagationModel;

import java.util.HashMap;
import java.util.Map;

public class PropagationModelConfiguration<T> extends PluginConfiguration<PropagationModelPlugin<T>, T> implements PropagationModel<T> {

    private final static Map<Class, Boolean> variations = new HashMap<>();
    public static void addVariations(Class<?> clazz, boolean variations ) {
        PropagationModelConfiguration.variations.put(clazz, variations);
    }

    @Override
    public PropagationModelConfiguration<T> instance(T t) {
        return SeamcatFactory.propagation().getByClass(getPluginClass()).setModel(t);
    }

    private boolean variationSelected;

    PropagationModelConfiguration(Class<? extends PropagationModelPlugin<T>> clazz, T model) {
        super(PluginJarFiles.findLocation(clazz), ProxyHelper.classInstance(clazz), model);
        if ( !variations.containsKey(getModelClass())) {
            variationSelected = true;
        } else {
            variationSelected = variations.get(getModelClass());
        }
    }

    @Override
    public Class<? extends Configuration> getTypeClass() {
        return PropagationModel.class;
    }

    @Override
    public PropagationModelConfiguration<T> deepClone() {
        PropagationModelConfiguration<T> clone = SeamcatFactory.propagation().getByClass(getPluginClass(), getModel(), isVariationSelected());
        clone.setDescription(description());
        clone.setNotes(getNotes());
        return clone;
    }

    @Override
    public double evaluate(LinkResult linkResult, boolean variation) {
        try{
            return getPlugin().evaluate(linkResult, variation, getModel());
        } catch (RuntimeException e) {
            throw new SimulationInvalidException("Propagation model "+getPlugin().description().name()+" failed.", e);
        }
    }

    @Override
    public double evaluate(LinkResult linkResult) {
        return evaluate(linkResult, isVariationSelected());
    }

    public boolean isVariationSelected() {
        return variationSelected;
    }

    public PropagationModelConfiguration<T> setVariationSelected( boolean variationSelected ) {
        this.variationSelected = variationSelected;
        return this;
    }

    @Override
    public PropagationModelConfiguration<T> setModel(T t) {
        super.setModel(t);
        return this;
    }
}
