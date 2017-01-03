package org.seamcat.presentation.systems.generic;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.ColocationSelectedEvent;
import org.seamcat.events.CorrelationModelChanged;
import org.seamcat.model.IdElement;
import org.seamcat.model.InterferenceLinkElement;
import org.seamcat.model.Workspace;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.generic.InterferingLinkRelativePosition.CorrelationMode;
import org.seamcat.model.generic.RelativeLocation;
import org.seamcat.model.systems.generic.SystemModelGeneric;

import org.seamcat.model.workspace.InterferenceLinkUI;
import org.seamcat.model.workspace.RelativeLocationInterferenceUI;
import org.seamcat.presentation.DistributionDialog;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.SeamcatIcons;
import org.seamcat.presentation.components.LocationModeComboboxModel;
import org.seamcat.presentation.valuepreview.ButtonWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.LabelWithValuePreviewTip;
import org.seamcat.presentation.valuepreview.ValuePreviewTextUtil;
import org.seamcat.presentation.valuepreview.ValuePreviewableDistributionAdapter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import static org.seamcat.model.factory.Factory.*;
import static org.seamcat.model.generic.InterferingLinkRelativePosition.CorrelationMode.*;

public class RelativeLocationInterferingLinkPanel extends JPanel {
    protected static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private JLabel helpHintIcon = new JLabel(SeamcatIcons.getImageIcon("SEAMCAT_ICON_INFORMATION", SeamcatIcons.IMAGE_SIZE_TOOLBAR));

    final static public Set<CorrelationMode> positionToWT = new HashSet<CorrelationMode>( Arrays.asList(
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_WT,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_WT,
            VICTIM_CLASSICAL_INTERFERER_DMA_COR_WT,
            VICTIM_CLASSICAL_INTERFERER_DMA_DYN_WT) );

    final	static public Set<CorrelationMode> positionToVR = new HashSet<CorrelationMode>( Arrays.asList(
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_IT_VR,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_WR_VR,
            VICTIM_CLASSICAL_INTERFERER_DMA_COR_VR,
            VICTIM_CLASSICAL_INTERFERER_DMA_DYN_VR) );


    final static public Set<CorrelationMode> modesForWrCenterOfItDistribution = new HashSet<CorrelationMode>( Arrays.asList(
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_VR,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_VR,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_UNIFORM_WT,
            VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_CLOSEST_WT,
            VICTIM_DMA_INTERFERER_CLASSICAL_NONE,
            VICTIM_DMA_INTERFERER_CLASSICAL_UNIFORM,
            VICTIM_DMA_INTERFERER_CLASSICAL_CLOSEST));


    private LocationModeComboboxModel locationComboBoxModel;

    private InterferenceLinkElement interferenceLink;
    private List<InterferenceLinkElement> interferenceLinks;
    private DistributionDialog distributionDialog;


    private boolean interferingIsDMA;
    private boolean victimIsDMA;
    private ActionListener coLocationListener;
    private ActionListener locationModelListener;
    private Workspace workspace;
    private RelativeLocationInterferenceUI model;
    private Distribution azimuth;
    private Distribution pathDistanceFactor;
    private Distribution turnDistribution;
    private Distribution deltaX;
    private Distribution deltaY;
    private Distribution colocateX;
    private Distribution colocateY;
    private Distribution minimumCouplingLoss;
    private Distribution protectionDistance;

