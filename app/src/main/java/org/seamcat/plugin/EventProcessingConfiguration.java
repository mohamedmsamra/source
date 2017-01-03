package org.seamcat.plugin;

import org.seamcat.model.PluginJarFiles;
import org.seamcat.model.Scenario;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.eventprocessing.EventProcessingPlugin;
import org.seamcat.model.plugin.eventprocessing.PanelDefinition;
import org.seamcat.model.simulation.result.EventResult;
import org.seamcat.model.types.Configuration;
import org.seamcat.model.types.EventProcessing;
import org.seamcat.model.types.result.ResultTypes;

import java.util.LinkedHashMap;

public class EventProcessingConfiguration<T> extends PluginConfiguration<EventProcessingPlugin<T>, T> implements EventProcessing<T> {

    @Override
    public EventProcessingConfiguration<T> instance(T t) {
        return event(getPluginClass(), t);
    }

    public static <T> EventProcessingConfiguration<T> event(Class<? extends EventProcessingPlugin<T>> clazz,  T model) {
        return new EventProcessingConfiguration<T>(clazz, model);
    }

    public static <T> EventProcessingConfiguration<T> event(Class<? extends EventProcessingPlugin<T>> clazz ) {
        return event(clazz, null );
    }
    private String id;

    private CustomUIState customUIState = new CustomUIState(new LinkedHashMap<PanelDefinition<?>, Object>());

    EventProcessingConfiguration(Class<? extends EventProcessingPlugin<T>> clazz, T model ) {
        super(PluginJarFiles.findLocation(clazz), ProxyHelper.classInstance(clazz), model);
    }

    @Override
    public EventProcessingConfiguration<T> deepClone() {
        JarConfigurationModel jar = PluginJarFiles.getJarConfiguration(getLocation().getJarId());
        EventProcessingConfiguration<T> configuration = (EventProcessingConfiguration<T>) jar.getPluginClass(getLocation().getClassName())
                .configuration(ProxyHelper.copy(getModelClass(), getModel()));
        configuration.setId( getId() );
        configuration.setDescription( description() );
        configuration.setNotes( getNotes() );
        return configuration;
    }

    @Override
    public Class<? extends Configuration> getTypeClass() {
        return EventProcessing.class;
    }

    @Override
    public ResultTypes evaluate(Scenario scenario, Iterable<EventResult> results) {
        ResultTypes resultTypes = getPlugin().evaluate(scenario, results, getModel());
        if ( resultTypes == null ) {
            resultTypes = new ResultTypes();
        }
        return resultTypes;
    }

    public CustomUIState getCustomUIState() {
        return customUIState;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
