package org.seamcat.model.plugin.eventprocessing;

import javax.swing.*;

/**
 * Class representing a custom UI used to from an Event Processing Plugin.
 * Classes must have a default constructor to be loaded from a workspace
 * result.
 *
 * If additional results are produced they must be added to the allResults
 */
public interface CustomUI {

    String getTitle();

    void buildUI( JPanel canvas, Panels panels );

    PanelDefinition[] panelDefinitions();
}
