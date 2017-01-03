package org.seamcat.model.workspace;

import org.seamcat.presentation.genericgui.panelbuilder.PanelModelEditor;

public interface CustomPanelBuilder {

    boolean canBuild( Class<?> modelClass );

    <T> PanelModelEditor<T> build(Class<T> modelClass, T model, final String name);
}
