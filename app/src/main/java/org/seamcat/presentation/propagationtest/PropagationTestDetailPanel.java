package org.seamcat.presentation.propagationtest;

import org.seamcat.events.TextWidgetValueUpdatedEvent;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.LocalEnvironmentsTxRxModel;
import org.seamcat.presentation.components.LocalEnvironmentsTxRxPanel;
import org.seamcat.presentation.eventprocessing.PluginConfigurationPanel;
import org.seamcat.presentation.genericgui.ViewHelper;
import org.seamcat.presentation.genericgui.item.DistributionItem;
import org.seamcat.presentation.genericgui.item.Item;
import org.seamcat.presentation.genericgui.panelbuilder.ChangeListener;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PropagationTestDetailPanel extends JPanel {

    private final PluginConfigurationPanel pm;
    private final GenericPanelEditor<LinkResultConfiguration> link;
    private final GenericPanelEditor<CommonConfiguration> common;
    private final LocalEnvironmentsTxRxPanel localEnvironmentsPanel;
    private PropagationTestModel model;

    public PropagationTestDetailPanel( JFrame owner, PropagationTestModel model, ChangeListener<CommonConfiguration> listener) {
        super(new BorderLayout());
        this.model = model;

        pm = new PluginConfigurationPanel(owner, model.getPropagationModel(), false, PropagationModelConfiguration.class);
        link = new GenericPanelEditor<LinkResultConfiguration>(owner, LinkResultConfiguration.class, model.getLinkResultConfiguration());
        common = new GenericPanelEditor<CommonConfiguration>(owner, CommonConfiguration.class, model.getCommonConfiguration());
        common.addChangeListener( listener );
        localEnvironmentsPanel = new LocalEnvironmentsTxRxPanel(new LocalEnvironmentsTxRxModel(),"Local environments");

        JSplitPane right = ViewHelper.vSplit(
                ViewHelper.vSplit(
                        new BorderPanel(link, "Link Configuration"),
                        new BorderPanel(localEnvironmentsPanel,"Local environments"),180),
                new BorderPanel(common, "Plot Settings"), 450);
        add(ViewHelper.hSplit(new BorderPanel(new JScrollPane(pm), "Propagation Model"), right, 500), BorderLayout.CENTER);
    }

    public void updateModel() {
        model.setPropagationModelConfiguration((PropagationModelConfiguration) pm.getModel());
        model.setLinkResultConfiguration( link.getModel() );
        model.setCommonConfiguration( common.getModel() );
        model.setLocalEnvironments( localEnvironmentsPanel.getModel() );
    }

    public PropagationTestModel getModel() {
        return model;
    }

    public void updateRelevance(boolean forceDistribution) {
        DistributionItem dist = commonDistribution();
        if ( common.getModel().common() == CommonConfiguration.Common.Frequency ) {
            if ( forceDistribution ) dist.setValue((AbstractDistribution) link.getModel().frequency());
            dist.setUnitText( "MHz" ).setLabelText("Frequency");
            off(1);
        } else if ( common.getModel().common() == CommonConfiguration.Common.Distance ) {
            if ( forceDistribution ) dist.setValue((AbstractDistribution) link.getModel().distance());
            dist.setUnitText("km").setLabelText("Distance");
            off(0);
        } else if ( common.getModel().common() == CommonConfiguration.Common.TX_Height ){
            if ( forceDistribution ) dist.setValue((AbstractDistribution) link.getModel().txHeight());
            dist.setUnitText("m").setLabelText("TX Height");
            off(2);
        } else {
            if ( forceDistribution ) dist.setValue((AbstractDistribution) link.getModel().rxHeight());
            dist.setUnitText("m").setLabelText("RX Height");
            off(3);
        }
    }

    private DistributionItem commonDistribution() {
        for (Item item : common.getItems()) {
            if ( item instanceof DistributionItem ) {
                return (DistributionItem) item;
            }
        }
        throw new RuntimeException("No distribution item found");
    }

    private void off( int index ) {
        List<Item> items = link.getItems();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            item.setRelevant(true);
            if ( i == index ) {
                item.setRelevant( false );
            }
        }
    }

    public boolean matchEvent(TextWidgetValueUpdatedEvent event) {
        return event.getContext() == pm.getIdPanel();
    }
}

