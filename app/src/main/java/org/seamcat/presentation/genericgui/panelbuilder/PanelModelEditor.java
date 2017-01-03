package org.seamcat.presentation.genericgui.panelbuilder;

import javax.swing.*;

public interface PanelModelEditor<T> {

    JPanel getPanel();

    T getModel();
}
