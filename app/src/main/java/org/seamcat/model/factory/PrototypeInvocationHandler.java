package org.seamcat.model.factory;

import org.seamcat.model.PluginJarFiles;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.generic.SeamcatInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class PrototypeInvocationHandler<T> implements InvocationHandler {

    private final Map<Method, Object> values;
    private final Class<T> clazz;

    private static boolean DEFAULT_BOOLEAN;
    private static byte DEFAULT_BYTE;
    private static short DEFAULT_SHORT;
    private static int DEFAULT_INT;
    private static long DEFAULT_LONG;
    private static float DEFAULT_FLOAT;
    private static double DEFAULT_DOUBLE;

    PrototypeInvocationHandler(Class<T> clazz, T t) {
        this.clazz = clazz;
        if ( t == null ) {
            values = PluginJarFiles.getDefaultValues(clazz);
        } else {
            values = getHandler(t).getValues();
        }
    }

    private SeamcatInvocationHandler getHandler(T t) {
        SeamcatInvocationHandler handler = ProxyHelper.getHandler(t);
        if ( handler != null ) return handler;

        throw new IllegalArgumentException("Instance must be of type Seamcat proxy");
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if ( method.getReturnType().isAssignableFrom( PrototypeInvocationHandler.class) ) {
            return this;
        }
        BuildersImpl.method.set( method );
        BuildersImpl.values.set( values );
        Class<?> returnType = method.getReturnType();
        if ( returnType.isPrimitive() ) {
            if (returnType.equals(boolean.class)) return DEFAULT_BOOLEAN;
            if (returnType.equals(byte.class)) return DEFAULT_BYTE;
            if (returnType.equals(short.class)) return DEFAULT_SHORT;
            if (returnType.equals(int.class)) return DEFAULT_INT;
            if (returnType.equals(long.class)) return DEFAULT_LONG;
            if (returnType.equals(float.class)) return DEFAULT_FLOAT;
            if (returnType.equals(double.class)) return DEFAULT_DOUBLE;
        }
        return null;
    }

    protected T build() {
        return ProxyHelper.proxy(clazz, values);
    }
}
