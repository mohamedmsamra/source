package org.seamcat.model.plugin.eventprocessing;

import javax.swing.*;

public interface ModelPanel<T> {

    JPanel getPanel();
    T getModel();
    PanelDefinition getDefinition();
}
