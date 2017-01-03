package org.seamcat.model.scenariocheck;

import org.seamcat.model.plugin.Validator;
import org.seamcat.plugin.PluginConfiguration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ValidatorImpl<T> implements Validator<T> {

    private T interceptProxy;
    private PluginConfiguration configuration;

    public ValidatorImpl(Class<T> clazz, final PluginConfiguration configuration ) {
        this.configuration = configuration;
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                configuration.addMethodError( method );
                if ( method.getReturnType().isPrimitive() ) {
                    if ( double.class.isAssignableFrom( method.getReturnType())) {
                        return 0.0;
                    } else if ( int.class.isAssignableFrom(method.getReturnType())) {
                        return 0;
                    } else if ( boolean.class.isAssignableFrom(method.getReturnType())) {
                        return false;
                    } else if ( long.class.isAssignableFrom(method.getReturnType()))  {
                        return 0L;
                    } else return null;
                } else {
                    return null;
                }
            }
        };
        interceptProxy = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }

    @Override
    public T error(String message) {
        configuration.addMessageError( message );
        return interceptProxy;
    }
}
