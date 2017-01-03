package org.seamcat.presentation.builder;

import org.apache.log4j.Logger;
import org.seamcat.presentation.menu.Menus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class PanelBuilder {

    private JPanel panel;
    private static final Logger LOG = Logger.getLogger(PanelBuilder.class);

    public static PanelBuilder panel() {
        PanelBuilder builder = new PanelBuilder();
        builder.panel = new JPanel();
        return builder;
    }
    public static PanelBuilder panel( LayoutManager layout ) {
        PanelBuilder builder = new PanelBuilder();
        builder.panel = new JPanel( layout );
        return builder;
    }
    public static JButton buildButton( String name, Object instance, String method ) {
        return addAction(new JButton(name), instance, method );
    }
    public static JButton addAction( JButton button, Object instance, String method ) {
        Class<?> aClass = instance.getClass();
        findAnnotation(instance, method, button, aClass);
        return button;
    }
    public static JButton buildButton( String name, Class<?> clazz ) {
        JButton button = new JButton(name);
        button.addActionListener(Menus.action( clazz) );
        return button;
    }
    public PanelBuilder button( String name, Class<?> clazz )  {
        panel.add( buildButton( name, clazz ));
        return this;
    }

    public PanelBuilder button(String name, Object instance, String method ) {
        panel.add( buildButton(name, instance, method ) );
        return this;
    }

    private static boolean findAnnotation(final Object instance, String method, JButton button, Class<?> aClass) {
        for (final Method m : aClass.getDeclaredMethods()) {
            for (Annotation annotation : m.getDeclaredAnnotations()) {
                if ( AsActionListener.class.isAssignableFrom( annotation.getClass() ) ) {
                    if (((AsActionListener)annotation).value().equals(method)) {
                        button.addActionListener( new ActionListener() {
                            public void actionPerformed(ActionEvent actionEvent) {
                                m.setAccessible( true );
                                try {
                                    if ( m.getParameterTypes() != null && m.getParameterTypes().length == 1 ){
                                        m.invoke( instance, actionEvent);
                                    } else {
                                        m.invoke( instance );
                                    }
                                } catch (Exception e ) {
                                    LOG.error("error invoking action listener", e.getCause());
                                }
                            }
                        });
                        return true;
                    }
                }
            }
        }
        Class<?> decl = aClass.getSuperclass();
        return decl != Object.class && findAnnotation(instance, method, button, decl);
    }

    public PanelBuilder add( Component component ) {
        panel.add( component );
        return this;
    }

    public JPanel get() {
        return panel;
    }
}
