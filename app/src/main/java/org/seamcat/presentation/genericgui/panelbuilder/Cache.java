package org.seamcat.presentation.genericgui.panelbuilder;

import org.seamcat.model.plugin.Config;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public class Cache {

    private static Map<Class<?>, List<Method>> cache = new HashMap<>();

    public static List<Method> ordered( Class<?> compositeClass) {

        if ( cache.containsKey( compositeClass )) {
            return cache.get( compositeClass );
        }

        SortedMap<Integer, Method> positions = new TreeMap<>();
        SortedMap<Integer, Method> tabs = new TreeMap<>();

        for (Method method : compositeClass.getDeclaredMethods()) {

            UIPosition panel = method.getAnnotation(UIPosition.class);
            if ( panel != null ) {
                positions.put( panel.col()*10 + panel.row(), method);
            }

            UITab tab = method.getAnnotation(UITab.class);
            if ( tab != null ) {
                tabs.put(tab.order(), method);
            }
        }

        List<Method> ordered = new ArrayList<>( positions.values());
        ordered.addAll( tabs.values());
        cache.put( compositeClass, ordered );

        return ordered;
    }

    public static List<Method> orderedConfig( Class<?> panelClass ) {
        if ( cache.containsKey( panelClass )) {
            return cache.get( panelClass );
        }

        SortedMap<Integer, Method> methods = new TreeMap<>();
        for (Method method : panelClass.getDeclaredMethods()) {
            Config config = method.getAnnotation(Config.class);
            if ( config != null ) {
                methods.put( config.order(), method);
            }
        }

        List<Method> ordered = new ArrayList<>(methods.values());
        cache.put( panelClass, ordered );

        return ordered;
    }


    public static <T extends Annotation> LinkedHashMap<Method, T> ordered( Class<T> annClass, Class<?> clazz, Order<T> order ) {

        Map<Method, T> annotations = new HashMap<>();
        SortedMap<Integer, Method> sorted = new TreeMap<>();

        for (Method method : clazz.getDeclaredMethods()) {

            T t = method.getAnnotation(annClass);
            if ( t != null ) {
                sorted.put( order.getOrder(t), method);
                annotations.put(method, t);
            }
        }
        LinkedHashMap<Method, T> result = new LinkedHashMap<>();
        for (Method method : sorted.values()) {
            result.put( method, annotations.get(method));
        }

        return result;
    }


    public interface Order<T> {
        int getOrder(T t);
    }

}
