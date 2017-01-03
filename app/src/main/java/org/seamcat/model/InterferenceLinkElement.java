package org.seamcat.model;

import org.seamcat.model.workspace.InterferenceLinkUI;

import java.util.UUID;

public class InterferenceLinkElement {

    private String id;
    private String interferingSystemId;
    private String name;
    private InterferenceLinkUI settings;

    public InterferenceLinkElement(String interferingSystemId, String name, InterferenceLinkUI settings ) {
        this(UUID.randomUUID().toString(), interferingSystemId, name, settings);
    }

    public InterferenceLinkElement(String id, String interferingSystemId, String name, InterferenceLinkUI settings ) {
        this.id = id;
        this.interferingSystemId = interferingSystemId;
        this.name = name;
        this.settings = settings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InterferenceLinkElement idElement = (InterferenceLinkElement) o;

        if (!id.equals(idElement.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getInterferingSystemId() {
        return interferingSystemId;
    }

    public InterferenceLinkUI getSettings() {
        return settings;
    }

    public void setSettings( InterferenceLinkUI settings ) {
        this.settings = settings;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }
}

