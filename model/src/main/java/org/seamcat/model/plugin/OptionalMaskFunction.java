package org.seamcat.model.plugin;

import org.seamcat.model.functions.MaskFunction;

public class OptionalMaskFunction {

    private boolean relevant;
    private MaskFunction maskFunction;

    public OptionalMaskFunction(boolean relevant, MaskFunction maskFunction) {
        this.relevant = relevant;
        this.maskFunction = maskFunction;
    }


    public boolean isRelevant() {
        return relevant;
    }

    public MaskFunction getMaskFunction() {
        return maskFunction;
    }

    @Override
    public String toString() {
        return maskFunction.toString() + " enabled " + relevant;
    }
}
