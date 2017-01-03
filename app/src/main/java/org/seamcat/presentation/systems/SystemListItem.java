package org.seamcat.presentation.systems;

import org.seamcat.model.IdElement;
import org.seamcat.model.systems.SystemModel;

public class SystemListItem {

    private IdElement<SystemModel> element;

    public SystemListItem( IdElement<SystemModel> element ) {
        this.element = element;
    }

    public IdElement<SystemModel> getElement() {
        return element;
    }

    @Override
    public String toString() {
        return element.getElement().description().name();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SystemListItem that = (SystemListItem) o;

        return element.getId().equals(that.element.getId());

    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }
}
