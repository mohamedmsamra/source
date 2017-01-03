package org.seamcat.presentation;

import org.seamcat.model.types.result.BarChartResultType;
import org.seamcat.model.types.result.ScatterDiagramResultType;
import org.seamcat.model.types.result.VectorGroupResultType;
import org.seamcat.model.types.result.VectorResultType;
import org.seamcat.presentation.compareVector.BarChartDialog;
import org.seamcat.presentation.compareVector.VectorGroupDialog;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

public class MouseClickTypeHandler extends MouseAdapter {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private JTable table;

    public MouseClickTypeHandler(JTable table) {
        this.table = table;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            if (e.getClickCount() > 1) {
                Object value = table.getModel().getValueAt(table.getSelectedRow(), 1);
                if ( value instanceof VectorResultType) {
                    VectorResultType vector = (VectorResultType) value;
                    double[] values = vector.getValue().asArray();
                    if ( values == null || values.length == 0 ) return;
                    createNewRssDialog().show(vector.getValue().asArray(), vector.getName(), vector.getUnit(), vector.getLabel());
                } else if ( value instanceof ScatterDiagramResultType ) {
                    new ScatterDiagramDialog( (ScatterDiagramResultType) value);
                } else if ( value instanceof VectorGroupResultType) {
                    new VectorGroupDialog((VectorGroupResultType) value);
                } else if ( value instanceof BarChartResultType) {
                    new BarChartDialog((BarChartResultType) value);
                } else if ( value instanceof Exception ) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ((Exception) value).printStackTrace(pw);
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), sw.toString(), "Exception Occurred", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private DialogDisplaySignal createNewRssDialog() {
        DialogDisplaySignal drssDialog = new DialogDisplaySignal(MainWindow.getInstance(), STRINGLIST.getString("VECTOR_GRAPH_AXIX_TITLE_X"), STRINGLIST.getString("VECTOR_GRAPH_AXIX_TITLE_Y"));
        drssDialog.displayDataSelectionPanel(false);
        return drssDialog;
    }

}
