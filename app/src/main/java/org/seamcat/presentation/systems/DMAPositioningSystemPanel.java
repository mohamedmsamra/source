package org.seamcat.presentation.systems;

import org.seamcat.calculator.CalculatorInputField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;

import static org.seamcat.model.cellular.CellularLayout.SectorSetup.*;
import static org.seamcat.model.factory.Factory.*;

public class DMAPositioningSystemPanel extends JPanel {

    private static DecimalFormat numberFormat = new DecimalFormat("0.0##");
    private JLabel baseStationCountValueLabel;
    private JLabel	bs2bsDistanceValueLabel;
    private JRadioButton omniDirectionalCellsRadioButton;
    private JRadioButton triSectorCells_3GGP_RadioButton;
    private JRadioButton triSectorCells_3GGP2_RadioButton;
    private JRadioButton twoTiersRadioButton;
    private JRadioButton oneTierRadioButton;
    private JRadioButton singleCellRadioButton;
    private CalculatorInputField cellRadiusInputField;
    private JPanel innerPanel;
    private CellularPositionPanel.Refresher refresher;
    private CellularPositionHolder cellularPositionHolder;

    public void setModel(CellularPositionHolder cellularPositionHolder) {
        this.cellularPositionHolder = cellularPositionHolder;
        refreshFromModel();
    }

    public CellularPositionHolder getModel() {
        return cellularPositionHolder;
    }

    public DMAPositioningSystemPanel(CellularPositionPanel.Refresher refresher) {
        this.refresher = refresher;
        init();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(innerPanel);
    }

