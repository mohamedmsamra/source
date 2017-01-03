package org.seamcat.presentation.components;

import org.apache.log4j.Logger;
import org.seamcat.function.EmissionMaskImpl;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.presentation.DialogTableToDataSet;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static org.seamcat.model.factory.Factory.*;

public class DiscreteFunction2Panel extends JPanel {

    private static final Logger LOG = Logger.getLogger(DiscreteFunction2Panel.class);

    private JTable dataTable;
    private DiscreteFunction2TableModelAdapter model = new DiscreteFunction2TableModelAdapter();
    private UnwantedEmissionGraph2 userDefinitionPanel = new UnwantedEmissionGraph2(model);
    private GenericPanelEditor<ACLRUI> editor;

    public DiscreteFunction2Panel() {
        super(new BorderLayout());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Constructing Panel for 3D Functions");
        }
        dataTable = new SeamcatTable(model);

        JScrollPane dataTableScrollPane = new JScrollPane(dataTable);
        Dimension paneDims = new Dimension(225, 350);
        dataTableScrollPane.setMinimumSize(paneDims);
        dataTableScrollPane.setPreferredSize(paneDims);
        dataTableScrollPane.setMaximumSize(paneDims);

        GridBagConstraints constr = new GridBagConstraints(
                GridBagConstraints.RELATIVE, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTH, GridBagConstraints.VERTICAL, new Insets(
                0, 0, 0, 0), 0, 0);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.add(dataTableScrollPane, constr);
        centerPanel.add(new DialogFunctionButtonPanel(model), constr);
        constr.fill = GridBagConstraints.BOTH;
        constr.weightx = 50;
        constr.weighty = 50;
        centerPanel.add(userDefinitionPanel, constr);

        add(centerPanel, BorderLayout.CENTER);
    }

    public EmissionMaskImpl getFunctionable() {
        return model.getDiscreteFunction2();
    }

    public void setFunctionable(EmissionMaskImpl f, String xUnit, String yUnit) {
        model.setDiscreteFunction2(f);
        if (xUnit != null || yUnit != null) {
            userDefinitionPanel.setLabels(xUnit, yUnit);
        }
    }

    public void clear() {
        setFunctionable(new EmissionMaskImpl(), null, null);
    }

    public void stopEditing() {
        if (dataTable.isEditing()) {
            dataTable.getCellEditor().stopCellEditing();
        }
    }

    public void setVictimCharacteristics(double victimBandwidth, double frequencyOffset, boolean showACLR, double interfererBandwidth) {
        userDefinitionPanel.setVictimCharacteristics(victimBandwidth, frequencyOffset, showACLR);
        if (showACLR && editor==null) {
            ACLRUI aclrui = prototype(ACLRUI.class);
            when(aclrui.displayACLR()).thenReturn(interfererBandwidth);
            editor = new GenericPanelEditor<ACLRUI>(MainWindow.getInstance(), ACLRUI.class, build(aclrui));
            editor.getCalculatedValues().get(0).getEvaluateButton().addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ACLRUI model = editor.getModel();
                    if (model.showACLR()) {
                        userDefinitionPanel.setAclrEnabled(true, true);
                        userDefinitionPanel.setInterferingBandwidth(model.displayACLR());
                        userDefinitionPanel.setAdjacentChannel(model.adjacentChannel());
                        userDefinitionPanel.refreshChart();
                    } else {
                        userDefinitionPanel.setAclrEnabled(false, false);
                        userDefinitionPanel.refreshChart();
                    }
                }
            });
            add(editor, BorderLayout.SOUTH);
            userDefinitionPanel.setInterferingBandwidth(interfererBandwidth);
        }
    }

    private class DialogFunctionButtonPanel extends FunctionButtonPanel {

        public DialogFunctionButtonPanel(DiscreteFunctionTableModelAdapterInterface model) {
            super(model);
        }

        @Override
        public void saveChartImage() {
            userDefinitionPanel.saveChartImage();
        }

        @Override
        public void btnDeleteActionPerformed() {
            model.deleteRow(dataTable.getSelectedRow());
        }

        @Override
        public void btnSaveActionPerformed() {
            stopEditing();
            super.btnSaveActionPerformed();
        }

        @Override
        public void btnSymActionPerformed() {
            // check if all offset values are different
            List<Point2D> pointsList = model.getFunction().points();
            if (pointsList != null) {
                stopEditing();
                checkEntries();
                DialogTableToDataSet.symmetrizeFunction(model.getFunction(), 0);
                model.sortPoints();
                model.fireChangeListeners();
            }
        }

        private void checkEntries() {
            boolean hasBeenChanged = false;
            List<Point2D> points2D = new ArrayList<>();
            points2D.addAll(model.getFunction().points());
            for (int i = 1; i < points2D.size(); i++) {
                if (Mathematics.equals(points2D.get(i).getX(), points2D.get(i - 1).getX(), 0.00001)) {
                    points2D.set(i, new Point2D(points2D.get(i).getX() + 0.0001, points2D.get(i).getY()));
                    hasBeenChanged = true;
                }
            }
            if (hasBeenChanged) {
                //TODO create new emission mask
                List<Double> mask = new ArrayList<>();
                for (int i = 0; i < points2D.size(); i++) {
                    mask.add(model.function.getMask(model.function.points().get(i)));
                }
                EmissionMask emission = functionFactory().emissionMask(points2D, mask);
                model.setDiscreteFunction2((EmissionMaskImpl) emission);
            }
        }
    }
}