package org.seamcat.presentation.library;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.Workspace;
import org.seamcat.model.core.SystemSimulationModel;
import org.seamcat.model.distributions.ConstantDistributionImpl;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Factory;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.generic.InterferenceCriteria;
import org.seamcat.model.generic.ReceptionCharacteristics;
import org.seamcat.model.plugin.Horizontal;
import org.seamcat.model.plugin.OptionalFunction;
import org.seamcat.model.plugin.Spherical;
import org.seamcat.model.plugin.Vertical;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.UIToModelConverter;
import org.seamcat.model.systems.cdma.CDMAGeneralSettings;
import org.seamcat.model.systems.cdma.CDMAGeneralSettingsUpLink;
import org.seamcat.model.systems.cdma.SystemModelCDMADownLink;
import org.seamcat.model.systems.cdma.SystemModelCDMAUpLink;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.model.systems.ofdma.SystemModelOFDMADownLink;
import org.seamcat.model.systems.ofdma.SystemModelOFDMAUpLink;
import org.seamcat.model.types.Configuration;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.ofdma.UplinkOfdmaSystem;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.JarConfigurationModel;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.presentation.AntennaPatterns;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.JarComponentPanel;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.DiscreteFunctionGraph;
import org.seamcat.presentation.components.DiscreteFunctionTableModelAdapter;
import org.seamcat.presentation.eventprocessing.PluginConfigurationPanel;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.item.CalculatedValueItem;
import org.seamcat.presentation.genericgui.item.Item;
import org.seamcat.presentation.genericgui.panelbuilder.Cache;
import org.seamcat.presentation.genericgui.panelbuilder.CompositeEditor;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;
import org.seamcat.presentation.systems.generic.InterferenceCriteriaDialog;
import org.seamcat.presentation.systems.generic.WSConsistency;
import org.seamcat.simulation.result.MutableEventResult;
import org.seamcat.simulation.result.PreSimulationResultsImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

public class LibraryDetailPanel<M extends LibraryItem> extends JPanel {

    private final int index;
    private Class<M> clazz;
    private ModelHolder<M> modelHolder;
    private DecimalFormat df = new DecimalFormat("#.####");
    private JFrame parent;
    private ChangeNotifier notifier;
    private Workspace workspace;

    public LibraryDetailPanel(final JFrame parent, Class<M> clazz, final LibraryItemWrapper<M> model, final ChangeNotifier notifier) {
        this(parent, clazz, model, notifier, null);
    }

