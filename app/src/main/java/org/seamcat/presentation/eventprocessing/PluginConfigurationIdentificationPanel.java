package org.seamcat.presentation.eventprocessing;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.InfoMessageEvent;
import org.seamcat.events.TextWidgetValueUpdatedEvent;
import org.seamcat.model.Library;
import org.seamcat.model.factory.Model;
import org.seamcat.model.factory.SeamcatFactory;
import org.seamcat.model.types.Description;
import org.seamcat.plugin.*;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.components.GenericLibraryImportExportResetWidget;
import org.seamcat.presentation.components.GenericSelectionDialog;
import org.seamcat.presentation.genericgui.GenericPanel;
import org.seamcat.presentation.genericgui.item.BooleanItem;
import org.seamcat.presentation.genericgui.item.DoubleItem;
import org.seamcat.presentation.genericgui.item.TextItem;
import org.seamcat.presentation.genericgui.panelbuilder.PluginEditorPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PluginConfigurationIdentificationPanel extends GenericPanel {

    private final TextItem title;
    private final TextItem description;
    private final TextItem notes;
    private final BooleanItem variations;
    private final DoubleItem peakGain;
    private GenericLibraryImportExportResetWidget importExportWidget;
    private PluginEditorPanel container;
    private PluginConfiguration model;

    public PluginConfigurationIdentificationPanel(final PluginConfigurationPanel context, final JFrame parent, boolean libraryMode, PluginConfiguration model, PluginEditorPanel container, final Class<? extends PluginConfiguration> clazz) {
        this.model = model;
        this.container = container;
        if ( !libraryMode ) {
            importExportWidget = new GenericLibraryImportExportResetWidget();
            importExportWidget.addImportHandler( new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    List<PluginConfiguration> configurations = Model.getInstance().getLibrary().getPluginConfigurations(clazz);
                    GenericSelectionDialog<PluginConfiguration> dialog = new GenericSelectionDialog<>(parent, Library.name(clazz) + " Library", configurations);
                    if ( dialog.display() ) {
                        context.setModel(dialog.getSelectedValue() );
                        setModel(dialog.getSelectedValue() );
                        EventBusFactory.getEventBus().publish(new TextWidgetValueUpdatedEvent("", PluginConfigurationIdentificationPanel.this));
                    }
                }
            });
            importExportWidget.addExportHandler(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    PluginConfiguration clone = getModel().deepClone();
                    if ( !Model.getInstance().getLibrary().addPluginConfiguration(clone)) {
                        if ( DialogHelper.overrideInLibrary(parent, getModel().description().name()) ) {
                            Model.getInstance().getLibrary().replacePluginInstance(clone);
                            EventBusFactory.getEventBus().publish( new InfoMessageEvent(String.format("'%s' overridden in plugin library", getModel().description().name())));
                        }
                    } else {
                        Model.getInstance().getLibrary().addJarFile( clone );
                        EventBusFactory.getEventBus().publish( new InfoMessageEvent(String.format("'%s' added to plugin library", getModel().description().name())));
                    }
                }
            });
            importExportWidget.addRestoreHandle(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    PluginConfiguration pc = getModel();
                    PluginConfiguration defaultConf = pc;
                    if ( pc instanceof PropagationModelConfiguration ) {
                        defaultConf = SeamcatFactory.propagation().getByClass(pc.getPluginClass());
                    } else if ( pc instanceof CoverageRadiusConfiguration ) {
                        defaultConf = CoverageRadiusConfiguration.coverage(pc.getPluginClass());
                    } else if ( pc instanceof EventProcessingConfiguration) {
                        defaultConf = EventProcessingConfiguration.event(pc.getPluginClass());
                    } else if ( pc instanceof AntennaGainConfiguration) {
                        defaultConf = SeamcatFactory.antennaGain().getByClass(pc.getPluginClass());
                    }
                    context.setModel(defaultConf );
                    setModel( defaultConf );
                    EventBusFactory.getEventBus().publish(new TextWidgetValueUpdatedEvent("", PluginConfigurationIdentificationPanel.this));

                }
            });
            addItem(importExportWidget);
        }

        title = new TextItem().label("Name").valueUpdatedEvent(new TextWidgetValueUpdatedEvent("", this));
        addItem( title );
        description = new TextItem().label("Description").readOnly();
        addItem(description);
        description.initialize();
        Description description = model.description();

        if ( description.description() != null ) {
            this.description.setValue(description.description());
            this.description.setRelevant(true);
        } else {
            this.description.setValue("");
            this.description.setRelevant(false);
        }
        title.initialize();
        notes = new TextItem().label("Notes").linesToDisplay(3);
        addItem(notes);
        notes.initialize();
        notes.setValue(model.getNotes());

        variations = new BooleanItem().label("Variations");
        peakGain = new DoubleItem().label("Antenna Peak Gain").unit("dBi");
        if ( model instanceof PropagationModelConfiguration ) {
            variations.initialize();
            addItem(variations);
        } else if ( model instanceof AntennaGainConfiguration ) {
            peakGain.initialize();
            addItem(peakGain);
        }
        setModel( model );
        initializeWidgets();
    }

    public void setModel( PluginConfiguration model ) {
        this.model = model;
        Description description = model.description();
        title.setValue(description.name());
        if ( description.description() != null ) {
            this.description.setValue(description.description());
            this.description.setRelevant(true);
        } else {
            this.description.setValue("");
            this.description.setRelevant(false);
        }
        notes.setValue(model.getNotes());
        if ( model instanceof PropagationModelConfiguration ) {
            variations.setValue( ((PropagationModelConfiguration) model).isVariationSelected() );
        } else if ( model instanceof AntennaGainConfiguration ) {
            peakGain.setValue( ((AntennaGainConfiguration) model).peakGain() );
        }
    }

    public PluginConfiguration getModel() {
        PluginConfiguration instance = model.instance(container.getModel());
        if ( instance instanceof PropagationModelConfiguration ) {
            ((PropagationModelConfiguration) instance).setVariationSelected( variations.getValue() );
        } else if ( instance instanceof AntennaGainConfiguration ) {
            ((AntennaGainConfiguration) instance).setPeakGain( peakGain.getValue() );
        }
        instance.setNotes( notes.getValue() );
        instance.setName( title.getValue() );
        return instance;
    }

    public void setContainer( PluginEditorPanel container ) {
        this.container = container;
    }

    @Override
    public void setGlobalRelevance(boolean relevance) {
        super.setGlobalRelevance(relevance);

        if ( importExportWidget != null ) {
            importExportWidget.setGlobalRelevance( relevance );
        }
    }
}
