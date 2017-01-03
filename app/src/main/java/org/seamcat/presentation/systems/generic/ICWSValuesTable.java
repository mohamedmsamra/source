package org.seamcat.presentation.systems.generic;

import org.seamcat.model.generic.InterferenceCriteria;

import javax.swing.table.DefaultTableModel;

import static org.seamcat.model.factory.Factory.*;

public class ICWSValuesTable extends DefaultTableModel {

    private final CriteriaChanged changed;
    private String[] criterias = {"C/I", "C/(N+I)", "(N+I)/N", "I/N"};
    private double[] inputValues;
    private boolean wsConsistency;

    public ICWSValuesTable(InterferenceCriteria criteria, CriteriaChanged changed) {
        this.changed = changed;
        setModel( criteria, false);
    }

    public void setModel( InterferenceCriteria criteria, boolean wsConsistency ) {
        inputValues = new double[]{criteria.protection_ratio(), criteria.extended_protection_ratio(),
                criteria.noise_augmentation(), criteria.interference_to_noise_ratio()};
        this.wsConsistency = wsConsistency;
    }

    @Override
    public int getRowCount() {
        return 4;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Criteria" : "Current";
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if ( wsConsistency && row == 1 ) return false;
        return column == 1;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) return criterias[row];
        return inputValues[row];
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        try {
            inputValues[ row ] = Double.parseDouble( aValue.toString() );
            changed.changed( getModel() );
        } catch (RuntimeException e ) {
            // ignore
        }

    }

    public InterferenceCriteria getModel() {
        InterferenceCriteria prototype = prototype(InterferenceCriteria.class);
        when(prototype.protection_ratio()).thenReturn(inputValues[0]);
        when(prototype.extended_protection_ratio()).thenReturn(inputValues[1]);
        when(prototype.noise_augmentation()).thenReturn(inputValues[2]);
        when(prototype.interference_to_noise_ratio()).thenReturn(inputValues[3]);
        return build(prototype);
    }
}