    public RelativeLocationInterferingLinkPanel(JFrame parent, InterferenceLinkElement ilElem, Workspace workspace) {
        this.workspace = workspace;
        setBorder( BorderFactory.createEmptyBorder(0,0,0,0));
        initComponents();
        locationComboBoxModel = new LocationModeComboboxModel();
        locationModeComboBox.setModel(locationComboBoxModel);
        distributionDialog = new DistributionDialog(parent, true);

        InterferenceLinkUI il = ilElem.getSettings();
        RelativeLocationInterferenceUI ui = il.path().relativeLocation();
        model = ui;
        azimuth = model.pathAzimuth();
        pathDistanceFactor = model.pathDistanceFactor();
        interferenceLink = ilElem;
        interferenceLinks = workspace.getInterferenceLinkUIs();

        setCdmaMode(!(workspace.getSystem(ilElem.getInterferingSystemId()) instanceof SystemModelGeneric));
        coLocationCheckBox.setSelected(model.isCoLocated());
        setupHintAndUpdate();
        if (model.isCoLocated()) {
            modeLabel.setEnabled(false);
            locationModeComboBox.setEnabled(false);
            positionRelativeLabel.setEnabled(false);
            positionRelativeVR.setEnabled(false);
            positionRelativeWT.setEnabled(false);
            setInputXYEnabled(false);
            wrAtCenter.setEnabled(false);
            setAzimuthEnabled(false);
            setDistFactorEnabled(false);
            setSimulationRadius(false);
            activeTransmittersLabel.setEnabled(false);
            activeTransmittersInputField.setEnabled(false);
            coLocationLinksComboBox.setEnabled(true);
            coLocateDeltasSetEnable(true);
            EventBusFactory.getEventBus().publish(new ColocationSelectedEvent(this, true));
        } else {
            modeLabel.setEnabled(true);
            locationModeComboBox.setEnabled(true);
            simulationRadiusInputField.setValue(model.simulationRadius());
            activeTransmittersInputField.setValue((model.numberOfActiveTransmitters()));

            setElementStatus(locationModeComboBox.getSelectedIndex());
        }

        deltaX = ui.deltaX();
        deltaY = ui.deltaY();
        colocateX = ui.coLocationX();
        colocateY = ui.coLocationY();
        minimumCouplingLoss = ui.minimumCouplingLoss();
        protectionDistance = ui.protectionDistance();

        setMCLEnabled( true );

        distValuePreview(deltaX, deltaXValueLabel, deltaXButton);
        distValuePreview(deltaY, deltaYValueLabel, deltaYButton);
        distValuePreview(colocateX, coLocateDeltaXValueLabel, coLocateDeltaXButton);
        distValuePreview(colocateY, coLocateDeltaYValueLabel, coLocateDeltaYButton);
        distValuePreview(minimumCouplingLoss, mclValueLabel, mclButton);

        wrAtCenter.setSelected( model.setILRatTheCenter() );

        distValuePreview(protectionDistance, pdValueLabel, pdButton);
        distValuePreview(azimuth, azimuthValueLabel, azimuthButton);
        distValuePreview(pathDistanceFactor, distFactorValueLabel, distFactorButton);

        usePolygon.setSelected(ui.usePolygon());
        shape.setModel( new DefaultComboBoxModel<RelativeLocation.Shape>( RelativeLocation.Shape.values() ) );
        shape.setSelectedItem(ui.shape());
        turnDistribution = ui.turnCCW();

        shapeEnablement();
    }

    public void dispose() {
        coLocationCheckBox.removeActionListener( coLocationListener );
        distFactorButton.removeAll();
        azimuthButton.removeAll();
        locationModeComboBox.removeActionListener(locationModelListener);
        distributionDialog.dispose();
        removeAll();
    }

    private List<InterferenceLinkElement> getColocationList(boolean isDMA, InterferenceLinkElement il, List<InterferenceLinkElement> all) {
        if ( isDMA ) return Collections.emptyList();
        List<InterferenceLinkElement> links = new ArrayList<InterferenceLinkElement>();
        for ( InterferenceLinkElement link : all ) {
            RelativeLocationInterferenceUI settings = link.getSettings().path().relativeLocation();
            if ( settings.isCoLocated() && settings.coLocatedWith().equals(il.getId())) {
                // since someone is co-located with this link co-location is not possible
                return Collections.emptyList();
            }

            if ((!link.equals(il) && workspace.getSystem(link.getInterferingSystemId()) instanceof SystemModelGeneric && !settings.isCoLocated())) {
                links.add(link);
            }
        }
        return links;
    }

