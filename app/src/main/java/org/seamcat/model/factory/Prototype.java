package org.seamcat.model.factory;

// marker interface to detect a prototype instance
public interface Prototype<T> {

    PrototypeInvocationHandler<T> getHandler();
}
