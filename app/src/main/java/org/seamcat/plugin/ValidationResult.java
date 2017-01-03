package org.seamcat.plugin;

import java.lang.reflect.Method;
import java.util.*;

public class ValidationResult {

    private Stack<String> messages;
    private Map<Method, String> fieldError;

    public ValidationResult() {
        messages = new Stack<String>();
        fieldError = new HashMap<Method, String>();
    }

    public void addMessage( String message ) {
        messages.push( message );
    }

    public void addMethod( Method method) {
        fieldError.put(method, messages.pop());
    }

    public void clear() {
        messages.clear();
        fieldError.clear();
    }

    public List<String> getModelErrors() {
        return new ArrayList<String>( messages );
    }

    public Map<Method, String> getFieldError()  {
        return fieldError;
    }
}