    public RelativeLocationInterferenceUI getModel() {
        RelativeLocationInterferenceUI prototype = prototype(RelativeLocationInterferenceUI.class, model);
        CorrelationMode value = locationComboBoxModel.getValue(locationModeComboBox.getSelectedIndex());
        CorrelationMode selected = CorrelationModesForUI.getMode(value, positionRelativeVR.isSelected());
        when(prototype.mode()).thenReturn(selected);

        if ( modesForWrCenterOfItDistribution.contains(selected)) {
            when(prototype.setILRatTheCenter()).thenReturn(wrAtCenter.isSelected());
        } else {
            when(prototype.setILRatTheCenter()).thenReturn(false);
        }

        when(prototype.deltaX()).thenReturn(deltaX);
        when(prototype.deltaY()).thenReturn(deltaY);

        if (activeTransmittersInputField.isEnabled()) {
            when(prototype.numberOfActiveTransmitters()).thenReturn(activeTransmittersInputField.getValueAsInteger());
        } else {
            when(prototype.numberOfActiveTransmitters()).thenReturn(1);
        }
        when(prototype.simulationRadius()).thenReturn(simulationRadiusInputField.getValueAsDouble());

        if (coLocationCheckBox.isSelected()) {
            when(prototype.isCoLocated()).thenReturn(true);
            IdElement<String> item = (IdElement<String>) coLocationLinksComboBox.getSelectedItem();
            when(prototype.coLocatedWith()).thenReturn(item.getElement());
            when(prototype.coLocationX()).thenReturn(colocateX);
            when(prototype.coLocationY()).thenReturn(colocateY);
        } else {
            when(prototype.isCoLocated()).thenReturn(false);
        }

        when(prototype.minimumCouplingLoss()).thenReturn(minimumCouplingLoss);

        locationModeComboBox.updateUI();

        when(prototype.protectionDistance()).thenReturn(protectionDistance);

        when(prototype.pathAzimuth()).thenReturn(azimuth);
        when(prototype.pathDistanceFactor()).thenReturn(pathDistanceFactor);

        when(prototype.usePolygon()).thenReturn(usePolygon.isSelected());
        when(prototype.shape()).thenReturn((RelativeLocation.Shape) shape.getSelectedItem());
        when(prototype.turnCCW()).thenReturn(turnDistribution);

        return build(prototype);
    }

    private void setCdmaMode(boolean interferingIsDMA) {
        List<InterferenceLinkElement> links = getColocationList( interferingIsDMA, interferenceLink, interferenceLinks );

        boolean victimClassical = workspace.getVictimSystem() instanceof SystemModelGeneric;
        positionRelativeLabel.setVisible( victimClassical );
        positionRelativeVR.setVisible( victimClassical );
        positionRelativeWT.setVisible( victimClassical );

        
        
        boolean enabled = links.size() > 0;
        coLocationCheckBox.setEnabled( enabled );
        if ( !enabled ) {
            coLocationCheckBox.setSelected( enabled );
            coLocationLinksComboBox.setEnabled( enabled );
            coLocateDeltasSetEnable( enabled );
        }
        DefaultComboBoxModel<IdElement<String>> boxModel = new DefaultComboBoxModel<>();
        for (InterferenceLinkElement link : links) {
            boxModel.addElement(new IdElement<String>(link.getName(), link.getId()));
        }

        coLocationLinksComboBox.setModel( boxModel );
        if ( model.isCoLocated() ) {
            for (int i = 0; i<boxModel.getSize(); i++) {
                IdElement<String> elementAt = boxModel.getElementAt(i);
                if ( elementAt.getElement().equals( model.coLocatedWith())) {
                    coLocationLinksComboBox.setSelectedIndex(i);
                }

            }
        }

        this.interferingIsDMA = interferingIsDMA;
        this.victimIsDMA = !(workspace.getVictimSystem() instanceof SystemModelGeneric);

        List<CorrelationMode> group = CorrelationModesForUI.getGroup(victimIsDMA, interferingIsDMA);
        if (interferingIsDMA) {
            distFactorLabel.setText("Path distance");
            distFactorUnitLabel.setText("km");
            simulationRadiusInputField.setValue(1);
            mclLabel.setEnabled(true);
            mclUnitLabel.setEnabled(true);
        } else {
            distFactorLabel.setText("Path distance factor");
            distFactorUnitLabel.setText("");
            mclLabel.setEnabled(victimIsDMA);
            mclUnitLabel.setEnabled(victimIsDMA);
        }
        locationComboBoxModel.setValues(group);
        int index = CorrelationModesForUI.getIndex(victimIsDMA, interferingIsDMA, model.mode());
        locationModeComboBox.setSelectedIndex(index);
        if ( CorrelationModesForUI.alignedSelection(victimIsDMA, interferingIsDMA, model.mode())) {
            setRelativePositionTo(model.mode());
        } else {
            setRelativePositionTo(group.get( index ));
        }
    }

