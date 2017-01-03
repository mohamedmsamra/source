package org.seamcat.model.eventprocessing;

import org.seamcat.model.plugin.eventprocessing.CustomUI;
import org.seamcat.model.plugin.eventprocessing.ModelPanel;
import org.seamcat.model.plugin.eventprocessing.PanelDefinition;
import org.seamcat.model.plugin.eventprocessing.Panels;
import org.seamcat.plugin.CustomUIState;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * method to generate the custom UI panels
 */
public class CustomUIPanels implements CustomUI {

    private CustomUI decorated;
    private Map<String, ModelPanel> panelsMap;
    private Panels panels;

    public CustomUIPanels(CustomUI decorated, CustomUIState state) {
        this.decorated = decorated;
        panelsMap = new HashMap<String, ModelPanel>();

        for (Map.Entry<PanelDefinition<?>, Object> entry : state.get().entrySet()) {
            final PanelDefinition key = entry.getKey();
            final GenericPanelEditor<?> editor = new GenericPanelEditor<Object>(MainWindow.getInstance(), key.getModelClass(), entry.getValue());
            final JPanel panel = new BorderPanel(new JScrollPane(editor), key.getName());

            panelsMap.put(key.getName(), new ModelPanel() {
                public JPanel getPanel() { return panel; }
                public Object getModel() { return editor.getModel();}
                public PanelDefinition getDefinition() { return key; }
            });
        }

        panels = new Panels() {
            public <T> ModelPanel<T> get(String name) {
                return panelsMap.get( name );
            }
        };
    }

    public CustomUIState getState() {
        Map<PanelDefinition<?>, Object> models = new HashMap<>();
        for (ModelPanel mp : panelsMap.values()) {
            models.put( mp.getDefinition(), mp.getModel());
        }
        return new CustomUIState(models);
    }

    @Override
    public String getTitle() {
        return decorated.getTitle();
    }

    @Override
    public void buildUI(JPanel canvas, Panels panels) {
        decorated.buildUI(canvas, this.panels);
    }

    @Override
    public PanelDefinition[] panelDefinitions() {
        return decorated.panelDefinitions();
    }

    public CustomUI getDecorated() {
        if ( decorated instanceof CustomUIPanels) {
            return ((CustomUIPanels) decorated).getDecorated();
        }
        return decorated;
    }

    public Panels getPanels() {
        return panels;
    }
}
