package org.seamcat.presentation.genericgui.panelbuilder;

import com.rits.cloning.Cloner;
import org.seamcat.model.systems.generic.LocalEnvironments;
import org.seamcat.model.types.LocalEnvironment;
import org.seamcat.presentation.components.LocalEnvironmentsTxRxModel;
import org.seamcat.presentation.components.LocalEnvironmentsTxRxPanel;
import org.seamcat.presentation.systems.CellularPosition;
import org.seamcat.presentation.systems.CellularPositionPanel;

import javax.swing.*;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomPanelHandler {

    private static Set<Class<?>> customUI;

    static {
        customUI = new HashSet<>();
        customUI.add(LocalEnvironments.class);
        customUI.add(CellularPosition.class);
    }


    public static boolean canHandle( Class<?> clazz ) {
        return customUI.contains( clazz );
    }

    public static <T> PanelModelEditor<T> create( Method method, Class<T> modelClass, T model, final String name ) {
        if ( LocalEnvironments.class.isAssignableFrom(modelClass)) {
            LocalEnvironments envs = (LocalEnvironments) model;
            Cloner cloner = new Cloner();
            List<LocalEnvironment> recs = cloner.deepClone( envs.receiverEnvironments() );
            List<LocalEnvironment> trans = cloner.deepClone( envs.transmitterEnvironments() );
            final LocalEnvironmentsTxRxModel uiModel = new LocalEnvironmentsTxRxModel(recs, trans);
            return (PanelModelEditor<T>) new LocalEnvironmentsTxRxPanel(uiModel, name);
        } else if ( CellularPosition.class.isAssignableFrom(modelClass)) {
            final CellularPositionPanel panel = new CellularPositionPanel(method, (CellularPosition) model);
            return new PanelModelEditor<T>() {
                public JPanel getPanel() {
                    return panel;
                }
                public T getModel() {
                    return (T) panel.getModel();
                }
            };
        }

        throw new RuntimeException("Panel not supported");
    }

}
