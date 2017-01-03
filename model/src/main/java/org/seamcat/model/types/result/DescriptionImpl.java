package org.seamcat.model.types.result;

import org.seamcat.model.types.Description;

public class DescriptionImpl implements Description {
    private String name,description;
    public DescriptionImpl(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }
}
