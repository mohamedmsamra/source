package org.seamcat.presentation.systems.generic;

import org.seamcat.model.factory.Factory;
import org.seamcat.model.generic.InterferenceCriteria;
import org.seamcat.model.systems.generic.ReceiverModel;
import org.seamcat.model.systems.generic.T_ReceiverModel;
import org.seamcat.presentation.DialogHelper;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.components.NavigateButtonPanel;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.panelbuilder.ChangeListener;
import org.seamcat.presentation.genericgui.panelbuilder.GenericPanelEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class InterferenceCriteriaDialog extends EscapeDialog {

    private final ICTable exampleValuesTable;
    private static final DecimalFormat formatter = new DecimalFormat( "0.0#" );
    private final JTable values;
    private final GenericPanelEditor<WSConsistency> ws;

    public InterferenceCriteriaDialog(JFrame owner, ReceiverModel model) {
        super(owner, "Interference Criteria Calculator", true);

        WSConsistency prototype = Factory.prototype(WSConsistency.class);
        Factory.when(prototype.noiseFloor()).thenReturn(model.receptionCharacteristics().noiseFloor().trial());
        Factory.when(prototype.sensitivity()).thenReturn(model.receptionCharacteristics().sensitivity());
        ws = new GenericPanelEditor<WSConsistency>(owner, WSConsistency.class, Factory.build(prototype));

        getContentPane().setLayout(new BorderLayout());
        ws.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        getContentPane().add(ws, BorderLayout.NORTH);

        InterferenceCriteria criteria = model.interferenceCriteria();

        JPanel tables = new JPanel(new BorderLayout());
        final ICWSValuesTable dm = new ICWSValuesTable(criteria, new CriteriaChanged() {
            public void changed(InterferenceCriteria criteria) {
                setValues(ws.getModel(), criteria);
            }
        });
        ws.addChangeListener(new ChangeListener<WSConsistency>() {
            @Override
            public void handle(WSConsistency model, List<AbstractItem> items, AbstractItem changedItem) {
                if ( model.wsConsistency() ) {
                    InterferenceCriteria icProto = Factory.prototype(InterferenceCriteria.class, dm.getModel());
                    Factory.when(icProto.extended_protection_ratio()).thenReturn(model.sensitivity() - model.noiseFloor());
                    InterferenceCriteria criteria = Factory.build(icProto);
                    setValues( model, criteria );
                    dm.setModel( criteria, true );
                    dm.fireTableDataChanged();
                } else {
                    setValues( model, dm.getModel() );
                    dm.setModel( dm.getModel(), false );
                }
            }
        });
        JTable table = new JTable(dm);
        table.setCellSelectionEnabled( true );
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(50, 50));
        sp.setBorder( BorderFactory.createEmptyBorder(10,30,10,5));
        tables.add(sp, BorderLayout.WEST );

        exampleValuesTable = new ICTable();
        values = new JTable(exampleValuesTable);
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                if ( value != null ) {
                    value = formatter.format((Number) value);
                }
                return super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, col);
            }
        };
        values.getColumnModel().getColumn(0).setCellRenderer(renderer);
        values.getColumnModel().getColumn(1).setCellRenderer(renderer);
        values.getColumnModel().getColumn(2).setCellRenderer(renderer);
        values.getColumnModel().getColumn(3).setCellRenderer(renderer);
        values.getColumnModel().getColumn(4).setCellRenderer(renderer);
        values.setColumnSelectionAllowed(true);
        values.setRowSelectionAllowed(false);

        JScrollPane sp2 = new JScrollPane(values);
        sp.setPreferredSize(new Dimension(180, 50));
        sp2.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 30));
        tables.add(sp2, BorderLayout.CENTER);
        setValues(ws.getModel(), criteria);

        getContentPane().add(tables, BorderLayout.CENTER);
        getContentPane().add(new NavigateButtonPanel(this, false) {
            @Override
            public void btnOkActionPerformed() {
                if ( !validateSelection() )  {
                    DialogHelper.illegalCriteriaSelection();
                    return;
                }
                super.btnOkActionPerformed();
            }
        }, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        setSize(800, 400);
    }

    public InterferenceCriteriaDialog(JFrame owner, T_ReceiverModel model) {
    	 super(owner, "Interference Criteria Calculator", true);

         WSConsistency prototype = Factory.prototype(WSConsistency.class);
         Factory.when(prototype.noiseFloor()).thenReturn(model.receptionCharacteristics().noiseFloor().trial());
         Factory.when(prototype.sensitivity()).thenReturn(model.receptionCharacteristics().sensitivity());
         ws = new GenericPanelEditor<WSConsistency>(owner, WSConsistency.class, Factory.build(prototype));

         getContentPane().setLayout(new BorderLayout());
         ws.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
         getContentPane().add(ws, BorderLayout.NORTH);

         InterferenceCriteria criteria = model.interferenceCriteria();

         JPanel tables = new JPanel(new BorderLayout());
         final ICWSValuesTable dm = new ICWSValuesTable(criteria, new CriteriaChanged() {
             public void changed(InterferenceCriteria criteria) {
                 setValues(ws.getModel(), criteria);
             }
         });
         ws.addChangeListener(new ChangeListener<WSConsistency>() {
             @Override
             public void handle(WSConsistency model, List<AbstractItem> items, AbstractItem changedItem) {
                 if ( model.wsConsistency() ) {
                     InterferenceCriteria icProto = Factory.prototype(InterferenceCriteria.class, dm.getModel());
                     Factory.when(icProto.extended_protection_ratio()).thenReturn(model.sensitivity() - model.noiseFloor());
                     InterferenceCriteria criteria = Factory.build(icProto);
                     setValues( model, criteria );
                     dm.setModel( criteria, true );
                     dm.fireTableDataChanged();
                 } else {
                     setValues( model, dm.getModel() );
                     dm.setModel( dm.getModel(), false );
                 }
             }
         });
         JTable table = new JTable(dm);
         table.setCellSelectionEnabled( true );
         JScrollPane sp = new JScrollPane(table);
         sp.setPreferredSize(new Dimension(50, 50));
         sp.setBorder( BorderFactory.createEmptyBorder(10,30,10,5));
         tables.add(sp, BorderLayout.WEST );

         exampleValuesTable = new ICTable();
         values = new JTable(exampleValuesTable);
         TableCellRenderer renderer = new DefaultTableCellRenderer() {
             @Override
             public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                 if ( value != null ) {
                     value = formatter.format((Number) value);
                 }
                 return super.getTableCellRendererComponent(jTable, value, isSelected, hasFocus, row, col);
             }
         };
         values.getColumnModel().getColumn(0).setCellRenderer(renderer);
         values.getColumnModel().getColumn(1).setCellRenderer(renderer);
         values.getColumnModel().getColumn(2).setCellRenderer(renderer);
         values.getColumnModel().getColumn(3).setCellRenderer(renderer);
         values.getColumnModel().getColumn(4).setCellRenderer(renderer);
         values.setColumnSelectionAllowed(true);
         values.setRowSelectionAllowed(false);

         JScrollPane sp2 = new JScrollPane(values);
         sp.setPreferredSize(new Dimension(180, 50));
         sp2.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 30));
         tables.add(sp2, BorderLayout.CENTER);
         setValues(ws.getModel(), criteria);

         getContentPane().add(tables, BorderLayout.CENTER);
         getContentPane().add(new NavigateButtonPanel(this, false) {
             @Override
             public void btnOkActionPerformed() {
                 if ( !validateSelection() )  {
                     DialogHelper.illegalCriteriaSelection();
                     return;
                 }
                 super.btnOkActionPerformed();
             }
         }, BorderLayout.SOUTH);

         pack();
         setLocationRelativeTo(owner);
         setSize(800, 400);
	}

	public WSConsistency getWSConsistency() {
        return ws.getModel();
    }

    private boolean validateSelection() {
        Double[][] calculatedValues = exampleValuesTable.getCalculatedValues();
        int selectedColumn = values.getSelectedColumn();
        if ( selectedColumn < 0 || calculatedValues[0][selectedColumn] == null )  {
            return false;
        }

        return true;
    }

    public InterferenceCriteria getSelectedCriteria() {
        Double[][] calculatedValues = exampleValuesTable.getCalculatedValues();
        int selectedColumn = values.getSelectedColumn();

        InterferenceCriteria prototype = Factory.prototype(InterferenceCriteria.class);
        Factory.when(prototype.protection_ratio()).thenReturn(calculatedValues[0][selectedColumn] );
        Factory.when(prototype.extended_protection_ratio()).thenReturn(calculatedValues[1][selectedColumn] );
        Factory.when(prototype.noise_augmentation()).thenReturn(calculatedValues[2][selectedColumn] );
        Factory.when(prototype.interference_to_noise_ratio()).thenReturn(calculatedValues[3][selectedColumn] );

        return Factory.build(prototype);
    }

    private void setValues( WSConsistency model, InterferenceCriteria criteria ) {
        double target = model.sensitivity() - model.noiseFloor();
        exampleValuesTable.setCalculatedValues(InterferenceCriteriaCalculator.calculate(model.wsConsistency(), target, criteria));
        exampleValuesTable.fireTableDataChanged();
        alignSelection();
    }

    private void alignSelection() {
        int selectedColumn = values.getSelectedColumn();
        if ( selectedColumn < 0 ) {
            selectedColumn = 0;
        }
        if ( exampleValuesTable.getCalculatedValues()[0][selectedColumn] == null ) {
            selectedColumn = 0;
        }
        values.setColumnSelectionInterval(selectedColumn, selectedColumn);
    }
}
