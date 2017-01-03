package org.seamcat.model.factory;

import org.seamcat.model.plugin.builder.Builder;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DeprecatedBuilderImpl<T> implements Builder<T> {

    private final Class<T> clazz;
    private Object value;
    private final T prototype;
    private PrototypeInvocationHandler<T> handler;

    public DeprecatedBuilderImpl(Class<T> clazz, T t ) {
        this.clazz = clazz;
        prototype = getPrototype(t);

    }

    public DeprecatedBuilderImpl(Class<T> clazz) {
        this.clazz = clazz;
        prototype = getPrototype(null);
    }

    private T getPrototype(T t) {
        handler = new PrototypeInvocationHandler<T>(clazz, t) {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                Object returnValue = super.invoke(o, method, objects);
                BuildersImpl.values.get().put(method, value);
                return returnValue;
            }
        };
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz, Prototype.class}, handler);
    }

    @Override
    public T returnValue(Object value) {
        this.value = value;
        return prototype;
    }

    @Override
    public T build() {
        return handler.build();
    }
}