    private void setRelativePositionTo(CorrelationMode correlationMode) {
        //System.out.println("Selection: " + correlationMode);
        if ( positionToVR.contains( correlationMode ) ) {
            positionRelativeVR.setSelected( true );
        }
        if ( positionToWT.contains( correlationMode ) ) {
            positionRelativeWT.setSelected( true );
        }
    }

    private void updateRelativePositionButtons (int index){
        if ( interferenceLink == null ) return;
        setRelativePositionEnabled( true );
    }
    private void updateInputXYButtons( int index ) {
        if ( interferenceLink == null ) return;
        setInputXYEnabled( true );
    }

    private void updateInputAzimuth( int index ) {
        if ( interferenceLink == null ) return;
        boolean victimDma = !(workspace.getVictimSystem() instanceof SystemModelGeneric);
        // azimuth should be off when selection is "Cor..."
        if ( interferingIsDMA && victimDma ) {
            setAzimuthEnabled( index == 1 );
        } else if ( interferingIsDMA && !victimDma ) {
            setAzimuthEnabled( index == 1 || index == 3 );
        } else if ( !interferingIsDMA && victimDma ) {
            setAzimuthEnabled( index == 0 || index == 1 || index == 2 || index == 4 || index == 6 );
        } else {
            // interferer classical, victim classical
            setAzimuthEnabled( index == 0 || index == 1 || index == 2  );
        }
    }

    private void updateDistFactorButton( int index ) {
        if ( interferenceLink == null ) return;
        if ( interferingIsDMA ) {
            // enable distribution factor if interferer is DMA and selection is "Dyn. (WTx -> interfering BS ref.cell)" or "Dyn. (Vr -> interfering BS ref.cell)"
            setDistFactorEnabled( index == 1 || index == 3 );
        } else {
            if ( !(workspace.getVictimSystem() instanceof SystemModelGeneric)) {
                // enable if interferer is classical (victim is DMA) and selection is "None"
                setDistFactorEnabled( index == 0 || index == 4 || index == 6);
            } else {
                // enable if interferer is classical (victim is classical) and selection is "None"
                setDistFactorEnabled( index == 0 );}
        }
    }

    private void updateProtectionDistanceButton( int index ) {
        if ( interferenceLink == null ) return;
        if ( interferingIsDMA ) {
            setProtectionDistance(false);
        } else {
            setProtectionDistance(index == 0 || index == 1 || index == 2);
        }

    }

    private void updateSimulationRadius( int index ) {
        if ( interferenceLink == null ) return;
        // set simulation radius if interferer is classical and selection is "None"
        setSimulationRadius( !interferingIsDMA && (index == 0) );
    }

    private void updateActiveTransmitters( int index ) {
        if ( interferenceLink == null ) return;
        // set active transmitter if interferer is classical and selection is "None" or "Uniform Density"
        setActiveTransmitters( !interferingIsDMA && ( index == 0 || index == 1 ) );
    }

    private void updateIPanelVisible( int index ) {
        if ( interferenceLink == null ) return;
        // set iPanel if interferer is classical and selection is "Uniform Density" or "Closest Interferer"
        EventBusFactory.getEventBus().publish(new ColocationSelectedEvent(this, interferingIsDMA || (index != 1 && index != 2)));
    }

    private void updateWrCenter( int index ) {
        boolean enabled =  !interferingIsDMA && (index == 0 || index == 1 || index == 2);
        wrAtCenter.setEnabled( enabled );
    }

