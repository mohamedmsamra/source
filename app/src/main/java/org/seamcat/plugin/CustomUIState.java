package org.seamcat.plugin;

import org.seamcat.model.plugin.eventprocessing.PanelDefinition;

import java.util.Map;

public class CustomUIState {

    public CustomUIState(Map<PanelDefinition<?>, Object> state) {
        this.state = state;
    }

    private Map<PanelDefinition<?>, Object> state;

    public Map<PanelDefinition<?>, Object> get() {
        return state;
    }

}

