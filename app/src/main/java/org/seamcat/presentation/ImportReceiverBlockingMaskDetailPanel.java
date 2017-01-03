package org.seamcat.presentation;

import org.seamcat.function.BlockingMaskImpl;
import org.seamcat.presentation.components.BorderPanel;
import org.seamcat.presentation.components.DiscreteFunctionGraph;
import org.seamcat.presentation.components.DiscreteFunctionTableModelAdapter;

import javax.swing.*;
import java.awt.*;

public class ImportReceiverBlockingMaskDetailPanel extends JPanel {

    public ImportReceiverBlockingMaskDetailPanel(BlockingMaskImpl function, String xAxis, String yAxis ) {
        setLayout(new BorderLayout());
        JPanel previewPanel = new JPanel(new BorderLayout());
        add(new BorderPanel(previewPanel, "Preview"), BorderLayout.CENTER);

        if ( function.isConstant() ) {
            previewPanel.add( new JLabel("Constant "+function.getConstant()+ " dB"), BorderLayout.CENTER);
        } else {
            DiscreteFunctionGraph graph = new DiscreteFunctionGraph(new DiscreteFunctionTableModelAdapter(), xAxis, yAxis);
            graph.getDataSet().setFunction(function);
            previewPanel.setPreferredSize(new Dimension(200,200));
            previewPanel.add(graph);
        }
    }
}