package org.seamcat.plugin;

import org.seamcat.function.MutableLibraryItem;
import org.seamcat.model.PluginJarFiles;
import org.seamcat.model.Scenario;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.Plugin;
import org.seamcat.model.scenariocheck.ValidatorImpl;
import org.seamcat.model.types.Configuration;
import org.seamcat.model.types.result.DescriptionImpl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

public abstract class PluginConfiguration<P extends Plugin<Model>, Model> extends MutableLibraryItem {

    private PluginLocation location;
    private P plugin;
    private Model model;
    private Class<Model> clazz;
    private ValidationResult validationResult;
    private String notes;

    PluginConfiguration( PluginLocation location, P plugin, Model model ) {
        validationResult = new ValidationResult();
        this.location = location;
        this.plugin = plugin;
        setDescription( plugin.description() );
        this.clazz = (Class<Model>) ((ParameterizedType)plugin.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
        if ( model == null ) {
            this.model = ProxyHelper.proxy(clazz, PluginJarFiles.getDefaultValues(clazz));
        } else {
            this.model = model;
        }
        notes = "";
    }

    public Model getModel() {
        return model;
    }

    public PluginConfiguration<P,Model> setModel( Model model ){
        this.model = model;
        return this;
    }

    public Class<Model> getModelClass() {
        return clazz;
    }

    public P getPlugin() {
        return plugin;
    }

    public String toString() {
        return description().name();
    }

    public void setName(String name) {
        setDescription( new DescriptionImpl(name, description().description()));
    }

    public void setNotes( String notes ) {
        this.notes = notes;
    }

    public String getNotes() {
        return notes;
    }

    public PluginLocation getLocation() {
        return location;
    }

    public boolean isBuiltIn() {
        return location.isBuiltIn();
    }

    public abstract PluginConfiguration<P,Model> deepClone();

    public abstract PluginConfiguration<P,Model> instance(Model model);

    public Class<P> getPluginClass() {
        return (Class<P>) plugin.getClass();
    }

    public abstract Class<? extends Configuration> getTypeClass();

    public Model getConfiguration() {
        return getModel();
    }

    public void addMessageError( String message ) {
        validationResult.addMessage( message );
    }

    public void addMethodError(Method method) {
        validationResult.addMethod(method);
    }

    public ValidationResult consistencyCheck(Scenario scenario, List<Object> path) {
        validationResult.clear();
        ValidatorImpl<Model> validator = new ValidatorImpl<Model>(getModelClass(), this);
        getPlugin().consistencyCheck( scenario, path, getModel(), validator);
        return validationResult;
    }
}