    public LibraryDetailPanel(final JFrame parent, Class<M> clazz, final LibraryItemWrapper<M> model, final ChangeNotifier notifier, Workspace workspace) {
        this.parent = parent;
        this.clazz = clazz;
        this.notifier = notifier;
        this.workspace = workspace;
        setLayout(new BorderLayout());
        index = model.getIndex();
        if ( Configuration.class.isAssignableFrom( clazz) ) {
            // special panel
            PluginConfiguration configuration = ((PluginConfiguration) model.getItem());
            final PluginConfigurationPanel panel = new PluginConfigurationPanel(parent, configuration, true, PluginConfiguration.class);
            if ( configuration instanceof AntennaGainConfiguration ) {
                JPanel combined = new JPanel(new GridLayout(1,2));
                combined.add(new BorderPanel(new JScrollPane(panel), "Plugin Configuration"));
                combined.add(new BorderPanel(antennaPreview((AntennaGainConfiguration) configuration), "Preview" ));
                add( combined, BorderLayout.CENTER);
            } else {
                add(new BorderPanel(new JScrollPane(panel), "Plugin Configuration"), BorderLayout.CENTER);
            }
            modelHolder = new ModelHolder<M>() {
                public M getModel() {
                    return (M) panel.getModel();
                }
                public Component getComponent() {
                    return panel;
                }
            };
        } else if (EmissionMask.class.isAssignableFrom(clazz)) {
            final SpectrumEmissionMaskPanel panel = new SpectrumEmissionMaskPanel((EmissionMaskImpl) model.getItem(), notifier);
            add( new BorderPanel(panel, "Spectrum Emission Mask"), BorderLayout.CENTER);
            modelHolder = new ModelHolder<M>() {
                public M getModel() {
                    return (M) panel.getModel();
                }
                public Component getComponent() {
                    return panel;
                }
            };
        } else if (BlockingMask.class.isAssignableFrom(clazz)) {
            final ReceiverBlockingMaskDetailPanel panel = new ReceiverBlockingMaskDetailPanel(parent, (BlockingMaskImpl) model.getItem(), notifier);
            add( new BorderPanel(panel, "Receiver Blocking Mask"), BorderLayout.CENTER);
            modelHolder = new ModelHolder<M>() {
                public M getModel() {
                    return (M) panel.getModel();
                }
                public Component getComponent() {
                    return panel;
                }
            };
        } else if (JarConfigurationModel.class.isAssignableFrom(clazz)) {
            final JarComponentPanel panel = new JarComponentPanel((JarConfigurationModel) model.getItem());
            add( new BorderPanel(panel, "Installed Jar Files"), BorderLayout.CENTER);
            modelHolder = new ModelHolder<M>() {
                public M getModel() {
                    return model.getItem();
                }
                public Component getComponent() {
                    return panel;
                }
            };
        } else if (CDMALinkLevelData.class.isAssignableFrom( clazz)) {
            final LinkLevelDataDetailPanel panel = new LinkLevelDataDetailPanel(parent, (CDMALinkLevelData) model.getItem());
            add(new BorderPanel(panel, "CDMA Link Level Data"), BorderLayout.CENTER);
            modelHolder = new ModelHolder<M>() {
                public M getModel() {
                    return model.getItem();
                }

                public Component getComponent() {
                    return panel;
                }
            };
        } else if (ReceiverModel.class.isAssignableFrom(clazz)) {
            final CompositeEditor detailPanel = new CompositeEditor<M>(parent, clazz, model.getItem(), true, notifier);
            attachICBehaviorSimple( detailPanel );
            add(detailPanel, BorderLayout.CENTER);
            modelHolder = new ModelHolder<M>() {
                @Override
                public M getModel() {
                    return (M)detailPanel.getModel();
                }

                @Override
                public Component getComponent() {
                    return detailPanel;
                }
            };
        } else if (T_ReceiverModel.class.isAssignableFrom(clazz)) {
            final CompositeEditor detailPanel = new CompositeEditor<M>(parent, clazz, model.getItem(), true, notifier);
            attacht_ICBehaviorSimple( detailPanel );
            add(detailPanel, BorderLayout.CENTER);
            modelHolder = new ModelHolder<M>() {
                @Override
                public M getModel() {
                    return (M)detailPanel.getModel();
                }

                @Override
                public Component getComponent() {
                    return detailPanel;
                }
            };
        }
        
        
        else {
            final CompositeEditor<M> detailPanel;
            if ( model.getItem() instanceof SystemModel ) {
                if ( model.getItem() instanceof SystemModelGeneric ) {
                    createSystemModelGeneric((SystemModelGeneric) model.getItem());
                    return;
                } 
                
                else if ( model.getItem() instanceof SystemModelCDMAUpLink ){
                    detailPanel = new CompositeEditor<M>(parent, (Class<M>)SystemModelCDMAUpLink.class, model.getItem(), true, notifier, null, (SystemModel)model.getItem(), workspace);
                    detailPanel.enableItem(CDMAGeneralSettingsUpLink.class, 7, false);
                } else if ( model.getItem() instanceof SystemModelCDMADownLink){
                    detailPanel = new CompositeEditor<M>(parent, (Class<M>)SystemModelCDMADownLink.class, model.getItem(), true, notifier, null, (SystemModel)model.getItem(), workspace);
                    detailPanel.enableItem(CDMAGeneralSettings.class, 7, false);
                } else if ( model.getItem() instanceof SystemModelOFDMAUpLink){
                    detailPanel = new CompositeEditor<M>(parent, (Class<M>)SystemModelOFDMAUpLink.class, model.getItem(), true, notifier, null, (SystemModel)model.getItem(), workspace);
                    attachOFDMAULBehavior((CompositeEditor<SystemModelOFDMAUpLink>) detailPanel);
                }else {
                    detailPanel = new CompositeEditor<M>(parent, (Class<M>)SystemModelOFDMADownLink.class, model.getItem(), true, notifier, null, (SystemModel)model.getItem(), workspace);
                }
                ((CompositeEditor)detailPanel).activeTab(SystemModelActiveTab.activeTab(SystemModel.class));
            } else {
                detailPanel = new CompositeEditor<M>(parent, clazz, model.getItem(), true, notifier);
            }
            add(detailPanel, BorderLayout.CENTER);
            modelHolder = new ModelHolder<M>() {
                public M getModel() {
                    return detailPanel.getModel();
                }
                public Component getComponent() {
                    return detailPanel;
                }
            };
        }
	}

  
   
   

	

