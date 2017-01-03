package org.seamcat.model.factory;

import org.seamcat.model.plugin.builder.Builder;
import org.seamcat.model.plugin.builder.Returner;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class BuildersImpl implements Factory.Builders {

    protected static ThreadLocal<Method> method = new ThreadLocal<>();
    protected static ThreadLocal<Map<Method, Object>> values = new ThreadLocal<>();

    @Override
    public <T> T prototype(Class<T> clazz, T t) {
        PrototypeInvocationHandler handler = new PrototypeInvocationHandler(clazz,t);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz, Prototype.class}, handler);
    }

    @Override
    public <T> T prototype(final Class<T> clazz) {
        return prototype(clazz, null);
    }

    @Override
    public <V> Returner<V> when(V value) {
        final Map<Method, Object> values = BuildersImpl.values.get();
        final Method method = BuildersImpl.method.get();
        return new Returner<V>() {
            @Override
            public void thenReturn(V value) {
                values.put( method, value);
            }
        };
    }

    @Override
    public <T> T build(T prototype) {
        if ( prototype instanceof Prototype ) {
            PrototypeInvocationHandler<T> handler = ((Prototype<T>) prototype).getHandler();
            return handler.build();
        }

        throw new RuntimeException("Not a prototype instance");
    }

    @Override
    public <T> Builder<T> createBuilder(Class<T> clazz) {
        return new DeprecatedBuilderImpl<T>(clazz);
    }

    @Override
    public <T> Builder<T> createBuilder(Class<T> clazz, T t) {
        return new DeprecatedBuilderImpl<T>(clazz, t);
    }
}
