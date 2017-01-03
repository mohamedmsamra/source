package org.seamcat.presentation.propagationtest;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.TextWidgetValueUpdatedEvent;
import org.seamcat.exception.SimulationInvalidException;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.IdElement;
import org.seamcat.model.Workspace;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.ConstantDistributionImpl;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.factory.Model;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.generic.RXAntennaPointingUI;
import org.seamcat.model.generic.TXAntennaPointingUI;
import org.seamcat.model.scenariocheck.ScenarioCheckResult;
import org.seamcat.model.scenariocheck.ScenarioCheckUtils;
import org.seamcat.model.simulation.result.LinkResult;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.SystemModelGeneric;
import org.seamcat.model.systems.generic.T_ReceiverModel;

import org.seamcat.model.systems.generic.TransmitterModel;
import org.seamcat.model.systems.ofdma.General;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.presentation.DialogDisplaySignal;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.LabeledPairLayout;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.GenericListDetailDialog;
import org.seamcat.presentation.eventprocessing.ReadOnlyPanel;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.item.DistributionItem;
import org.seamcat.presentation.genericgui.item.SelectionItem;
import org.seamcat.presentation.genericgui.panelbuilder.ChangeListener;
import org.seamcat.scenario.WorkspaceScenario;
import org.seamcat.simulation.result.MutableLinkResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import static org.seamcat.model.factory.Factory.*;
import static org.seamcat.simulation.LocalEnvironmentSelector.pickLocalEnvironment;

public class PropagationTestPanel extends EscapeDialog {

    private PropagationTestSelectionPanel selectionPanel;
    private PropagationTestDetailPanel detailPanel;
    private JPanel modelPanel = new JPanel(new BorderLayout());
    private JButton generate;
    private JButton generateSorted;

    private Workspace defaultWorkspace;

    private ChangeListener<CommonConfiguration> listener = new ChangeListener<CommonConfiguration>() {
        public void handle(CommonConfiguration model, List<AbstractItem> items, AbstractItem changedItem) {
            if ( detailPanel == null ) return;
            detailPanel.updateModel();
            if ( changedItem instanceof SelectionItem ) {
                detailPanel.updateRelevance(true);
            } else if ( changedItem instanceof DistributionItem ) {
                Distribution distribution = detailPanel.getModel().getCommonConfiguration().distribution();
                for (PropagationTestModel ptModel : selectionPanel.getModels()) {
                    LinkResultConfiguration conf = ptModel.getLinkResultConfiguration();
                    LinkResultConfiguration prototype = prototype(LinkResultConfiguration.class, conf);
                    ptModel.setLinkResultConfiguration( setAndBuild(prototype, distribution, detailPanel.getModel().getCommonConfiguration().common()));
                }

                setDetailView(selectionPanel.getSelected());
            }

            handleButtonEnablement();
        }
    };