	public JPanel antennaPreview(AntennaGainConfiguration configuration) {
        JPanel previewPanel = new JPanel(new GridLayout(2,1));
        Class modelClass = configuration.getModelClass();
        int added = 0;
        try {
            for (Method method : Cache.orderedConfig(modelClass)) {
                JPanel horizontal = handle(method, AntennaPatterns.HORIZONTAL, Horizontal.class, "Horizontal", configuration.getModel());
                if ( horizontal != null ) {
                    previewPanel.add( horizontal);
                    added++;
                }
                JPanel vertical = handle(method, AntennaPatterns.VERTICAL, Vertical.class, "Vertical", configuration.getModel());
                if ( vertical != null ) {
                    previewPanel.add( vertical );
                    added++;
                }
                JPanel spherical = handle(method, AntennaPatterns.SPHERICAL, Spherical.class, "Spherical", configuration.getModel());
                if ( spherical != null ) {
                    previewPanel.add( spherical);
                    added++;
                }


                if (added == 2) break;
            }
        } catch ( Exception e) {
        }
        return previewPanel;
    }

    private JPanel handle(Method method, AntennaPatterns pat, Class<? extends Annotation> ann, String name, Object model) throws InvocationTargetException, IllegalAccessException {
        Annotation annotation = method.getAnnotation(ann);
        if (annotation != null) {
            DiscreteFunction function = getFunction(method.invoke(model));
            if ( function != null ) {
                DiscreteFunctionTableModelAdapter tableModel = new DiscreteFunctionTableModelAdapter();
                tableModel.setDiscreteFunction(function);
                DiscreteFunctionGraph graph = new DiscreteFunctionGraph(tableModel, pat, "Degree", "dB");
                return new BorderPanel(graph, name);
            }
        }
        return null;
    }

    private DiscreteFunction getFunction(Object instance) {
        if (instance instanceof Function) {
            return (DiscreteFunction) instance;
        } else if (instance instanceof OptionalFunction) {
            if ( ((OptionalFunction) instance).isRelevant() ) {
                return (DiscreteFunction) ((OptionalFunction) instance).getFunction();
            }
        }

        return null;
    }

    public M getModel() {
        M model = modelHolder.getModel();
        if ( model instanceof SystemModel ) {
            Component component = modelHolder.getComponent();
            Integer activeTab = ((CompositeEditor) component).getActiveTab();
            if ( model instanceof SystemModelGeneric ) {
                SystemModelActiveTab.activeTab(SystemModelGeneric.class,activeTab);   
            }  
            else {
                SystemModelActiveTab.activeTab(SystemModel.class,activeTab);
            }
        }
        return model;
    }

    public Component getComponent() {
        return modelHolder.getComponent();
    }

    public int getIndex() {
        return index;
    }

    public Class<M> getModelClass() {
        return clazz;
    }

    public boolean containsItem(Item item) {
        Component component = getComponent(0);
        if ( component instanceof CompositeEditor) {
            return ((CompositeEditor) component).containsItem( item );
        }
        return false;
    }

    public boolean match(Object context) {
        Component component = modelHolder.getComponent();
        if ( component instanceof PluginConfigurationPanel ) {
            if ( ((PluginConfigurationPanel) component).getIdPanel() == context ) {
                return true;
            }
        }
        return false;
    }


    private interface ModelHolder<T> {
        T getModel();
        Component getComponent();
    }

    private void createSystemModelGeneric(SystemModelGeneric model) {
        final CompositeEditor<SystemModelGeneric> panel = new CompositeEditor<>(parent, SystemModelGeneric.class, model, true, notifier);
        panel.activeTab( SystemModelActiveTab.activeTab( SystemModelGeneric.class));

        attachICBehavior(panel);
      
       // attacht_ICBehavior(panel);
        removeAll();
        add(panel, BorderLayout.CENTER);
       // add(panel, BorderLayout.CENTER);
        modelHolder = new ModelHolder<M>() {
            public M getModel() {return (M) panel.getModel();
            }
            public Component getComponent() {
                return panel;
            }
        };
    }