    private void setRelativePositionEnabled( boolean enabled ){
        positionRelativeLabel.setEnabled( enabled );
        positionRelativeVR.setEnabled( enabled );
        positionRelativeWT.setEnabled( enabled );
    }
    private void setInputXYEnabled( boolean enabled ) {
        deltaXButton.setEnabled(enabled);
        deltaXValueLabel.setEnabled( enabled );
        deltaXLabel.setEnabled( enabled );
        deltaXUnitLabel.setEnabled(enabled);
        deltaYValueLabel.setEnabled( enabled );
        deltaYLabel.setEnabled( enabled );
        deltaYButton.setEnabled(enabled);
        deltaYUnitLabel.setEnabled(enabled);
    }

    private void setMCLEnabled( boolean enabled ) {
        mclButton.setEnabled( enabled );
        mclLabel.setEnabled( enabled);
        mclValueLabel.setEnabled( enabled );
        mclUnitLabel.setEnabled(enabled);
    }

    private void setAzimuthEnabled( boolean enabled ) {
        azimuthButton.setEnabled( enabled );
        azimuthLabel.setEnabled( enabled );
        azimuthValueLabel.setEnabled( enabled );
        azimuthUnitLabel.setEnabled(enabled);
    }

    private void setDistFactorEnabled( boolean enabled ) {
        distFactorButton.setEnabled( enabled );
        distFactorLabel.setEnabled( enabled );
        distFactorValueLabel.setEnabled( enabled );
        distFactorUnitLabel.setEnabled(enabled);
    }

    private void setSimulationRadius( boolean enabled ) {
        simulationRadiusInputField.setEnabled( enabled );
        simulationLabel.setEnabled( enabled );
        if ( !enabled ) simulationRadiusInputField.setValue(1);
        simulationRadiusUnitLabel.setEnabled(enabled);
    }

    private void setActiveTransmitters( boolean enabled ) {
        activeTransmittersInputField.setEnabled( enabled );
        activeTransmittersLabel.setEnabled( enabled );
        if ( !enabled ) activeTransmittersInputField.setValue(1);
    }

    private void setProtectionDistance( boolean enabled){
        protectionDistanceLabel.setEnabled( enabled );
        pdButton.setEnabled(enabled);
        pdValueLabel.setEnabled(enabled);
        protectionDistanceUnitLabel.setEnabled( enabled );
    }

    private void setElementStatus(int index) {
        updateRelativePositionButtons ( index );
        updateInputXYButtons( index );
        updateInputAzimuth(index);
        updateDistFactorButton( index );
        updateProtectionDistanceButton ( index );
        updateSimulationRadius( index );
        updateActiveTransmitters(index);
        updateIPanelVisible( index );
        updateWrCenter( index );
        shapeEnablement();
    }

    private void shapeEnablement() {
        CorrelationMode mode = locationComboBoxModel.getValue(locationModeComboBox.getSelectedIndex());
        boolean enable = ( mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_VR ||
                mode == VICTIM_DMA_INTERFERER_CLASSICAL_NONE ||
                mode == VICTIM_DMA_INTERFERER_CLASSICAL_DYN_IT ||
                mode == VICTIM_DMA_INTERFERER_CLASSICAL_DYN_WR ||
                mode == VICTIM_CLASSICAL_INTERFERER_CLASSICAL_RELATIVE_LOCATION_NONE_WT);
        usePolygon.setEnabled( enable );
        if ( enable ) {
            enable = usePolygon.isSelected();
        }
        shapeLabel.setEnabled(enable);
        shape.setEnabled(enable );
        turnLabel.setEnabled( enable );
        turn.setEnabled(enable);
        turnUnit.setEnabled(enable);
    }

    private void locationModelComboBoxActionPerformed() {
        EventBusFactory.getEventBus().publish( new CorrelationModelChanged());
        setElementStatus(locationModeComboBox.getSelectedIndex());
    }

    private void coLocateDeltasSetEnable( boolean enable ) {
        coLocateDeltaXLabel.setEnabled( enable );
        coLocateDeltaXUnit.setEnabled( enable );
        coLocateDeltaXValueLabel.setEnabled(enable);
        coLocateDeltaXButton.setEnabled(enable );
        coLocateDeltaYLabel.setEnabled( enable );
        coLocateDeltaYUnit.setEnabled( enable );
        coLocateDeltaYValueLabel.setEnabled(enable);
        coLocateDeltaYButton.setEnabled(enable);
    }

