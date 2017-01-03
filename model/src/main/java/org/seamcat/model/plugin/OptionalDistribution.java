package org.seamcat.model.plugin;

import org.seamcat.model.distributions.Distribution;

public class OptionalDistribution {

    private boolean relevant;
    private Distribution value;

    public OptionalDistribution(boolean relevant, Distribution value) {
        this.relevant = relevant;
        this.value = value;
    }


    public boolean isRelevant() {
        return relevant;
    }

    public Distribution getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString() + " enabled " + relevant;
    }
}
