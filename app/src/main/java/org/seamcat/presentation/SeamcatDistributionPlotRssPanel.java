package org.seamcat.presentation;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

import static org.seamcat.model.simulation.result.SimulationResult.DRSS;
import static org.seamcat.model.simulation.result.SimulationResult.IRSS_UNWANTED;

public class SeamcatDistributionPlotRssPanel extends JPanel {

    private VectorsOutlinePanel dRssPanel, iRssUnwanted, iRssBlocking;

    public SeamcatDistributionPlotRssPanel(int numberOfEvents) {
        setLayout(new GridLayout(3,1));

        dRssPanel = new VectorsOutlinePanel("", "", numberOfEvents);
        dRssPanel.setBorder(new TitledBorder("dRss"));

        iRssUnwanted = new VectorsOutlinePanel( "", "", numberOfEvents);
        iRssUnwanted.setBorder(new TitledBorder("iRss unwanted (summation)"));

        iRssBlocking = new VectorsOutlinePanel("", "", numberOfEvents);
        iRssBlocking.setBorder(new TitledBorder("iRss blocking (summation)"));

        createSubPanels();
        add(dRssPanel);
        add(iRssUnwanted);
        add(iRssBlocking);
    }

    private void createSubPanels() {
        iRssUnwanted.show("", "dBm");
        iRssBlocking.show("", "dBm");
        dRssPanel.show("", "dBm");
    }

    public VectorsOutlinePanel getPanel( String name ) {
        if ( name.equals(DRSS)) {
            return dRssPanel;
        } else if ( name.equals(IRSS_UNWANTED)) {
            return iRssUnwanted;
        }
        return iRssBlocking;
    }

    public void reset() {
        dRssPanel.reset();
        iRssUnwanted.reset();
        iRssBlocking.reset();
    }
}