    private void setupHintAndUpdate() {
        String helpHintText = STRINGLIST.getString("COLOCATION_INFORMATION");
        helpHintIcon.setToolTipText(helpHintText);
        helpHintIcon.setVisible(helpHintText != null);
        revalidate();
        repaint();
    }

    private void coLocationCheckBoxActionPerformed() {
        EventBusFactory.getEventBus().publish(new ColocationSelectedEvent(this, coLocationCheckBox.isSelected()));
        modeLabel.setEnabled(!coLocationCheckBox.isSelected());
        locationModeComboBox.setEnabled(!coLocationCheckBox.isSelected());
        coLocateDeltasSetEnable( coLocationCheckBox.isSelected() );
        setupHintAndUpdate();

        if (coLocationCheckBox.isSelected()) {
            positionRelativeLabel.setEnabled(false);
            positionRelativeVR.setEnabled(false);
            positionRelativeWT.setEnabled(false);
            wrAtCenter.setEnabled(false);
            setInputXYEnabled(false);
            setAzimuthEnabled( false );
            setDistFactorEnabled(false);
            setSimulationRadius(false);
            activeTransmittersLabel.setEnabled(false);
            activeTransmittersInputField.setEnabled(false);
        } else {
            setElementStatus(locationModeComboBox.getSelectedIndex());
        }
        coLocationLinksComboBox.setEnabled(coLocationCheckBox.isSelected());
    }

    private Distribution distButtonClicked( Distribution current, LabelWithValuePreviewTip label, ButtonWithValuePreviewTip button ) {
        Distribution distribution = distributionDialog( current );

        distValuePreview(distribution, label, button);
        return distribution;
    }

    private void distValuePreview(Distribution distribution, LabelWithValuePreviewTip label, ButtonWithValuePreviewTip button) {
        label.setText(ValuePreviewTextUtil.previewLabelText( distribution.toString() ));

        ValuePreviewableDistributionAdapter previewable = new ValuePreviewableDistributionAdapter((AbstractDistribution) distribution);
        label.setPreviewable(previewable);
        button.setPreviewable(previewable);
    }

    private Distribution distributionDialog(Distribution distribution) {
        if ( distributionDialog.showDistributionDialog((AbstractDistribution) distribution, "Edit distribution")) {
            return distributionDialog.getDistribution();
        }

        return distribution;
    }

