package org.seamcat.model.plugin;

import org.seamcat.model.functions.Function;

public class OptionalFunction {
    private boolean relevant;
    private Function function;

    public OptionalFunction(boolean relevant, Function function) {
        this.relevant = relevant;
        this.function = function;
    }


    public boolean isRelevant() {
        return relevant;
    }

    public Function getFunction() {
        return function;
    }

    @Override
    public String toString() {
        return function.toString() + " enabled " + relevant;
    }
}
