package org.seamcat.model;

public class IdElement<T> {

    private String id;
    private T element;

    public IdElement(String id, T element) {
        this.id = id;
        this.element= element;
    }

    public String getId() {
        return id;
    }

    public T getElement() {
        return element;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IdElement idElement = (IdElement) o;

        if (!id.equals(idElement.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
