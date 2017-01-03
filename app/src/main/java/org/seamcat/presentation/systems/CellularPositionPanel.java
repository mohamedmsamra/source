package org.seamcat.presentation.systems;

import org.seamcat.model.systems.ofdma.OFDMAPositioningTab;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.ScrollingBorderPanel;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

public class CellularPositionPanel extends JPanel {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private final ReferenceCellSelectionPanel referenceCellSelectionPanel;
    private final DMAPositioningSystemPanel systemPanel;
    private final SystemLayoutPanel systemLayoutPanel;

    public CellularPositionPanel( Method method, CellularPosition model ) {
        super(new BorderLayout());
        JSplitPane leftSplit = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        JSplitPane container = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );

        Refresher refresher = new Refresher();
        systemPanel = new DMAPositioningSystemPanel( refresher );
        boolean entireEnable = true;
        if ( method.getDeclaringClass() == OFDMAPositioningTab.class ) {
            entireEnable = false;
        }
        systemLayoutPanel = new SystemLayoutPanel( entireEnable, refresher );
        referenceCellSelectionPanel = new ReferenceCellSelectionPanel();

        CellularPositionHolder uiModel = new CellularPositionHolder();
        uiModel.setCellularPosition( model );
        systemPanel.setModel( uiModel );
        systemLayoutPanel.setModel( uiModel );
        referenceCellSelectionPanel.setModel( uiModel );

        leftSplit.add(new ScrollingBorderPanel(systemPanel, "System","System help","PositioningSystem"));
        leftSplit.setDividerLocation(250);
        leftSplit.add(new ScrollingBorderPanel(systemLayoutPanel, "System Layout","System layout help","PositioningSystemLayout", STRINGLIST.getString("CDMA_REFERENCE_NOTE")));

        container.add(leftSplit);
        container.setDividerLocation(390);
        container.add(new BorderPanel(referenceCellSelectionPanel, "System Layout preview","System layout preview help","PositioningSystemLayoutPreview"));

        add(container, BorderLayout.CENTER);
        refreshFromModel();
    }


    private void updateModel() {
        systemPanel.updateModel();
        systemLayoutPanel.updateModel();
    }

    public CellularPosition getModel(){
        updateModel();
        return systemPanel.getModel().getCellularPosition();
    }

    private void refreshFromModel() {
        systemPanel.refreshFromModel();
        systemLayoutPanel.refreshFromModel();
        referenceCellSelectionPanel.refreshFromModel();
    }

    public class Refresher {
        void refresh() {
            refreshFromModel();
        }
    }

}
