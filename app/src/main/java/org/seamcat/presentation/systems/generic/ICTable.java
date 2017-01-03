package org.seamcat.presentation.systems.generic;

import javax.swing.table.DefaultTableModel;

public class ICTable extends DefaultTableModel {

    private Double[][] calculatedValues;

    public ICTable() {
        calculatedValues = new Double[4][5];
    }

    public void setCalculatedValues( Double[][] calculatedValues ) {
        this.calculatedValues = calculatedValues;
    }

    public Double[][] getCalculatedValues() {
        return calculatedValues;
    }

    @Override
    public int getRowCount() {
        return 4;
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int column) {
        return "Option "+(column+1);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return calculatedValues[row][column];
    }
}