    public PropagationTestPanel(Frame owner) {
        super(owner,  "Seamcat Propagation Model Test");
        defaultWorkspace = Model.openDefaultWorkspace();

        selectionPanel = new PropagationTestSelectionPanel(new PropagationTestSelectionPanel.SelectedModel() {
            @Override
            public void selected(PropagationTestModel model) {
                if ( detailPanel != null ) {
                    detailPanel.updateModel();
                    model.setCommonConfiguration( detailPanel.getModel().getCommonConfiguration() );
                }
                setDetailView(model);
            }
        });
        selectionPanel.addModel( new PropagationTestModel());
        selectionPanel.addAddRemoveListener( new AddRemovePanel.AddRemoveListener() {
            @Override
            public void add() {
                detailPanel.updateModel();
                List<PluginConfiguration> plugins = Model.getInstance().getLibrary().getPluginConfigurations( PropagationModelConfiguration.class );

                GenericListDetailDialog<PluginConfiguration> dialog = new GenericListDetailDialog<PluginConfiguration>(MainWindow.getInstance(), "Select Propagation Model", plugins) {
                    public void selectedElement(PluginConfiguration model) {
                        JPanel jPanel = new JPanel(new LabeledPairLayout());
                        jPanel.add( new JLabel("Name"), LabeledPairLayout.LABEL);
                        jPanel.add( new JLabel(model.description().name()), LabeledPairLayout.FIELD);
                        jPanel.add( new JLabel("Description"), LabeledPairLayout.LABEL);
                        jPanel.add( new JLabel(model.description().description()), LabeledPairLayout.FIELD);
                        ReadOnlyPanel.addReadOnly(jPanel, model);
                        setDetail(new BorderPanel(jPanel, "Plugin Configuration"));
                    }
                };
                if ( dialog.display() ) {
                    PropagationModelConfiguration configuration = (PropagationModelConfiguration) dialog.getSelectedValue();
                    PropagationTestModel model = new PropagationTestModel();
                    model.setPropagationModelConfiguration( configuration );
                    model.setCommonConfiguration( detailPanel.getModel().getCommonConfiguration());
                    selectionPanel.addModel(model);
                }
            }

            @Override
            public void remove() {
                selectionPanel.removeSelectedItem();
            }

            @Override
            public void help() {
                SeamcatHelpResolver.showHelp(PropagationTestPanel.this);
            }
        });

        // buttons on south
        JPanel controlPanel = new JPanel();
        generate = new JButton("Generate samples");
        generate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if ( consistencyCheck() ) {
                    generateAndShow();
                }
            }
        });
        generateSorted = new JButton("Generate sorted samples");
        generateSorted.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if ( consistencyCheck()) {
                    generateAndShowSampleInXY();
                }
            }
        });
        controlPanel.add( generate);
        controlPanel.add( generateSorted);
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });
        controlPanel.add( close );

        getContentPane().setLayout( new BorderLayout());
        getContentPane().add(selectionPanel, BorderLayout.WEST);
        getContentPane().add( modelPanel, BorderLayout.CENTER );
        getContentPane().add( controlPanel, BorderLayout.SOUTH );

        handleButtonEnablement();

        pack();
        setLocationRelativeTo(owner);
        setSize(1200, 800);

        EventBusFactory.getEventBus().subscribe( this );
    }

    private LinkResultConfiguration setAndBuild(LinkResultConfiguration prototype, Distribution distribution, CommonConfiguration.Common common) {
        switch (common) {
            case Distance:
                when(prototype.distance()).thenReturn(distribution);
                break;
            case Frequency:
                when(prototype.frequency()).thenReturn(distribution);
                break;
            case RX_Height:
                when(prototype.rxHeight()).thenReturn(distribution);
                break;
            case TX_Height:
                when(prototype.txHeight()).thenReturn(distribution);
                break;
        }

        return build(prototype);
    }

    private void setDetailView(PropagationTestModel model) {
        detailPanel = new PropagationTestDetailPanel(MainWindow.getInstance(), model, listener);
        detailPanel.updateRelevance(false);
        modelPanel.removeAll();
        modelPanel.add( detailPanel );
        modelPanel.revalidate();
        modelPanel.repaint();
    }

    @UIEventHandler
    public void handle(TextWidgetValueUpdatedEvent event) {
        if ( detailPanel.matchEvent( event )) {
            detailPanel.updateModel();
            selectionPanel.refreshFromModel();
        }
    }

    private void handleButtonEnablement() {
        Distribution distribution = detailPanel.getModel().getCommonConfiguration().distribution();
        generateSorted.setEnabled( !( distribution instanceof ConstantDistributionImpl));
    }

    private boolean consistencyCheck() {
        detailPanel.updateModel();
        List<Object> path = new ArrayList<Object>();
        List<ScenarioCheckResult> results = new ArrayList<ScenarioCheckResult>();

        for (PropagationTestModel model : selectionPanel.getModels()) {
            // setup victim system according to settings
            // modify( SMG.class, model, frequency, "generic", "frequency" )
            SystemModelGeneric victimSystem = (SystemModelGeneric) defaultWorkspace.getVictimSystem();
            SystemModelGeneric systemProto = prototype(SystemModelGeneric.class, victimSystem);
            
      
            
            General genProto = prototype(General.class, victimSystem.general());
            when(genProto.frequency()).thenReturn(model.getLinkResultConfiguration().frequency());
           
            ReceiverModel recProto = prototype(ReceiverModel.class, victimSystem.receiver());
           // T_ReceiverModel t_recProto = prototype(T_ReceiverModel.class, victimSystem.receiver());
            
            //RXAntennaPointingUI prototype = prototype(RXAntennaPointingUI.class, victimSystem.t_receiver().antennaPointing());
            RXAntennaPointingUI prototype = prototype(RXAntennaPointingUI.class, victimSystem.receiver().antennaPointing());
            when(prototype.antennaHeight()).thenReturn(model.getLinkResultConfiguration().rxHeight());
            when(recProto.antennaPointing()).thenReturn(build(prototype));
           
            TransmitterModel tranBPro = prototype(TransmitterModel.class, victimSystem.transmitter());
            TXAntennaPointingUI tranProto = prototype(TXAntennaPointingUI.class, victimSystem.transmitter().antennaPointing());
            when(tranProto.antennaHeight()).thenReturn(model.getLinkResultConfiguration().txHeight());
            when(tranBPro.antennaPointing()).thenReturn(build(tranProto));
           // when(systemProto.receiver()).thenReturn(build(recProto) );
            when(systemProto.receiver()).thenReturn(build(recProto) );
            when(systemProto.transmitter()).thenReturn(build(tranBPro) );
            
           // when(t_systemProto.t_receiver()).thenReturn(build(t_recProto) );
            //when(t_systemProto.transmitter()).thenReturn(build(tranBPro) );
            // replace current with updated victim system
            defaultWorkspace.getSystemModels().clear();
            defaultWorkspace.getSystemModels().add(0, new IdElement<SystemModel>(defaultWorkspace.getVictimSystemId(), build(systemProto)));
            //defaultWorkspace.getSystemModels().add(0, new IdElement<SystemModel>(defaultWorkspace.getVictimSystemId(), build(t_systemProto)));
            WorkspaceScenario scenario = new WorkspaceScenario(defaultWorkspace);
            path.clear();
            path.add( scenario.getVictimSystem() );

            ScenarioCheckUtils.check(scenario, path, results, model.getPropagationModel(), model.toString());
        }

        return MainWindow.displayScenarioCheckResults(results, false, true, MainWindow.getInstance());
    }

    private void generateAndShow() {
        DialogDisplaySignal displaySignal;
        if ( detailPanel == null ) return;
        detailPanel.updateModel();
        CommonConfiguration commonConfiguration = detailPanel.getModel().getCommonConfiguration();
        int events = commonConfiguration.samples();

        List<PropagationHolder> results = new ArrayList<PropagationHolder>();

        Map<PropagationTestModel, PropagationHolder> resultMap = new HashMap<PropagationTestModel, PropagationHolder>();
        for (PropagationTestModel model : selectionPanel.getModels()) {
            PropagationHolder holder = new PropagationHolder();
            holder.setTitle( model.toString() );
            holder.setData( new double[events]);
            resultMap.put(model, holder);
            results.add( holder );
        }
        try {
            for (int i = 0; i < events; i++) {
                double common = commonConfiguration.distribution().trial();
                for (PropagationTestModel model : selectionPanel.getModels()) {
                    resultMap.get(model).getData()[i] = evaluate(i, common, model);
                }
            }
            displaySignal = new DialogDisplaySignal(MainWindow.getInstance(), "Event","Signal Loss, dB");
            displaySignal.show(results, events + " samples from "
                    + results.size() + " models", "Loss (dB)");
        } catch (RuntimeException e ) {
            // evaluation terminated. already handled
        }
    }

    private double evaluate(int i, double common, PropagationTestModel model ) {
        LinkResult result = generateLinkResult(common, model);
        PropagationModelConfiguration conf = model.getPropagationModel();
        try {
            return conf.evaluate(result);
        } catch (SimulationInvalidException e ) {
            RuntimeException e1 = e.getOrigin();
            int ExceptionResponse = JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                    "<html><b>Exception message:</b><br>\"" + e1.getMessage() + "\"<br>It is recommended that you check your input parameters.<br>" +
                            "Do you want to abort propagation model test?</html>", "Exception occurred in propagation model test at sample #" + i,
                    JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

            if (ExceptionResponse == 0) {
                throw new RuntimeException("Stop");
            }
            return -1;
        }
    }

    private String unit( CommonConfiguration.Common common ) {
        switch (common) {
            case Distance: return "km";
            case Frequency: return "MHz";
            default: return "m";
        }
    }

    private void generateAndShowSampleInXY() {
        DialogDisplaySignal displaySignal;
        if ( detailPanel == null ) return;
        detailPanel.updateModel();
        CommonConfiguration commonConfiguration = detailPanel.getModel().getCommonConfiguration();
        int events = commonConfiguration.samples();

        List<PropagationHolder> results = new ArrayList<PropagationHolder>();
        Map<PropagationTestModel, List<Point2D>> resultMap = new LinkedHashMap<PropagationTestModel, List<Point2D>>();
        for (PropagationTestModel model : selectionPanel.getModels()) {
            resultMap.put(model, new ArrayList<Point2D>());
        }
        try {

            for (int i = 0; i < events; i++) {
                double common = commonConfiguration.distribution().trial();
                for (PropagationTestModel model : selectionPanel.getModels()) {
                    double evaluate = evaluate(i, common, model);
                    resultMap.get(model).add( new Point2D(common, evaluate));
                }
            }

            String label = commonConfiguration.common().toString() + " (" + unit(commonConfiguration.common())+")";

            // now fill the actual results
            for (Map.Entry<PropagationTestModel, List<Point2D>> entry : resultMap.entrySet()) {
                PropagationHolder holder = new PropagationHolder();
                holder.setTitle( entry.getKey().toString() );
                holder.setSortedTitle( label );
                Collections.sort( entry.getValue() );
                holder.setData(new double[events]);
                holder.setSortedDistributions(new double[events]);
                List<Point2D> value = entry.getValue();
                for (int i = 0; i < value.size(); i++) {
                    Point2D point = value.get(i);
                    holder.getSortedDistributions()[i] = point.getX();
                    holder.getData()[i] = point.getY();
                }
                results.add( holder );
            }

            AbstractDistribution commonDistribution = (AbstractDistribution) commonConfiguration.distribution();

            double min = commonDistribution.getMin();
            double max = commonDistribution.getMax();

            String title = events + " samples from " + results.size()+ " models";
            displaySignal = new DialogDisplaySignal(MainWindow.getInstance(), "Event", "Signal Loss, dB");
            displaySignal.show(results, title, "Loss (dB)", label, max, min, events);
        } catch (RuntimeException e ) {
            // evaluation terminated. Exception already handled
        }
    }

    private LinkResult generateLinkResult( double common, PropagationTestModel model ){
        MutableLinkResult result = new MutableLinkResult();
        result.setFrequency(model.getLinkResultConfiguration().frequency().trial());
        result.setTxRxDistance(model.getLinkResultConfiguration().distance().trial());
        result.txAntenna().setHeight(model.getLinkResultConfiguration().txHeight().trial());
        result.txAntenna().setLocalEnvironment(pickLocalEnvironment(model.getLocalEnvironments().transmitterEnvironments()));
        result.rxAntenna().setHeight(model.getLinkResultConfiguration().rxHeight().trial());
        result.rxAntenna().setLocalEnvironment(pickLocalEnvironment(model.getLocalEnvironments().receiverEnvironments()));

        switch (detailPanel.getModel().getCommonConfiguration().common()) {
            case Distance:
                result.setTxRxDistance(common);
                break;
            case Frequency:
                result.setFrequency( common);
                break;
            case TX_Height:
                result.txAntenna().setHeight( common );
                break;
            case RX_Height:
                result.rxAntenna().setHeight(common);
                break;
        }
        return result;
    }
}
