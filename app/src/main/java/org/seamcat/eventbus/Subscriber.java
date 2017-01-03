package org.seamcat.eventbus;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class Subscriber {

    private static Set<Component> subscriptions = new HashSet<Component>();

    public static void subscribe( Component component ) {
        EventBusFactory.getEventBus().subscribe( component );
        subscriptions.add( component );
    }

    public static void unSubscribeDeep( Container container ) {
        if ( container == null ) return;
        unSubscribe(container);
        for (Component component : container.getComponents()) {
            if ( component instanceof Container ) {
                unSubscribeDeep((Container) component);
            } else {
                unSubscribe(component);
            }
        }
    }

    private static void unSubscribe( Component component ) {
        if ( subscriptions.contains( component ) ) {
            subscriptions.remove(component);
            EventBusFactory.getEventBus().unsubscribe( component );
        }
    }
}
