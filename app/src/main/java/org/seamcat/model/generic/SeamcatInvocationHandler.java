package org.seamcat.model.generic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class SeamcatInvocationHandler implements InvocationHandler {

    private Class clazz;
    private Map<Method, Object> values;

    SeamcatInvocationHandler(Class clazz, Map<Method, Object> values) {
        this.clazz = clazz;
        this.values = values;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        if ( method.getName().equals("toString") /*&& method.getReturnType().isAssignableFrom(String.class)*/) {
            return clazz.getName();
        }
        return values.get(method);
    }

    public Map<Method, Object> getValues() {
        return new LinkedHashMap<>(values);
    }
}
