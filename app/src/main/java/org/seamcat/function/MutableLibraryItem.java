package org.seamcat.function;

import org.seamcat.model.types.Description;
import org.seamcat.model.types.LibraryItem;

public class MutableLibraryItem implements LibraryItem {

    private Description description;

    @Override
    public Description description() {
        return description;
    }

    public void setDescription( Description description ) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description.name();
    }
}