    private void initComponents() {
        CellConstraints cc = new CellConstraints();
        setLayout(new FormLayout(
                "65dlu, $lcgap, 10dlu:grow, $lcgap, 60dlu, $lcgap, 13dlu","17*(default, $lgap), default"));

        //---- modeLabel ----
        add(modeLabel, cc.xy(1, 1));

        //---- locationModeComboBox ----
        locationModelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                locationModelComboBoxActionPerformed();
            }
        };
        locationModeComboBox.addActionListener(locationModelListener);
        add(locationModeComboBox, cc.xywh(3, 1, 3, 1));

        ButtonGroup positionGroup = new ButtonGroup();
        positionGroup.add(positionRelativeWT);
        positionGroup.add(positionRelativeVR);

        add(positionRelativeLabel, cc.xy(1, 3));
        add(positionRelativeWT, cc.xywh(3, 3, 3, 1));
        add(positionRelativeVR, cc.xywh(3, 5, 3, 1));

        add(deltaXLabel, cc.xy(1, 7));
        add(deltaXValueLabel, cc.xy(3, 7));
        deltaXButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deltaX = distButtonClicked(deltaX, deltaXValueLabel, deltaXButton);
            }
        });
        add(deltaXButton, cc.xy(5, 7));
        add(deltaXUnitLabel, cc.xy(7, 7));

        add(deltaYLabel, cc.xy(1, 9));
        add(deltaYValueLabel, cc.xy(3, 9));
        deltaYButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deltaY = distButtonClicked(deltaY, deltaYValueLabel, deltaYButton);
            }
        });
        add(deltaYButton, cc.xy(5, 9));
        add(deltaYUnitLabel, cc.xy(7, 9));

        wrAtCenter.setToolTipText("This selection will disable any settings you have made in the relative location of ILR->ILT panel");
        add( wrAtCenter, cc.xyw(1, 11, 3));

        add(azimuthLabel, cc.xy(1, 13));
        add(azimuthValueLabel, cc.xy(3, 13));
        azimuthButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                azimuth = distButtonClicked(azimuth, azimuthValueLabel, azimuthButton);
            }
        });
        add(azimuthButton, cc.xy(5, 13));
        add(azimuthUnitLabel, cc.xy(7, 13));

        add(distFactorLabel, cc.xy(1, 15));
        add(distFactorValueLabel, cc.xy(3, 15));
        distFactorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                pathDistanceFactor = distButtonClicked(pathDistanceFactor, distFactorValueLabel, distFactorButton);
            }
        });
        add(distFactorButton, cc.xy(5, 15));
        add(distFactorUnitLabel, cc.xy(7, 15));

        add(simulationLabel, cc.xy(1, 17)); add(simulationRadiusInputField, cc.xy(5, 17)); add(simulationRadiusUnitLabel, cc.xy(7, 17));

        add(activeTransmittersLabel, cc.xyw(1, 19, 3));
        activeTransmittersInputField.setIntegerMode(true);
        add(activeTransmittersInputField, cc.xy(5, 19));


        coLocationListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                coLocationCheckBoxActionPerformed();
            }
        };
        coLocationCheckBox.addActionListener(coLocationListener);

        add(coLocationCheckBox, cc.xy(1, 21));
        add(coLocationLinksComboBox, cc.xywh(3, 21, 3, 1));
        add(helpHintIcon, cc.xy(7, 21));

        add(coLocateDeltaXLabel, cc.xy(1, 23));
        add(coLocateDeltaXValueLabel, cc.xy(3, 23));
        coLocateDeltaXButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                colocateX = distButtonClicked(colocateX, coLocateDeltaXValueLabel, coLocateDeltaXButton);
            }
        });
        add(coLocateDeltaXButton, cc.xy(5, 23));
        add(coLocateDeltaXUnit, cc.xy(7, 23));

        add(coLocateDeltaYLabel, cc.xy(1, 25));
        add(coLocateDeltaYValueLabel, cc.xy(3, 25));
        coLocateDeltaYButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                colocateY = distButtonClicked(colocateY, coLocateDeltaYValueLabel, coLocateDeltaYButton);
            }
        });
        add(coLocateDeltaYButton, cc.xy(5, 25));
        add(coLocateDeltaYUnit, cc.xy(7, 25));

        add(mclLabel, cc.xy(1, 27));
        add(mclValueLabel, cc.xy(3, 27));
        mclButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                minimumCouplingLoss = distButtonClicked(minimumCouplingLoss, mclValueLabel, mclButton);
            }
        });
        add(mclButton, cc.xy(5, 27));
        add(mclUnitLabel, cc.xy(7, 27));

        add(protectionDistanceLabel, cc.xy(1, 29));
        add(pdValueLabel, cc.xy(3, 29));
        pdButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                protectionDistance = distButtonClicked(protectionDistance, pdValueLabel, pdButton);
            }
        });
        add(pdButton, cc.xy(5, 29));
        add(protectionDistanceUnitLabel, cc.xy(7, 29));

        usePolygon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                shapeEnablement();
            }
        });
        add(usePolygon, cc.xy(1, 31));

        add(shapeLabel, cc.xy(1, 33)); add(shape, cc.xy(5, 33));

        add(turnLabel, cc.xy(1, 35)); add(turn, cc.xy(5, 35)); add(turnUnit, cc.xy(7, 35));

        turn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                DistributionDialog dialog = new DistributionDialog(MainWindow.getInstance(), true);
                if (dialog.showDistributionDialog((AbstractDistribution) turnDistribution, "Edit distribution")) {
                    turnDistribution = dialog.getDistribution();
                }
            }
        });
    }

    private ButtonWithValuePreviewTip deltaXButton         = new ButtonWithValuePreviewTip("Distribution");
    private ButtonWithValuePreviewTip deltaYButton         = new ButtonWithValuePreviewTip("Distribution");
    private ButtonWithValuePreviewTip azimuthButton        = new ButtonWithValuePreviewTip("Distribution");
    private ButtonWithValuePreviewTip pdButton             = new ButtonWithValuePreviewTip("Distribution");
    private ButtonWithValuePreviewTip distFactorButton     = new ButtonWithValuePreviewTip("Distribution");
    private ButtonWithValuePreviewTip coLocateDeltaXButton = new ButtonWithValuePreviewTip("Distribution");
    private ButtonWithValuePreviewTip mclButton            = new ButtonWithValuePreviewTip("Distribution");
    private ButtonWithValuePreviewTip coLocateDeltaYButton = new ButtonWithValuePreviewTip("Distribution");

    private LabelWithValuePreviewTip deltaXValueLabel = new LabelWithValuePreviewTip();
    private LabelWithValuePreviewTip deltaYValueLabel = new LabelWithValuePreviewTip();
    private LabelWithValuePreviewTip azimuthValueLabel = new LabelWithValuePreviewTip();
    private LabelWithValuePreviewTip pdValueLabel = new LabelWithValuePreviewTip();
    private LabelWithValuePreviewTip distFactorValueLabel = new LabelWithValuePreviewTip();
    private LabelWithValuePreviewTip coLocateDeltaYValueLabel = new LabelWithValuePreviewTip();
    private LabelWithValuePreviewTip mclValueLabel = new LabelWithValuePreviewTip();
    private LabelWithValuePreviewTip coLocateDeltaXValueLabel = new LabelWithValuePreviewTip();

    private JLabel modeLabel = new JLabel("Mode");
    private JComboBox locationModeComboBox = new JComboBox();
    private JLabel deltaXLabel = new JLabel("Delta X");
    private JLabel deltaXUnitLabel = new JLabel("km");
    private JLabel deltaYLabel = new JLabel("Delta Y");
    private JLabel deltaYUnitLabel = new JLabel("km");
    private JCheckBox wrAtCenter = new JCheckBox("Set ILR at the center of the ILT distribution");
    private JLabel azimuthLabel = new JLabel("Path azimuth");
    private JLabel azimuthUnitLabel = new JLabel("deg");
    private JLabel distFactorLabel = new JLabel("Path distance");
    private JLabel distFactorUnitLabel = new JLabel("km");
    private JLabel simulationLabel = new JLabel("Simulation radius");
    private CalculatorInputField simulationRadiusInputField = new CalculatorInputField();
    private JLabel simulationRadiusUnitLabel = new JLabel("km");
    private JLabel activeTransmittersLabel = new JLabel("Number of active transmitters");
    private CalculatorInputField activeTransmittersInputField = new CalculatorInputField();
    private JCheckBox coLocationCheckBox = new JCheckBox("To position with");
    private JComboBox coLocationLinksComboBox = new JComboBox();
    private JLabel coLocateDeltaXLabel = new JLabel("   Delta x");
    private JLabel coLocateDeltaXUnit = new JLabel("km");
    private JLabel coLocateDeltaYLabel = new JLabel("   Delta y");

    private JLabel coLocateDeltaYUnit = new JLabel("km");
    private JRadioButton positionRelativeWT = new JRadioButton("Victim Link Transmitter");
    private JRadioButton positionRelativeVR = new JRadioButton("Victim Link Receiver");
    private JLabel positionRelativeLabel = new JLabel("Position relative to");;

    private JLabel mclLabel = new JLabel("Minimum coupling Loss");
    private JLabel mclUnitLabel = new JLabel("dB");
    private JLabel protectionDistanceLabel = new JLabel(STRINGLIST.getString("DENSITY_PROTECTION_DIST"));
    private JLabel protectionDistanceUnitLabel = new JLabel("km");

    private JCheckBox usePolygon = new JCheckBox("Use a polygon");
    private JLabel shapeLabel = new JLabel("Shape of the polygon");
    private JComboBox<RelativeLocation.Shape> shape = new JComboBox<>();
    private JLabel turnLabel = new JLabel("Turn ccw");
    private JButton turn = new JButton("Distribution");
    private JLabel turnUnit = new JLabel("degree");
}
