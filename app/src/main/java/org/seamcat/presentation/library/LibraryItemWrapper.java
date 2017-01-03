package org.seamcat.presentation.library;

import org.seamcat.model.types.LibraryItem;

public class LibraryItemWrapper<T extends LibraryItem> {

    private int index;
    private T item;

    public LibraryItemWrapper(int index, T item) {
        this.index = index;
        this.item = item;
    }

    public T getItem() {
        return item;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return item.description().name();
    }
}
