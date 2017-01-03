package org.seamcat.objectutils;

import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.genericgui.panelbuilder.Cache;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class ModelConverter {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    public static LinkedHashMap<String, LinkedHashMap<String, Object>> groups( Class<?> clazz, Object composite ) {
        LinkedHashMap<String, LinkedHashMap<String, Object>> groups = new LinkedHashMap<>();
        try {
            for (Method method : Cache.ordered(clazz)) {

                UIPosition position = method.getAnnotation(UIPosition.class);
                if (position != null) {
                    final LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
                    groups.put( position.name(), values);

                    Object invoke = method.invoke(composite);
                    if ( invoke instanceof PluginConfiguration) {
                        add((PluginConfiguration) invoke, values);
                    } else {
                        add(method.getReturnType(), invoke, values);
                    }
                }
                UITab tab = method.getAnnotation(UITab.class);
                if ( tab != null ) {
                    groups.put( tab.value(), new LinkedHashMap<String, Object>() );
                    groups.putAll( groups(method.getReturnType(), method.invoke(composite)));
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return groups;
    }

    private static void add( PluginConfiguration configuration, LinkedHashMap<String, Object> values ) {
        configuration.getModel();
        values.put( "Plugin name", configuration.description().name());
        for (Method method : configuration.getModelClass().getDeclaredMethods()) {
            Config annotation = method.getAnnotation(Config.class);
            if ( annotation == null ) continue;
            try {
                Object invoke = method.invoke(configuration.getModel());
                values.put( annotation.name(), invoke==null?"": invoke);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static LinkedHashMap<String, Object> add( Class<?> panelClass, Object instance ) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        add( panelClass, instance, result);
        return result;
    }

    private static void add( Class<?> panelClass, Object instance, LinkedHashMap<String, Object> values ) {
        LinkedHashMap<Method, Object> defaults = ProxyHelper.defaultValues(panelClass);

        for (Method method : defaults.keySet()) {
            String name;
            Config annotation = method.getAnnotation(Config.class);
            if ( STRINGLIST.containsKey( annotation.name() ) ) {
                name = STRINGLIST.getString(annotation.name());
            } else {
                name = annotation.name();
            }
            try {
                Object invoke = method.invoke(instance);
                values.put( name, invoke == null ? "" :invoke);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
