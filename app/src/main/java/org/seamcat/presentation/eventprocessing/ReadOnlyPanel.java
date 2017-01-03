package org.seamcat.presentation.eventprocessing;

import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.Config;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.LabeledPairLayout;
import org.seamcat.presentation.genericgui.panelbuilder.Cache;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

public class ReadOnlyPanel {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    public static void addReadOnly( JPanel panel, PluginConfiguration configuration ) {
        configuration.getModel();
        panel.add(new JLabel("Plugin name"), LabeledPairLayout.LABEL);
        panel.add(new JLabel(configuration.description().name()), LabeledPairLayout.FIELD);
        for (Method method : configuration.getModelClass().getDeclaredMethods()) {
            Config annotation = method.getAnnotation(Config.class);
            if ( annotation == null ) continue;
            panel.add(new JLabel(annotation.name()), LabeledPairLayout.LABEL);
            try {
                Object invoke = method.invoke(configuration.getModel());
                panel.add(new JLabel( invoke == null ? "" : invoke.toString() ), LabeledPairLayout.FIELD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void readOnly( JPanel panel, Class<?> panelClass, Object instance ) {
        LinkedHashMap<Method, Object> values = ProxyHelper.defaultValues(panelClass);

        for (Method method : values.keySet()) {
            Config annotation = method.getAnnotation(Config.class);
            if ( STRINGLIST.containsKey( annotation.name() ) ) {
                panel.add(new JLabel(STRINGLIST.getString(annotation.name())), LabeledPairLayout.LABEL);
            } else {
                panel.add(new JLabel( annotation.name()), LabeledPairLayout.LABEL);
            }
            try {
                Object invoke = method.invoke(instance);
                panel.add(new JLabel(invoke == null ? "" : invoke.toString()), LabeledPairLayout.FIELD);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void compositeReadOnly( JPanel panel, Class<?> clazz, Object composite ) {
        try {
            for (Method method : Cache.ordered(clazz)) {

                UIPosition position = method.getAnnotation(UIPosition.class);
                if (position != null) {
                    panel.add( new JLabel("<html><b>"+position.name()+"</b></html>"), LabeledPairLayout.LABEL);
                    panel.add( new JLabel(""), LabeledPairLayout.FIELD);
                    Object invoke = method.invoke(composite);
                    if ( invoke instanceof PluginConfiguration ) {
                        addReadOnly(panel, (PluginConfiguration) invoke);
                    } else {
                        readOnly(panel, method.getReturnType(), invoke);
                    }
                }
                UITab tab = method.getAnnotation(UITab.class);
                if ( tab != null ) {
                    panel.add(new JLabel("<html><b>"+tab.value()+"</b></html>"), LabeledPairLayout.LABEL);
                    panel.add( new JLabel(""), LabeledPairLayout.FIELD);
                    compositeReadOnly( panel, method.getReturnType(), method.invoke(composite));
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
