package org.seamcat.presentation.systems;

import org.seamcat.model.cellular.CellularLayout;
import org.seamcat.presentation.layout.VerticalSubPanelLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.seamcat.model.cellular.CellularLayout.SystemLayout.*;
import static org.seamcat.model.factory.Factory.*;

public class SystemLayoutPanel extends JPanel {

    private JRadioButton center = new JRadioButton("Center of \"infinite\" network");
    private JRadioButton left = new JRadioButton("Left hand side of network edge");
    private JRadioButton right = new JRadioButton("Right hand side of network edge");
    private JCheckBox interferenceFromCluster = new JCheckBox("Measure Interference from entire cluster");
    private JCheckBox plotWrapAround = new JCheckBox("Generate Wrap-Around");

    private Box innerPanel;
    private boolean entireEnable;
    private CellularPositionPanel.Refresher refresher;
    private CellularPositionHolder cellularPositionHolder;

    public SystemLayoutPanel( boolean entireEnable, CellularPositionPanel.Refresher refresher) {
        this.entireEnable = entireEnable;
        this.refresher = refresher;
        initializeWidgets();
        setLayout( new VerticalSubPanelLayoutManager());
        add( innerPanel );
    }

    public void setModel(CellularPositionHolder cellularPositionHolder) {
        this.cellularPositionHolder = cellularPositionHolder;
        refreshFromModel();
    }

    public CellularPositionHolder getModel() {
        return cellularPositionHolder;
    }

    public void initializeWidgets() {
        innerPanel = Box.createVerticalBox();

        ButtonGroup networkEdgeButtons = new ButtonGroup();
        networkEdgeButtons.add(center);
        networkEdgeButtons.add(left);
        networkEdgeButtons.add(right);

        innerPanel.add(center);
        innerPanel.add(left);
        innerPanel.add(right);

        innerPanel.add(Box.createVerticalStrut(20));
        innerPanel.add(interferenceFromCluster);
        innerPanel.add(plotWrapAround);

        for (Component c: innerPanel.getComponents()) {
            if (c instanceof JComponent) {
                ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
            }
        }

        center.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if ( center.isSelected() ) {
                    localUpdate();
                    updateModel();
                }

            }
        });

        left.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if ( left.isSelected()) {
                    localUpdate();
                    updateModel();
                }
            }
        });

        right.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                if (right.isSelected()) {
                    localUpdate();
                    updateModel();
                }
            }
        });

        plotWrapAround.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                updateModel();
            }
        });

        if ( !entireEnable) {
            interferenceFromCluster.setVisible( false );
        } else {
            interferenceFromCluster.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    updateModel();
                }
            });
        }
    }

    public void refreshFromModel() {
        CellularPosition model = getModel().getCellularPosition();
        if ( model.tiers() != 2 ) {
            plotWrapAround.setSelected( false );
            plotWrapAround.setEnabled( false );
        } else {
            plotWrapAround.setEnabled( true );
            plotWrapAround.setSelected( model.generateWrapAround() );
        }

        if ( model.layout().equals(CenterOfInfiniteNetwork)) {
            center.setSelected( true );
        } else if ( model.layout().equals(LeftHandSideOfNetworkEdge)) {
            left.setSelected(true);
        } else {
            right.setSelected(true);
        }

        interferenceFromCluster.setEnabled(true);
        interferenceFromCluster.setSelected( model.measureFromEntireCluster());
    }

    private void localUpdate() {
        CellularPosition prototype = prototype(CellularPosition.class, getModel().getCellularPosition());
        if ( center.isSelected() ) {
            when(prototype.referenceSector()).thenReturn(0);
            when(prototype.referenceCellId()).thenReturn(0);
        } else {
            // Left or Right:
            when(prototype.referenceSector()).thenReturn(0);
            switch (getModel().getCellularPosition().tiers()) {
                case 2:
                    when(prototype.referenceCellId()).thenReturn(left.isSelected() ? 13 : 7);
                    break;
                case 1:
                    if ( getModel().getCellularPosition().sectorType().equals(CellularLayout.SectorSetup.TriSector3GPP)) {
                        when(prototype.referenceCellId()).thenReturn(left.isSelected()?4:6);
                    } else {
                        when(prototype.referenceCellId()).thenReturn(left.isSelected() ? 3 : 1);
                    }
                    break;
                default:
                    when(prototype.referenceCellId()).thenReturn(0);
            }
        }
        getModel().setCellularPosition(build(prototype));
    }

    public void updateModel() {
        CellularPosition prototype = prototype(CellularPosition.class, getModel().getCellularPosition());
        when(prototype.generateWrapAround()).thenReturn(plotWrapAround.isSelected());
        when(prototype.measureFromEntireCluster()).thenReturn(interferenceFromCluster.isSelected());
        if ( center.isSelected() ) {
            when(prototype.layout()).thenReturn(CenterOfInfiniteNetwork);
        } else {
            // Left or Right:
            CellularLayout.SystemLayout layout = left.isSelected() ? LeftHandSideOfNetworkEdge : RightHandSideOfNetworkEdge;
            when(prototype.layout()).thenReturn(layout);
        }
        getModel().setCellularPosition( build(prototype) );

        refresher.refresh();
    }
}
