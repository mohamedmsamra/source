package org.seamcat.model;

import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.types.result.*;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SimulationResultGroupTableModel extends DefaultTableModel {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private SimulationResultGroup result;
    private List<Object> values = new ArrayList<>();
    private boolean plugin;
    private int size = 0;
    private String helpContents;

    public SimulationResultGroupTableModel(SimulationResultGroup result ) {
        this(result, false);
    }

    public SimulationResultGroupTableModel(SimulationResultGroup result, boolean plugin) {
        this.plugin = plugin;
        this.result = result;

        String key = "SimulationResult." +result.getName().replaceAll(" ", "_");
        if ( STRINGLIST.containsKey(key)) {
            helpContents = STRINGLIST.getString(key);
        }
        update();
    }

    public void setResults(ResultTypes result) {
        this.result.setResultTypes( result );
        update();
    }

    @Override
    public int getRowCount() {
        return size;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }
    private final String[] COLS = new String[] {"Name", "Value", "Unit", "Type"};

    @Override
    public String getColumnName(int i) {
        return COLS[i];
    }

    public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

    @Override
    public boolean isCellEditable(int i, int i2) {
        return false;
    }

    public void update() {
        values.clear();
        if ( result.failed() ) {
            values.add(result);
        } else {
            for (SingleValueTypes<?> value : result.getResultTypes().getSingleValueTypes()) {
                values.add( value );
            }
            for (VectorResultType value : result.getResultTypes().getVectorResultTypes()) {
                values.add(value);
            }
            for (VectorGroupResultType value : result.getResultTypes().getVectorGroupResultTypes()) {
                values.add(value);
            }
            for (ScatterDiagramResultType value : result.getResultTypes().getScatterDiagramResultTypes()) {
                values.add(value);
            }
            for (BarChartResultType value : result.getResultTypes().getBarChartResultTypes()) {
                values.add(value);
            }
        }
        size = values.size();
        fireTableStructureChanged();
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= values.size()) return null;
        Object value = values.get(row);
        if ( value instanceof SimulationResultGroup) {
            return getValue((SimulationResultGroup)value, col);
        } else if ( value instanceof SingleValueTypes ) {
            return getValue((SingleValueTypes<?>) value, col );
        } else if ( value instanceof VectorGroupResultType ) {
            return getValue((VectorGroupResultType) value, col);
        } else if ( value instanceof VectorResultType ) {
            return getValue((VectorResultType)value, col);
        } else if ( value instanceof ScatterDiagramResultType )  {
            return getValue((ScatterDiagramResultType)value, col );
        } else if ( value instanceof BarChartResultType ) {
            return getValue((BarChartResultType)value, col );
        }

        throw new RuntimeException("unknown type: " + value);
    }

	@Override
	public String toString() {
		if ( plugin ) {
            //return "Results for plugin '"+result.getName()+"'";
        }
        return result.getName();
	}

    private Object getValue(SimulationResultGroup result, int col) {
        if ( col == 0 ) return result.getName();
        if ( col == 1 ) return result.getException();
        if ( col == 2 ) return "N/A";
        return "Exception";
    }

    private Object getValue(SingleValueTypes<?> single, int col) {
        if ( col == 0 ) return single.getName();
        if ( col == 1 ) return single.getValue();
        if ( col == 2) return single.getUnit();
        return single.getType();
    }

    private Object getValue( VectorGroupResultType vg, int col) {
        if ( col == 0 ) return vg.getName();
        if ( col == 1 ) return vg;
        if ( col == 2 ) return vg.getUnit();
        return "Vector Group";
    }

    private Object getValue(VectorResultType v, int col) {
        if ( col == 0 ) return v.getName();
        if ( col == 1 ) return v;
        if ( col == 2 ) return v.getUnit();
        return "Vector";
    }

    private Object getValue( ScatterDiagramResultType s, int col ) {
        if ( col == 0 ) return s.getTitle();
        if ( col == 1 ) return s;
        if ( col == 2 ) return "N/A";
        return "Scatter";
    }

    private Object getValue( BarChartResultType bar, int col ) {
        if ( col == 0 ) return bar.getTitle();
        if ( col == 1 ) return bar;
        if ( col == 2 ) return bar.getyLabel();
        return "Bar Chart";
    }

    public String getHelpContents() {
        return helpContents;
    }
}