    private void attachICBehaviorSimple(final CompositeEditor<ReceiverModel> editor) {
        CalculatedValueItem calc = editor.getCalculatedValues().get(0);
        calc.getEvaluateButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ReceiverModel model = editor.getModel();
                ReceptionCharacteristics characteristics  = model.receptionCharacteristics();
                Distribution distribution = characteristics.noiseFloor();

                if ( distribution instanceof ConstantDistributionImpl) {
                    // ok
                    InterferenceCriteriaDialog dialog = new InterferenceCriteriaDialog(MainWindow.getInstance(), model);
                    if ( dialog.display() ) {

                        InterferenceCriteria ic = dialog.getSelectedCriteria();
                        GenericPanelEditor panel = editor.getEditor(InterferenceCriteria.class);
                        java.util.List<AbstractItem> allItems = panel.getAllItems();
                        allItems.get(0).setValue(ic.protection_ratio());
                        allItems.get(1).setValue(ic.extended_protection_ratio());
                        allItems.get(2).setValue(ic.noise_augmentation());
                        allItems.get(3).setValue(ic.interference_to_noise_ratio());

                        WSConsistency wsConsistency = dialog.getWSConsistency();
                        if ( wsConsistency.wsConsistency() ) {
                            GenericPanelEditor charPanel = editor.getEditor(ReceptionCharacteristics.class);
                            java.util.List<AbstractItem> allItems1 = charPanel.getAllItems();

                            Distribution nf = Factory.distributionFactory().getConstantDistribution(wsConsistency.noiseFloor());
                            allItems1.get(0).setValue( nf );
                            allItems1.get(5).setValue(wsConsistency.sensitivity());
                        }

                        panel.revalidate();
                        panel.repaint();
                    }
                } else {
                    // error
                    DialogHelper.interferenceCriteriaError();
                }

            }
        });
    }

    private void attacht_ICBehaviorSimple(final CompositeEditor<T_ReceiverModel> editor) {
        CalculatedValueItem calc = editor.getCalculatedValues().get(0);
        calc.getEvaluateButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                T_ReceiverModel model = editor.getModel();
                ReceptionCharacteristics characteristics  = model.receptionCharacteristics();
                Distribution distribution = characteristics.noiseFloor();

                if ( distribution instanceof ConstantDistributionImpl) {
                    // ok
                    InterferenceCriteriaDialog dialog = new InterferenceCriteriaDialog(MainWindow.getInstance(), model);
                    if ( dialog.display() ) {

                        InterferenceCriteria ic = dialog.getSelectedCriteria();
                        GenericPanelEditor panel = editor.getEditor(InterferenceCriteria.class);
                        java.util.List<AbstractItem> allItems = panel.getAllItems();
                        allItems.get(0).setValue(ic.protection_ratio());
                        allItems.get(1).setValue(ic.extended_protection_ratio());
                        allItems.get(2).setValue(ic.noise_augmentation());
                        allItems.get(3).setValue(ic.interference_to_noise_ratio());

                        WSConsistency wsConsistency = dialog.getWSConsistency();
                        if ( wsConsistency.wsConsistency() ) {
                            GenericPanelEditor charPanel = editor.getEditor(ReceptionCharacteristics.class);
                            java.util.List<AbstractItem> allItems1 = charPanel.getAllItems();

                            Distribution nf = Factory.distributionFactory().getConstantDistribution(wsConsistency.noiseFloor());
                            allItems1.get(0).setValue( nf );
                            allItems1.get(5).setValue(wsConsistency.sensitivity());
                        }

                        panel.revalidate();
                        panel.repaint();
                    }
                } else {
                    // error
                    DialogHelper.interferenceCriteriaError();
                }

            }
        });
    }

    private void attachICBehavior(final CompositeEditor<SystemModelGeneric> editor) {
        CompositeEditor<ReceiverModel> tab = editor.getTab(ReceiverModel.class);
        attachICBehaviorSimple( tab );
       
    }

    private void attacht_ICBehavior(final CompositeEditor<SystemModelGeneric> editor) {
        CompositeEditor<T_ReceiverModel> tab = editor.getTab(T_ReceiverModel.class);
        attacht_ICBehaviorSimple( tab );
       
    }
   
    private void attachOFDMAULBehavior(final CompositeEditor<SystemModelOFDMAUpLink> editor) {
        final CalculatedValueItem ic = editor.getCalculatedValues().get(0);
        ic.getEvaluateButton().addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        final SystemModelOFDMAUpLink model = editor.getModel();
                        final SystemSimulationModel simulationModel = UIToModelConverter.convert(model);
                        simulationModel.getDMASystem().initialize(new MutableEventResult(-1));

                        new SwingWorker<Void,Void>(){
                            protected Void doInBackground() throws Exception {

                                try {
                                    simulationModel.getDMASystem().setResults( new PreSimulationResultsImpl());
                                    simulationModel.getDMASystem().performPreSimulationTasks(model.general().frequency().trial());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                            protected void done() {
                                ic.setResult(df.format(simulationModel.getDMASystem().getResults().getPreSimulationResults().findDoubleValue(UplinkOfdmaSystem.COUPLING_LOSS_PERCENTILE)));
                                setCursor(Cursor.getDefaultCursor());
                            }
                        }.execute();
                    }
                }
        );
    }
}