    public void init() {
        innerPanel = new JPanel();

        JLabel baseStationCountLabel = new JLabel("Number of base stations in the system ");
        baseStationCountValueLabel = new JLabel();
        baseStationCountValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        baseStationCountValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel labelCellLayout = new JLabel("Cell layout");
        JLabel cellRadiusLabel = new JLabel("Cell radius");
        cellRadiusInputField = new CalculatorInputField();
        JLabel cellRadiusUnitLabel = new JLabel("km");

        JLabel bs2bsDistanceLabel = new JLabel("BS to BS distance ");
        bs2bsDistanceValueLabel = new JLabel();
        bs2bsDistanceValueLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        bs2bsDistanceValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel bs2bsDistanceUnitLabel = new JLabel("km");

        innerPanel.setLayout(new GridBagLayout());
        innerPanel.add(baseStationCountLabel, makeGridConstraint(0, 0, 2, 1));
        innerPanel.add(baseStationCountValueLabel, makeGridConstraint(2, 0));

        innerPanel.add(Box.createVerticalStrut(4), makeGridConstraint(0, 1));

        innerPanel.add(labelCellLayout, makeGridConstraint(0, 2));
        GridBagConstraints radioButtonPanelLayout = makeGridConstraint(1, 2, 2, 1);
        radioButtonPanelLayout.anchor = GridBagConstraints.BASELINE_TRAILING;
        innerPanel.add(makeRadioButtonPanel(), radioButtonPanelLayout);

        innerPanel.add(Box.createVerticalStrut(4), makeGridConstraint(0, 3));

        innerPanel.add(cellRadiusLabel, makeGridConstraint(0, 4));
        GridBagConstraints cellRadiusInputFieldLayout = makeGridConstraint(1, 4);
        cellRadiusInputFieldLayout.anchor = GridBagConstraints.BASELINE_TRAILING;
        innerPanel.add(cellRadiusInputField, cellRadiusInputFieldLayout);
        innerPanel.add(cellRadiusUnitLabel, makeGridConstraint(2, 4));

        innerPanel.add(bs2bsDistanceLabel, makeGridConstraint(0, 5));
        innerPanel.add(bs2bsDistanceValueLabel, makeGridConstraint(1, 5));
        innerPanel.add(bs2bsDistanceUnitLabel, makeGridConstraint(2, 5));

        innerPanel.add(Box.createVerticalGlue(), makeExpandingVSpaceGridConstraint(0, 6));

        cellRadiusInputField.addFocusListener( new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                updateModel();
            }
        });

        ActionListener cellLayoutListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateModel();
            }
        };
        omniDirectionalCellsRadioButton.addActionListener(cellLayoutListener);
        triSectorCells_3GGP_RadioButton.addActionListener(cellLayoutListener);
        triSectorCells_3GGP2_RadioButton.addActionListener(cellLayoutListener);
        twoTiersRadioButton.addActionListener(cellLayoutListener);
        oneTierRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if ( oneTierRadioButton.isSelected() ) {
                    localUpdate();
                    updateModel();
                }
            }
        });
        singleCellRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if ( singleCellRadioButton.isSelected() ) {
                    localUpdate();
                    updateModel();
                }
            }
        });
    }

    private Component makeRadioButtonPanel() {
        omniDirectionalCellsRadioButton = new JRadioButton("Single-Sector");
        triSectorCells_3GGP_RadioButton = new JRadioButton("Tri-Sector (3GPP)");
        triSectorCells_3GGP2_RadioButton = new JRadioButton("Tri-Sector (3GPP2)");
        twoTiersRadioButton = new JRadioButton("2-tiers");
        oneTierRadioButton = new JRadioButton("1-tier");
        singleCellRadioButton = new JRadioButton("Single cell");

        ButtonGroup cellLayoutButtonGroup = new ButtonGroup();
        cellLayoutButtonGroup.add(omniDirectionalCellsRadioButton);
        cellLayoutButtonGroup.add(triSectorCells_3GGP_RadioButton);
        cellLayoutButtonGroup.add(triSectorCells_3GGP2_RadioButton);

        ButtonGroup tierButtonGroup = new ButtonGroup();
        tierButtonGroup.add(twoTiersRadioButton);
        tierButtonGroup.add(oneTierRadioButton);
        tierButtonGroup.add(singleCellRadioButton);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(twoTiersRadioButton, makeGridConstraint(0, 0));
        panel.add(oneTierRadioButton, makeGridConstraint(0, 1));
        panel.add(singleCellRadioButton, makeGridConstraint(0, 2));
        panel.add(omniDirectionalCellsRadioButton, makeGridConstraint(1, 0));
        panel.add(triSectorCells_3GGP_RadioButton, makeGridConstraint(1, 1));
        panel.add(triSectorCells_3GGP2_RadioButton, makeGridConstraint(1, 2));
        return panel;
    }

    protected GridBagConstraints makeGridConstraint(int col, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = col;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        if (col==1) {
            constraints.weightx=1.0;
        }
        return constraints;
    }

    protected GridBagConstraints makeGridConstraint(int col, int row, int colspan, int rowspan) {
        GridBagConstraints constraints = makeGridConstraint(col, row);
        constraints.gridwidth = colspan;
        constraints.gridheight = rowspan;
        return constraints;
    }

    protected Object makeExpandingVSpaceGridConstraint(int col, int row) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = col;
        constraints.gridy = row;
        if (col==0) {
            constraints.weighty=1.0;
        }
        return constraints;
    }

    public void refreshFromModel() {
        CellularPosition model = getModel().getCellularPosition();
        cellRadiusInputField.setValue(model.cellRadius());

        omniDirectionalCellsRadioButton.setSelected(model.sectorType().equals(SingleSector));
        triSectorCells_3GGP_RadioButton.setSelected(model.sectorType().equals(TriSector3GPP));
        triSectorCells_3GGP2_RadioButton.setSelected(model.sectorType().equals(TriSector3GPP2));

        int baseStations = model.sectorType().equals(SingleSector) ? 1 : 3;
        switch ( model.tiers() ) {
            case 0:
                singleCellRadioButton.setSelected( true );
                break;
            case 1:
                baseStations *= 7;
                oneTierRadioButton.setSelected(true );
                break;
            default:
                baseStations *= 19;
                twoTiersRadioButton.setSelected(true);
        }
        baseStationCountValueLabel.setText("" + baseStations );

        String text = "     " + numberFormat.format(ReferenceCellSelector.getInterCellDistance(model));
        bs2bsDistanceValueLabel.setText(text);
    }

    private void localUpdate() {
        CellularPositionHolder model = getModel();
        CellularPosition prototype = prototype(CellularPosition.class, model.getCellularPosition());
        if ( oneTierRadioButton.isSelected() ){
            if ( model.getCellularPosition().referenceCellId() > 6 ) {
                when(prototype.referenceCellId()).thenReturn(0);
            }
        } else if ( singleCellRadioButton.isSelected() ) {
            if ( model.getCellularPosition().referenceCellId() > 0 ) {
                when(prototype.referenceCellId()).thenReturn(0);
            }
        }
        model.setCellularPosition( build(prototype) );
    }

    public void updateModel() {
        CellularPositionHolder model = getModel();
        int baseStations = omniDirectionalCellsRadioButton.isSelected() ? 1 : 3;
        CellularPosition prototype = prototype(CellularPosition.class, model.getCellularPosition());
        when(prototype.cellRadius()).thenReturn(cellRadiusInputField.getValueAsDouble());
        if ( twoTiersRadioButton.isSelected() ) {
            baseStations *= 19;
            when(prototype.tiers()).thenReturn(2);
        } else if ( oneTierRadioButton.isSelected() ) {
            baseStations *= 7;
            when(prototype.tiers()).thenReturn(1);
            when(prototype.generateWrapAround()).thenReturn(false);
        } else {
            when(prototype.tiers()).thenReturn(0);
            when(prototype.generateWrapAround()).thenReturn(false);
        }
        baseStationCountValueLabel.setText("" + baseStations );

        if (omniDirectionalCellsRadioButton.isSelected()) {
            when(prototype.referenceSector()).thenReturn(0);
            when(prototype.sectorType()).thenReturn(SingleSector);
        } else {
            if(triSectorCells_3GGP2_RadioButton.isSelected()){
                when(prototype.sectorType()).thenReturn(TriSector3GPP2);
            }else if (triSectorCells_3GGP_RadioButton.isSelected()){
                when(prototype.sectorType()).thenReturn(TriSector3GPP);
            }
        }
        model.setCellularPosition( build(prototype) );

        refresher.refresh();
    }
}